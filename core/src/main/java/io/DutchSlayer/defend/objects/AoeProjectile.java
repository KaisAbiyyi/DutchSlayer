// AoeProjectile.java
package io.DutchSlayer.defend.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.enemy.Enemy;

/**
 * AoeProjectile adalah projectile yang memberikan damage area (AOE - Area of Effect)
 * Ketika mengenai target, semua enemy dalam radius tertentu akan terkena damage
 */
public class AoeProjectile extends Projectile {
    private final int damage;       // Damage yang diberikan per enemy
    private final float radius;     // Radius area damag

    /**
     * Constructor untuk AOE Projectile
     * @param tex Texture projectile
     * @param startX Posisi awal X
     * @param startY Posisi awal Y
     * @param targetX Target posisi X
     * @param targetY Target posisi Y
     * @param radius Jangkauan area damage
     * @param scale Skala sprite
     * @param damage Damage per enemy
     */
    public AoeProjectile(Texture tex,
                         float startX, float startY,
                         float targetX, float targetY,
                          float radius, float scale, int damage) {
        super(tex, startX, startY, targetX, targetY, scale);
        this.damage = damage;
        this.radius = radius;
    }

    /**
     * Override method dari parent class
     * Memberikan AOE damage pada semua enemy dalam radius
     */
    @Override
    public void onHit(Array<Enemy> enemies) {
        float projX = getX();   // Posisi projectile saat hit
        float projY = getY();
        int hitCount = 0;

        // Loop semua enemy untuk cek distance
        for (Enemy e : enemies) {
            // Hitung posisi center enemy
            float enemyX = e.getBounds().x + e.getBounds().width/2;
            float enemyY = e.getBounds().y + e.getBounds().height/2;

            // Hitung jarak menggunakan Pythagorean theorem
            float distance = (float) Math.sqrt(
                Math.pow(projX - enemyX, 2) + Math.pow(projY - enemyY, 2)
            );
            System.out.println("Checking enemy at (" + enemyX + ", " + enemyY + "), distance: " + distance);

            // Jika dalam radius, berikan damage
            if (distance <= radius) {
                e.takeDamage(damage);
                hitCount++;
                System.out.println(">>> AOE HIT! Enemy damaged for " + damage + " HP!");
            } else {
                System.out.println(">>> AOE MISS - too far");
            }
        }
        System.out.println("AOE explosion hit " + hitCount + " enemies!");
    }
}
