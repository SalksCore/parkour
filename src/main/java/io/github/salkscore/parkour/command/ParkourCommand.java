package io.github.salkscore.parkour.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.salkscore.parkour.ParkourPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.List;
import org.bukkit.command.CommandSender;

public final class ParkourCommand {

    private ParkourCommand() {
    }

    public static void register(ParkourPlugin plugin) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("parkour")
                .executes(ctx -> help(plugin, ctx.getSource().getSender()));

        new PlayerCommands(plugin).attach(root);
        new AdminCommands(plugin).attach(root);
        new EditCommands(plugin).attach(root);

        LiteralCommandNode<CommandSourceStack> node = root.build();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(node, "Parcours et classements", List.of("pk")));
    }

    private static int help(ParkourPlugin plugin, CommandSender sender) {
        plugin.messages().sendList(sender, "help");
        if (sender.hasPermission("parkour.admin")) plugin.messages().sendList(sender, "help-admin");
        return Command.SINGLE_SUCCESS;
    }
}
