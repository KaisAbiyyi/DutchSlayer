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
    private final Texture tex;
    private final float scaledW;
    private final float scaledH;
    private final Rectangle bounds;

    private final Vector2 pos;
    private final Vector2 vel;

    private final int damage;

    private static final float SHOOTER_SPEED = 1200f;
    private static final float BOSS_SPEED = 700f;

    // Scale configurations per enemy type
    private static final float SHOOTER_SCALE = 0.01f;
    private static final float BOSS_SCALE = 0.07f;

    /**
     * Constructor utama dengan custom speed
     */
    public EnemyProjectile(Texture tex, float startX, float startY, int damage, float speed, float scale) {
        this.tex = tex;
        this.damage = damage;
        this.pos = new Vector2(startX, startY);

        this.scaledW = tex.getWidth() * scale;
        this.scaledH = tex.getHeight() * scale;

        this.vel = new Vector2(-speed, 0f);

        this.bounds = new Rectangle(
            pos.x - scaledW/2,
            pos.y - scaledH/2,
            scaledW,
            scaledH
        );
    }


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
     * Update posisi projectile setiap frame
     */
    public void update(float delta) {
        pos.mulAdd(vel, delta);

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

    public float getX() { return pos.x; }
    public Rectangle getBounds() { return bounds; }
    public int getDamage() { return damage; }
}
