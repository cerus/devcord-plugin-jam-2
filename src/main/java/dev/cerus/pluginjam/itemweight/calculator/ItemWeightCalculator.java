package dev.cerus.pluginjam.itemweight.calculator;

import dev.cerus.pluginjam.itemweight.ItemWeightRegistry;
import org.bukkit.Material;

public interface ItemWeightCalculator {

    static ItemWeightCalculator basic(final ItemWeightRegistry registry, final Material type) {
        return new BasicItemWeightCalculator(type, registry.lookup(type).orElse(0f));
    }

    static ItemWeightCalculator recipe(final Material type) {
        return new RecipeItemWeightCalculator(type);
    }

    float calculateWeight(ItemWeightRegistry registry, Material itemType, int amount);

}
