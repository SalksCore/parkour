package io.github.salkscore.parkour.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.course.Course;
import io.github.salkscore.parkour.util.Locations;
import io.github.salkscore.parkour.util.Messages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;

final class CheckpointCommands extends CommandGroup {

    CheckpointCommands(ParkourPlugin plugin) {
        super(plugin, "parkour.admin");
    }

    @Override
    void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(literal("addcp").then(courseArg()
                .executes(ctx -> add(ctx, 0))
                .then(number("n").executes(ctx -> add(ctx, get(ctx, "n"))))));

        root.then(literal("delcp").then(courseArg()
                .executes(ctx -> remove(ctx, 0))
                .then(number("n").executes(ctx -> remove(ctx, get(ctx, "n"))))));

        root.then(literal("movecp").then(courseArg()
                .then(number("from").then(number("to")
                        .executes(ctx -> move(ctx, get(ctx, "from"), get(ctx, "to")))))));
    }

    // sans numéro, le checkpoint va à la fin
    private int add(CommandContext<CommandSourceStack> ctx, int n) {
        Player player = player(ctx);
        Course course = player == null ? null : course(ctx);
        if (course == null) return 0;

        Location loc = Locations.snap(player.getLocation());
        if (plugin.courses().pointAt(loc) != null) {
            plugin.messages().send(player, "edit.occupied");
            return 0;
        }

        List<Location> checkpoints = course.checkpoints();
        int index = n == 0 ? checkpoints.size() + 1 : Math.min(n, checkpoints.size() + 1);
        checkpoints.add(index - 1, loc);
        plugin.refresh(course);
        plugin.messages().send(player, "edit.checkpoint-added", Messages.of("n", index));
        return OK;
    }

    private int remove(CommandContext<CommandSourceStack> ctx, int n) {
        Course course = course(ctx);
        if (course == null) return 0;
        int index = n == 0 ? course.size() : n;
        if (!exists(ctx, course, index)) return 0;

        course.checkpoints().remove(index - 1);
        plugin.refresh(course);
        plugin.messages().send(sender(ctx), "edit.checkpoint-removed", Messages.of("n", index));
        return OK;
    }

    private int move(CommandContext<CommandSourceStack> ctx, int from, int to) {
        Course course = course(ctx);
        if (course == null || !exists(ctx, course, from) || !exists(ctx, course, to)) return 0;

        List<Location> checkpoints = course.checkpoints();
        checkpoints.add(to - 1, checkpoints.remove(from - 1));
        plugin.refresh(course);
        plugin.messages().send(sender(ctx), "edit.checkpoint-moved",
                Messages.of("from", from), Messages.of("to", to));
        return OK;
    }

    private boolean exists(CommandContext<CommandSourceStack> ctx, Course course, int n) {
        if (n >= 1 && n <= course.size()) return true;
        plugin.messages().send(sender(ctx), "edit.no-checkpoint", Messages.of("n", n));
        return false;
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Integer> number(String name) {
        return Commands.argument(name, IntegerArgumentType.integer(1));
    }

    private static int get(CommandContext<CommandSourceStack> ctx, String name) {
        return IntegerArgumentType.getInteger(ctx, name);
    }
}
