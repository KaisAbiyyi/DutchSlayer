package io.DutchSlayer.defend.entities.traps;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import io.DutchSlayer.defend.ui.ImageLoader;

public class Trap {
    /* ===== GEOMETRY & COLLISION ===== */
    public final Rectangle bounds;          // Area collision trap
    public boolean occupied = false;        // Apakah trap sudah dipasang
    private final float centerX, centerY;   // Center point untuk calculations
    private final float[] verts;                  // Vertex array untuk polygon shape

    private final float w, h;               // Ukuran sprite setelah scaling
    private Texture tex;                    // Texture trap berdasarkan type

    // ===== TRAP TYPE SYSTEM =====
    private final TrapType type;                  // Jenis trap (ATTACK/SLOW/EXPLOSION)

    /* ===== TRAP MECHANICS ===== */
    private float cooldown = 0f;            // Cooldown setelah aktivasi

    private static final boolean SINGLE_USE = true;     // Trap hilang setelah sekali pakai

    // ===== TRAP STATS PER TYPE =====
    // Attack Trap: Damage + Light Slow

    // Slow Trap: Heavy Slow (almost freeze)
    // 90% speed reduction


    /**
     * Constructor untuk membuat trap dengan type tertentu
     * @param verts Vertex array untuk hit-zone polygon
     * @param scale Scale factor untuk sprite
     * @param type Jenis trap (ATTACK/SLOW/EXPLOSION)
     */
    public Trap(float[] verts, float scale, TrapType type) {
        this.verts = verts.clone();     // Copy array untuk safety
        this.type = type;
        /* ===== VISUAL COMPONENTS ===== */
        // Scale factor untuk sprite

        // ===== CALCULATE COLLISION BOUNDS FROM VERTICES =====
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;

        // Loop vertices untuk find bounding box
        for (int i = 0; i < verts.length; i += 2) {
            minX = Math.min(minX, verts[i]);        // X coordinates
            maxX = Math.max(maxX, verts[i]);
            minY = Math.min(minY, verts[i+1]);      // Y coordinates
            maxY = Math.max(maxY, verts[i+1]);
        }

        // Create collision bounds (expanded untuk easier collision)
        this.bounds = new Rectangle(minX, minY, maxX-minX, maxY-minY);

        // Calculate center point untuk distance calculations
        this.centerX = minX + (maxX-minX)/2f;
        this.centerY = minY + (maxY-minY)/2f;

        // ===== SET TEXTURE BASED ON TRAP TYPE =====
        switch(type) {
            case ATTACK:
                this.tex = ImageLoader.trapAttackTex;
                break;
            case SLOW:
                this.tex = ImageLoader.trapSlowTex;
                break;
            case EXPLOSION:
                this.tex = ImageLoader.trapBombTex;
                break;
            default:
                this.tex = ImageLoader.trapTex; // Fallback
                break;
        }

        // Ultimate fallback jika texture null
        if (tex == null) {
            tex = ImageLoader.trapTex;
        }

        // Calculate sprite dimensions
        this.w       = tex.getWidth()  * scale;
        this.h       = tex.getHeight() * scale;
    }

    /**
     * Old constructor untuk backward compatibility (default ke ATTACK type)
     */
    public Trap(float[] verts, float scale) {
        this(verts, scale, TrapType.ATTACK);
    }

    /**
     * Update trap logic setiap frame (handle cooldown)
     */
    public void update(float delta) {
        if (cooldown > 0) {
            cooldown -= delta;
        }
    }

    /**
     * Render trap dengan visual effects
     */
    public void drawBatch(SpriteBatch batch) {
        // Hanya draw jika occupied dan belum used (untuk single-use)
        // Sudah digunakan (single-use system)
        boolean isUsed = false;
        if (occupied && !(SINGLE_USE && isUsed)) {
            // ===== VISUAL EFFECT BERDASARKAN STATE =====
            if (isOnCooldown()) {
                // Trap on cooldown - warna agak redup
                batch.setColor(0.5f, 0.5f, 0.5f, 0.8f);
            } else {
                // Trap ready - warna normal
                batch.setColor(1f, 1f, 1f, 1f);
            }

            // Calculate sprite position
            float spriteX = centerX - w/2f;
            float spriteY = getTowerAlignedY();  // Special alignment method

            batch.draw(tex, spriteX, spriteY, w, h);

            // Reset color untuk sprites berikutnya
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    /**
     * Calculate Y position yang aligned dengan tower deployment
     * Formula sama dengan tower deployment di GameScreen
     */
    private float getTowerAlignedY() {
        // verts format: [x0,y0, x1,y1, x2,y2, x3,y3]
        // Ambil semua Y coordinates: verts[1], verts[3], verts[5], verts[7]
        float y0 = verts[1];  // Y coordinate vertex 0
        float y1 = verts[3];  // Y coordinate vertex 1
        float y2 = verts[5];  // Y coordinate vertex 2
        float y3 = verts[7];  // Y coordinate vertex 3

        // Formula PERSIS SAMA dengan tower deployment

        return (y0 + y1 + y2 + y3) / 4.4f;
    }

    /* ===== GETTERS ===== */
    public boolean isOnCooldown() {
        return cooldown > 0;
    }
    public boolean isUsed() {
        return false;
    }
    public TrapType getType() {
        return type;
    }
    public float getCenterX() {
        return centerX;
    }
    public float getCenterY() {
        return centerY;
    }
}
