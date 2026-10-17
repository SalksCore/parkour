package io.github.salkscore.parkour.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.board.Leaderboard;
import io.github.salkscore.parkour.course.Course;
import io.github.salkscore.parkour.run.RunManager;
import io.github.salkscore.parkour.run.StopReason;
import io.github.salkscore.parkour.util.Messages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class PlayerCommands extends CommandGroup {

    PlayerCommands(ParkourPlugin plugin) {
        super(plugin, "parkour.use");
    }

    @Override
    void attach(LiteralArgumentBuilder<CommandSourceStack> root) {
        RunManager runs = plugin.runs();
        root.then(literal("join").then(courseArg().executes(this::join)));
        root.then(literal("leave")
                .executes(ctx -> inRun(ctx, p -> runs.stop(p, StopReason.LEAVE))));
        root.then(literal("checkpoint").executes(ctx -> inRun(ctx, runs::respawn)));
        root.then(literal("restart").executes(ctx -> inRun(ctx, runs::restart)));
        root.then(literal("top").then(courseArg().executes(this::top)));
        root.then(literal("list").executes(this::list));
    }

    private int join(CommandContext<CommandSourceStack> ctx) {
        Player player = player(ctx);
        Course course = player == null ? null : course(ctx);
        if (course == null) return 0;
        if (!course.isReady()) {
            plugin.messages().send(player, "run.not-ready");
            return 0;
        }
        plugin.runs().stop(player, StopReason.SWITCH);
        player.teleport(course.start());
        plugin.runs().start(player, course);
        return OK;
    }

    private int inRun(CommandContext<CommandSourceStack> ctx, Consumer<Player> action) {
        Player player = player(ctx);
        if (player == null) return 0;
        if (plugin.runs().get(player) == null) {
            plugin.messages().send(player, "run.not-running");
            return 0;
        }
        action.accept(player);
        return OK;
    }

    private int top(CommandContext<CommandSourceStack> ctx) {
        Course course = course(ctx);
        if (course == null) return 0;
        CommandSender sender = sender(ctx);
        Messages messages = plugin.messages();
        TagResolver tags = Messages.course(course);
        List<Leaderboard.Entry> top = plugin.leaderboard().top(course.id(), 10);
        if (top.isEmpty()) {
            messages.send(sender, "top.empty", tags);
            return OK;
        }

        messages.send(sender, "top.header", tags);
        for (int i = 0; i < top.size(); i++) {
            Leaderboard.Entry entry = top.get(i);
            messages.send(sender, "top.line", Messages.of("rank", i + 1),
                    Messages.of("player", entry.name()), Messages.time(entry.time()));
        }

        if (!(sender instanceof Player player)) return OK;
        Leaderboard.Entry best = plugin.leaderboard().best(course.id(), player.getUniqueId());
        if (best != null) {
            int rank = plugin.leaderboard().rank(course.id(), player.getUniqueId());
            messages.send(sender, "top.self",
                    Messages.time(best.time()), Messages.of("rank", rank));
        }
        return OK;
    }

    private int list(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = sender(ctx);
        Messages messages = plugin.messages();
        boolean admin = sender.hasPermission("parkour.admin");
        List<Course> courses = plugin.courses().all().stream()
                .filter(course -> admin || course.isReady())
                .toList();
        if (courses.isEmpty()) {
            messages.send(sender, "list.empty");
            return OK;
        }

        messages.send(sender, "list.header");
        for (Course course : courses) {
            TagResolver tags = TagResolver.resolver(Messages.course(course),
                    Messages.of("n", course.size()));
            Component line = messages.get("list.entry", tags)
                    .clickEvent(ClickEvent.runCommand("/parkour join " + course.id()))
                    .hoverEvent(messages.get("list.hover", tags));
            sender.sendMessage(line);
        }
        return OK;
    }
}
