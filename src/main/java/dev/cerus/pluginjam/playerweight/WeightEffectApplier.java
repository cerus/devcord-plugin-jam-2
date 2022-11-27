package dev.cerus.pluginjam.playerweight;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class WeightEffectApplier extends BukkitRunnable implements Listener {

    private static final float JUMP_RESTRICTION_THRESHOLD = 1000;
    private static final float DAMAGE_THRESHOLD = 20_000;
    private static final float MAX_ACCEPTABLE_WEIGHT = 1000;
    private static final float DEFAULT_WALK_SPEED = 0.2f;
    private static final float SPEED_REDUCTION_STEP = 0.01f;
    private static final float JUMP_REDUCTION_STEP = 0.01f;

    private final PlayerWeightCalculator playerWeightCalculator;

    public WeightEffectApplier(final PlayerWeightCalculator playerWeightCalculator) {
        this.playerWeightCalculator = playerWeightCalculator;
    }

    //@EventHandler
    public void handlePlayerMove(final PlayerMoveEvent event) {
        final float weight = this.playerWeightCalculator.getPlayerWeight(event.getPlayer().getUniqueId());
        if (weight < JUMP_RESTRICTION_THRESHOLD) {
            return;
        }
        final Vector velocity = event.getPlayer().getVelocity().clone();
        if (velocity.getY() > 0) {
            final float fraction = weight / MAX_ACCEPTABLE_WEIGHT;
            final float sub = fraction * SPEED_REDUCTION_STEP;
            final float mult = 1f - sub;
            velocity.setY(mult <= 0f ? 0f : velocity.getY() * mult);
        }
        event.getPlayer().setVelocity(velocity);
    }

    @Override
    public void run() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            final float weight = this.playerWeightCalculator.getPlayerWeight(player.getUniqueId());
            final float fraction = weight / MAX_ACCEPTABLE_WEIGHT;
            if (fraction < 1f) {
                player.setWalkSpeed(DEFAULT_WALK_SPEED);
                return;
            }

            final float sub = fraction * SPEED_REDUCTION_STEP;
            final float speed = Math.max(0f, DEFAULT_WALK_SPEED - sub);
            if (player.getWalkSpeed() != speed) {
                player.setWalkSpeed(speed);
            }

            if (weight > DAMAGE_THRESHOLD) {
                player.damage(1);
            }
        });
    }

}
