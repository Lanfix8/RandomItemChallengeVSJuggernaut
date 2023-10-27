package fr.lanfix.randomitemchallengevsjuggernaut.events;

import fr.lanfix.randomitemchallengevsjuggernaut.RandomItemChallengeVSJuggernaut;
import fr.lanfix.randomitemchallengevsjuggernaut.game.Game;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemEvents implements Listener {

    private final Game game;

    public ItemEvents(Game game) {
        this.game = game;
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        // => PersistentDataContainer
        if (event.getEntity() instanceof Player player && event.getItem().getItemStack().hasItemMeta()) {
            Item item = event.getItem();
            ItemStack itemStack = item.getItemStack();
            ItemMeta itemMeta = itemStack.getItemMeta();
            assert itemMeta != null;
            PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
            if (dataContainer.has(RandomItemChallengeVSJuggernaut.protectedDataKey, PersistentDataType.STRING)) {
                String protection = dataContainer.get(RandomItemChallengeVSJuggernaut.protectedDataKey, PersistentDataType.STRING);
                assert protection != null;
                if ((protection.equalsIgnoreCase("survivor") && !game.isSurvivor(player)) ||
                        (protection.equalsIgnoreCase("juggernaut") && !game.isJuggernaut(player))) {
                    event.setCancelled(true);
                }
            }
        }
    }

}
