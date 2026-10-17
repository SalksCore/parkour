package io.github.salkscore.parkour.course;

public record Point(Course course, Type type, int index) {

    public enum Type {
        START, CHECKPOINT, FINISH
    }
}
