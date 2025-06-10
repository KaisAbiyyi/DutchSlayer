// AoeProjectile.java
package io.DutchSlayer.defend.entities.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.enemies.Enemy;

/**
 * AoeProjectile adalah projectile yang memberikan damage area (AOE - Area of Effect)
 * Ketika mengenai target, semua enemy dalam radius tertentu akan terkena damage
 */
public class AoeProjectile extends Projectile {
    private int aoeRadius;                        // Radius area damage
    private int aoeDamage;                        // Damage per enemy dalam AOE

    // ===== PARABOLA TRAJECTORY SYSTEM =====
    private boolean useParabola = true;           // Enable parabola trajectory
    private float initialVelocityX;               // Kecepatan horizontal awal
    private float initialVelocityY;               // Kecepatan vertikal awal
    private float gravity = -800f;                // Gravitasi (negatif = ke bawah)
    private float timeElapsed = 0f;               // Waktu yang telah berlalu
    private Vector2 startPos;                     // Posisi awal peluru
    private Vector2 targetPos;                    // Posisi target
    private boolean hasExploded = false;          // Flag untuk mencegah multiple explosion

    // ===== VISUAL EFFECTS =====
    private float maxHeight;                      // Tinggi maksimal parabola
    private float totalTime;                      // Total waktu perjalanan

    private static final float FIXED_ARC_HEIGHT = 120f; // Tinggi parabola tetap (bisa diubah)

    /**
     * Constructor untuk AOE Projectile
     *
     * @param tex     Texture projectile
     * @param startX  Posisi awal X
     * @param startY  Posisi awal Y
     * @param targetX Target posisi X
     * @param targetY Target posisi Y
     * @param radius  Jangkauan area damage
     * @param scale   Skala sprite
     * @param damage  Damage per enemy
     */
    public AoeProjectile(Texture tex,
                         float startX, float startY,
                         float targetX, float targetY,
                         float radius, float scale, int damage, float customSpeed) {
        // Call parent constructor dengan parameter yang benar
        super(tex, startX, startY, targetX, targetY, scale, customSpeed, damage);

        this.aoeRadius = (int)radius;
        this.aoeDamage = damage;

        // ===== SETUP PARABOLA PHYSICS =====
        this.startPos = new Vector2(startX, startY);
        this.targetPos = new Vector2(targetX, targetY);

        // Tinggi parabola TETAP 150px, tidak tergantung jarak
        this.maxHeight = 150f; // KONSTANTA - selalu 150px ke atas

        // Hitung jarak horizontal
        float horizontalDistance = Math.abs(targetX - startX);

        // Waktu perjalanan berdasarkan jarak horizontal dan kecepatan base
        this.totalTime = horizontalDistance / (customSpeed * 0.8f);

        // Kecepatan horizontal konstan
        this.initialVelocityX = (targetX - startX) / totalTime;

        // ===== KALKULASI KECEPATAN VERTIKAL UNTUK TINGGI TETAP =====
        // Untuk mencapai tinggi maksimal 150px, lalu turun ke target
        float heightDiff = targetY - startY; // Perbedaan tinggi start vs target

        // Rumus: untuk mencapai peak height, lalu turun ke target
        // v0 = (heightDiff + maxHeight) / (totalTime/2) - g*(totalTime/2)
        // Tapi kita ingin peak di tengah perjalanan, jadi:
        this.initialVelocityY = (2f * maxHeight) / totalTime;

        // Adjust gravity agar projectile mencapai target Y dengan tepat
        // g = 2 * (v0*t - heightDiff) / t^2
        this.gravity = -2f * (initialVelocityY * totalTime - (heightDiff + maxHeight)) / (totalTime * totalTime);

        System.out.println("🎯 AOE Mortir fired! Distance: " + horizontalDistance + ", Total time: " + totalTime);
        System.out.println("   Fixed height: " + maxHeight + "px, Gravity: " + gravity);
        System.out.println("   From (" + startX + ", " + startY + ") to (" + targetX + ", " + targetY + ")");
    }

    /**
     * Backward compatibility constructor
     */
    public AoeProjectile(Texture tex,
                         float startX, float startY,
                         float targetX, float targetY,
                         float radius, float scale, int damage, float v, float customSpeed) {
        this(tex, startX, startY, targetX, targetY, radius, scale, damage, customSpeed);
    }

    /**
     * Override update untuk menggunakan fisika parabola
     */
    @Override
    public void update(float delta) {
        if (hasExploded) return;

        if (useParabola) {
            updateParabolaTrajectory(delta);
        } else {
            super.update(delta); // Fallback ke movement linear
        }

        // Check collision dengan target area
        checkTargetCollision();
    }

