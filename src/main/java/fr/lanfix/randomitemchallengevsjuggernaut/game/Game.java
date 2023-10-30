package fr.lanfix.randomitemchallengevsjuggernaut.game;

import fr.lanfix.randomitemchallengevsjuggernaut.RandomItemChallengeVSJuggernaut;
import fr.lanfix.randomitemchallengevsjuggernaut.utils.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;

public class Game {

    private final RandomItemChallengeVSJuggernaut main;

    private final Random random;

    private boolean running;
    private BukkitRunnable itemLoop;
    private BukkitRunnable compassLoop;

    private final List<Player> survivors;
    private final List<Player> juggernauts;

    public Game(RandomItemChallengeVSJuggernaut main) {
        this.main = main;
        this.random = new Random();
        this.running = false;
        this.survivors = new ArrayList<>();
        this.juggernauts = new ArrayList<>();
    }

    public void start(Location spawnLocation) {
        Bukkit.getLogger().log(Level.INFO, "Starting Random Item Challenge VS Juggernaut");
        World world = spawnLocation.getWorld();
        assert world != null;
        world.setTime(0);
        world.getWorldBorder().reset();
        world.setSpawnLocation(spawnLocation);
        for (Player survivor: survivors) {
            survivor.setHealth(20);
            survivor.setSaturation(20);
            survivor.setFoodLevel(20);
            survivor.setTotalExperience(0);
            survivor.setExp(0);
            survivor.sendExperienceChange(0, 0);
            survivor.setGameMode(GameMode.SURVIVAL);
            survivor.teleport(spawnLocation);
            survivor.getInventory().clear();
            survivor.getActivePotionEffects().forEach(effect -> survivor.removePotionEffect(effect.getType()));
            survivor.sendTitle("You are a survivor", "Defeat the ender dragon to win", 10, 70, 20);
        }
        for (Player juggernaut: juggernauts) {
            juggernaut.setHealth(20);
            juggernaut.setSaturation(20);
            juggernaut.setFoodLevel(20);
            juggernaut.setTotalExperience(0);
            juggernaut.setExp(0);
            juggernaut.sendExperienceChange(0, 0);
            juggernaut.setGameMode(GameMode.SURVIVAL);
            juggernaut.teleport(spawnLocation);
            juggernaut.getInventory().clear();
            JuggernautKit.giveKit(juggernaut);
            juggernaut.getActivePotionEffects().forEach(effect -> juggernaut.removePotionEffect(effect.getType()));
            juggernaut.sendTitle("You are a juggernaut", "Kill all survivors to win", 10, 70, 20);
        }
        this.running = true;
        this.itemLoop = new BukkitRunnable() {
            @Override
            public void run() {
                giveItems();
            }
        };
        itemLoop.runTaskTimer(main, 20 * 15, 20L * main.getConfig().getInt("drop-interval", 120));
        this.compassLoop = new BukkitRunnable() {
            @Override
            public void run() {
                updateCompasses();
            }
        };
        compassLoop.runTaskTimer(main, 0, 20);
    }

    public void stop() {
        this.itemLoop.cancel();
        this.compassLoop.cancel();
        survivors.clear();
        juggernauts.clear();
        this.running = false;
    }

    public void giveItems() {
        List<Material> choices = switch(main.getConfig().getString("itemChooseMode", "custom")) {
            case "custom" -> {
                List<Material> r = new ArrayList<>();
                main.getConfig().getStringList("items").forEach(string -> r.add(Material.valueOf(string)));
                yield r;
            }
            case "allItems" -> {
                List<Material> r = new ArrayList<>();
                for (Material material : Material.values()) if (material.isItem()) r.add(material);
                yield r;
            }
            default -> throw new IllegalStateException("Wrong itemChooseMode: " + main.getConfig().getString("itemChooseMode"));
        };
        // repeat for all players
        for (Player player: survivors) {
            for (int i = 0; i < main.getConfig().getInt("drop-count", 1); i++) {
                // find the location and choose item
                Location location = player.getLocation();
                Material material = choices.get(this.random.nextInt(choices.size()));
                ItemStack itemStack = new ItemStack(material, material.getMaxStackSize());
                // set item protected to survivors
                PersistentDataContainer dataContainer = Objects.requireNonNull(itemStack.getItemMeta()).getPersistentDataContainer();
                dataContainer.set(RandomItemChallengeVSJuggernaut.protectedDataKey, PersistentDataType.STRING, "survivor");
                // drop the items
                for (int j = 0; j < main.getConfig().getInt("stacks", 9); j++) {
                    player.getWorld().dropItem(location, itemStack, item -> {
                        // set item protected to survivors again because the first option doesn't work at the drop but works when dropped by a player (wtf)
                        PersistentDataContainer itemDataContainer = item.getPersistentDataContainer();
                        itemDataContainer.set(RandomItemChallengeVSJuggernaut.protectedDataKey, PersistentDataType.STRING, "survivor");
                    });
                }
                player.sendMessage(ChatColor.GREEN + "You just got your random item: "
                        + StringUtils.snakeCaseToSpacedPascalCase(material.toString()));
            }
        }
    }

    private void updateCompasses() {
        for (Player juggernaut: this.juggernauts) {
            double nearestDistance = Double.POSITIVE_INFINITY;
            Player nearestEnemy = juggernaut;
            for (Player survivor: this.survivors) {
                double distance = juggernaut.getLocation().distance(survivor.getLocation());
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestEnemy = survivor;
                }
            }
            Tracker.trackLocation(juggernaut, nearestEnemy.getLocation(), "Nearest Survivor");
        }
    }

    public void survivorDeath(Player player) {
        if (this.survivors.contains(player)) {
            this.survivors.remove(player);
            if (this.survivors.isEmpty()) {
                Bukkit.broadcastMessage("%s%sJuggernauts have won, they killed all survivors !".formatted(ChatColor.BOLD, ChatColor.BLUE));
                this.stop();
            }
        }
    }

    public List<Player> getSurvivors() {
        return survivors;
    }

    public boolean isSurvivor(Player player) {
        return this.survivors.contains(player);
    }

    public void setSurvivors(List<Player> survivors) {
        this.survivors.clear();
        this.survivors.addAll(survivors);
    }

    public void clearSurvivors() {
        this.survivors.clear();
    }

    public void addSurvivors(List<Player> survivors) {
        this.survivors.addAll(survivors);
    }

    public List<Player> getJuggernauts() {
        return juggernauts;
    }

    public boolean isJuggernaut(Player player) {
        return this.juggernauts.contains(player);
    }

    public void setJuggernauts(List<Player> juggernauts) {
        this.juggernauts.clear();
        this.juggernauts.addAll(juggernauts);
    }

    public void clearJuggernauts() {
        this.juggernauts.clear();
    }

    public void addJuggernauts(List<Player> juggernauts) {
        this.juggernauts.addAll(juggernauts);
    }

    public boolean isRunning() {
        return running;
    }

}
