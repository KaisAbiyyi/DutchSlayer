// ===== INSTANT BOMB EXPLOSION - NO COUNTDOWN =====
// File: BombAsset.java
package io.DutchSlayer.defend.entities.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.towers.Tower;
import io.DutchSlayer.defend.ui.ImageLoader;
import io.DutchSlayer.defend.utils.AudioManager;

/**
 * BombAsset dengan instant explosion saat menyentuh tanah
 */
public class BombAsset {
    /* ===== VISUAL COMPONENTS ===== */
    private final Texture tex;
    private float scaledW, scaledH;
    private final Rectangle bounds;

    /* ===== POSITION & PHYSICS ===== */
    private final float baseX, baseY;
    private float currentX, currentY;
    private float offsetY = 0f;

    /* ===== PHYSICS ===== */
    private float velocityY = 150f;
    private float gravity = -500f;
    private boolean falling = true;
    private boolean isLanded = false;

    /* ===== INSTANT EXPLOSION ===== */
    private boolean hasExploded = false;
    private boolean isShowingExplosion = false;
    private float explosionTimer = 0f;
    private float explosionScale = 1.0f;

    /* ===== VISUAL SCALING ===== */
    private final float bombScale = 0.2f;  // ⭐ FIXED: Constant scale untuk bomb

    /* ===== CONSTANTS - INSTANT EXPLOSION ===== */
    private static final int DAMAGE = 2;
    private static final float EXPLOSION_RADIUS = 250f;
    private static final float GROUND_Y = 150f;
    private static final float EXPLOSION_DISPLAY_TIME = 0.3f;
    private static final float EXPLOSION_MAX_SCALE = 2.0f;

    public BombAsset(Texture tex, float dropX, float dropY) {
        this.tex = tex;
        this.baseX = dropX;
        this.baseY = dropY;
        this.currentX = dropX;
        this.currentY = dropY;

        // ⭐ FIXED SCALING: No animation scaling
        this.scaledW = tex.getWidth() * bombScale;
        this.scaledH = tex.getHeight() * bombScale;

        this.bounds = new Rectangle(
            currentX - scaledW/2,
            currentY - scaledH/2,
            scaledW, scaledH
        );

        System.out.println("💣 Bomb created with INSTANT explosion on ground contact!");
    }

    public BombAsset(Texture tex, float startX, float startY, float targetX, float targetY) {
        this(tex, targetX, targetY);
    }

    /**
     * ⭐ SIMPLIFIED: Update dengan instant explosion
     */
    public void update(float delta) {
        // ===== EXPLOSION UPDATE =====
        if (isShowingExplosion) {
            updateExplosion(delta);
            return;
        }

        if (hasExploded) {
            return; // Sudah selesai
        }

        // ===== PHYSICS UPDATE =====
        if (falling && !isLanded) {
            velocityY += gravity * delta;
            offsetY += velocityY * delta;

            float actualY = baseY + offsetY;
            if (actualY <= GROUND_Y) {
                // ⭐ INSTANT EXPLOSION saat menyentuh tanah
                offsetY = GROUND_Y - baseY;
                currentY = baseY + offsetY;
                falling = false;
                isLanded = true;

                // ⭐ TRIGGER EXPLOSION IMMEDIATELY
                triggerExplosion();

                System.out.println("💣 Bomb hit ground and EXPLODED INSTANTLY!");
                return;
            }
        }

        currentY = baseY + offsetY;
        updateVisuals();
    }

    /**
     * ⭐ NEW: Trigger explosion immediately
     */
    private void triggerExplosion() {
        isShowingExplosion = true;
        explosionTimer = 0f;
        explosionScale = 1.0f;

        // ⭐ PLAY EXPLOSION SOUND
        AudioManager.playTrapExplosionHit();

        // ⭐ DAMAGE TOWERS IMMEDIATELY
        damageTowersInRadius();

        System.out.println("💥 INSTANT EXPLOSION triggered!");
    }

    /**
     * ⭐ NEW: Damage towers dalam radius
     */
    private void damageTowersInRadius() {
        // This will be called from GameLogic when shouldExplode() returns true
        // For now, just mark that explosion should happen
    }

    /**
     * ⭐ SIMPLIFIED: Update explosion visual
     */
    private void updateExplosion(float delta) {
        explosionTimer += delta;

        // ⭐ SIMPLE SCALING: Cepat membesar lalu mengecil
        float progress = explosionTimer / EXPLOSION_DISPLAY_TIME;

        if (progress <= 0.3f) {
            // Phase 1: Rapid expansion
            float expandProgress = progress / 0.3f;
            explosionScale = 1.0f + (EXPLOSION_MAX_SCALE - 1.0f) * expandProgress;
        } else if (progress <= 0.7f) {
            // Phase 2: Hold at max
            explosionScale = EXPLOSION_MAX_SCALE;
        } else {
            // Phase 3: Quick fade
            float fadeProgress = (progress - 0.7f) / 0.3f;
            explosionScale = EXPLOSION_MAX_SCALE * (1.0f - fadeProgress);
        }

        // ⭐ EXPLOSION SELESAI
        if (explosionTimer >= EXPLOSION_DISPLAY_TIME) {
            hasExploded = true;
            isShowingExplosion = false;
            System.out.println("💥 Explosion completed!");
        }
    }

    private void updateVisuals() {
        // ⭐ FIXED: No scaling animation, constant size
        bounds.set(currentX - scaledW/2, currentY - scaledH/2, scaledW, scaledH);
    }