    /**
     * Update posisi menggunakan rumus fisika parabola
     */
    private void updateParabolaTrajectory(float delta) {
        timeElapsed += delta;

        // Rumus kinematika: s = s0 + v0*t + 0.5*a*t^2
        float newX = startPos.x + initialVelocityX * timeElapsed;
        float newY = startPos.y + initialVelocityY * timeElapsed + 0.5f * gravity * timeElapsed * timeElapsed;

        // Update posisi bounds (menggunakan method dari parent class)
        bounds.setPosition(newX - bounds.width/2, newY - bounds.height/2);

        // Debug info
        if (timeElapsed < 0.5f) { // Only print for first 0.5 seconds
            System.out.println("Parabola update: t=" + String.format("%.2f", timeElapsed) +
                ", pos=(" + String.format("%.1f", newX) + ", " + String.format("%.1f", newY) + ")");
        }
    }

    /**
     * Check collision dengan area target
     */
    private void checkTargetCollision() {
        float currentX = bounds.x + bounds.width/2;
        float currentY = bounds.y + bounds.height/2;

        float distanceToTarget = Vector2.dst(currentX, currentY, targetPos.x, targetPos.y);

        // Jika sudah dekat dengan target atau sudah melewati target (Y), explode
        if (distanceToTarget < 50f || currentY <= targetPos.y || timeElapsed > totalTime * 1.2f) {
            explode();
        }

        // Safety timeout - explode setelah 8 detik jika belum sampai
        if (timeElapsed > 8f) {
            System.out.println("⏰ AOE Projectile timeout - forced explosion");
            explode();
        }
    }

    /**
     * Trigger explosion - pindahkan projectile ke luar screen untuk removal
     */
    private void explode() {
        if (hasExploded) return;

        hasExploded = true;
        // Pindahkan ke posisi luar screen agar game loop remove
        bounds.setPosition(-1000f, -1000f);
        System.out.println("💥 AOE Mortir exploded at target! Time: " + String.format("%.2f", timeElapsed) + "s");
    }

    /**
     * METHOD BARU: Trigger AOE damage secara manual
     * Dipanggil oleh GameLogic saat projectile explode
     */
    public void triggerAOEDamage(Array<Enemy> enemies) {
        if (!hasExploded) return; // Hanya trigger damage setelah explode

        float explosionX = targetPos.x; // Gunakan target position sebagai pusat explosion
        float explosionY = targetPos.y;
        int hitCount = 0;

        System.out.println("🎯 Triggering AOE damage at (" + explosionX + ", " + explosionY + ") with radius " + aoeRadius);

        // Loop semua enemy untuk cek distance
        for (Enemy e : enemies) {
            if (e.isDestroyed()) continue;

            float enemyX = e.getBounds().x + e.getBounds().width/2;
            float enemyY = e.getBounds().y + e.getBounds().height/2;

            float distance = Vector2.dst(explosionX, explosionY, enemyX, enemyY);

            System.out.println("   Enemy at (" + String.format("%.1f", enemyX) + ", " + String.format("%.1f", enemyY) +
                "), Distance: " + String.format("%.1f", distance) + ", In range: " + (distance <= aoeRadius));

            if (distance <= aoeRadius) {
                e.takeDamage(aoeDamage);
                hitCount++;
                System.out.println("   ✅ Hit enemy! HP remaining: " + e.getHealth());
            }
        }

        System.out.println("💥 AOE explosion hit " + hitCount + " enemies total!");
    }

    /**
     * Override onHit untuk AOE damage
     */
    @Override
    public void onHit(Array<Enemy> enemies) {
        if (hasExploded) {
            triggerAOEDamage(enemies);
        }
    }

    /**
     * Override drawBatch untuk menggunakan posisi bounds (bukan posisi internal parent)
     */
    @Override
    public void drawBatch(com.badlogic.gdx.graphics.g2d.SpriteBatch batch) {
        if (tex != null && !hasExploded) {
            // Gunakan posisi dari bounds yang sudah diupdate oleh parabola
            float renderX = bounds.x;
            float renderY = bounds.y;

            batch.draw(
                tex,
                renderX,           // X position dari bounds
                renderY,           // Y position dari bounds
                bounds.width,      // Width
                bounds.height      // Height
            );
        }
    }

    /**
     * Override getX dan getY untuk konsistensi dengan posisi visual
     */
    @Override
    public float getX() {
        return bounds.x + bounds.width/2;
    }

    @Override
    public float getY() {
        return bounds.y + bounds.height/2;
    }

    /**
     * Check apakah projectile sudah exploded (untuk removal)
     */
    public boolean hasExploded() {
        return hasExploded;
    }

    /**
     * Get current height untuk visual effects (optional)
     */
    public float getCurrentHeight() {
        return bounds.y + bounds.height/2;
    }

    /**
     * Check apakah projectile masih dalam fase terbang
     */
    public boolean isFlying() {
        return !hasExploded && bounds.x > -500f && bounds.y > -500f;
    }

    /**
     * Get tinggi maksimal parabola saat ini
     */
    public float getMaxHeight() {
        return maxHeight;
    }

    /**
     * Get progress parabola (0.0 = start, 0.5 = peak, 1.0 = target)
     */
    public float getProgress() {
        return Math.min(1.0f, timeElapsed / totalTime);
    }

    /**
     * Method static untuk mengubah tinggi arc global
     * Berguna untuk testing atau game balancing
     */
    public static float getFixedArcHeight() {
        return FIXED_ARC_HEIGHT;
    }
}
