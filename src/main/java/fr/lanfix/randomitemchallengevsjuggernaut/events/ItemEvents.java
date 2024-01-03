package fr.lanfix.randomitemchallengevsjuggernaut.events;

import fr.lanfix.randomitemchallengevsjuggernaut.RandomItemChallengeVSJuggernaut;
import fr.lanfix.randomitemchallengevsjuggernaut.game.Game;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class ItemEvents implements Listener {

    private final Game game;

    public ItemEvents(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (game.isRunning() && (game.isJuggernaut(player) || game.isSurvivor(player))) {
            Item item = event.getItemDrop();
            PersistentDataContainer dataContainer = Objects.requireNonNull(item.getPersistentDataContainer());
            dataContainer.set(RandomItemChallengeVSJuggernaut.protectedDataKey, PersistentDataType.STRING,
                    game.isSurvivor(player) ? "survivor" : "juggernaut");
        }
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (game.isRunning() && event.getEntity() instanceof Player player) {
            System.out.println("First condition");
            Item item = event.getItem();
            PersistentDataContainer dataContainer = Objects.requireNonNull(item.getItemStack().getItemMeta()).getPersistentDataContainer();
            if (dataContainer.has(RandomItemChallengeVSJuggernaut.protectedDataKey, PersistentDataType.STRING)) {
                String protection = dataContainer.get(RandomItemChallengeVSJuggernaut.protectedDataKey, PersistentDataType.STRING);
                assert protection != null;
                System.out.println("has key in itemstack");
                if ((protection.equalsIgnoreCase("survivor") && !game.isSurvivor(player)) ||
                        (protection.equalsIgnoreCase("juggernaut") && !game.isJuggernaut(player))) {
                    event.setCancelled(true);
                }
            }
            // also in the datacontainer of the item entity because it's broken without that wtf
            PersistentDataContainer itemDataContainer = Objects.requireNonNull(item.getPersistentDataContainer());
            if (itemDataContainer.has(RandomItemChallengeVSJuggernaut.protectedDataKey, PersistentDataType.STRING)) {
                String protection = itemDataContainer.get(RandomItemChallengeVSJuggernaut.protectedDataKey, PersistentDataType.STRING);
                assert protection != null;
                System.out.println("has key in item entity");
                if ((protection.equalsIgnoreCase("survivor") && !game.isSurvivor(player)) ||
                        (protection.equalsIgnoreCase("juggernaut") && !game.isJuggernaut(player))) {
                    event.setCancelled(true);
                }
            }
        }
    }

}
