package io.github.salkscore.parkour.run;

import io.github.salkscore.parkour.course.Course;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;

public final class Session {

    private final UUID player;
    private final Course course;
    private final PlayerState state;
    private final BossBar bar;
    private long startedAt;
    private int reached;
    private boolean bypass;

    Session(UUID player, Course course, PlayerState state, BossBar bar) {
        this.player = player;
        this.course = course;
        this.state = state;
        this.bar = bar;
        restart();
    }

    void restart() {
        startedAt = System.currentTimeMillis();
        reached = 0;
        bar.progress(0f);
    }

    void reach(int index) {
        reached = index;
        bar.progress(Math.min(1f, (float) reached / (course.size() + 1)));
    }

    void bypass(Runnable action) {
        bypass = true;
        try {
            action.run();
        } finally {
            bypass = false;
        }
    }

    public boolean bypassing() {
        return bypass;
    }

    public long elapsed() {
        return System.currentTimeMillis() - startedAt;
    }

    public UUID player() {
        return player;
    }

    public Course course() {
        return course;
    }

    public int reached() {
        return reached;
    }

    PlayerState state() {
        return state;
    }

    BossBar bar() {
        return bar;
    }
}
