package fr.lanfix.randomitemchallengevsjuggernaut;

import fr.lanfix.randomitemchallengevsjuggernaut.commands.GameCommand;
import fr.lanfix.randomitemchallengevsjuggernaut.events.GameEvents;
import fr.lanfix.randomitemchallengevsjuggernaut.events.ItemEvents;
import fr.lanfix.randomitemchallengevsjuggernaut.game.Game;
import fr.lanfix.randomitemchallengevsjuggernaut.game.JuggernautKit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class RandomItemChallengeVSJuggernaut extends JavaPlugin {

    public static NamespacedKey protectedDataKey;

    private Game game;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        load();
        // Register events
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new GameEvents(this.game), this);
        pluginManager.registerEvents(new ItemEvents(this.game), this);
        // Register commands
        getCommand("ricvsjuggernaut").setExecutor(new GameCommand(this.game));
    }

    private void load() {
        protectedDataKey = new NamespacedKey(this, "protected");
        this.game = new Game(this);
        JuggernautKit.initialize(this.getConfig().getMapList("juggernaut-kit"));
    }

    @Override
    public void onDisable() {
        if (this.game.isRunning()) {
            this.game.stop();
        }
    }
}
