package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public static final float SCALE = 0.2f;           // bisa diakses luar
    private final Texture tex;
    private final Vector2 pos;
    private final Rectangle bounds;
    private final float scaledWidth;
    private final float scaledHeight;

    private final float baseSpeed = 100f;
    private float currentSpeed = 100f;

    // ===== TAMBAHAN UNTUK HEAVY SLOW =====
    private boolean isSlowed = false;
    private float slowDuration = 0f;
    private float slowStrength = 0.5f;    // Default normal slow = 50% reduction

    private boolean isKnockedBack = false;
    private float knockbackTimer = 0f;
    private float knockbackDuration = 0.5f;  // Durasi knockback 0.5 detik
    private float knockbackSpeed = 200f;     // Kecepatan mundur
    private float originalSpeed;

    private float attackCooldown = 0f;
    private static final float ATTACK_COOLDOWN_DURATION = 1f;  // 1 detik antar serangan

    private int health;     // HP

    public Enemy(Texture tex, float xCenter, float yCenter) {
        this.tex    = tex;
        this.health = 3;
        this.scaledWidth  = tex.getWidth() * SCALE;
        this.scaledHeight = tex.getHeight() * SCALE;
        // simpan posisi sebagai center
        this.pos = new Vector2(xCenter, yCenter);
        this.bounds = new Rectangle(
            xCenter - scaledWidth/2,
            yCenter - scaledHeight/2,
            scaledWidth,
            scaledHeight
        );
        this.originalSpeed = this.baseSpeed;
    }

    // Method untuk memulai knockback
    public void knockback() {
        if (!isKnockedBack) {  // Hanya knockback jika belum dalam state knockback
            isKnockedBack = true;
            knockbackTimer = knockbackDuration;
            attackCooldown = ATTACK_COOLDOWN_DURATION;
            System.out.println("Enemy knockback started!"); // Debug
        }
    }

    public boolean canAttack() {
        return attackCooldown <= 0 && !isKnockedBack;
    }

    public void takeDamage(int dmg) {
        health = Math.max(0, health - dmg);
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    /** Panggil untuk menerapkan slow pada musuh */
    public void slow(float duration) {
        this.slowDuration = duration;
        this.slowStrength = 0.5f; // Normal slow = 50% speed reduction
        this.isSlowed = true;
        System.out.println("Enemy slowed! Speed reduced by 50% for " + duration + "s");
    }

    /** Method untuk heavy slow (hampir berhenti) */
    public void slowHeavy(float duration, float strength) {
        this.slowDuration = duration;
        this.slowStrength = strength; // 0.1f = 90% speed reduction
        this.isSlowed = true;
        System.out.println("Enemy heavily slowed! Speed reduced by " + ((1-strength)*100) + "% for " + duration + "s");
    }

    public void update(float delta) {
        // Handle knockback
        if (isKnockedBack) {
            knockbackTimer -= delta;
            if (knockbackTimer <= 0) {
                isKnockedBack = false;
                System.out.println("Knockback ended!"); // Debug
            }
        }

        // Handle attack cooldown
        if (attackCooldown > 0) {
            attackCooldown -= delta;
        }

        // ===== PERBAIKI SLOW HANDLING =====
        if (isSlowed && slowDuration > 0) {
            slowDuration -= delta;
            if (slowDuration <= 0) {
                isSlowed = false;
                System.out.println("Slow effect ended!");
            }
        }

        // 3) Determine current speed
        if (isKnockedBack) {
            // PERBAIKAN: Saat knockback, bergerak mundur
            currentSpeed = -knockbackSpeed;  // Negatif = mundur
        } else if (isSlowed) {
            // Slow effect
            currentSpeed = baseSpeed * slowStrength;
            System.out.println("🐌 Enemy moving slowly: " + currentSpeed + " (normal: " + baseSpeed + ")");
        } else {
            // Normal movement
            currentSpeed = baseSpeed;
        }

        pos.x -= currentSpeed * delta;
        bounds.setPosition(pos.x - bounds.width/2, pos.y - bounds.height/2);
    }

    // Getter untuk cek status knockback
    public boolean isKnockedBack() {
        return isKnockedBack;
    }


    public void drawBatch(SpriteBatch batch) {
        if (tex != null) {
            // ===== ENABLED: Visual effects =====
            if (isKnockedBack) {
                batch.setColor(1f, 0.5f, 0.5f, 1f); // Warna kemerahan saat knockback
            } else if (isSlowed) {
                batch.setColor(0.5f, 0.5f, 1f, 1f); // Warna biru saat slowed
            } else {
                batch.setColor(1f, 1f, 1f, 1f); // Normal color
            }

            batch.draw(
                tex,
                pos.x - scaledWidth/2,
                pos.y - scaledHeight/2,
                scaledWidth,
                scaledHeight
            );

//            batch.setColor(oldColor); // Restore color
        }
    }

    public void drawShape(ShapeRenderer shapes) {
        if (tex == null) {
            if (isKnockedBack) {
                shapes.setColor(Color.ORANGE);
            } else if (isSlowed) {
                shapes.setColor(Color.BLUE);
            } else {
                shapes.setColor(Color.RED);
            }
            float radius = scaledWidth/2f;
            shapes.circle(pos.x, pos.y, radius);;
        }
    }

    public float getX()     { return pos.x; }
    public float getWidth() { return scaledWidth; }
    public Rectangle getBounds() { return bounds; }

    public int getHealth() {
        return health;
    }
}
