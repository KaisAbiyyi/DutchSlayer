// SlowProjectile.java
package io.DutchSlayer.defend.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.enemy.Enemy;

/**
 * SlowProjectile adalah projectile yang memberikan efek slow pada enemy
 * Bisa juga memberikan damage (opsional)
 */
public class SlowProjectile extends Projectile {
    private final float slowDuration;   // Durasi slow effect (detik)
    private final int damage;           // Damage tambahan (opsional)

    /**
     * Constructor lengkap dengan damage
     */
    public SlowProjectile(Texture tex,
                          float startX, float startY,
                          float targetX, float targetY,
                          float slowDuration, float scale, int damage, float customSpeed) {
        super(tex, startX, startY, targetX, targetY, scale, customSpeed, damage);
        this.slowDuration = slowDuration;
        this.damage = damage;
    }

    // Constructor dengan speed (tanpa damage)
    public SlowProjectile(Texture tex,
                          float startX, float startY,
                          float targetX, float targetY,
                          float slowDuration, float scale,
                          float customSpeed) {  // ← PARAMETER BARU
        this(tex, startX, startY, targetX, targetY, slowDuration, scale, 0, customSpeed);
    }

    /**
     * Constructor tanpa damage (backward compatibility)
     * Untuk tower SLOW yang hanya memberikan efek slow tanpa damage
     */
    public SlowProjectile(Texture tex,
                          float startX, float startY,
                          float targetX, float targetY,
                          float slowDuration, float scale) {
        this(tex, startX, startY, targetX, targetY, slowDuration, scale, 0);
    }

    /**
     * Override method dari parent class
     * Memberikan efek slow pada enemy yang terkena collision
     */
    @Override
    public void onHit(Array<Enemy> enemies) {
        // Hanya affect 1 enemy pertama yang collision (single target)
        for (Enemy e : enemies) {
            if (getBounds().overlaps(e.getBounds())) {
                // Berikan efek slow
                e.slow(slowDuration);

                // Berikan damage jika ada
                if (damage > 0) {
                    e.takeDamage(damage);
                    System.out.println("Slow hit! Damage: " + damage + ", Slow: " + slowDuration + "s");
                }
                break;  // Hanya affect 1 enemy
            }
        }
    }
}
