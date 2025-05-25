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
                          float radius, float scale, int damage) {
        super(tex, startX, startY, targetX, targetY, scale);
        this.damage = damage;
        this.radius = radius;
    }

    @Override
    public void onHit(Array<Enemy> enemies) {
        float projX = getX();
        float projY = getY();
        int hitCount = 0;

        // PERBAIKAN: Cek distance untuk AOE damage
        for (Enemy e : enemies) {
            float enemyX = e.getBounds().x + e.getBounds().width/2;
            float enemyY = e.getBounds().y + e.getBounds().height/2;

            float distance = (float) Math.sqrt(
                Math.pow(projX - enemyX, 2) + Math.pow(projY - enemyY, 2)
            );

            System.out.println("Checking enemy at (" + enemyX + ", " + enemyY + "), distance: " + distance);

            if (distance <= radius) {
                e.takeDamage(damage);
                hitCount++;
                System.out.println(">>> AOE HIT! Enemy damaged for " + damage + " HP!");
            } else {
                System.out.println(">>> AOE MISS - too far");
            }
        }
        System.out.println("AOE explosion hit " + hitCount + " enemies!");
        System.out.println("================================");
    }
}
