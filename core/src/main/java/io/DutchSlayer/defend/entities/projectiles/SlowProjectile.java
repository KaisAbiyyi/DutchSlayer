package io.DutchSlayer.defend.entities.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.enemies.Enemy;

/**
 * SlowProjectile adalah projectile yang memberikan efek slow pada enemy
 * Bisa juga memberikan damage (opsional) - OPTIMIZED VERSION
 */
public class SlowProjectile extends Projectile {
    private final float slowDuration;
    private final int slowDamage;

    /**
     * Constructor lengkap dengan damage - OPTIMIZED
     */
    public SlowProjectile(Texture tex,
                          float startX, float startY,
                          float targetX,
                          float slowDuration, float scale, int damage, float customSpeed) {
        super(tex, startX, startY, targetX, scale, customSpeed, damage);
        this.slowDuration = slowDuration;
        this.slowDamage = damage;
    }

    /**
     * Constructor dengan speed (tanpa damage) - OPTIMIZED
     */
    public SlowProjectile(Texture tex,
                          float startX, float startY,
                          float targetX,
                          float slowDuration, float scale,
                          float customSpeed) {
        this(tex, startX, startY, targetX, slowDuration, scale, 0, customSpeed);
    }

    /**
     * Override method dari parent class
     * Return type VOID untuk compatibility, dengan optimasi performa
     */
    @Override
    public void onHit(Array<Enemy> enemies) {
        if (!isActive()) return;

        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            if (e.isDestroyed()) continue;

            if (getBounds().overlaps(e.getBounds())) {
                e.slow(slowDuration);

                if (slowDamage > 0) {
                    e.takeDamage(slowDamage);
                }

                setActive(false);
                return;
            }
        }
    }

    /**
     * BARU: Reset method untuk object pooling support
     */
    public void reset(float startX, float startY, float targetX,
                      float customSpeed, int damage) {
        super.reset(startX, startY, targetX, customSpeed, damage);
    }

}
