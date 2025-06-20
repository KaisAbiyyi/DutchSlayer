package io.DutchSlayer.attack.boss;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.DutchSlayer.utils.Constant;

public class BossGrenade {

    private final Vector2 position;
    private final Vector2 velocity;
    private final float height = 24f;
    private boolean isAlive = true;
    private boolean exploded = false;
    private boolean damagePhaseActive = false;

    private float fuseTime = 2.5f;
    private final float explosionRadius = 80f;

    private float explosionVisualTimer = 0f;

    private final float groundY = Constant.TERRAIN_HEIGHT;

    private final Texture grenadeProjectileTexture;
    private final Texture explosionTexture;
    private final Sound explosionSound;
    private final Vector2 explosionPosition;

    public BossGrenade(float startX, float startY, float initialVx, float initialVy, Texture grenadeProjectileTexture, Texture explosionTexture, Sound explosionSound) {
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(initialVx, initialVy);
        this.grenadeProjectileTexture = grenadeProjectileTexture;
        this.explosionTexture = explosionTexture;
        this.explosionSound = explosionSound;
        this.explosionPosition = new Vector2();
    }

    public void update(float delta) {
        if (!isAlive) return;

        if (!exploded) {
            float gravity = 700f;
            velocity.y -= gravity * delta;
            position.x += velocity.x * delta;
            position.y += velocity.y * delta;
            fuseTime -= delta;

            if (position.y - height / 2f <= groundY && velocity.y < 0) {
                position.y = groundY + height / 2f;
                velocity.y = 0;
                velocity.x *= 0.5f;
            }

            if (fuseTime <= 0f) {
                explode();
            }
        } else {
            explosionVisualTimer -= delta;
            if (explosionVisualTimer <= 0f) {
                isAlive = false;
            }
        }
    }

    private void explode() {
        if (exploded) return;
        exploded = true;
        damagePhaseActive = true;
        float explosionVisualDuration = 0.4f;
        explosionVisualTimer = explosionVisualDuration;

        this.explosionPosition.set(this.position.x, this.groundY);

        if (explosionSound != null) {
            explosionSound.play(0.6f);
        }
        System.out.println("Grenade exploded at ground level: " + explosionPosition.x + ", " + explosionPosition.y);
    }
    public void render(SpriteBatch batch) {
        if (!isAlive) return;

        if (!exploded) {
            if (grenadeProjectileTexture != null) {
                float width = 24f;
                batch.draw(grenadeProjectileTexture, position.x - width / 2f, position.y - height / 2f, width, height);
            }
        } else {
            if (explosionTexture != null) {
                float explosionDiameter = explosionRadius * 2f;

                batch.draw(explosionTexture,
                    explosionPosition.x - explosionDiameter / 2f,
                    explosionPosition.y,
                    explosionDiameter, explosionDiameter);
            }
        }
    }

    public boolean isAlive() { return isAlive; }
    public boolean hasExploded() { return exploded; }
    public boolean isDamagePhase() { return exploded && damagePhaseActive; }
    public void markDamagePhaseDone() { damagePhaseActive = false; }

    public float getX() { return exploded ? explosionPosition.x : position.x; }
    public float getY() { return exploded ? explosionPosition.y : position.y; }

    public float getExplosionRadius() { return explosionRadius; }
    public float getDamage() {
        float damage = 30f;
        return damage; }
}
