package dev.cerus.pluginjam.itemweight;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemWeightReader {

    private final JavaPlugin plugin;
    private final ItemWeightRegistry registry;

    public ItemWeightReader(final JavaPlugin plugin, final ItemWeightRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    public int readAndRegister() {
        // TODO: Proper exception handling

        final StringBuilder builder = new StringBuilder();
        try (final InputStream in = this.plugin.getClass().getClassLoader().getResourceAsStream("weight.yml")) {
            final byte[] buf = new byte[512];
            int read;
            while ((read = in.read(buf)) != -1) {
                builder.append(new String(buf, 0, read));
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        final YamlConfiguration weightConfig = new YamlConfiguration();
        try {
            weightConfig.loadFromString(builder.toString());
        } catch (final InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        int count = 0;
        for (final String key : weightConfig.getKeys(false)) {
            if (key.startsWith("#")) {
                final String[] split = key.substring(1).split(":");
                final String tagRegistry = split[0];
                final String namespaceKey = split[1];
                final Tag<Material> tag = Bukkit.getTag(tagRegistry, NamespacedKey.minecraft(namespaceKey), Material.class);
                if (tag != null) {
                    final float weight = (float) weightConfig.getDouble(key);
                    for (final Material value : tag.getValues()) {
                        this.registry.register(value, weight);
                        count++;
                    }
                }
            } else if (key.startsWith("!")) {
                final String regex = key.substring(1);
                final float weight = (float) weightConfig.getDouble(key);
                final List<Material> mats = Arrays.stream(Material.values())
                        .filter(v -> v.name().matches(regex))
                        .toList();
                mats.forEach(m -> this.registry.register(m, weight));
                count += mats.size();
            } else if (key.startsWith("minecraft:")) {
                final Material material = Material.matchMaterial(key);
                if (material != null) {
                    final float weight = (float) weightConfig.getDouble(key);
                    this.registry.register(material, weight);
                    count++;
                }
            } else {
                final float weight = (float) weightConfig.getDouble(key);
                this.registry.register(Material.valueOf(key), weight);
                count++;
            }
        }
        return count;
    }

}
