// SlowProjectile.java
package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class SlowProjectile extends Projectile {
    private final float slowDuration;

    public SlowProjectile(Texture tex,
                          float startX, float startY,
                          float targetX, float targetY,
                          float slowDuration, float scale) {
        super(tex, startX, startY, targetX, targetY, scale);
        this.slowDuration = slowDuration;
    }

    @Override
    public void onHit(Array<Enemy> enemies) {
        // slow hanya 1 musuh pertama yang kena collision
        for (Enemy e : enemies) {
            if (getBounds().overlaps(e.getBounds())) {
                e.slow(slowDuration);
                break;
            }
        }
    }
}
