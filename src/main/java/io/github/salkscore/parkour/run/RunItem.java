package io.github.salkscore.parkour.run;

import java.util.Locale;

public enum RunItem {
    CHECKPOINT, RESTART, LEAVE;

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }
}
