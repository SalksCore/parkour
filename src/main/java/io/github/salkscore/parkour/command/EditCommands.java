package io.github.salkscore.parkour.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.course.Course;
import io.github.salkscore.parkour.course.Point;
import io.github.salkscore.parkour.util.Locations;
import io.github.salkscore.parkour.util.Messages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.entity.Player;

final class EditCommands extends CommandGroup {

    private enum Target {
        START, FINISH, SPAWN, BOARD, FALL;

        String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private static final List<Target> OPTIONAL = List.of(Target.SPAWN, Target.BOARD, Target.FALL);

    EditCommands(ParkourPlugin plugin) {
        super(plugin, "parkour.admin");
    }

    @Override
    void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        RequiredArgumentBuilder<CommandSourceStack, String> set = courseArg();
        for (Target target : Target.values()) {
            set.then(Commands.literal(target.key()).executes(ctx -> set(ctx, target)));
        }
        RequiredArgumentBuilder<CommandSourceStack, String> unset = courseArg();
        for (Target target : OPTIONAL) {
            unset.then(Commands.literal(target.key()).executes(ctx -> unset(ctx, target)));
        }

        root.then(literal("set").then(set));
        root.then(literal("unset").then(unset));
        root.then(literal("addcp").then(courseArg().executes(this::addCheckpoint)));
        root.then(literal("delcp").then(courseArg()
                .executes(ctx -> removeCheckpoint(ctx, -1))
                .then(Commands.argument("n", IntegerArgumentType.integer(1))
                        .executes(ctx -> removeCheckpoint(ctx,
                                IntegerArgumentType.getInteger(ctx, "n"))))));
    }

    private int set(CommandContext<CommandSourceStack> ctx, Target target) {
        Player player = player(ctx);
        Course course = player == null ? null : course(ctx);
        if (course == null) return 0;
        Location loc = Locations.snap(player.getLocation());

        switch (target) {
            case START -> {
                if (occupied(player, loc, course, Point.Type.START)) return 0;
                course.start(loc);
            }
            case FINISH -> {
                if (occupied(player, loc, course, Point.Type.FINISH)) return 0;
                course.finish(loc);
            }
            case SPAWN -> course.spawn(player.getLocation());
            case BOARD -> course.board(loc.add(0, 0.5, 0));
            case FALL -> course.fallY((double) player.getLocation().getBlockY());
        }

        plugin.refresh(course);
        String value = course.fallY() == null ? "" : String.valueOf(course.fallY().intValue());
        plugin.messages().send(player, "edit." + target.key(), Messages.of("value", value));
        return OK;
    }

    private int unset(CommandContext<CommandSourceStack> ctx, Target target) {
        Course course = course(ctx);
        if (course == null) return 0;
        switch (target) {
            case SPAWN -> course.spawn(null);
            case BOARD -> course.board(null);
            case FALL -> course.fallY(null);
            default -> {
                return 0;
            }
        }
        plugin.refresh(course);
        plugin.messages().send(sender(ctx), "edit.unset");
        return OK;
    }

    private int addCheckpoint(CommandContext<CommandSourceStack> ctx) {
        Player player = player(ctx);
        Course course = player == null ? null : course(ctx);
        if (course == null) return 0;
        Location loc = Locations.snap(player.getLocation());
        if (occupied(player, loc, course, null)) return 0;

        course.checkpoints().add(loc);
        plugin.refresh(course);
        plugin.messages().send(player, "edit.checkpoint-added", Messages.of("n", course.size()));
        return OK;
    }

    private int removeCheckpoint(CommandContext<CommandSourceStack> ctx, int index) {
        Course course = course(ctx);
        if (course == null) return 0;
        int n = index == -1 ? course.size() : index;
        if (n < 1 || n > course.size()) {
            plugin.messages().send(sender(ctx), "edit.no-checkpoint");
            return 0;
        }

        course.checkpoints().remove(n - 1);
        plugin.refresh(course);
        plugin.messages().send(sender(ctx), "edit.checkpoint-removed", Messages.of("n", n));
        return OK;
    }

    private boolean occupied(Player player, Location loc, Course course, Point.Type type) {
        Point point = plugin.courses().pointAt(loc);
        if (point == null || point.course() == course && point.type() == type) return false;
        plugin.messages().send(player, "edit.occupied");
        return true;
    }
}
