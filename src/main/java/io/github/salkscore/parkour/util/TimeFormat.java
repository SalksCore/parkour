package io.github.salkscore.parkour.util;

public final class TimeFormat {

    private TimeFormat() {
    }

    public static String format(long millis) {
        long hours = millis / 3_600_000;
        long minutes = millis / 60_000 % 60;
        long seconds = millis / 1000 % 60;
        long rest = millis % 1000;
        if (hours > 0) {
            return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, rest);
        }
        return String.format("%02d:%02d.%03d", minutes, seconds, rest);
    }
}
