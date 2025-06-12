// ===== 3) BUAT ENEMY PROJECTILE CLASS =====
// File: EnemyProjectile.java
package io.DutchSlayer.defend.entities.projectiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * EnemyProjectile adalah projectile yang ditembakkan oleh enemy (shooter/boss)
 * untuk menyerang tower. Bergerak horizontal ke kiri (menuju tower)
 */
public class EnemyProjectile {
    /* ===== VISUAL COMPONENTS ===== */
    private final Texture tex;          // Texture sprite
    private final float scaledW;        // Lebar sprite setelah scaling
    private final float scaledH;        // Tinggi sprite setelah scaling
    private final Rectangle bounds;     // Collision box

    /* ===== MOVEMENT ===== */
    private final Vector2 pos;          // Posisi saat ini
    private final Vector2 vel;          // Velocity (kecepatan + arah)

    /* ===== COMBAT ===== */
    private final int damage;           // Damage yang diberikan ke tower

    /* ===== CONFIGURATION CONSTANTS ===== */
    // Speed configurations per enemy type
    private static final float SHOOTER_SPEED = 1200f;    // Medium speed
    private static final float BOSS_SPEED = 700f;       // Slower but powerful

    // Scale configurations per enemy type
    private static final float SHOOTER_SCALE = 0.01f;   // Small projectile
    private static final float BOSS_SCALE = 0.07f;      // Large projectile

    /**
     * Constructor utama dengan custom speed
     */
    public EnemyProjectile(Texture tex, float startX, float startY, int damage, float speed, float scale) {
        this.tex = tex;
        this.damage = damage;
        this.pos = new Vector2(startX, startY);

        // Setup sprite scaling
        this.scaledW = tex.getWidth() * scale;
        this.scaledH = tex.getHeight() * scale;

        // Set velocity: bergerak horizontal ke kiri (menuju tower)
        this.vel = new Vector2(-speed, 0f);  // Hanya bergerak horizontal ke kiri

        // Setup collision bounds
        this.bounds = new Rectangle(
            pos.x - scaledW/2,
            pos.y - scaledH/2,
            scaledW,
            scaledH
        );
    }

    /**
     * Constructor dengan default speed (untuk backward compatibility)
     */
    public EnemyProjectile(Texture tex, float startX, float startY, int damage, float speed) {
        this(tex, startX, startY, damage, speed, SHOOTER_SCALE); // Default scale
    }

    /**
     * Constructor lama (untuk backward compatibility)
     * Parameter targetX dan targetY diabaikan
     */
    public EnemyProjectile(Texture tex, float startX, float startY, float targetX, float targetY, int damage) {
        this(tex, startX, startY, damage, SHOOTER_SPEED, SHOOTER_SCALE);
    }

    /* ===== FACTORY METHODS ===== */

    /**
     * Buat projectile untuk SHOOTER enemy
     */
    public static EnemyProjectile createShooterProjectile(Texture tex, float startX, float startY, int damage) {
        return new EnemyProjectile(tex, startX, startY, damage, SHOOTER_SPEED, SHOOTER_SCALE);
    }

    /**
     * Buat projectile untuk BOSS enemy
     */
    public static EnemyProjectile createBossProjectile(Texture tex, float startX, float startY, int damage) {
        return new EnemyProjectile(tex, startX, startY, damage, BOSS_SPEED, BOSS_SCALE);
    }

    /**
     * Buat projectile custom
     */
    public static EnemyProjectile createCustomProjectile(Texture tex, float startX, float startY,
                                                         int damage, float speed, float scale) {
        return new EnemyProjectile(tex, startX, startY, damage, speed, scale);
    }

    /**
     * Update posisi projectile setiap frame
     */
    public void update(float delta) {
        // Update posisi berdasarkan velocity
        pos.mulAdd(vel, delta);

        // Update collision bounds
        bounds.setPosition(pos.x - scaledW/2, pos.y - scaledH/2);
    }

    /**
     * Render projectile menggunakan SpriteBatch
     */
    public void drawBatch(SpriteBatch batch) {
        if (tex != null) {
            batch.draw(tex, pos.x - scaledW/2, pos.y - scaledH/2, scaledW, scaledH);
        }
    }

    /**
     * Render projectile menggunakan ShapeRenderer (fallback jika texture null)
     */
    public void drawShape(ShapeRenderer shapes) {
        if (tex == null) {
            shapes.setColor(Color.RED);
            shapes.circle(pos.x, pos.y, scaledW/2);
        }
    }

    /* ===== GETTERS ===== */
    public float getX() { return pos.x; }
    public Rectangle getBounds() { return bounds; }
    public int getDamage() { return damage; }
}
