package io.github.salkscore.parkour.run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

final class StateStore {

    private final File folder;
    private final Logger logger;

    StateStore(File folder, Logger logger) {
        this.folder = folder;
        this.logger = logger;
    }

    void save(UUID id, PlayerState state) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("gamemode", state.gameMode().name());
        yaml.set("allow-flight", state.allowFlight());
        yaml.set("flying", state.flying());
        yaml.set("effects", new ArrayList<>(state.effects()));
        byte[] items = ItemStack.serializeItemsAsBytes(state.contents());
        yaml.set("items", Base64.getEncoder().encodeToString(items));

        try {
            folder.mkdirs();
            yaml.save(file(id));
        } catch (IOException e) {
            logger.warning("Inventaire de " + id + " non sauvegardé : " + e.getMessage());
        }
    }

    PlayerState load(UUID id) {
        File file = file(id);
        if (!file.exists()) return null;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        byte[] items = Base64.getDecoder().decode(yaml.getString("items", ""));
        List<PotionEffect> effects = yaml.getList("effects", List.of()).stream()
                .filter(PotionEffect.class::isInstance)
                .map(PotionEffect.class::cast)
                .toList();

        return new PlayerState(
                ItemStack.deserializeItemsFromBytes(items),
                GameMode.valueOf(yaml.getString("gamemode", "SURVIVAL")),
                yaml.getBoolean("allow-flight"),
                yaml.getBoolean("flying"),
                effects);
    }

    void delete(UUID id) {
        File file = file(id);
        if (file.exists() && !file.delete()) logger.warning("Impossible de supprimer " + file);
    }

    private File file(UUID id) {
        return new File(folder, id + ".yml");
    }
}
