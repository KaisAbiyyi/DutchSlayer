package io.DutchSlayer.attack.player.weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import io.DutchSlayer.utils.Constant;

public class Grenade {

    private final transient Texture grenadeTexture;
    private final transient Texture explosionTexture;
    private final Sound explosionSound;
    private final Sound groundHitSound;

    private float x;
    private float y;
    private float vx;
    private float vy;

    private final float radius = Constant.GRENADE_RADIUS * 1.5f;
    private final float gravity = -1500f;

    private float timer;
    private float postExplodeTimer = 0f;
    private long groundHitSoundId = -1;

    private boolean exploded = false;
    private boolean isAlive = true;
    private boolean hasDealtDamage = false;
    private boolean hasTouchedGround = false;
    private final boolean isEnemyGrenade;

    private Float impactY = null;

    public Grenade(float startX, float startY, float angleRad, float power, boolean isEnemyGrenade, Texture grenadeTexture, Texture explosionTexture) { // Parameter Sound dihapus
        this.x = startX;
        this.y = startY;
        this.vx = MathUtils.cos(angleRad) * power;
        this.vy = MathUtils.sin(angleRad) * power;
        float explosionDelay = Constant.GRENADE_TIMER;
        this.timer = explosionDelay;
        this.isEnemyGrenade = isEnemyGrenade;
        this.grenadeTexture = grenadeTexture;
        this.explosionTexture = explosionTexture;

        this.explosionSound = Gdx.audio.newSound(Gdx.files.internal("player/grenade.mp3"));
        this.groundHitSound = Gdx.audio.newSound(Gdx.files.internal("player/grenade_ground.mp3"));
    }

    public boolean isEnemyGrenade() {
        return isEnemyGrenade;
    }

    public void update(float delta) {
        if (!exploded) {
            vy += gravity * delta;

            x += vx * delta;
            y += vy * delta;

            if (y <= Constant.TERRAIN_HEIGHT) {
                y = Constant.TERRAIN_HEIGHT;

                if (!hasTouchedGround) {
                    hasTouchedGround = true;
                    vy = 0f;
                    if (groundHitSound != null) {
                        groundHitSound.play(0.5f);
                    }
                }

                vx *= 0.9f;
                if (Math.abs(vx) < 5f) vx = 0f;
            }

            timer -= delta;
            if (timer <= 0f) {
                explode();
            }
        } else {
            postExplodeTimer -= delta;
            if (postExplodeTimer <= 0f) {
                isAlive = false;
            }
        }
    }

    public void render(SpriteBatch spriteBatch) {
        if (!isAlive) return;

        if (!exploded) {
            if (grenadeTexture != null) {
                float width = 24f;
                float height = 24f;
                spriteBatch.draw(grenadeTexture, x - width / 2f, y, width, height);
            }
        } else {
            if (explosionTexture != null) {
                float explosionDiameter = radius * 2;
                float renderX = x - radius;
                float renderY;

                if (impactY != null) {
                    renderY = Math.max(impactY, Constant.TERRAIN_HEIGHT) - 20f;
                    renderX += 20f;
                } else if (hasTouchedGround) {
                    renderY = Constant.TERRAIN_HEIGHT;
                } else {
                    renderY = y - radius;
                }

                spriteBatch.draw(explosionTexture, renderX, renderY, explosionDiameter, explosionDiameter);
            }
        }
    }

    public void forceExplode(float impactY) {
        if (!exploded) {
            this.impactY = impactY;
            explode();
        }
    }

    private void explode() {
        exploded = true;
        timer = 0f;
        float explosionDuration = 0.4f;
        postExplodeTimer = explosionDuration;

        if (groundHitSound != null && groundHitSoundId != -1) {
            groundHitSound.stop(groundHitSoundId);
            groundHitSoundId = -1;
        }

        if (explosionSound != null) {
            explosionSound.play(0.7f);
        }
    }

    public boolean isFinished() {
        return exploded && postExplodeTimer <= 0f;
    }

    public boolean isExploded() {
        return exploded;
    }

    public boolean shouldDealDamage() {
        return exploded && !hasDealtDamage;
    }

    public void markDamageDealt() {
        hasDealtDamage = true;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getDamage() {
        int damage = 25;
        return damage;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void dispose() {
        if (explosionSound != null) {
            explosionSound.dispose();
        }
    }

    public float getRadius() {
        return radius;
    }
}
