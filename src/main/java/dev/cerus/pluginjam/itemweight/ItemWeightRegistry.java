package dev.cerus.pluginjam.itemweight;

import org.bukkit.Material;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ItemWeightRegistry {

    private final Map<Material, Float> itemWeights;

    public ItemWeightRegistry() {
        this.itemWeights = new ConcurrentHashMap<>();
    }

    public void register(final Material itemType, final float weight) {
        this.itemWeights.put(itemType, weight);
    }

    public Optional<Float> lookup(final Material itemType) {
        return Optional.ofNullable(this.itemWeights.get(itemType));
    }

}
