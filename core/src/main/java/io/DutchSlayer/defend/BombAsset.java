// ===== 4) BUAT BOMB ASSET CLASS =====
// File: BombAsset.java
package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;


/**
 * BombAsset adalah bomb yang dijatuhkan oleh enemy BOMBER
 * Memiliki physics jatuh, countdown timer, dan AOE explosion damage
 */
public class BombAsset {
    /* ===== VISUAL COMPONENTS ===== */
    private final Texture tex;                      // Texture bomb
    private float scaledW, scaledH;                 // Collision bounds
    private final Rectangle bounds;                 // Collision bounds

    /* ===== POSITION & PHYSICS ===== */
    private final float targetx, targety;           // Posisi akhir di ground
    private float currentX, currentY;               // Posisi saat ini (untuk falling effect)
    private float velocityY = 0f;                   // Kecepatan jatuh
    private boolean isLanded = false;               // Sudah mendarat di ground?

    /* ===== TIMING ===== */
    private float timer = 0f;                       // Timer total sejak dibuat
    private boolean hasExploded = false;            // Sudah meledak?

    /* ===== VISUAL SCALING EFFECTS ===== */
    private final float initialScale = 0.15f;       // Ukuran awal
    private final float maxScale = 0.30f;           // Ukuran maksimal (saat mau meledak)
    private float currentScale;                     // Ukuran saat ini

    /* ===== PHYSICS CONSTANTS ===== */
    private static final float GRAVITY = -800f;         // Gravitasi (negatif = ke bawah)
    private static final float INITIAL_HEIGHT = 100f;   // Tinggi awal jatuh
    private static final float FALL_TIME = 0.8f;        // Waktu jatuh (detik)

    /* ===== EXPLOSION CONSTANTS ===== */
    private static final float EXPLODE_TIME = 3f;       // Total waktu sampai meledak
    private static final int DAMAGE = 2;                // Damage per tower
    private static final float EXPLOSION_RADIUS = 250f; // Radius ledakan

    /**
     * Constructor - Bomb mulai jatuh dari atas target position
     * @param tex Texture bomb
     * @param x Target posisi X di ground
     * @param y Target posisi Y di ground
     */
    public BombAsset(Texture tex, float x, float y) {
        this.tex = tex;
        this.targetx = x;
        this.targety = y;

        // Setup posisi awal: mulai jatuh dari atas
        this.currentX = x;
        this.currentY = y + INITIAL_HEIGHT; // Start 100px above ground
        this.velocityY = 0f; // Start with no velocity

        // Setup visual scaling
        this.currentScale = initialScale;
        this.scaledW = tex.getWidth() * currentScale;
        this.scaledH = tex.getHeight() * currentScale;

        // Setup collision bounds
        this.bounds = new Rectangle(
            currentX - scaledW/2,
            currentY - scaledH/2,
            scaledW, scaledH
        );

        System.out.println("💣 Bomb created at (" + x + ", " + y + ") - Starting fall from height " + INITIAL_HEIGHT);
    }

    /**
     * Update bomb logic setiap frame
     */
    public void update(float delta) {
        if (hasExploded) return; // Skip jika sudah meledak

        timer += delta;

        // ===== HANDLE FALLING PHYSICS =====
        if (!isLanded) {
            updateFalling(delta);
        }

        // ===== HANDLE VISUAL SCALING =====
        updateScaling();

        // ===== UPDATE VISUAL COMPONENTS =====
        updateVisuals();
    }

    /**
     * Handle falling physics dengan gravitasi
     */
    private void updateFalling(float delta) {
        // Apply gravitasi (acceleration)
        velocityY += GRAVITY * delta;

        // Update posisi Y
        currentY += velocityY * delta;

        // Check apakah sudah mendarat
        if (currentY <= targety) {
            currentY = targety;     // Snap ke ground
            velocityY = 0f;         // Stop falling
            isLanded = true;
            System.out.println("💣 Bomb landed on ground! Starting countdown...");
        }
    }

    /**
     * Handle scaling effect berdasarkan state
     */
    private void updateScaling() {
        if (isLanded) {
            // Setelah mendarat: gradually membesar sampai mau meledak
            float landedTime = timer - FALL_TIME;
            if (landedTime > 0) {
                float scaleProgress = landedTime / (EXPLODE_TIME - FALL_TIME);
                currentScale = initialScale + (maxScale - initialScale) * Math.min(1f, scaleProgress);
            }
        } else {
            // Saat jatuh: tetap ukuran kecil
            currentScale = initialScale;
        }
    }

