// AoeProjectile.java - OPTIMIZED VERSION
package io.DutchSlayer.defend.entities.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.enemies.Enemy;

/**
 * AoeProjectile adalah projectile yang memberikan damage area (AOE - Area of Effect)
 * Ketika mengenai target, semua enemy dalam radius tertentu akan terkena damage
 * OPTIMIZED VERSION dengan reduced object allocation dan improved performance
 */
public class AoeProjectile extends Projectile {
    private int aoeRadius;                        // Radius area damage
    private int aoeDamage;                        // Damage per enemy dalam AOE

    private float initialVelocityX;
    private float initialVelocityY;
    private float gravity;
    private float timeElapsed = 0f;
    private final Vector2 startPos;
    private final Vector2 targetPos;
    private boolean hasExploded = false;

    // ===== PRE-CALCULATED VALUES - OPTIMIZED =====
    private final float maxHeight;
    private float totalTime;
    private final float halfWidth;                      // Pre-calculated untuk performa
    private final float halfHeight;                     // Pre-calculated untuk performa

    private static final float EXPLOSION_THRESHOLD = 50f; // Pre-calculated constant
    private static final float TIMEOUT_DURATION = 8f;     // Pre-calculated constant

    /**
     * Constructor untuk AOE Projectile - OPTIMIZED
     */
    public AoeProjectile(Texture tex,
                         float startX, float startY,
                         float targetX, float targetY,
                         float radius, float scale, int damage, float customSpeed) {
        super(tex, startX, startY, targetX, scale, customSpeed, damage);

        this.aoeRadius = (int)radius;
        this.aoeDamage = damage;

        // Pre-calculate half dimensions untuk performa
        this.halfWidth = tex.getWidth() * scale / 2f;
        this.halfHeight = tex.getHeight() * scale / 2f;

        // ===== SETUP PARABOLA PHYSICS - OPTIMIZED =====
        this.startPos = new Vector2(startX, startY);
        this.targetPos = new Vector2(targetX, targetY);
        this.maxHeight = 150f;

        // Pre-calculate values untuk mengurangi komputasi di runtime
        float horizontalDistance = Math.abs(targetX - startX);
        this.totalTime = horizontalDistance / (customSpeed * 0.8f);
        this.initialVelocityX = (targetX - startX) / totalTime;
        this.initialVelocityY = (2f * maxHeight) / totalTime;

        float heightDiff = targetY - startY;
        this.gravity = -2f * (initialVelocityY * totalTime - (heightDiff + maxHeight)) / (totalTime * totalTime);
    }

    /**
     * Override update untuk menggunakan fisika parabola - OPTIMIZED
     */
    @Override
    public void update(float delta) {
        if (hasExploded || !isActive()) return; // Early exit optimization

        // ===== PARABOLA TRAJECTORY SYSTEM - OPTIMIZED =====
        boolean useParabola = true;
        if (useParabola) {
            updateParabolaTrajectory(delta);
        } else {
            super.update(delta);
        }

        checkTargetCollision();
    }

    /**
     * Update posisi menggunakan rumus fisika parabola - OPTIMIZED
     */
    private void updateParabolaTrajectory(float delta) {
        timeElapsed += delta;

        // OPTIMIZED: Pre-calculate time squared untuk mengurangi operasi
        float timeSquared = timeElapsed * timeElapsed;

        // Rumus kinematika: s = s0 + v0*t + 0.5*a*t^2
        float newX = startPos.x + initialVelocityX * timeElapsed;
        float newY = startPos.y + initialVelocityY * timeElapsed + 0.5f * gravity * timeSquared;

        // OPTIMIZED: Update bounds position langsung tanpa method call overhead
        bounds.x = newX - halfWidth;
        bounds.y = newY - halfHeight;
    }

