package dev.cerus.pluginjam.playerweight;

import dev.cerus.pluginjam.itemweight.ItemWeightRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerWeightCalculator extends BukkitRunnable {

    private final Map<UUID, Float> playerWeights;
    private final ItemWeightRegistry itemWeightRegistry;

    public PlayerWeightCalculator(final ItemWeightRegistry itemWeightRegistry) {
        this.playerWeights = new ConcurrentHashMap<>();
        this.itemWeightRegistry = itemWeightRegistry;
    }

    @Override
    public void run() {
        this.playerWeights.clear();
        Bukkit.getOnlinePlayers().forEach(player -> {
            final float weight = this.calculatePlayerWeight(player);
            this.playerWeights.put(player.getUniqueId(), weight);
            player.sendActionBar(Component.text(String.format("§aDu wiegst aktuell §b%.2fkg§a.", weight)));
        });
    }

    private float calculatePlayerWeight(final Player player) {
        float weight = 0;
        for (final ItemStack item : player.getInventory()) {
            if (item == null) {
                continue;
            }
            weight += item.getAmount() * this.itemWeightRegistry.lookup(item.getType()).orElse(0f);
        }
        weight += player.getArrowsInBody() * this.itemWeightRegistry.lookup(Material.ARROW).orElse(0f);
        return weight;
    }

}