    /**
     * Update visual components berdasarkan scaling
     */
    private void updateVisuals() {
        // Update scaled dimensions
        scaledW = tex.getWidth() * currentScale;
        scaledH = tex.getHeight() * currentScale;

        // Update collision bounds
        bounds.set(currentX - scaledW/2, currentY - scaledH/2, scaledW, scaledH);
    }

    /**
     * Check apakah bomb siap meledak
     */
    public boolean shouldExplode() {
        return timer >= EXPLODE_TIME && !hasExploded;
    }

    /**
     * Ledakkan bomb dan berikan AOE damage ke towers
     */
    public void explode(Array<Tower> towers) {
        if (hasExploded) return;    // Prevent double explosion

        hasExploded = true;
        System.out.println("💣 BOMB EXPLODED at (" + targetx + ", " + targety + ")!");

        int hitCount = 0;

        // Loop semua tower untuk cek damage
        for (Tower tower : towers) {
            if (tower.isDestroyed()) continue; // Skip tower yang sudah hancur

            // Hitung jarak dari explosion center
            float distance = (float) Math.sqrt(
                Math.pow(targetx - tower.x, 2) + Math.pow(targety - tower.y, 2)
            );

            // Jika dalam radius explosion
            if (distance <= EXPLOSION_RADIUS) {
                int oldHp = tower.getHealth();
                tower.takeDamage(DAMAGE);
                int newHp = tower.getHealth();
                hitCount++;
                System.out.println("💥 Tower hit! HP: " + oldHp + " → " + newHp + " (distance: " + distance + ")");

                if (tower.isDestroyed()) {
                    System.out.println("🏗️ Tower destroyed by bomb explosion!");
                }
            } else {
                System.out.println("❌ Tower too far! Distance: " + distance + " > Radius: " + EXPLOSION_RADIUS);
            }
        }

        System.out.println("💥 Bomb explosion hit " + hitCount + " towers within radius " + EXPLOSION_RADIUS);
    }

    /**
     * Render bomb dengan visual effects
     */
    public void drawBatch(SpriteBatch batch) {
        if (tex == null || hasExploded) return; // Skip jika tidak ada texture atau sudah meledak

        // ===== FALLING EFFECT =====
        if (!isLanded) {
            // Saat jatuh: sedikit transparent
            batch.setColor(1f, 1f, 1f, 0.9f);
        }
        // ===== LANDED FLASHING EFFECT =====
        else {
            applyFlashingEffect(batch);
        }

        // Draw bomb dengan posisi dan ukuran yang berubah
        batch.draw(tex, currentX - scaledW/2, currentY - scaledH/2, scaledW, scaledH);

        // Reset color ke normal
        batch.setColor(1f, 1f, 1f, 1f);

    }

    /**
     * Apply flashing effect ketika bomb sudah mendarat dan hampir meledak
     */
    private void applyFlashingEffect(SpriteBatch batch) {
        float landedTime = timer - FALL_TIME;
        float flashThreshold = (EXPLODE_TIME - FALL_TIME) * 0.4f; // Mulai flash di 60% countdown

        if (landedTime > flashThreshold) {
            // Hitung flash intensity berdasarkan sisa waktu
            float timeLeft = (EXPLODE_TIME - FALL_TIME) - landedTime;
            float flashSpeed = 15f + (15f * (1f - timeLeft / ((EXPLODE_TIME - FALL_TIME) - flashThreshold)));

            // Oscillating flash effect
            float flashValue = (float) Math.sin(landedTime * flashSpeed);
            if (flashValue > 0) {
                // White transparent flash
                batch.setColor(1f, 1f, 1f, 0.6f + 0.4f * flashValue);
            } else {
                // Normal color dengan sedikit transparency
                batch.setColor(1f, 1f, 1f, 0.8f);
            }
        } else {
            // Normal appearance setelah landing
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    /* ===== GETTERS ===== */
    public float getX() { return currentX; }
    public float getY() { return currentY; }
    public boolean hasExploded() { return hasExploded; }
    public boolean isLanded() { return isLanded; }

    /**
     * Dapatkan sisa waktu sampai meledak
     */
    public float getTimeLeft() {
        if (!isLanded) return EXPLODE_TIME; // Full time jika masih jatuh
        return Math.max(0, EXPLODE_TIME - timer);
    }

    /**
     * Dapatkan progress countdown (0.0 - 1.0)
     */
    public float getProgress() {
        if (!isLanded) return 0f; // No progress jika masih jatuh
        return (timer - FALL_TIME) / (EXPLODE_TIME - FALL_TIME);
    }

    /**
     * Dapatkan progress falling (0.0 - 1.0)
     */
    public float getFallProgress() {
        return Math.min(1f, timer / FALL_TIME);
    }
}
