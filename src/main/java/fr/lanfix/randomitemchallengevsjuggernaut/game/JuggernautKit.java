package fr.lanfix.randomitemchallengevsjuggernaut.game;

import fr.lanfix.randomitemchallengevsjuggernaut.RandomItemChallengeVSJuggernaut;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JuggernautKit {

    private static final List<ItemStack> kit = new ArrayList<>();

    public static void initialize(List<Map<?, ?>> kitSection) {
        kitSection.forEach(map -> {
            ItemStack item = new ItemStack(
                    Material.valueOf((String) map.get("material")),
                    map.containsKey("count") ? (int) map.get("count") : 1);
            PersistentDataContainer dataContainer = Objects.requireNonNull(item.getItemMeta()).getPersistentDataContainer();
            dataContainer.set(RandomItemChallengeVSJuggernaut.protectedDataKey, PersistentDataType.STRING, "juggernaut");
            kit.add(item);
        });
    }

    public static void giveKit(Player player) {
        kit.forEach(item -> {
            PlayerInventory inv = player.getInventory();
            switch (item.getType()) {
                case CHAINMAIL_HELMET, DIAMOND_HELMET, GOLDEN_HELMET, IRON_HELMET, LEATHER_HELMET, NETHERITE_HELMET, TURTLE_HELMET -> inv.setHelmet(item);
                case CHAINMAIL_CHESTPLATE, DIAMOND_CHESTPLATE, GOLDEN_CHESTPLATE, IRON_CHESTPLATE, LEATHER_CHESTPLATE, NETHERITE_CHESTPLATE -> inv.setChestplate(item);
                case CHAINMAIL_LEGGINGS, DIAMOND_LEGGINGS, GOLDEN_LEGGINGS, IRON_LEGGINGS, LEATHER_LEGGINGS, NETHERITE_LEGGINGS -> inv.setLeggings(item);
                case CHAINMAIL_BOOTS, DIAMOND_BOOTS, GOLDEN_BOOTS, IRON_BOOTS, LEATHER_BOOTS, NETHERITE_BOOTS -> inv.setBoots(item);
                default -> inv.addItem(item);
            }
        });
    }

    public static void removeKitFromItemList(List<ItemStack> items) {
        List<ItemStack> toRemove = new ArrayList<>(kit);
        items.forEach(item -> {
            for (ItemStack match : toRemove) {
                if (item.isSimilar(match)) {
                    if (item.getAmount() > match.getAmount()) {
                        item.setAmount(item.getAmount() - match.getAmount());
                        toRemove.remove(match);
                    } else {
                        match.setAmount(match.getAmount() - item.getAmount());
                        items.remove(item);
                    }
                    break;
                }
            }
        });
    }

}
