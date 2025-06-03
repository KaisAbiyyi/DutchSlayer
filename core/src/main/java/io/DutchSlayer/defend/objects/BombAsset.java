// ===== BOMB ASSET WITH PICKUP-STYLE ANIMATION =====
// File: BombAsset.java
package io.DutchSlayer.defend.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * BombAsset dengan animasi drop seperti PickupItem - terlempar ke atas dulu, lalu jatuh
 */
public class BombAsset {
    /* ===== VISUAL COMPONENTS ===== */
    private final Texture tex;                      // Original texture
    private float scaledW, scaledH;                 // Visual dimensions
    private final Rectangle bounds;                 // Collision bounds

    /* ===== POSITION & PHYSICS ===== */
    private final float baseX, baseY;               // Posisi dasar (target landing)
    private float currentX, currentY;               // Posisi saat ini
    private float offsetY = 0f;                     // Offset dari posisi dasar (seperti PickupItem)

    /* ===== PHYSICS - SAME AS PICKUPITEM ===== */
    private float velocityY = 150f;                 // Initial velocity ke atas (sama dengan PickupItem)
    private float gravity = -500f;                  // Gravitasi turun (sama dengan PickupItem)
    private boolean falling = true;                 // Masih dalam proses jatuh?
    private boolean isLanded = false;               // Sudah mendarat di ground?

    /* ===== TIMING ===== */
    private float timer = 0f;                       // Timer total sejak dibuat
    private boolean hasExploded = false;            // Sudah meledak?

    /* ===== VISUAL SCALING EFFECTS ===== */
    private final float initialScale = 0.15f;       // Ukuran awal
    private final float maxScale = 0.30f;           // Ukuran maksimal (saat mau meledak)
    private float currentScale;                     // Ukuran saat ini

    /* ===== EXPLOSION CONSTANTS ===== */
    private static final float EXPLODE_TIME = 3f;       // Waktu countdown setelah landing
    private static final int DAMAGE = 2;                // Damage per tower
    private static final float EXPLOSION_RADIUS = 250f; // Radius ledakan
    private static final float GROUND_Y = 150f;         // Ground level (same as GROUND_Y constant)

    /**
     * Constructor - Bomb dengan pickup-style drop animation
     * @param tex Texture bomb
     * @param dropX Posisi X tempat bomb akan jatuh (target final)
     * @param dropY Posisi Y tempat bomb akan jatuh (biasanya GROUND_Y)
     */
    public BombAsset(Texture tex, float dropX, float dropY) {
        this.tex = tex;
        this.baseX = dropX;
        this.baseY = dropY;

        // Setup posisi awal (sama dengan target, tapi akan ter-offset ke atas)
        this.currentX = dropX;
        this.currentY = dropY;

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

        System.out.println("💣 Bomb dropped with pickup-style animation!");
        System.out.println("   Target landing: (" + dropX + ", " + dropY + ")");
        System.out.println("   Initial velocity: " + velocityY + " (upward)");
    }

    /**
     * 4-parameter constructor untuk backward compatibility
     */
    public BombAsset(Texture tex, float startX, float startY, float targetX, float targetY) {
        // Gunakan target sebagai drop position, ignore start position
        this(tex, targetX, targetY);
        System.out.println("   Note: Using target position (" + targetX + ", " + targetY + ") as drop location");
    }

    /**
     * Update bomb logic setiap frame - SAMA PERSIS SEPERTI PICKUPITEM
     */
    public void update(float delta) {
        if (hasExploded) return; // Skip jika sudah meledak

        timer += delta;

        // ===== PHYSICS UPDATE - IDENTICAL TO PICKUPITEM =====
        if (!isLanded && falling) {
            velocityY += gravity * delta;        // Apply gravity
            offsetY += velocityY * delta;        // Update offset

            float actualY = baseY + offsetY;
            if (actualY <= GROUND_Y) {
                offsetY = GROUND_Y - baseY;      // Snap to ground
                falling = false;
                isLanded = true;
                System.out.println("💣 Bomb landed! Starting countdown...");
            }
        }

        // Update current position dengan offset
        currentY = baseY + offsetY;

        // ===== VISUAL SCALING =====
        updateScaling();

        // ===== UPDATE VISUAL COMPONENTS =====
        updateVisuals();
    }

