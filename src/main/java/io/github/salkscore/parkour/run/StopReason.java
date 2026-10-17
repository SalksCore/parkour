package io.github.salkscore.parkour.run;

public enum StopReason {
    FINISH(false, null),
    LEAVE(true, "run.leave"),
    CANCEL(false, "run.cancelled"),
    EDIT(true, "run.edited"),
    SWITCH(false, null),
    QUIT(true, null),
    SHUTDOWN(true, null);

    private final boolean teleport;
    private final String message;

    StopReason(boolean teleport, String message) {
        this.teleport = teleport;
        this.message = message;
    }

    public boolean teleport() {
        return teleport;
    }

    public String message() {
        return message;
    }
}
