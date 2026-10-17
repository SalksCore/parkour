package io.github.salkscore.parkour.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.course.Course;
import io.github.salkscore.parkour.util.Messages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

abstract class CommandGroup {

    protected static final int OK = Command.SINGLE_SUCCESS;

    protected final ParkourPlugin plugin;
    private final String permission;

    CommandGroup(ParkourPlugin plugin, String permission) {
        this.plugin = plugin;
        this.permission = permission;
    }

    abstract void attach(LiteralArgumentBuilder<CommandSourceStack> root);

    protected LiteralArgumentBuilder<CommandSourceStack> literal(String name) {
        return Commands.literal(name).requires(src -> src.getSender().hasPermission(permission));
    }

    protected RequiredArgumentBuilder<CommandSourceStack, String> courseArg() {
        return Commands.argument("course", StringArgumentType.word())
                .suggests(this::suggestCourses);
    }

    private CompletableFuture<Suggestions> suggestCourses(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String input = builder.getRemainingLowerCase();
        for (String id : plugin.courses().ids()) {
            if (id.startsWith(input)) builder.suggest(id);
        }
        return builder.buildFuture();
    }

    protected Course course(CommandContext<CommandSourceStack> ctx) {
        String id = StringArgumentType.getString(ctx, "course").toLowerCase(Locale.ROOT);
        Course course = plugin.courses().get(id);
        if (course == null) {
            plugin.messages().send(sender(ctx), "course.unknown", Messages.of("id", id));
        }
        return course;
    }

    protected Player player(CommandContext<CommandSourceStack> ctx) {
        if (sender(ctx) instanceof Player player) return player;
        plugin.messages().send(sender(ctx), "error.player-only");
        return null;
    }

    protected static CommandSender sender(CommandContext<CommandSourceStack> ctx) {
        return ctx.getSource().getSender();
    }
}
