package fr.lanfix.randomitemchallengevsjuggernaut.events;

import fr.lanfix.randomitemchallengevsjuggernaut.RandomItemChallengeVSJuggernaut;
import fr.lanfix.randomitemchallengevsjuggernaut.game.Game;
import fr.lanfix.randomitemchallengevsjuggernaut.game.JuggernautKit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class GameEvents implements Listener {

    private final RandomItemChallengeVSJuggernaut plugin;
    private final Game game;

    public GameEvents(RandomItemChallengeVSJuggernaut plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    @EventHandler
    public void onSurvivorWin(EnderDragonChangePhaseEvent event) {
        if (event.getNewPhase().equals(EnderDragon.Phase.DYING)) {
            Bukkit.broadcastMessage("%s%sSurvivors have won, they defeated the ender dragon !".formatted(ChatColor.BOLD, ChatColor.BLUE));
            game.stop();
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (this.game.isRunning()) {
            Player player = event.getEntity();
            if (this.game.isSurvivor(player)) {
                player.setGameMode(GameMode.SPECTATOR);  // Maybe improve the spectator when death change
                this.game.survivorDeath(player);
            } else if (this.game.isJuggernaut(player)) {
                JuggernautKit.removeKitFromItemList(event.getDrops());
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (this.game.isRunning() && this.game.isJuggernaut(player)) {
            JuggernautKit.giveKit(player);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        logOffTimer(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        logOffTimer(event.getPlayer());
    }

    private void logOffTimer(Player player) {
        Bukkit.broadcastMessage(player.getDisplayName() + " logged off, if he does not log back in less than 5 minutes, he'll be out...");
        new BukkitRunnable() {
            int second = 0;
            @Override
            public void run() {
                second++;
                if (player.isOnline()) {
                    cancel();
                } else if (second >= 300) {
                    Bukkit.broadcastMessage(player.getDisplayName() + " is out because he left for more than 5 minutes !");
                    game.survivorDeath(player);
                    player.setGameMode(GameMode.SPECTATOR);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }

}
