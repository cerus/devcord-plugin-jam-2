package dev.cerus.pluginjam;

import dev.cerus.pluginjam.itemweight.ItemWeightReader;
import dev.cerus.pluginjam.itemweight.ItemWeightRegistry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PluginJamPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getLogger().info("DAS IST DIE TANNE VON MEINEN SCHWIEGERELTERN DAS WEISST DU ODER???");

        final ItemWeightRegistry itemWeightRegistry = new ItemWeightRegistry();
        this.getServer().getServicesManager().register(
                ItemWeightRegistry.class,
                itemWeightRegistry,
                this,
                ServicePriority.Normal
        );

        final ItemWeightReader itemWeightReader = new ItemWeightReader(this, itemWeightRegistry);
        final int count = itemWeightReader.readAndRegister();
        this.getLogger().info(count + " weights were registered");

        this.getCommand("fettsack").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("dump2")) {
                // Dump all items that do not have a recipe and no weight associated with them
                final ItemWeightRegistry weightRegistry = Bukkit.getServicesManager().getRegistration(ItemWeightRegistry.class).getProvider();
                final List<Material> dump = new ArrayList<>();
                for (final Material value : Material.values()) {
                    if (!value.name().startsWith("LEGACY_") && value.isItem() && Bukkit.getRecipesFor(new ItemStack(value)).isEmpty() && weightRegistry.lookup(value).isEmpty()) {
                        dump.add(value);
                    }
                }

                this.getDataFolder().mkdirs();
                final File out = new File(this.getDataFolder(), "dump2.txt");
                try (final FileOutputStream outStr = new FileOutputStream(out)) {
                    for (final Material material : dump) {
                        outStr.write(material.name().getBytes(StandardCharsets.UTF_8));
                        outStr.write('\n');
                    }
                    outStr.flush();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
                sender.sendMessage("Dumped");
            }
            if (args[0].equalsIgnoreCase("dump")) {
                // Dump all items that do not have a recipe
                final List<Material> dump = new ArrayList<>();
                for (final Material value : Material.values()) {
                    if (!value.name().startsWith("LEGACY_") && value.isItem() && Bukkit.getRecipesFor(new ItemStack(value)).isEmpty()) {
                        dump.add(value);
                    }
                }

                this.getDataFolder().mkdirs();
                final File out = new File(this.getDataFolder(), "dump.txt");
                try (final FileOutputStream outStr = new FileOutputStream(out)) {
                    for (final Material material : dump) {
                        outStr.write(material.name().getBytes(StandardCharsets.UTF_8));
                        outStr.write('\n');
                    }
                    outStr.flush();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
                sender.sendMessage("Dumped");
            }
        }
        return true;
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Bravo Six, going dark");
    }

}
