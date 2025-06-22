package io.DutchSlayer.defend.entities.projectiles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.towers.Tower;
import io.DutchSlayer.defend.ui.ImageLoader;
import io.DutchSlayer.defend.utils.AudioManager;

public class BombAsset {
    private final Texture tex;
    private final float scaledW;
    private final float scaledH;
    private final Rectangle bounds;

    private final float baseX, baseY;
    private final float currentX;
    private float currentY;
    private float offsetY = 0f;

    private float velocityY = 150f;
    private boolean falling = true;
    private boolean isLanded = false;

    private boolean hasExploded = false;
    private boolean isShowingExplosion = false;
    private float explosionTimer = 0f;
    private float explosionScale = 2.0f;

    private static final int DAMAGE = 2;
    private static final float EXPLOSION_RADIUS = 250f;
    private static final float GROUND_Y = 150f;
    private static final float EXPLOSION_DISPLAY_TIME = 0.3f;
    private static final float EXPLOSION_MAX_SCALE = 5.0f;

    public BombAsset(Texture tex, float dropX, float dropY) {
        this.tex = tex;
        this.baseX = dropX;
        this.baseY = dropY;
        this.currentX = dropX;
        this.currentY = dropY;

        float bombScale = 0.1f;
        this.scaledW = tex.getWidth() * bombScale;
        this.scaledH = tex.getHeight() * bombScale;

        this.bounds = new Rectangle(
            currentX - scaledW/2,
            currentY - scaledH/2,
            scaledW, scaledH
        );

        System.out.println("💣 Bomb created with INSTANT explosion on ground contact!");
    }

    public void update(float delta) {

        if (isShowingExplosion) {
            updateExplosion(delta);
            return;
        }

        if (hasExploded) {
            return;
        }


        if (falling && !isLanded) {
            float gravity = -500f;
            velocityY += gravity * delta;
            offsetY += velocityY * delta;

            float actualY = baseY + offsetY;
            if (actualY <= GROUND_Y) {

                offsetY = GROUND_Y - baseY;
                currentY = baseY + offsetY;
                falling = false;
                isLanded = true;

                triggerExplosion();

                return;
            }
        }

        currentY = baseY + offsetY;
        updateVisuals();
    }

    private void triggerExplosion() {
        isShowingExplosion = true;
        explosionTimer = 0f;
        explosionScale = 1.0f;

        AudioManager.playTrapExplosionHit();
        damageTowersInRadius();
    }


    private void damageTowersInRadius() {
    }

    private void updateExplosion(float delta) {
        explosionTimer += delta;
        float progress = explosionTimer / EXPLOSION_DISPLAY_TIME;

        if (progress <= 0.3f) {
            float expandProgress = progress / 0.3f;
            explosionScale = 1.0f + expandProgress;
        } else if (progress <= 0.7f) {
            explosionScale = EXPLOSION_MAX_SCALE;
        } else {
            float fadeProgress = (progress - 0.7f) / 0.3f;
            explosionScale = EXPLOSION_MAX_SCALE * (1.0f - fadeProgress);
        }

        if (explosionTimer >= EXPLOSION_DISPLAY_TIME) {
            hasExploded = true;
            isShowingExplosion = false;
        }
    }

    private void updateVisuals() {
        bounds.set(currentX - scaledW/2, currentY - scaledH/2, scaledW, scaledH);
    }

    public boolean shouldExplode() {
        return isLanded && !hasExploded && !isShowingExplosion;
    }


    public void explode(Array<Tower> towers) {
        if (hasExploded || isShowingExplosion) return;
        int hitCount = 0;

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

    public void drawBatch(SpriteBatch batch) {
        if (tex == null) return;

        if (isShowingExplosion) {
            drawExplosion(batch);
            return;
        }

        if (hasExploded) return;

        if (falling) {
            float altitude = currentY - GROUND_Y;
            float alpha = 0.7f + (0.3f * Math.max(0, altitude / 100f));
            batch.setColor(1f, 1f, 1f, alpha);
        } else {
            batch.setColor(1f, 1f, 1f, 1f);
        }

        batch.draw(tex, currentX - scaledW/2, currentY - scaledH/2, scaledW, scaledH);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawExplosion(SpriteBatch batch) {
        Texture explosionTex = getExplosionTexture();

        if (explosionTex != null) {
            float progress = explosionTimer / EXPLOSION_DISPLAY_TIME;
            if (progress <= 0.2f) {
                batch.setColor(1.3f, 1.3f, 1.3f, 1f);
            } else if (progress <= 0.5f) {
                batch.setColor(1f, 0.8f, 0.3f, 1f);
            } else {
                float alpha = 1f - ((progress - 0.5f) / 0.5f) * 0.7f;
                batch.setColor(1f, 0.4f, 0.1f, alpha);
            }

            float explosionW = scaledW * explosionScale;
            float explosionH = scaledH * explosionScale;
            batch.draw(explosionTex,
                currentX - explosionW/2,
                currentY - explosionH/2,
                explosionW, explosionH);
        } else {
            drawFallbackExplosion(batch);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private Texture getExplosionTexture() {
        if (ImageLoader.explosionTex != null) {
            return ImageLoader.explosionTex;
        }
        return tex;
    }

    private void drawFallbackExplosion(SpriteBatch batch) {
        float progress = explosionTimer / EXPLOSION_DISPLAY_TIME;

        for (int i = 0; i < 3; i++) {
            float layerScale = explosionScale * (1f + i * 0.4f);
            float layerW = scaledW * layerScale;
            float layerH = scaledH * layerScale;
            switch(i) {
                case 0:
                    batch.setColor(1.2f, 1.2f, 1.2f, 0.9f);
                    break;
                case 1:
                    batch.setColor(1f, 0.7f, 0.2f, 0.7f);
                    break;
                case 2:
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

    public float getX() { return currentX; }
    public float getY() { return currentY; }
    public boolean hasExploded() { return hasExploded; }
}