    /**
     * Handle scaling effect berdasarkan state
     */
    private void updateScaling() {
        if (isLanded) {
            // Setelah mendarat: gradually membesar sampai mau meledak
            float landedTime = timer - getFallTime();
            if (landedTime > 0) {
                float scaleProgress = landedTime / EXPLODE_TIME;
                currentScale = initialScale + (maxScale - initialScale) * Math.min(1f, scaleProgress);
            }
        } else {
            // Saat terbang: slight size variation berdasarkan altitude
            float altitude = currentY - GROUND_Y;
            float altitudeProgress = Math.max(0, altitude / 100f); // Scale based on 100px max height
            currentScale = initialScale + (initialScale * 0.2f * altitudeProgress);
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
     * Estimasi waktu jatuh berdasarkan physics
     */
    private float getFallTime() {
        // Waktu untuk mencapai puncak: t = v₀ / g
        float timeToPeak = 150f / 500f; // velocityY / abs(gravity)

        // Estimasi total waktu jatuh (simplified)
        return timeToPeak * 2f; // Approximately time to go up and come down
    }

    /**
     * Check apakah bomb siap meledak
     */
    public boolean shouldExplode() {
        if (!isLanded) return false; // Belum bisa meledak kalau masih terbang

        float landedTime = timer - getFallTime();
        return landedTime >= EXPLODE_TIME && !hasExploded;
    }

    /**
     * Ledakkan bomb dan berikan AOE damage ke towers
     */
    public void explode(Array<Tower> towers) {
        if (hasExploded) return;    // Prevent double explosion

        hasExploded = true;
        System.out.println("💣 BOMB EXPLODED at (" + currentX + ", " + currentY + ")!");

        int hitCount = 0;

        // Loop semua tower untuk cek damage
        for (Tower tower : towers) {
            if (tower.isDestroyed()) continue; // Skip tower yang sudah hancur

            // Hitung jarak dari explosion center
            float distance = (float) Math.sqrt(
                Math.pow(currentX - tower.x, 2) + Math.pow(currentY - tower.y, 2)
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
            }
        }

        System.out.println("💥 Bomb explosion hit " + hitCount + " towers within radius " + EXPLOSION_RADIUS);
    }

    /**
     * Render bomb dengan pickup-style visual effects (no rotation)
     */
    public void drawBatch(SpriteBatch batch) {
        if (tex == null || hasExploded) return; // Skip jika tidak ada texture atau sudah meledak

        // ===== FLYING EFFECT =====
        if (!isLanded) {
            // Saat terbang: transparency berdasarkan altitude (NO SPINNING)
            float altitude = currentY - GROUND_Y;
            float alpha = 0.7f + (0.3f * Math.max(0, altitude / 100f)); // More opaque at higher altitude
            batch.setColor(1f, 1f, 1f, alpha);

            // Draw WITHOUT rotation - simple static bomb
            batch.draw(tex, currentX - scaledW/2, currentY - scaledH/2, scaledW, scaledH);
        }
        // ===== LANDED FLASHING EFFECT =====
        else {
            applyFlashingEffect(batch);

            // Draw normal (no rotation when landed)
            batch.draw(tex, currentX - scaledW/2, currentY - scaledH/2, scaledW, scaledH);
        }

        // Reset color ke normal
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * Apply flashing effect ketika bomb sudah mendarat dan hampir meledak
     */
    private void applyFlashingEffect(SpriteBatch batch) {
        float landedTime = timer - getFallTime();
        float flashThreshold = EXPLODE_TIME * 0.5f; // Mulai flash di 50% countdown

        if (landedTime > flashThreshold) {
            // Hitung flash intensity berdasarkan sisa waktu
            float timeLeft = EXPLODE_TIME - landedTime;
            float flashSpeed = 8f + (12f * (1f - timeLeft / (EXPLODE_TIME - flashThreshold)));

            // Oscillating flash effect
            float flashValue = (float) Math.sin(landedTime * flashSpeed);
            if (flashValue > 0) {
                // Red warning flash
                batch.setColor(1f, 0.3f, 0.3f, 0.8f + 0.2f * flashValue);
            } else {
                // Normal color
                batch.setColor(1f, 1f, 1f, 1f);
            }
        } else {
            // Normal appearance setelah landing
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    /* ===== GETTERS - COMPATIBLE WITH EXISTING CODE ===== */
    public float getX() { return currentX; }
    public float getY() { return currentY; }
    public boolean hasExploded() { return hasExploded; }
    public boolean isLanded() { return isLanded; }
    public boolean isFlying() { return !isLanded && !hasExploded; }

    /**
     * Dapatkan sisa waktu sampai meledak
     */
    public float getTimeLeft() {
        if (!isLanded) return EXPLODE_TIME; // Full countdown time
        float landedTime = timer - getFallTime();
        return Math.max(0, EXPLODE_TIME - landedTime);
    }

    /**
     * Dapatkan current altitude dari ground level
     */
    public float getAltitude() {
        return Math.max(0, currentY - GROUND_Y);
    }

    /**
     * Check apakah bomb masih dalam proses jatuh
     */
    public boolean isFalling() {
        return falling;
    }
}
