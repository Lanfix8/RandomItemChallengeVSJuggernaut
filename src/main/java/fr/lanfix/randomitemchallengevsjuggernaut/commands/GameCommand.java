package fr.lanfix.randomitemchallengevsjuggernaut.commands;

import fr.lanfix.randomitemchallengevsjuggernaut.game.Game;
import fr.lanfix.randomitemchallengevsjuggernaut.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameCommand implements CommandExecutor, TabCompleter {

    private final Game game;

    public GameCommand(Game game) {
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please specify at least one argument");
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (sender instanceof Player player) {
                    if (!game.isRunning()) {
                        if (!game.getJuggernauts().isEmpty() && !game.getSurvivors().isEmpty()) {
                            game.start(player.getLocation());
                        } else {
                            sender.sendMessage(ChatColor.RED + "Please select at least a survivor and a juggernaut");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "A game is already running...");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED +
                            "Please execute this command as a player, it will start the game at your location.");
                }
            }
            case "stop" -> {
                if (game.isRunning()) {
                    Bukkit.broadcastMessage(ChatColor.RED + "Forced stop of the Random Item Challenge VS Juggernaut");
                    game.stop();
                } else {
                    sender.sendMessage(ChatColor.RED + "No game is currently running...");
                }
            }
            case "survivors", "juggernauts" -> {
                if (args.length == 1) {
                    switch (args[0].toLowerCase()) {
                        case "survivors" -> sender.sendMessage(ChatColor.BLUE + "The survivors are : "
                                + StringUtils.streamAsBulletList(game.getSurvivors().stream().map(Player::getName)));
                        case "juggernauts" -> sender.sendMessage(ChatColor.BLUE + "The juggernauts are : "
                                + StringUtils.streamAsBulletList(game.getJuggernauts().stream().map(Player::getName)));
                    }
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "clear" -> {
                        switch (args[0].toLowerCase()) {
                            case "survivors" -> game.clearSurvivors();
                            case "juggernauts" -> game.clearJuggernauts();
                        }
                        sender.sendMessage(ChatColor.GREEN + "Successfully cleared " + args[0] + " !");
                    }
                    case "set" -> {
                        List<Player> newList = getPlayerListFromArgs(args);
                        switch (args[0].toLowerCase()) {
                            case "survivors" -> {
                                newList.removeAll(game.getJuggernauts());
                                game.setSurvivors(newList);
                            }
                            case "juggernauts" -> {
                                newList.removeAll(game.getSurvivors());
                                game.setJuggernauts(newList);
                            }
                        }
                        sender.sendMessage(ChatColor.GREEN + "The " + args[0] + " list is now set to: "
                                + Arrays.toString(newList.stream().map(Player::getName).toArray()));
                    }
                    case "add" -> {
                        List<Player> toAdd = getPlayerListFromArgs(args);
                        toAdd.removeAll(game.getSurvivors());
                        toAdd.removeAll(game.getJuggernauts());
                        switch (args[0].toLowerCase()) {
                            case "survivors" -> {
                                game.addSurvivors(toAdd);
                                sender.sendMessage(ChatColor.GREEN + "Successfully added " +
                                        Arrays.toString(toAdd.stream().map(Player::getName).toArray()) + " to the survivors.\nThe list is now : "
                                        + Arrays.toString(game.getSurvivors().stream().map(Player::getName).toArray()));
                            }
                            case "juggernauts" -> {
                                game.addJuggernauts(toAdd);
                                sender.sendMessage(ChatColor.GREEN + "Successfully added " +
                                        Arrays.toString(toAdd.stream().map(Player::getName).toArray()) + " to the juggernauts.\nThe list is now : "
                                        + Arrays.toString(game.getJuggernauts().stream().map(Player::getName).toArray()));
                            }
                        }
                    }
                    default -> {
                        sender.sendMessage(ChatColor.RED + "Your second argument is invalid ('" + args[1] + "').");
                        sender.sendMessage(ChatColor.RED + "/ricvsjuggernaut " + args[0] + " <clear/set/add>");
                    }
                }
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "Your first argument is invalid ('" + args[0] + "').");
                return false;
            }
        }
        return true;
    }

    public List<Player> getPlayerListFromArgs(String[] args) {
        List<Player> r = new ArrayList<>();
        for (String arg : args) {
            Player player = Bukkit.getPlayer(arg);
            if (player != null) r.add(player);
        }
        return r;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String msg, String[] args) {
        List<String> r = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                List<String> completeArgs = List.of("start", "stop", "juggernauts", "survivors");
                completeArgs.forEach(arg -> {
                    if (arg.toLowerCase().startsWith(args[0].toLowerCase())) r.add(arg);
                });
            }
            case 2 -> {
                switch (args[0].toLowerCase()) {
                    case "juggernauts", "survivors" -> {
                        List<String> listEditingArgs = List.of("clear", "set", "add");
                        listEditingArgs.forEach(arg -> {
                            if (arg.toLowerCase().startsWith(args[1].toLowerCase())) r.add(arg);
                        });
                    }
                }
            }
            default -> {
                switch (args[0].toLowerCase()) {
                    case "juggernauts", "survivors" -> {
                        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                        players.removeAll(game.getSurvivors());
                        players.removeAll(game.getJuggernauts());
                        List<String> completeArgs = new ArrayList<>(players.stream().map(Player::getName).toList());
                        completeArgs.removeAll(List.of(args));
                        completeArgs.forEach(arg -> {
                            if (arg.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) r.add(arg);
                        });
                    }
                }
            }
        }
        return r;
    }
}