    /**
     * ⭐ INSTANT: Check apakah bomb siap meledak (immediately after landing)
     */
    public boolean shouldExplode() {
        return isLanded && !hasExploded && !isShowingExplosion;
    }

    /**
     * ⭐ UPDATED: Ledakkan bomb (called by GameLogic)
     */
    public void explode(Array<Tower> towers) {
        if (hasExploded || isShowingExplosion) return;

        System.out.println("💣 BOMB EXPLODING - damaging towers in radius!");

        int hitCount = 0;

        // Damage towers dalam radius
        for (Tower tower : towers) {
            if (tower.isDestroyed()) continue;

            float distance = (float) Math.sqrt(
                Math.pow(currentX - tower.x, 2) + Math.pow(currentY - tower.y, 2)
            );

            if (distance <= EXPLOSION_RADIUS) {
                int oldHp = tower.getHealth();
                tower.takeDamage(DAMAGE);
                int newHp = tower.getHealth();
                hitCount++;
                System.out.println("💥 Tower hit! HP: " + oldHp + " → " + newHp);

                if (tower.isDestroyed()) {
                    System.out.println("🏗️ Tower destroyed by bomb!");
                }
            }
        }

        System.out.println("💥 Bomb hit " + hitCount + " towers");
    }

    /**
     * ⭐ SIMPLIFIED: Render bomb atau explosion
     */
    public void drawBatch(SpriteBatch batch) {
        if (tex == null) return;

        // ===== EXPLOSION RENDERING =====
        if (isShowingExplosion) {
            drawExplosion(batch);
            return;
        }

        // ===== NORMAL BOMB RENDERING =====
        if (hasExploded) return;

        // ⭐ SIMPLE: No flashing, no scaling animation
        if (falling) {
            // Flying effect
            float altitude = currentY - GROUND_Y;
            float alpha = 0.7f + (0.3f * Math.max(0, altitude / 100f));
            batch.setColor(1f, 1f, 1f, alpha);
        } else {
            // Normal color (no flashing since it explodes instantly)
            batch.setColor(1f, 1f, 1f, 1f);
        }

        batch.draw(tex, currentX - scaledW/2, currentY - scaledH/2, scaledW, scaledH);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * ⭐ EXPLOSION RENDERING
     */
    private void drawExplosion(SpriteBatch batch) {
        Texture explosionTex = getExplosionTexture();

        if (explosionTex != null) {
            // ⭐ EXPLOSION COLOR PROGRESSION
            float progress = explosionTimer / EXPLOSION_DISPLAY_TIME;

            if (progress <= 0.2f) {
                batch.setColor(1.3f, 1.3f, 1.3f, 1f); // Bright white flash
            } else if (progress <= 0.5f) {
                batch.setColor(1f, 0.8f, 0.3f, 1f); // Orange fire
            } else {
                float alpha = 1f - ((progress - 0.5f) / 0.5f) * 0.7f;
                batch.setColor(1f, 0.4f, 0.1f, alpha); // Red fade
            }

            // ⭐ CALCULATE EXPLOSION SIZE
            float explosionW = scaledW * explosionScale;
            float explosionH = scaledH * explosionScale;

            batch.draw(explosionTex,
                currentX - explosionW/2,
                currentY - explosionH/2,
                explosionW, explosionH);

        } else {
            // ⭐ FALLBACK: Multi-layer bomb texture
            drawFallbackExplosion(batch);
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    private Texture getExplosionTexture() {
        if (ImageLoader.explosionTex != null) {
            return ImageLoader.explosionTex;
        }
        return tex; // Fallback
    }

    private void drawFallbackExplosion(SpriteBatch batch) {
        float progress = explosionTimer / EXPLOSION_DISPLAY_TIME;

        // ⭐ MULTI-LAYER FALLBACK
        for (int i = 0; i < 3; i++) {
            float layerScale = explosionScale * (1f + i * 0.4f);
            float layerW = scaledW * layerScale;
            float layerH = scaledH * layerScale;

            switch(i) {
                case 0: // Inner white core
                    batch.setColor(1.2f, 1.2f, 1.2f, 0.9f);
                    break;
                case 1: // Middle orange
                    batch.setColor(1f, 0.7f, 0.2f, 0.7f);
                    break;
                case 2: // Outer red
                    float alpha = (1f - progress) * 0.5f;
                    batch.setColor(1f, 0.3f, 0.1f, alpha);
                    break;
            }

            batch.draw(tex,
                currentX - layerW/2,
                currentY - layerH/2,
                layerW, layerH);
        }
    }

    public boolean willDamageTower(Tower tower) {
        if (hasExploded()) return false;
        float distance = (float) Math.sqrt(
            Math.pow(baseX - tower.x, 2) + Math.pow(baseY - tower.y, 2)
        );
        return distance <= EXPLOSION_RADIUS;
    }

    /* ===== GETTERS ===== */
    public float getX() { return currentX; }
    public float getY() { return currentY; }
    public boolean hasExploded() { return hasExploded; }
    public boolean isLanded() { return isLanded; }
    public boolean isFlying() { return falling; }
    public boolean isExploding() { return isShowingExplosion; }

    // ⭐ SIMPLIFIED: No timer since explosion is instant
    public float getTimeLeft() { return falling ? 1f : 0f; }
    public float getAltitude() { return Math.max(0, currentY - GROUND_Y); }
    public boolean isFalling() { return falling; }
}
