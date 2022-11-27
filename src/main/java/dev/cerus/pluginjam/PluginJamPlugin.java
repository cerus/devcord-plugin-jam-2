package dev.cerus.pluginjam;

import dev.cerus.pluginjam.itemweight.ItemWeightReader;
import dev.cerus.pluginjam.itemweight.ItemWeightRegistry;
import dev.cerus.pluginjam.itemweight.calculator.ItemWeightCalculator;
import dev.cerus.pluginjam.playerweight.PlayerWeightCalculator;
import dev.cerus.pluginjam.playerweight.WeightEffectApplier;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

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

        final PlayerWeightCalculator playerWeightCalculator = new PlayerWeightCalculator(itemWeightRegistry);
        playerWeightCalculator.runTaskTimerAsynchronously(this, 0, 40);

        final WeightEffectApplier weightEffectApplier = new WeightEffectApplier(playerWeightCalculator);
        this.getServer().getPluginManager().registerEvents(weightEffectApplier, this);
        weightEffectApplier.runTaskTimer(this, 0, 20);

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
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Bravo Six, going dark");
    }

}
