package io.github.salkscore.parkour.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.course.Course;
import io.github.salkscore.parkour.course.CourseManager;
import io.github.salkscore.parkour.run.StopReason;
import io.github.salkscore.parkour.util.Locations;
import io.github.salkscore.parkour.util.Messages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class AdminCommands extends CommandGroup {

    AdminCommands(ParkourPlugin plugin) {
        super(plugin, "parkour.admin");
    }

    @Override
    void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(literal("create")
                .then(Commands.argument("id", StringArgumentType.word()).executes(this::create)));
        root.then(literal("delete").then(courseArg().executes(this::delete)));
        root.then(literal("rename").then(courseArg()
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(this::rename))));
        RequiredArgumentBuilder<CommandSourceStack, String> player =
                Commands.argument("player", StringArgumentType.word())
                        .suggests(this::suggestPlayers)
                        .executes(ctx -> reset(ctx, StringArgumentType.getString(ctx, "player")));
        root.then(literal("reset")
                .then(courseArg().executes(ctx -> reset(ctx, null)).then(player)));
        root.then(literal("tp").then(courseArg().executes(this::tp)));
        root.then(literal("info").then(courseArg().executes(this::info)));
        root.then(literal("reload").executes(this::reload));
    }

    private int create(CommandContext<CommandSourceStack> ctx) {
        String id = StringArgumentType.getString(ctx, "id").toLowerCase(Locale.ROOT);
        CommandSender sender = sender(ctx);
        if (!CourseManager.isValidId(id)) {
            plugin.messages().send(sender, "course.invalid-id");
            return 0;
        }
        if (plugin.courses().create(id) == null) {
            plugin.messages().send(sender, "course.exists", Messages.of("id", id));
            return 0;
        }
        plugin.courses().save();
        plugin.messages().send(sender, "course.created", Messages.of("id", id));
        return OK;
    }

    private int delete(CommandContext<CommandSourceStack> ctx) {
        Course course = course(ctx);
        if (course == null) return 0;
        plugin.runs().stopAll(course, StopReason.EDIT);

        plugin.displays().remove(course.id());
        plugin.leaderboard().reset(course.id());
        plugin.courses().delete(course.id());
        plugin.messages().send(sender(ctx), "course.deleted", Messages.course(course));
        return OK;
    }

    private int rename(CommandContext<CommandSourceStack> ctx) {
        Course course = course(ctx);
        if (course == null) return 0;
        course.name(StringArgumentType.getString(ctx, "name"));
        plugin.courses().save();
        plugin.displays().rebuild(course);
        plugin.messages().send(sender(ctx), "course.renamed", Messages.course(course));
        return OK;
    }

    private int reset(CommandContext<CommandSourceStack> ctx, String name) {
        Course course = course(ctx);
        if (course == null) return 0;
        CommandSender sender = sender(ctx);
        if (name == null) {
            plugin.leaderboard().reset(course.id());
            plugin.messages().send(sender, "top.reset", Messages.course(course));
        } else if (plugin.leaderboard().reset(course.id(), name)) {
            plugin.messages().send(sender, "top.reset-player", Messages.of("player", name));
        } else {
            plugin.messages().send(sender, "top.unknown-player", Messages.of("player", name));
            return 0;
        }
        plugin.displays().refresh(course);
        return OK;
    }

    private CompletableFuture<Suggestions> suggestPlayers(
            CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String id = StringArgumentType.getString(ctx, "course").toLowerCase(Locale.ROOT);
        String input = builder.getRemainingLowerCase();
        for (String name : plugin.leaderboard().names(id)) {
            if (name.toLowerCase(Locale.ROOT).startsWith(input)) builder.suggest(name);
        }
        return builder.buildFuture();
    }

    private int tp(CommandContext<CommandSourceStack> ctx) {
        Player player = player(ctx);
        Course course = player == null ? null : course(ctx);
        if (course == null) return 0;
        if (course.start() == null) {
            plugin.messages().send(player, "edit.no-start");
            return 0;
        }
        plugin.runs().stop(player, StopReason.SWITCH);
        player.teleport(course.start());
        return OK;
    }

    private int info(CommandContext<CommandSourceStack> ctx) {
        Course course = course(ctx);
        if (course == null) return 0;
        CommandSender sender = sender(ctx);
        plugin.messages().send(sender, "info.header", Messages.course(course));
        line(sender, "start", place(course.start()));
        line(sender, "finish", place(course.finish()));
        line(sender, "spawn", place(course.spawn()));
        line(sender, "board", place(course.board()));
        line(sender, "fall", course.fallY() == null ? none() : "%.1f".formatted(course.fallY()));
        line(sender, "checkpoints", String.valueOf(course.size()));
        return OK;
    }

    private void line(CommandSender sender, String key, String value) {
        plugin.messages().send(sender, "info." + key, Messages.of("value", value));
    }

    private String place(Location loc) {
        return loc == null ? none() : Locations.pretty(loc);
    }

    private String none() {
        return plugin.messages().raw("info.none");
    }

    private int reload(CommandContext<CommandSourceStack> ctx) {
        plugin.reload();
        plugin.messages().send(sender(ctx), "reload");
        return OK;
    }
}
