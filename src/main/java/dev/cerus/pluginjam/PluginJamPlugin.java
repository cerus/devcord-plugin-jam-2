package dev.cerus.pluginjam;

import dev.cerus.pluginjam.itemweight.ItemWeightRegistry;
import dev.cerus.pluginjam.playerweight.PlayerWeightCalculator;
import org.bukkit.Material;
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
        itemWeightRegistry.register(Material.ARROW, 20);

        new PlayerWeightCalculator(itemWeightRegistry).runTaskTimerAsynchronously(this, 0, 40);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Bravo Six, going dark");
    }

}
