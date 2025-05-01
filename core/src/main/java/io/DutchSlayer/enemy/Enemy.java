package io.DutchSlayer.enemy;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.utils.Constant;
import io.DutchSlayer.player.Bullet;

public class Enemy {

    private float x, y;
    private float width, height;
    private float speed;

    private boolean isAlive = true;

    private int maxHealth = 3;
    private int currentHealth = maxHealth;

    private final Array<Bullet> bullets;
    private float fireCooldown = 1.5f;
    private float fireTimer = 0;

    private float moveDelay = 0f; // waktu jeda sebelum bisa mendekat lagi
    private boolean isTooFar = false;

    public Enemy(float spawnX, float spawnY) {
        this.x = spawnX;
        this.y = spawnY;
        this.width = Constant.PLAYER_WIDTH;
        this.height = Constant.PLAYER_HEIGHT;
        this.speed = Constant.PLAYER_SPEED;
        this.bullets = new Array<>();
    }

    public void update(float delta, Vector2 playerPos, float leftBound, float rightBound) {
        if (!isAlive) return;

        float followDistance = Constant.PLAYER_WIDTH * 25f;
        float distanceToPlayer = playerPos.x - x;

        // Cek apakah player menjauh setelah sebelumnya sudah dekat
        if (Math.abs(distanceToPlayer) > followDistance) {
            if (!isTooFar) {
                isTooFar = true;
                moveDelay = MathUtils.random(0.4f, 1.2f);
            }
        } else {
            isTooFar = false;
        }

        if (moveDelay > 0) {
            moveDelay -= delta;
        }

        if (x >= leftBound && x <= rightBound) {
            if (Math.abs(distanceToPlayer) > followDistance && moveDelay <= 0) {
                if (distanceToPlayer < 0) {
                    x -= speed * delta;
                } else {
                    x += speed * delta;
                }
            }

            // Fire cooldown
            fireTimer -= delta;
            if (fireTimer <= 0) {
                fireAtPlayer(playerPos);
                fireTimer = fireCooldown;
            }
        }

        // Update bullets
        for (Bullet bullet : bullets) {
            bullet.update(delta);
        }
        for (int i = bullets.size - 1; i >= 0; i--) {
            if (!bullets.get(i).isAlive()) {
                bullets.removeIndex(i);
            }
        }
    }

    private void fireAtPlayer(Vector2 playerPos) {
        float centerX = x + width / 2;
        float centerY = y + height * 0.65f; // default fire height

        float dx = playerPos.x - centerX;
        float dy = playerPos.y - centerY;

        float angle;

        if (Math.abs(dx) > Math.abs(dy)) {
            angle = dx > 0 ? 0f : (float) Math.PI;
        } else {
            angle = dy > 0 ? (float) Math.PI / 2 : (float) -Math.PI / 2;
        }

        if (angle == -Math.PI / 2) {
            centerY = y + height * 0.25f;
        }

        bullets.add(new Bullet(centerX, centerY, angle, true)); // true = bullet dari enemy

    }

    public void takeHit() {
        currentHealth--;
        if (currentHealth <= 0) {
            isAlive = false;
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (!isAlive) return;

        float barWidth = width;
        float barHeight = 4f;
        float barX = x;
        float barY = y + height + 4f;

        shapeRenderer.setColor(0.5f, 0, 0, 1);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        shapeRenderer.setColor(1f, 0f, 0f, 1);
        float healthRatio = (float) currentHealth / maxHealth;
        shapeRenderer.rect(barX, barY, barWidth * healthRatio, barHeight);

        shapeRenderer.setColor(1f, 0.2f, 0.2f, 1);
        shapeRenderer.rect(x, y, width, height);

        for (Bullet bullet : bullets) {
            bullet.render(shapeRenderer);
        }
    }

    public boolean isAlive() {
        return isAlive;
    }

    public Array<Bullet> getBullets() {
        return bullets;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
