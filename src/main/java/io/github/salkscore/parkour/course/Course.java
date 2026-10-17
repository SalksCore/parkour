package io.github.salkscore.parkour.course;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;

public final class Course {

    private final String id;
    private final List<Location> checkpoints = new ArrayList<>();
    private String name;
    private Location start;
    private Location finish;
    private Location spawn;
    private Location board;
    private Double fallY;

    public Course(String id) {
        this.id = id;
        this.name = id;
    }

    public boolean isReady() {
        return start != null && finish != null;
    }

    public int size() {
        return checkpoints.size();
    }

    public Location respawn(int reached) {
        Location loc = reached <= 0 ? start : checkpoints.get(reached - 1);
        return loc.clone();
    }

    public Location exit() {
        return (spawn != null ? spawn : start).clone();
    }

    public String id() {
        return id;
    }

    public List<Location> checkpoints() {
        return checkpoints;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public Location start() {
        return start;
    }

    public void start(Location start) {
        this.start = start;
    }

    public Location finish() {
        return finish;
    }

    public void finish(Location finish) {
        this.finish = finish;
    }

    public Location spawn() {
        return spawn;
    }

    public void spawn(Location spawn) {
        this.spawn = spawn;
    }

    public Location board() {
        return board;
    }

    public void board(Location board) {
        this.board = board;
    }

    public Double fallY() {
        return fallY;
    }

    public void fallY(Double fallY) {
        this.fallY = fallY;
    }
}
