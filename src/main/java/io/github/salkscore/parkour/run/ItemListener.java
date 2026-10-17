package io.github.salkscore.parkour.run;

import io.github.salkscore.parkour.ParkourPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class ItemListener implements Listener {

    private static final long COOLDOWN = 300L;

    private final ParkourPlugin plugin;
    private final Map<UUID, Long> lastUse = new HashMap<>();

    public ItemListener(ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;
        RunItem item = plugin.runs().items().read(event.getItem());
        if (item == null) return;

        event.setCancelled(true);
        if (event.getHand() != EquipmentSlot.HAND || !event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        RunManager runs = plugin.runs();
        if (runs.get(player) == null) {
            player.getInventory().remove(event.getItem());
            return;
        }
        if (!ready(player)) return;

        switch (item) {
            case CHECKPOINT -> runs.respawn(player);
            case RESTART -> runs.restart(player);
            case LEAVE -> runs.stop(player, StopReason.LEAVE);
        }
    }

    private boolean ready(Player player) {
        long now = System.currentTimeMillis();
        Long last = lastUse.put(player.getUniqueId(), now);
        return last == null || now - last >= COOLDOWN;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (running(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && running(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player && running(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (running(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastUse.remove(event.getPlayer().getUniqueId());
    }

    private boolean running(Player player) {
        return plugin.runs().get(player) != null;
    }
}
