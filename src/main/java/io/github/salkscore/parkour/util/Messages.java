package io.github.salkscore.parkour.util;

import io.github.salkscore.parkour.ParkourPlugin;
import io.github.salkscore.parkour.course.Course;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Messages {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final ParkourPlugin plugin;
    private YamlConfiguration yaml;
    private TagResolver prefix;

    public Messages(ParkourPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) plugin.saveResource("messages.yml", false);
        yaml = YamlConfiguration.loadConfiguration(file);

        try (InputStream in = plugin.getResource("messages.yml")) {
            if (in != null) {
                InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                yaml.setDefaults(YamlConfiguration.loadConfiguration(reader));
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Impossible de lire messages.yml : " + e.getMessage());
        }
        prefix = Placeholder.parsed("prefix", yaml.getString("prefix", ""));
    }

    public Component get(String key, TagResolver... tags) {
        return parse(yaml.getString(key, key), tags);
    }

    public String raw(String key) {
        return yaml.getString(key, key);
    }

    public void send(Audience to, String key, TagResolver... tags) {
        String raw = yaml.getString(key);
        if (raw == null || raw.isEmpty()) return;
        to.sendMessage(parse(raw, tags));
    }

    public void sendList(Audience to, String key, TagResolver... tags) {
        for (String line : yaml.getStringList(key)) {
            to.sendMessage(parse(line, tags));
        }
    }

    private Component parse(String raw, TagResolver... tags) {
        return MM.deserialize(raw, TagResolver.builder().resolver(prefix).resolvers(tags).build());
    }

    public static Component parse(String raw) {
        return MM.deserialize(raw);
    }

    public static TagResolver course(Course course) {
        return TagResolver.resolver(
                Placeholder.parsed("course", course.name()),
                Placeholder.unparsed("id", course.id()));
    }

    public static TagResolver time(long millis) {
        return Placeholder.unparsed("time", TimeFormat.format(millis));
    }

    public static TagResolver of(String key, Object value) {
        return Placeholder.unparsed(key, String.valueOf(value));
    }
}
