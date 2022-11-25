package dev.cerus.pluginjam;

import dev.cerus.pluginjam.itemweight.ItemWeightRegistry;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginJamPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getLogger().info("DAS IST DIE TANNE VON MEINEN SCHWIEGERELTERN DAS WEISST DU ODER???");

        this.getServer().getServicesManager().register(
                ItemWeightRegistry.class,
                new ItemWeightRegistry(),
                this,
                ServicePriority.Normal
        );
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Bravo Six, going dark");
    }

}
