package dev.cerus.pluginjam.itemweight.calculator;

import dev.cerus.pluginjam.itemweight.ItemWeightRegistry;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class RecipeItemWeightCalculator implements ItemWeightCalculator {

    public static boolean recalc = false;

    private final Material itemType;

    public RecipeItemWeightCalculator(final Material itemType) {
        this.itemType = itemType;
    }

    @Override
    public float calculateWeight(final ItemWeightRegistry registry, final Material itemType, final int amount) {
        if (itemType != this.itemType) {
            throw new IllegalStateException("Incompatible item type");
        }
        if (!recalc && registry.lookup(itemType).isPresent()) {
            return registry.lookup(itemType).get();
        }
        recalc = false;

        final List<Recipe> recipes = Bukkit.getRecipesFor(new ItemStack(itemType)).stream()
                .filter(recipe -> recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe)
                .toList();
        if (recipes.isEmpty()) {
            return ItemWeightCalculator.basic(registry, itemType).calculateWeight(registry, itemType, amount);
        }

        float weight = 0;
        for (final Recipe recipe : recipes) {
            weight += (float) this.calculateWeight(recipe, registry);
        }
        return (weight / recipes.size()) * amount;
    }

    private double calculateWeight(final Recipe recipe, final ItemWeightRegistry registry) {
        double weight = 0;
        final int resultAmount = recipe.getResult().getAmount();
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            weight = this.calculateWeight(shapedRecipe.getChoiceMap().values(), registry);
        } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            weight = this.calculateWeight(shapelessRecipe.getChoiceList(), registry);
        }
        return weight / resultAmount;
    }

    private double calculateWeight(final Collection<RecipeChoice> recipeChoices, final ItemWeightRegistry registry) {
        double weight = 0;
        for (final RecipeChoice value : recipeChoices) {
            List<Material> choices = List.of();
            if (value instanceof RecipeChoice.MaterialChoice matChoice) {
                choices = matChoice.getChoices();
            } else if (value instanceof RecipeChoice.ExactChoice exactChoice) {
                choices = exactChoice.getChoices().stream()
                        .map(ItemStack::getType)
                        .collect(Collectors.toList());
            }

            double ingredientWeight = 0;
            final int ingredients = choices.size();
            for (final Material ingredient : choices) {
                ingredientWeight += ItemWeightCalculator.recipe(ingredient).calculateWeight(registry, ingredient, 1);
            }
            if (ingredientWeight > 0f) {
                weight += ingredientWeight / ingredients;
            }
        }
        return weight;
    }

}
