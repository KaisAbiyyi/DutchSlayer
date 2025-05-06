package io.DutchSlayer.attack.player;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.DutchSlayer.utils.Constant;

public class Grenade {

    private float x;
    private float y;
    private float vx;
    private float vy;

    // Perbesar radius ledakan untuk memastikan mengenai musuh
    private final float radius = Constant.GRENADE_RADIUS * 1.5f;
    private final float gravity = -1500f;
    private final float explosionDelay = Constant.GRENADE_TIMER;
    private final float explosionDuration = 0.4f;

    private float timer;
    private float postExplodeTimer = 0f;

    // Tingkatkan damage untuk memastikan musuh menerima damage yang cukup
    private final int damage = 3;
    private boolean exploded = false;
    private boolean isAlive = true;
    private boolean hasDealtDamage = false;
    private boolean hasTouchedGround = false;

    public Grenade(float startX, float startY, float angleRad, float power) {
        this.x = startX;
        this.y = startY;

        this.vx = MathUtils.cos(angleRad) * power;
        this.vy = MathUtils.sin(angleRad) * power;

        this.timer = explosionDelay;
    }

    public void update(float delta) {
        if (!exploded) {
            // Efek gravitasi
            vy += gravity * delta;

            // Update posisi
            x += vx * delta;
            y += vy * delta;

            // Jika menyentuh tanah
            if (y <= Constant.TERRAIN_HEIGHT) {
                y = Constant.TERRAIN_HEIGHT;

                if (!hasTouchedGround) {
                    hasTouchedGround = true;
                    vy = 0f;
                }

                vx *= 0.9f; // Sliding/friksi
                if (Math.abs(vx) < 5f) vx = 0f;
            }

            // Hitung mundur timer ledakan
            timer -= delta;
            if (timer <= 0f) {
                explode();
            }
        } else {
            // Setelah meledak
            postExplodeTimer -= delta;
            if (postExplodeTimer <= 0f) {
                isAlive = false;
            }
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (!isAlive) return;

        if (!exploded) {
            shapeRenderer.setColor(0.3f, 0.8f, 0.3f, 1); // Warna hijau sebelum meledak
            shapeRenderer.circle(x, y + 5, 6); // Posisi sedikit naik biar jelas
        } else {
            shapeRenderer.setColor(1, 0.5f, 0f, 0.6f); // Warna oranye transparan saat meledak
            shapeRenderer.circle(x, y, radius);
        }
    }

    public void forceExplode() {
        if (!exploded) {
            System.out.println("Forcing grenade to explode!"); // Debug
            explode();
        }
    }

    private void explode() {
        exploded = true;
        timer = 0f;
        postExplodeTimer = explosionDuration;
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
        return damage;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRadius() {
        return radius;
    }
}
