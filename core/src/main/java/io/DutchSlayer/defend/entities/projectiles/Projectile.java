package io.DutchSlayer.defend.entities.projectiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.enemies.Enemy;

/**
 * Base class untuk semua projectile yang ditembakkan oleh tower
 * Menghandle movement, collision, dan basic damage - OPTIMIZED VERSION
 */
public class Projectile {
    /* ===== VISUAL COMPONENTS ===== */
    public final Texture tex;
    private final float scaledW;
    private final float scaledH;
    public final Rectangle bounds;

    /* ===== MOVEMENT - OPTIMIZED ===== */
    private final Vector2 pos;
    private final Vector2 vel;
    private final float halfWidth;      // Pre-calculated untuk performa
    private final float halfHeight;     // Pre-calculated untuk performa

    /* ===== COMBAT ===== */
    protected float speed = 400f;
    protected int damage = 1;

    /* ===== POOLING SUPPORT ===== */
    private boolean active = true;      // Untuk object pooling

    /* ===== REUSABLE OBJECTS - MENGURANGI GC ===== */
    private static final Vector2 TEMP_VEC = new Vector2(); // Shared temporary vector

    /**
     * Constructor master - semua parameter - OPTIMIZED
     */
    public Projectile(Texture tex, float startX, float startY, float targetX, float targetY, float scale, float customSpeed, int damage) {
        this.tex = tex;
        this.speed = customSpeed;
        this.damage = damage;
        this.pos = new Vector2(startX, startY);

        // Setup sprite scaling
        this.scaledW = tex.getWidth() * scale;
        this.scaledH = tex.getHeight() * scale;

        // Pre-calculate half dimensions untuk performa
        this.halfWidth = scaledW / 2f;
        this.halfHeight = scaledH / 2f;

        // OPTIMIZED: Langsung set velocity tanpa object temporary
        float direction = Math.signum(targetX - startX);
        this.vel = new Vector2(direction * speed, 0f);

        // Setup collision bounds (centered) - hanya sekali
        this.bounds = new Rectangle(
            pos.x - halfWidth,
            pos.y - halfHeight,
            scaledW,
            scaledH
        );
    }

    // Constructor overloads tetap sama...
    public Projectile(Texture tex, float startX, float startY, float targetX, float targetY, float scale, float customSpeed) {
        this(tex, startX, startY, targetX, targetY, scale, customSpeed, 1);
    }

    public Projectile(Texture tex, float startX, float startY, float targetX, float targetY, float scale, int damage) {
        this(tex, startX, startY, targetX, targetY, scale, 400f, damage);
    }

    public Projectile(Texture tex, float startX, float startY, float targetX, float targetY, float scale) {
        this(tex, startX, startY, targetX, targetY, scale, 400f, 1);
    }

    /**
     * Update projectile position - OPTIMIZED
     */
    public void update(float delta) {
        if (!active) return; // Early exit jika tidak aktif

        // Update posisi berdasarkan velocity
        pos.mulAdd(vel, delta);

        // OPTIMIZED: Update bounds position tanpa realokasi
        bounds.x = pos.x - halfWidth;
        bounds.y = pos.y - halfHeight;
    }

    /**
     * Render projectile menggunakan SpriteBatch - OPTIMIZED
     */
    public void drawBatch(SpriteBatch batch) {
        if (!active || tex == null) return; // Early exit

        batch.draw(
            tex,
            pos.x - halfWidth,  // Menggunakan pre-calculated values
            pos.y - halfHeight,
            scaledW,
            scaledH
        );
    }

    /**
     * Render projectile menggunakan ShapeRenderer - OPTIMIZED
     */
    public void drawShape(ShapeRenderer shapes) {
        if (!active) return; // Early exit

        if (tex == null) {
            shapes.setColor(Color.YELLOW);
            shapes.circle(pos.x, pos.y, halfWidth); // Menggunakan pre-calculated
        }
    }

    /**
     * Handle collision dengan enemies - OPTIMIZED
     * Menggunakan damage yang benar dan bisa di-override
     * RETURN VOID untuk backward compatibility dengan subclass
     */
    public void onHit(Array<Enemy> enemies) {
        if (!active) return;

        // OPTIMIZED: Loop dengan index untuk performa lebih baik
        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            if (e.isDestroyed()) continue;

            if (bounds.overlaps(e.getBounds())) {
                e.takeDamage(this.damage); // Menggunakan damage yang benar
                this.active = false; // Mark untuk removal/pooling
                return; // Hit confirmed, exit early
            }
        }
    }

    /**
     * BARU: Method terpisah untuk check hit dengan return boolean
     * Berguna untuk game logic yang perlu tahu apakah projectile hit target
     */
    public boolean checkHit(Array<Enemy> enemies) {
        if (!active) return false;

        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            if (e.isDestroyed()) continue;

            if (bounds.overlaps(e.getBounds())) {
                return true; // Hit detected
            }
        }
        return false; // No hit
    }

    /**
     * BARU: Check apakah projectile masih di dalam screen bounds
     * Untuk auto-cleanup projectiles yang keluar layar
     */
    public boolean isOutOfBounds(float screenWidth, float screenHeight) {
        return pos.x < -halfWidth || pos.x > screenWidth + halfWidth ||
            pos.y < -halfHeight || pos.y > screenHeight + halfHeight;
    }

    /**
     * BARU: Reset projectile untuk object pooling
     */
    public void reset(float startX, float startY, float targetX, float targetY, float customSpeed, int damage) {
        this.pos.set(startX, startY);
        this.speed = customSpeed;
        this.damage = damage;
        this.active = true;

        // Reset velocity
        float direction = Math.signum(targetX - startX);
        this.vel.set(direction * speed, 0f);

        // Reset bounds
        bounds.setPosition(pos.x - halfWidth, pos.y - halfHeight);
    }

    /* ===== GETTERS - OPTIMIZED ===== */
    public float getX() { return pos.x; }
    public float getY() { return pos.y; }
    public Rectangle getBounds() { return bounds; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // BARU: Getter untuk damage (berguna untuk UI/debugging)
    public int getDamage() { return damage; }
    public float getSpeed() { return speed; }
}
