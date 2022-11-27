package dev.cerus.pluginjam;

import dev.cerus.pluginjam.itemweight.ItemWeightReader;
import dev.cerus.pluginjam.itemweight.ItemWeightRegistry;
import dev.cerus.pluginjam.itemweight.calculator.ItemWeightCalculator;
import dev.cerus.pluginjam.itemweight.calculator.RecipeItemWeightCalculator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
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
        int count = itemWeightReader.readAndRegister();
        this.getLogger().info(count + " default weights were registered");

        count = 0;
        for (final Material value : Material.values()) {
            if (value == Material.ENCHANTED_BOOK) {
                continue;
            }
            if (!value.name().startsWith("LEGACY_") && value.isItem() && !Bukkit.getRecipesFor(new ItemStack(value)).stream()
                    .filter(recipe -> recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe)
                    .toList().isEmpty() && itemWeightRegistry.lookup(value).isEmpty()) {
                final float weight;
                try {
                    weight = ItemWeightCalculator.recipe(value).calculateWeight(itemWeightRegistry, value, 1);
                } catch (final StackOverflowError so) {
                    this.getLogger().severe("Stack Overflow during weight calculation of " + value);
                    continue;
                }
                itemWeightRegistry.register(value, weight);
                count++;
            }
        }
        this.getLogger().info(count + " recipe weights were registered");

        this.getCommand("fettsack").setExecutor(this);

        this.getServer().getScheduler().runTaskTimer(this, () -> {
            for (final Player player : this.getServer().getOnlinePlayers()) {
                final float v = this.calculatePlayerWeight(player, itemWeightRegistry);
                player.sendActionBar(v + "");
            }
        }, 0, 20);
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("get")) {
                final Player player = (Player) sender;
                final ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
                final ItemWeightRegistry weightRegistry = Bukkit.getServicesManager().getRegistration(ItemWeightRegistry.class).getProvider();
                sender.sendMessage(weightRegistry.lookup(itemInMainHand.getType()).map(v -> v + "").orElse("?"));
                RecipeItemWeightCalculator.recalc = true;
                sender.sendMessage(ItemWeightCalculator.recipe(itemInMainHand.getType()).calculateWeight(weightRegistry, itemInMainHand.getType(), 1) + "");
                RecipeItemWeightCalculator.recalc = false;
            }
            if (args[0].equalsIgnoreCase("dump2")) {
                // Dump all items that do not have a recipe and no weight associated with them
                final ItemWeightRegistry weightRegistry = Bukkit.getServicesManager().getRegistration(ItemWeightRegistry.class).getProvider();
                final List<Material> dump = new ArrayList<>();
                for (final Material value : Material.values()) {
                    if (!value.name().startsWith("LEGACY_") && value.isItem() && Bukkit.getRecipesFor(new ItemStack(value)).stream()
                            .filter(recipe -> recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe)
                            .toList().isEmpty() && weightRegistry.lookup(value).isEmpty()) {
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
                    if (!value.name().startsWith("LEGACY_") && value.isItem() && Bukkit.getRecipesFor(new ItemStack(value)).stream()
                            .filter(recipe -> recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe)
                            .toList().isEmpty()) {
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

    private float calculatePlayerWeight(final Player player, final ItemWeightRegistry itemWeightRegistry) {
        float weight = 0;
        for (final ItemStack item : player.getInventory()) {
            if (item == null) {
                continue;
            }
            weight += item.getAmount() * itemWeightRegistry.lookup(item.getType()).orElse(0f);
            if (item.getItemMeta() instanceof final BlockStateMeta meta
                    && meta.getBlockState() instanceof final Container container) {
                for (final ItemStack innerItem : container.getInventory()) {
                    if (innerItem == null) {
                        continue;
                    }
                    weight += innerItem.getAmount() * itemWeightRegistry.lookup(innerItem.getType()).orElse(0f);
                }
            }
        }
        weight += player.getArrowsInBody() * itemWeightRegistry.lookup(Material.ARROW).orElse(0f);
        return weight;
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Bravo Six, going dark");
    }

}
