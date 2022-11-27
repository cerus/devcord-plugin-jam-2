package dev.cerus.pluginjam.itemweight.calculator;

import dev.cerus.pluginjam.itemweight.ItemWeightRegistry;
import org.bukkit.Material;

public class BasicItemWeightCalculator implements ItemWeightCalculator {

    private final Material itemType;
    private final float weight;

    public BasicItemWeightCalculator(final Material itemType, final float weight) {
        this.itemType = itemType;
        this.weight = weight;
    }

    @Override
    public float calculateWeight(final ItemWeightRegistry registry, final Material itemType, final int amount) {
        if (itemType != this.itemType) {
            throw new IllegalStateException("Incompatible item type");
        }
        return this.weight * amount;
    }

    public Material getItemType() {
        return this.itemType;
    }

    public float getWeight() {
        return this.weight;
    }

}
