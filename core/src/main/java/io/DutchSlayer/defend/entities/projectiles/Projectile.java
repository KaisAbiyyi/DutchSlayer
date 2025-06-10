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
 * Menghandle movement, collision, dan basic damage
 */
public class Projectile {
    /* ===== VISUAL COMPONENTS ===== */
    public final Texture tex;          // Texture projectile
    private final float scaledW;        // Lebar sprite setelah scaling
    private final float scaledH;        // Tinggi sprite setelah scaling
    public final Rectangle bounds;     // Collision bounds

    /* ===== MOVEMENT ===== */
    private final Vector2 pos;          // Posisi saat ini
    private final Vector2 vel;          // Velocity (speed + direction)

    /* ===== COMBAT ===== */
    protected float speed = 400f;       // Kecepatan projectile
    protected int damage = 1;           // Damage yang diberikan

    /* ===== DEFAULT VALUES ===== */
    private static final float DEFAULT_SPEED = 400f;
    private static final int DEFAULT_DAMAGE = 1;

    /**
     * Constructor master - semua parameter
     */
    public Projectile(Texture tex, float startX, float startY, float targetX, float targetY, float scale, float customSpeed, int damage) {
        this.tex = tex;
        this.speed = customSpeed;
        this.damage = damage;
        this.pos = new Vector2(startX, startY);

        // Setup sprite scaling
        this.scaledW = tex.getWidth() * scale;
        this.scaledH = tex.getHeight() * scale;

        // Setup movement direction (horizontal only)
        float direction = Math.signum(targetX - startX); // +1 ke kanan, -1 ke kiri
        Vector2 dir = new Vector2(direction, 0f); // Zero di sumbu Y (horizontal movement)
        this.vel = dir.scl(speed);

        // Setup collision bounds (centered)
        this.bounds = new Rectangle(
            pos.x - scaledW/2,
            pos.y - scaledH/2,
            scaledW,
            scaledH
        );
    }

    /**
     * Constructor dengan custom speed (damage default)
     */
    public Projectile(Texture tex, float startX, float startY, float targetX, float targetY, float scale, float customSpeed) {
        this(tex, startX, startY, targetX, targetY, scale, customSpeed, 1);
    }

    /**
     * Constructor dengan custom damage (speed default)
     */
    public Projectile(Texture tex, float startX, float startY, float targetX, float targetY, float scale, int damage) {
        this(tex, startX, startY, targetX, targetY, scale, 400f, damage);
    }

    /**
     * Constructor default (speed dan damage default)
     */
    public Projectile(Texture tex, float startX, float startY, float targetX, float targetY, float scale) {
        this(tex, startX, startY, targetX, targetY, scale, 400f, 1);
    }

    /**
     * Update projectile position setiap frame
     */
    public void update(float delta) {
        // Update posisi berdasarkan velocity
        pos.mulAdd(vel, delta);

        // Update collision bounds agar collision detection tepat
        bounds.setPosition(pos.x - scaledW/2, pos.y - scaledH/2);
    }

    /**
     * Render projectile menggunakan SpriteBatch
     */
    public void drawBatch(SpriteBatch batch) {
        if (tex != null) {
            batch.draw(
                tex,
                pos.x - scaledW/2,  // X position (centered)
                pos.y - scaledH/2,  // Y position (centered)
                scaledW,            // Width
                scaledH              // Height
            );
        }
    }

    /**
     * Render projectile menggunakan ShapeRenderer (fallback jika texture null)
     */
    public void drawShape(ShapeRenderer shapes) {
        if (tex == null) {
            shapes.setColor(Color.YELLOW);
            shapes.circle(pos.x, pos.y, scaledW/2);
        }
    }

    /**
     * Handle collision dengan enemies
     * Method ini bisa di-override oleh subclass untuk behavior khusus
     */
    public void onHit(Array<Enemy> enemies) {
        // Default behavior: damage 1 enemy pertama yang collision
        for (Enemy e : enemies) {
            if (e.isDestroyed()) continue; // Skip enemy yang sudah mati

            if (getBounds().overlaps(e.getBounds())) {
                e.takeDamage(1);
                break;  // Hanya affect 1 enemy
            }
        }
    }

    /* ===== GETTERS ===== */
    public float getX() {
        return pos.x;
    }
    public float getY() {return pos.y;}
    public Rectangle getBounds() {
        return bounds;
    }
}
