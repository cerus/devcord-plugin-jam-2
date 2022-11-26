package dev.cerus.pluginjam.playerweight;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class WeightEffectApplier extends BukkitRunnable implements Listener {

    private static final float JUMP_RESTRICTION_THRESHOLD = 100;
    private static final float MAX_ACCEPTABLE_WEIGHT = 200;

    private final PlayerWeightCalculator playerWeightCalculator;

    public WeightEffectApplier(final PlayerWeightCalculator playerWeightCalculator) {
        this.playerWeightCalculator = playerWeightCalculator;
    }

    @EventHandler
    public void handlePlayerMove(final PlayerMoveEvent event) {
        final float weight = this.playerWeightCalculator.getPlayerWeight(event.getPlayer().getUniqueId());
        if (weight < JUMP_RESTRICTION_THRESHOLD) {
            return;
        }
        final Vector velocity = event.getPlayer().getVelocity().clone();
        velocity.setY(velocity.getY() * .3);
        event.getPlayer().setVelocity(velocity);
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            final float weight = this.playerWeightCalculator.getPlayerWeight(player.getUniqueId());
            final float fraction = weight / MAX_ACCEPTABLE_WEIGHT;
            player.removePotionEffect(PotionEffectType.SLOW);
            if (fraction < .2) {
                return;
            }
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW,
                    Integer.MAX_VALUE,
                    (int) Math.ceil(6 * fraction),
                    false,
                    false,
                    false
            ));
        });
    }

}
