package io.DutchSlayer.defend.entities.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.enemies.Enemy;

public class AoeProjectile extends Projectile {
    private int aoeRadius;
    private int aoeDamage;

    private float initialVelocityX;
    private float initialVelocityY;
    private float gravity;
    private float timeElapsed = 0f;
    private final Vector2 startPos;
    private final Vector2 targetPos;
    private boolean hasExploded = false;

    private final float maxHeight;
    private float totalTime;
    private final float halfWidth;
    private final float halfHeight;

    private static final float EXPLOSION_THRESHOLD = 50f;
    private static final float TIMEOUT_DURATION = 8f;

    public AoeProjectile(Texture tex,
                         float startX, float startY,
                         float targetX, float targetY,
                         float radius, float scale, int damage, float customSpeed) {
        super(tex, startX, startY, targetX, scale, customSpeed, damage);

        this.aoeRadius = (int)radius;
        this.aoeDamage = damage;

        this.halfWidth = tex.getWidth() * scale / 2f;
        this.halfHeight = tex.getHeight() * scale / 2f;

        this.startPos = new Vector2(startX, startY);
        this.targetPos = new Vector2(targetX, targetY);
        this.maxHeight = 150f;


        float horizontalDistance = Math.abs(targetX - startX);
        this.totalTime = horizontalDistance / (customSpeed * 0.8f);
        this.initialVelocityX = (targetX - startX) / totalTime;
        this.initialVelocityY = (2f * maxHeight) / totalTime;

        float heightDiff = targetY - startY;
        this.gravity = -2f * (initialVelocityY * totalTime - (heightDiff + maxHeight)) / (totalTime * totalTime);
    }

    @Override
    public void update(float delta) {
        if (hasExploded || !isActive()) return;

        boolean useParabola = true;
        if (useParabola) {
            updateParabolaTrajectory(delta);
        } else {
            super.update(delta);
        }

        checkTargetCollision();
    }

    private void updateParabolaTrajectory(float delta) {
        timeElapsed += delta;

        float timeSquared = timeElapsed * timeElapsed;

        float newX = startPos.x + initialVelocityX * timeElapsed;
        float newY = startPos.y + initialVelocityY * timeElapsed + 0.5f * gravity * timeSquared;

        bounds.x = newX - halfWidth;
        bounds.y = newY - halfHeight;
    }

    private void checkTargetCollision() {
        float currentX = bounds.x + halfWidth;
        float currentY = bounds.y + halfHeight;

        float dx = currentX - targetPos.x;
        float dy = currentY - targetPos.y;
        float distanceSquared = dx * dx + dy * dy;

        if (distanceSquared < EXPLOSION_THRESHOLD * EXPLOSION_THRESHOLD ||
            currentY <= targetPos.y ||
            timeElapsed > totalTime * 1.2f ||
            timeElapsed > TIMEOUT_DURATION) {
            explode();
        }
    }

    private void explode() {
        if (hasExploded) return;

        hasExploded = true;
        setActive(false);
    }

    public void triggerAOEDamage(Array<Enemy> enemies) {
        if (!hasExploded) return;

        float explosionX = targetPos.x;
        float explosionY = targetPos.y;

        float radiusSquared = aoeRadius * aoeRadius;

        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            if (e.isDestroyed()) continue;

            float enemyX = e.getBounds().x + e.getBounds().width * 0.5f;
            float enemyY = e.getBounds().y + e.getBounds().height * 0.5f;

            float dx = explosionX - enemyX;
            float dy = explosionY - enemyY;
            float distanceSquared = dx * dx + dy * dy;

            if (distanceSquared <= radiusSquared) {
                e.takeDamage(aoeDamage);
            }
        }
    }

    @Override
    public void onHit(Array<Enemy> enemies) {
        if (hasExploded) {
            triggerAOEDamage(enemies);
        }
    }

    @Override
    public void drawBatch(SpriteBatch batch) {
        if (tex == null || hasExploded || !isActive()) return;

        batch.draw(tex, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public float getX() {
        return bounds.x + halfWidth;
    }

    @Override
    public float getY() {
        return bounds.y + halfHeight;
    }

    public boolean hasExploded() { return hasExploded; }

    public void reset(float startX, float startY, float targetX, float targetY,
                      float radius, int damage, float customSpeed) {
        super.reset(startX, startY, targetX, customSpeed, damage);

        this.hasExploded = false;
        this.timeElapsed = 0f;
        this.aoeRadius = (int)radius;
        this.aoeDamage = damage;

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
