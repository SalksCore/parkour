package io.github.salkscore.parkour.run;

import io.github.salkscore.parkour.ParkourPlugin;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public final class ProtectionListener implements Listener {

    private static final Set<TeleportCause> BLOCKED = EnumSet.of(TeleportCause.ENDER_PEARL,
            TeleportCause.CONSUMABLE_EFFECT, TeleportCause.NETHER_PORTAL,
            TeleportCause.END_PORTAL, TeleportCause.END_GATEWAY);
    private static final Set<TeleportCause> ABORT = EnumSet.of(TeleportCause.COMMAND,
            TeleportCause.PLUGIN, TeleportCause.SPECTATE);

    private final ParkourPlugin plugin;

    public ProtectionListener(ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || session(player) == null) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
            plugin.runs().respawn(player);
        } else if (plugin.settings().noDamage()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFood(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || session(player) == null) return;
        if (plugin.settings().noHunger() && event.getFoodLevel() < player.getFoodLevel()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGameMode(PlayerGameModeChangeEvent event) {
        Session session = session(event.getPlayer());
        if (session == null || session.bypassing()) return;
        event.setCancelled(true);
        plugin.messages().send(event.getPlayer(), "run.gamemode");
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Session session = session(event.getPlayer());
        if (session == null || session.bypassing()) return;
        if (BLOCKED.contains(event.getCause())) {
            event.setCancelled(true);
        } else if (ABORT.contains(event.getCause())) {
            plugin.runs().stop(event.getPlayer(), StopReason.CANCEL);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlight(PlayerToggleFlightEvent event) {
        Session session = session(event.getPlayer());
        if (session != null && !session.bypassing() && event.isFlying()) event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (session(event.getPlayer()) == null) return;
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        event.getDrops().clear();
        event.setDroppedExp(0);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Session session = session(event.getPlayer());
        if (session == null) return;
        event.setRespawnLocation(session.course().respawn(session.reached()));
    }

    private Session session(Player player) {
        return plugin.runs().get(player);
    }
}
