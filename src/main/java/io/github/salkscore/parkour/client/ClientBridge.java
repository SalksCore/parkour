package io.github.salkscore.parkour.client;

import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.course.Course;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

public final class ClientBridge implements PluginMessageListener, Listener {

    public static final String RUN = "parkour:run";
    public static final String HELLO = "parkour:hello";

    private final ParkourPlugin plugin;
    private final Set<UUID> modded = new HashSet<>();

    public ClientBridge(ParkourPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Messenger messenger = plugin.getServer().getMessenger();
        messenger.registerOutgoingPluginChannel(plugin, RUN);
        messenger.registerIncomingPluginChannel(plugin, HELLO, this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!HELLO.equals(channel) || !modded.add(player.getUniqueId())) return;
        plugin.displays().hideFrom(player);
        sync(player);
    }

    public void sync(Player player) {
        if (hasMod(player)) send(player, CourseSync.packet(plugin, player));
    }

    public void syncAll() {
        for (UUID id : modded) {
            Player player = plugin.getServer().getPlayer(id);
            if (player != null) sync(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        modded.remove(event.getPlayer().getUniqueId());
    }

    public boolean hasMod(Player player) {
        return modded.contains(player.getUniqueId());
    }

    public void start(Player player, Course course) {
        if (!hasMod(player)) return;
        List<Location> points = new ArrayList<>();
        points.add(course.start());
        points.addAll(course.checkpoints());
        points.add(course.finish());

        PacketWriter packet = new PacketWriter(PacketType.START)
                .writeString(course.id())
                .writeString(CourseSync.name(course))
                .writeString(course.start().getWorld().getKey().toString())
                .writeVarInt(points.size());
        points.forEach(packet::writePosition);
        send(player, packet);
    }

    public void checkpoint(Player player, int index, long elapsed) {
        if (!hasMod(player)) return;
        send(player, new PacketWriter(PacketType.CHECKPOINT).writeVarInt(index).writeLong(elapsed));
    }

    public void finish(Player player, long time, int rank) {
        if (!hasMod(player)) return;
        send(player, new PacketWriter(PacketType.FINISH).writeLong(time).writeVarInt(rank));
    }

    public void stop(Player player) {
        if (!hasMod(player)) return;
        send(player, new PacketWriter(PacketType.STOP));
    }

    void send(Player player, PacketWriter packet) {
        player.sendPluginMessage(plugin, RUN, packet.toByteArray());
    }
}
