package io.github.salkscore.parkour;

import io.github.salkscore.parkour.board.Leaderboard;
import io.github.salkscore.parkour.client.ClientBridge;
import io.github.salkscore.parkour.command.ParkourCommand;
import io.github.salkscore.parkour.course.Course;
import io.github.salkscore.parkour.course.CourseManager;
import io.github.salkscore.parkour.holo.HologramManager;
import io.github.salkscore.parkour.holo.PointParticles;
import io.github.salkscore.parkour.run.ItemListener;
import io.github.salkscore.parkour.run.ProtectionListener;
import io.github.salkscore.parkour.run.RunHud;
import io.github.salkscore.parkour.run.RunListener;
import io.github.salkscore.parkour.run.RunManager;
import io.github.salkscore.parkour.run.StopReason;
import io.github.salkscore.parkour.util.Messages;
import io.github.salkscore.parkour.util.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ParkourPlugin extends JavaPlugin {

    private Settings settings;
    private Messages messages;
    private CourseManager courses;
    private Leaderboard leaderboard;
    private HologramManager holograms;
    private RunManager runs;
    private ClientBridge bridge;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        settings = Settings.load(getConfig());
        messages = new Messages(this);
        courses = new CourseManager(this);
        leaderboard = new Leaderboard(this);
        holograms = new HologramManager(this);
        runs = new RunManager(this);
        bridge = new ClientBridge(this);
        bridge.register();

        courses.load();
        leaderboard.load();
        holograms.rebuildAll();
        for (Player player : Bukkit.getOnlinePlayers()) {
            runs.restorePending(player);
        }

        listen(new RunListener(this), new ProtectionListener(this),
                new ItemListener(this), holograms, bridge);
        Bukkit.getScheduler().runTaskTimer(this, new RunHud(this), 1L, 2L);
        Bukkit.getScheduler().runTaskTimer(this, new PointParticles(this), 20L, 4L);
        ParkourCommand.register(this);
    }

    @Override
    public void onDisable() {
        if (runs != null) runs.stopAll(StopReason.SHUTDOWN);
        if (holograms != null) holograms.clear();
        if (leaderboard != null) leaderboard.close();
    }

    public void reload() {
        reloadConfig();
        settings = Settings.load(getConfig());
        messages.reload();
        holograms.rebuildAll();
    }

    public void refresh(Course course) {
        courses.save();
        runs.stopAll(course, StopReason.EDIT);
        holograms.rebuild(course);
    }

    private void listen(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    public Settings settings() {
        return settings;
    }

    public Messages messages() {
        return messages;
    }

    public CourseManager courses() {
        return courses;
    }

    public Leaderboard leaderboard() {
        return leaderboard;
    }

    public HologramManager holograms() {
        return holograms;
    }

    public RunManager runs() {
        return runs;
    }

    public ClientBridge bridge() {
        return bridge;
    }
}
