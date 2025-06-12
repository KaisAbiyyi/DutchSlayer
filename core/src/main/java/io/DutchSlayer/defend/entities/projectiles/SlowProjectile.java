// SlowProjectile.java - OPTIMIZED VERSION
package io.DutchSlayer.defend.entities.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.enemies.Enemy;

/**
 * SlowProjectile adalah projectile yang memberikan efek slow pada enemy
 * Bisa juga memberikan damage (opsional) - OPTIMIZED VERSION
 */
public class SlowProjectile extends Projectile {
    private final float slowDuration;   // Durasi slow effect (detik)
    private final int slowDamage;       // Damage tambahan (opsional)

    /**
     * Constructor lengkap dengan damage - OPTIMIZED
     */
    public SlowProjectile(Texture tex,
                          float startX, float startY,
                          float targetX, float targetY,
                          float slowDuration, float scale, int damage, float customSpeed) {
        super(tex, startX, startY, targetX, targetY, scale, customSpeed, damage);
        this.slowDuration = slowDuration;
        this.slowDamage = damage;
    }

    /**
     * Constructor dengan speed (tanpa damage) - OPTIMIZED
     */
    public SlowProjectile(Texture tex,
                          float startX, float startY,
                          float targetX, float targetY,
                          float slowDuration, float scale,
                          float customSpeed) {
        this(tex, startX, startY, targetX, targetY, slowDuration, scale, 0, customSpeed);
    }

    /**
     * Constructor tanpa damage (backward compatibility) - OPTIMIZED
     */
    public SlowProjectile(Texture tex,
                          float startX, float startY,
                          float targetX, float targetY,
                          float slowDuration, float scale) {
        this(tex, startX, startY, targetX, targetY, slowDuration, scale, 0, 400f);
    }

    /**
     * Override method dari parent class - OPTIMIZED
     * Return type VOID untuk compatibility, dengan optimasi performa
     */
    @Override
    public void onHit(Array<Enemy> enemies) {
        if (!isActive()) return; // Early exit jika tidak aktif

        // OPTIMIZED: Loop dengan index untuk performa lebih baik
        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            if (e.isDestroyed()) continue;

            if (getBounds().overlaps(e.getBounds())) {
                // Berikan efek slow
                e.slow(slowDuration);

                // Berikan damage jika ada
                if (slowDamage > 0) {
                    e.takeDamage(slowDamage);
                }

                // Mark projectile sebagai tidak aktif untuk removal/pooling
                setActive(false);
                return; // Hit confirmed, exit early
            }
        }
    }

    /**
     * BARU: Reset method untuk object pooling support
     */
    public void reset(float startX, float startY, float targetX, float targetY,
                      float slowDuration, float customSpeed, int damage) {
        super.reset(startX, startY, targetX, targetY, customSpeed, damage);
        // slowDuration adalah final, jadi tidak bisa direset
        // Untuk full pooling, perlu refactor menjadi non-final
    }

    /**
     * BARU: Getters untuk debugging/UI
     */
    public float getSlowDuration() {
        return slowDuration;
    }

    public int getSlowDamage() {
        return slowDamage;
    }

    /**
     * BARU: Check apakah projectile ini pure slow (tanpa damage)
     */
    public boolean isPureSlowProjectile() {
        return slowDamage <= 0;
    }
}
