// AoeProjectile.java
package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class AoeProjectile extends Projectile {
    private final int damage;
    private final float radius;

    public AoeProjectile(Texture tex,
                         float startX, float startY,
                         float targetX, float targetY,
                         int damage, float radius, float scale) {
        super(tex, startX, startY, targetX, targetY, scale);
        this.damage = damage;
        this.radius = radius;
    }

    @Override
    public void onHit(Array<Enemy> enemies) {
        // iterasi semua musuh, damage bila dalam radius
        for (Enemy e : enemies) {
            if (e.
//                getBounds().overlaps(getX(), getX(), radius) ||
                getBounds().contains(e.getBounds().x, e.getBounds().y)) {
                e.takeDamage(damage);
            }
        }
    }
}