    /**
     * Check collision dengan area target - OPTIMIZED
     */
    private void checkTargetCollision() {
        // OPTIMIZED: Pre-calculate center positions
        float currentX = bounds.x + halfWidth;
        float currentY = bounds.y + halfHeight;

        // OPTIMIZED: Inline distance calculation untuk performa
        float dx = currentX - targetPos.x;
        float dy = currentY - targetPos.y;
        float distanceSquared = dx * dx + dy * dy; // Avoid sqrt untuk performa

        // Check collision conditions (menggunakan distance squared untuk performa)
        if (distanceSquared < EXPLOSION_THRESHOLD * EXPLOSION_THRESHOLD ||
            currentY <= targetPos.y ||
            timeElapsed > totalTime * 1.2f ||
            timeElapsed > TIMEOUT_DURATION) {
            explode();
        }
    }

    /**
     * Trigger explosion - OPTIMIZED
     */
    private void explode() {
        if (hasExploded) return;

        hasExploded = true;
        setActive(false); // Use parent's active flag instead of manual positioning
    }

    /**
     * METHOD BARU: Trigger AOE damage secara manual - OPTIMIZED
     */
    public void triggerAOEDamage(Array<Enemy> enemies) {
        if (!hasExploded) return;

        float explosionX = targetPos.x;
        float explosionY = targetPos.y;

        // OPTIMIZED: Pre-calculate radius squared untuk menghindari sqrt
        float radiusSquared = aoeRadius * aoeRadius;

        // OPTIMIZED: Loop dengan index untuk performa lebih baik
        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            if (e.isDestroyed()) continue;

            // OPTIMIZED: Pre-calculate enemy center
            float enemyX = e.getBounds().x + e.getBounds().width * 0.5f;
            float enemyY = e.getBounds().y + e.getBounds().height * 0.5f;

            // OPTIMIZED: Distance check tanpa sqrt
            float dx = explosionX - enemyX;
            float dy = explosionY - enemyY;
            float distanceSquared = dx * dx + dy * dy;

            if (distanceSquared <= radiusSquared) {
                e.takeDamage(aoeDamage);
            }
        }
    }

    /**
     * Override onHit untuk AOE damage - OPTIMIZED dengan VOID return type
     */
    @Override
    public void onHit(Array<Enemy> enemies) {
        if (hasExploded) {
            triggerAOEDamage(enemies);
        }
    }

    /**
     * Override drawBatch - OPTIMIZED
     */
    @Override
    public void drawBatch(SpriteBatch batch) {
        if (tex == null || hasExploded || !isActive()) return; // Early exit

        batch.draw(tex, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * Override getX dan getY - OPTIMIZED dengan pre-calculated values
     */
    @Override
    public float getX() {
        return bounds.x + halfWidth;
    }

    @Override
    public float getY() {
        return bounds.y + halfHeight;
    }

    // ===== OPTIMIZED GETTERS - INLINE UNTUK PERFORMA =====
    public boolean hasExploded() { return hasExploded; }

    /**
     * BARU: Reset method untuk object pooling - OPTIMIZED
     */
    public void reset(float startX, float startY, float targetX, float targetY,
                      float radius, int damage, float customSpeed) {
        super.reset(startX, startY, targetX, customSpeed, damage);

        this.hasExploded = false;
        this.timeElapsed = 0f;
        this.aoeRadius = (int)radius;
        this.aoeDamage = damage;

        // Reset trajectory calculation
        this.startPos.set(startX, startY);
        this.targetPos.set(targetX, targetY);

        float horizontalDistance = Math.abs(targetX - startX);
        this.totalTime = horizontalDistance / (customSpeed * 0.8f);
        this.initialVelocityX = (targetX - startX) / totalTime;
        this.initialVelocityY = (2f * maxHeight) / totalTime;

        float heightDiff = targetY - startY;
        this.gravity = -2f * (initialVelocityY * totalTime - (heightDiff + maxHeight)) / (totalTime * totalTime);
    }

}
