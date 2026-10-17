package io.github.salkscore.parkour.run;

import io.github.salkscore.parkour.ParkourPlugin;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class RunItems {

    private final ParkourPlugin plugin;
    private final NamespacedKey key;

    RunItems(ParkourPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "item");
    }

    void give(Player player) {
        plugin.settings().items().forEach((item, slot) -> {
            ItemStack stack = create(item, new ItemStack(slot.material()));
            player.getInventory().setItem(slot.slot(), stack);
        });
    }

    private ItemStack create(RunItem item, ItemStack stack) {
        stack.editMeta(meta -> {
            meta.displayName(plugin.messages().get("items." + item.key())
                    .decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, item.name());
        });
        return stack;
    }

    public RunItem read(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        ItemMeta meta = stack.getItemMeta();
        String raw = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (raw == null) return null;
        try {
            return RunItem.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
