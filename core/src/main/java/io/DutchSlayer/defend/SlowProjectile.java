// SlowProjectile.java
package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class SlowProjectile extends Projectile {
    private final float slowDuration;
    private final int damage;

    public SlowProjectile(Texture tex,
                          float startX, float startY,
                          float targetX, float targetY,
                          float slowDuration, float scale, int damage) {
        super(tex, startX, startY, targetX, targetY, scale);
        this.slowDuration = slowDuration;
        this.damage = damage;
    }

    // Constructor lama untuk compatibility
    public SlowProjectile(Texture tex,
                          float startX, float startY,
                          float targetX, float targetY,
                          float slowDuration, float scale) {
        this(tex, startX, startY, targetX, targetY, slowDuration, scale, 0);
    }

    @Override
    public void onHit(Array<Enemy> enemies) {
        // slow hanya 1 musuh pertama yang kena collision
        for (Enemy e : enemies) {
            if (getBounds().overlaps(e.getBounds())) {
                e.slow(slowDuration);
                if (damage > 0) {
                    e.takeDamage(damage);
                    System.out.println("Slow hit! Damage: " + damage + ", Slow: " + slowDuration + "s");
                }
                break;
            }
        }
    }
}
