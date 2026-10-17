package io.github.salkscore.parkour.run;

import java.util.Arrays;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

record PlayerState(
        ItemStack[] contents,
        GameMode gameMode,
        boolean allowFlight,
        boolean flying,
        List<PotionEffect> effects) {

    static PlayerState capture(Player player) {
        ItemStack[] contents = Arrays.stream(player.getInventory().getContents())
                .map(item -> item == null ? ItemStack.empty() : item.clone())
                .toArray(ItemStack[]::new);
        return new PlayerState(contents, player.getGameMode(), player.getAllowFlight(),
                player.isFlying(), List.copyOf(player.getActivePotionEffects()));
    }

    void apply(Player player) {
        player.getInventory().setContents(contents);
        player.setGameMode(gameMode);
        player.setAllowFlight(allowFlight);
        if (allowFlight) player.setFlying(flying);
        player.clearActivePotionEffects();
        player.addPotionEffects(effects);
    }
}
