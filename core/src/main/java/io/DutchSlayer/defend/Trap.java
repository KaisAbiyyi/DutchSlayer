package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Trap {
    public final Rectangle bounds;
    public boolean occupied = false;
    private final float centerX, centerY;
    private final float scale;
    private final float w, h;
    private float[] verts;

    // ===== NEW: TRAP TYPE SYSTEM =====
    private TrapType type;
    private Texture tex;

    // Trap effect properties
    private float cooldown = 0f;
    private static final float TRAP_COOLDOWN = 0.5f;
    private boolean isUsed = false;
    private static final boolean SINGLE_USE = true;

    // ===== TRAP STATS PER TYPE =====
    // Attack Trap: Damage + Light Slow
    private static final int ATTACK_DAMAGE = 1;
    private static final float ATTACK_SLOW_DURATION = 2f;

    // Slow Trap: Heavy Slow (almost freeze)
    private static final float SLOW_DURATION = 5f;
    private static final float SLOW_STRENGTH = 0.1f; // 90% speed reduction

    // Explosion Trap: AOE Damage
    private static final int EXPLOSION_DAMAGE = 2;
    private static final float EXPLOSION_RADIUS = 80f;


    // terima verts only untuk hit‐zone, plus scale untuk gambar
    public Trap(float[] verts, float scale, TrapType type) {
        this.verts = verts.clone();
        this.type = type;
        this.scale = scale;

        // hit‐zone calculation
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;

        for (int i = 0; i < verts.length; i += 2) {
            minX = Math.min(minX, verts[i]);
            maxX = Math.max(maxX, verts[i]);
            minY = Math.min(minY, verts[i+1]);
            maxY = Math.max(maxY, verts[i+1]);
        }
        // ===== EXPANDED: Make collision area larger than visual =====
        bounds = new Rectangle(minX, minY, maxX-minX, maxY-minY);

        // simpan center untuk draw
        this.centerX = minX + (maxX-minX)/2f;
        this.centerY = minY + (maxY-minY)/2f;

        // ===== SET TEXTURE BASED ON TYPE =====
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

        // Fallback jika texture null
        if (tex == null) {
            tex = ImageLoader.trapTex; // Ultimate fallback
        }

        this.w       = tex.getWidth()  * scale;
        this.h       = tex.getHeight() * scale;

        System.out.println("=== TRAP CREATED ===");
        System.out.println("=== TRAP CREATED ===");
        System.out.println("Type: " + type);
        System.out.println("Position: (" + centerX + ", " + centerY + ")");
        System.out.println("Bounds: " + bounds);
    }

    // Old constructor for compatibility
    public Trap(float[] verts, float scale) {
        this(verts, scale, TrapType.ATTACK); // Default to ATTACK type
    }

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    // Method baru: Update trap (handle cooldown)
    public void update(float delta) {
        if (cooldown > 0) {
            cooldown -= delta;
        }
    }

    // Method baru: Aktivasi trap ketika enemy masuk
    public boolean triggerTrap(Array<Enemy> enemies) {
        if (!occupied || cooldown > 0 || (SINGLE_USE && isUsed)) {
            return false;
        }

        boolean trapActivated = false;

        // ===== IMPROVED: Better collision detection =====
        for (Enemy enemy : enemies) {
            if (enemy.isDestroyed()) continue; // Skip dead enemies

            // Check if enemy center point is inside trap bounds
            float enemyX = enemy.getBounds().x + enemy.getBounds().width/2;
            float enemyY = enemy.getBounds().y + enemy.getBounds().height/2;

            // ===== SIMPLIFIED: Distance-based collision =====
            float distance = (float) Math.sqrt(
                Math.pow(centerX - enemyX, 2) + Math.pow(centerY - enemyY, 2)
            );

            // ===== VERY GENEROUS: 80px collision radius =====
            boolean collision = distance < 80f;

            // ===== DEBUG: Always log when enemy is nearby =====
            if (distance < 120f) {
                System.out.println("🔍 DISTANCE COLLISION CHECK:");
                System.out.println("  Enemy center: (" + enemyX + ", " + enemyY + ")");
                System.out.println("  Trap center: (" + centerX + ", " + centerY + ")");
                System.out.println("  Distance: " + distance + " (threshold: 80px)");
                System.out.println("  Collision: " + collision);
                System.out.println("  Bounds collision: " + bounds.contains(enemyX, enemyY));
            }

            if (collision) {
                System.out.println("🎯 " + type + " TRAP TRIGGERED BY DISTANCE!");
                activateTrapEffect(enemy, enemies);
                trapActivated = true;
                break;
            }
        }

        if (trapActivated) {
            cooldown = TRAP_COOLDOWN;  // Set cooldown setelah aktivasi

            if (SINGLE_USE) {
                isUsed = true;  // Mark sebagai sudah digunakan
                occupied = false;  // ← PENTING: Set occupied = false agar trap "hilang"
                System.out.println("Trap consumed (single-use)!");
            }
        }

        return trapActivated;
    }

    private void activateTrapEffect(Enemy triggerEnemy, Array<Enemy> allEnemies) {
        switch(type) {
            case ATTACK:
                System.out.println("=== ATTACK TRAP TRIGGERED ===");
                triggerEnemy.takeDamage(ATTACK_DAMAGE);
                triggerEnemy.slow(ATTACK_SLOW_DURATION);
                System.out.println("Enemy took " + ATTACK_DAMAGE + " damage and slowed for " + ATTACK_SLOW_DURATION + "s");
                break;

            case SLOW:
                System.out.println("=== SLOW TRAP TRIGGERED ===");
                triggerEnemy.slowHeavy(SLOW_DURATION, SLOW_STRENGTH); // Heavy slow method
                System.out.println("Enemy heavily slowed for " + SLOW_DURATION + "s (90% speed reduction)");
                break;

            case EXPLOSION:
                System.out.println("=== EXPLOSION TRAP TRIGGERED ===");
                explodeAOE(allEnemies);
                break;
        }
    }

    private void explodeAOE(Array<Enemy> enemies) {
        int hitCount = 0;
        float explodeX = centerX;
        float explodeY = centerY;

        System.out.println("EXPLOSION at (" + explodeX + ", " + explodeY + ") with radius " + EXPLOSION_RADIUS);
        System.out.println("Total enemies in game: " + enemies.size);


        for (Enemy e : enemies) {
            if (e.isDestroyed()) continue;

            float enemyX = e.getBounds().x + e.getBounds().width/2;
            float enemyY = e.getBounds().y + e.getBounds().height/2;

            float distance = (float) Math.sqrt(
                Math.pow(explodeX - enemyX, 2) + Math.pow(explodeY - enemyY, 2)
            );

            System.out.println("🎯 Checking enemy at (" + enemyX + ", " + enemyY + ") - Distance: " + distance);

            if (distance <= EXPLOSION_RADIUS) {
                int oldHp = e.getHealth();
                e.takeDamage(EXPLOSION_DAMAGE);
                int newHp = e.getHealth();
                hitCount++;
                System.out.println("💥 EXPLOSION HIT! HP: " + oldHp + " → " + newHp + " (damage: " + EXPLOSION_DAMAGE + ")");
            } else {
                System.out.println("❌ Enemy too far (distance: " + distance + " > radius: " + EXPLOSION_RADIUS + ")");
            }
        }

        System.out.println("Explosion hit " + hitCount + " enemies total!");
    }

    // Getter untuk cooldown (untuk visual effects)
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

    public void drawBatch(SpriteBatch batch) {
        if (occupied && !(SINGLE_USE && isUsed)) {
            // BONUS: Visual effect berdasarkan cooldown
            if (isOnCooldown()) {
                // Trap on cooldown - warna agak redup
                batch.setColor(0.5f, 0.5f, 0.5f, 0.8f);
            } else {
                // Trap ready - warna normal
                batch.setColor(1f, 1f, 1f, 1f);
            }

            // PERBAIKAN: Posisi X dan Y menggunakan formula tower
            float spriteX = centerX - w/2f;
            float spriteY = getTowerAlignedY();  // ← Method khusus untuk alignment

            batch.draw(tex, spriteX, spriteY, w, h);

            // Reset color
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private float getTowerAlignedY() {
        // Formula yang PERSIS SAMA dengan tower deployment di GameScreen:
        // float cy = (z.verts[1] + z.verts[3] + z.verts[5] + z.verts[7]) / 2.3f;

        // verts format: [x0,y0, x1,y1, x2,y2, x3,y3]
        // Indices:       [0,1,  2,3,   4,5,   6,7]
        // Kita ambil semua Y coordinates: verts[1], verts[3], verts[5], verts[7]

        float y0 = verts[1];  // Y coordinate vertex 0
        float y1 = verts[3];  // Y coordinate vertex 1
        float y2 = verts[5];  // Y coordinate vertex 2
        float y3 = verts[7];  // Y coordinate vertex 3

        // Formula PERSIS SAMA dengan tower deployment
        float alignedY = (y0 + y1 + y2 + y3) / 4.4f;

        return alignedY;
    }
}
