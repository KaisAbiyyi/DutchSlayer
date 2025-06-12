package io.DutchSlayer.defend.entities.traps;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.enemies.Enemy;
import io.DutchSlayer.defend.ui.ImageLoader;
import io.DutchSlayer.defend.utils.AudioManager;

public class Trap {
    /* ===== GEOMETRY & COLLISION ===== */
    public final Rectangle bounds;          // Area collision trap
    public boolean occupied = false;        // Apakah trap sudah dipasang
    private final float centerX, centerY;   // Center point untuk calculations
    private float[] verts;                  // Vertex array untuk polygon shape

    /* ===== VISUAL COMPONENTS ===== */
    private final float scale;              // Scale factor untuk sprite
    private final float w, h;               // Ukuran sprite setelah scaling
    private Texture tex;                    // Texture trap berdasarkan type

    // ===== TRAP TYPE SYSTEM =====
    private TrapType type;                  // Jenis trap (ATTACK/SLOW/EXPLOSION)

    /* ===== TRAP MECHANICS ===== */
    private float cooldown = 0f;            // Cooldown setelah aktivasi
    private boolean isUsed = false;         // Sudah digunakan (single-use system)

    /* ===== CONSTANTS ===== */
    private static final float TRAP_COOLDOWN = 0.5f;    // Cooldown duration
    private static final boolean SINGLE_USE = true;     // Trap hilang setelah sekali pakai
    private static final float COLLISION_RADIUS = 80f;  // Generous collision radius

    // ===== TRAP STATS PER TYPE =====
    // Attack Trap: Damage + Light Slow
    private static final int ATTACK_DAMAGE = 1;
    private static final float ATTACK_SLOW_DURATION = 2f;

    // Slow Trap: Heavy Slow (almost freeze)
    private static final float SLOW_DURATION = 5f;
    private static final float SLOW_STRENGTH = 0.1f;    // 90% speed reduction

    // Explosion Trap: AOE Damage
    private static final int EXPLOSION_DAMAGE = 2;
    private static final float EXPLOSION_RADIUS = 80f;


    /**
     * Constructor untuk membuat trap dengan type tertentu
     * @param verts Vertex array untuk hit-zone polygon
     * @param scale Scale factor untuk sprite
     * @param type Jenis trap (ATTACK/SLOW/EXPLOSION)
     */
    public Trap(float[] verts, float scale, TrapType type) {
        this.verts = verts.clone();     // Copy array untuk safety
        this.type = type;
        this.scale = scale;

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
     * Check apakah point x,y ada dalam trap bounds
     */
    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
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
     * Trigger trap ketika enemy masuk area
     * Returns true jika trap berhasil diaktivasi
     */
    public boolean triggerTrap(Array<Enemy> enemies) {
        // Skip jika trap tidak occupied, masih cooldown, atau sudah digunakan
        if (!occupied || cooldown > 0 || (SINGLE_USE && isUsed)) {
            return false;
        }

        boolean trapActivated = false;

        // ===== CHECK COLLISION DENGAN SEMUA ENEMIES =====
        for (Enemy enemy : enemies) {
            if (enemy.isDestroyed()) continue; // Skip dead enemies

            // Get enemy center point
            float enemyX = enemy.getBounds().x + enemy.getBounds().width/2;
            float enemyY = enemy.getBounds().y + enemy.getBounds().height/2;

            // ===== DISTANCE-BASED COLLISION DETECTION =====
            float distance = (float) Math.sqrt(
                Math.pow(centerX - enemyX, 2) + Math.pow(centerY - enemyY, 2)
            );

            // Check collision dengan generous radius
            if (distance < COLLISION_RADIUS) {
                activateTrapEffect(enemy, enemies);
                trapActivated = true;
                break;  // Hanya trigger untuk 1 enemy per frame
            }
        }

        // ===== HANDLE POST-ACTIVATION =====
        if (trapActivated) {
            cooldown = TRAP_COOLDOWN;   // Set cooldown

            if (SINGLE_USE) {
                isUsed = true;          // Mark sebagai used
                occupied = false;       // Hilangkan trap dari game
            }
        }

        return trapActivated;
    }

    /**
     * Apply trap effect berdasarkan type
     */
    private void activateTrapEffect(Enemy triggerEnemy, Array<Enemy> allEnemies) {
        switch(type) {
            case ATTACK:
                AudioManager.playTrapAttackHit();
                triggerEnemy.takeDamage(ATTACK_DAMAGE);
                triggerEnemy.slow(ATTACK_SLOW_DURATION);
                break;

            case SLOW:
                AudioManager.playTrapSlowHit();
                triggerEnemy.slowHeavy(SLOW_DURATION, SLOW_STRENGTH); // Heavy slow method
                break;

            case EXPLOSION:
                explodeAOE(allEnemies);
                AudioManager.playTrapExplosionHit();
                break;
        }
    }

    /**
     * Handle AOE explosion damage
     */
    private void explodeAOE(Array<Enemy> enemies) {
        int hitCount = 0;
        float explodeX = centerX;
        float explodeY = centerY;

        for (Enemy e : enemies) {
            if (e.isDestroyed()) continue;

            // Get enemy center
            float enemyX = e.getBounds().x + e.getBounds().width/2;
            float enemyY = e.getBounds().y + e.getBounds().height/2;

            // Calculate distance
            float distance = (float) Math.sqrt(
                Math.pow(explodeX - enemyX, 2) + Math.pow(explodeY - enemyY, 2)
            );

            // Apply AOE damage jika dalam radius
            if (distance <= EXPLOSION_RADIUS) {
                int oldHp = e.getHealth();
                e.takeDamage(EXPLOSION_DAMAGE);
                int newHp = e.getHealth();
                hitCount++;
            } else {
            }
        }

    }

    /**
     * Render trap dengan visual effects
     */
    public void drawBatch(SpriteBatch batch) {
        // Hanya draw jika occupied dan belum used (untuk single-use)
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
        float alignedY = (y0 + y1 + y2 + y3) / 4.4f;

        return alignedY;
    }

    /* ===== GETTERS ===== */
    public boolean isOnCooldown() {
        return cooldown > 0;
    }
    public boolean isUsed() {
        return SINGLE_USE && isUsed;
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
