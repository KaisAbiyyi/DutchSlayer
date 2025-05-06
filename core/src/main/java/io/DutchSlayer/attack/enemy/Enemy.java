package io.DutchSlayer.attack.enemy;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.utils.Constant;
import io.DutchSlayer.attack.player.Bullet;

public class Enemy {

    private float x, y;
    private final float width, height;
    private final float baseSpeed;

    private boolean isAlive = true;
    private int maxHealth = 3;
    private int currentHealth = maxHealth;

    private final Array<Bullet> bullets;
    private float fireCooldown = 1.5f;
    private float fireTimer = 0f;

    private enum State {PATROL, CHASE}

    private State currentState = State.PATROL;

    private float patrolMinX, patrolMaxX;
    private boolean movingRight = true;

    private final float awarenessRadius = Constant.SCREEN_WIDTH / 2f;
    private final float attackDistance = 500f;

    public Enemy(float spawnX, float spawnY) {
        this.x = spawnX;
        this.y = spawnY;
        this.width = Constant.PLAYER_WIDTH;
        this.height = Constant.PLAYER_HEIGHT;
        this.baseSpeed = Constant.PLAYER_SPEED * 0.4f;

        this.bullets = new Array<>();
        this.patrolMinX = Math.max(0, spawnX - 80);
        this.patrolMaxX = Math.min(Constant.MAP_WIDTH, spawnX + 80);
    }

    public void update(float delta, Vector2 playerPos, float leftBound, float rightBound) {
        if (!isAlive) return;

        float dx = playerPos.x - x;
        float distanceToPlayer = Math.abs(dx);

        currentState = (distanceToPlayer <= awarenessRadius) ? State.CHASE : State.PATROL;

        if (currentState == State.PATROL) {
            updatePatrol(delta);
        } else {
            updateChase(delta, playerPos, dx);
        }

        for (Bullet bullet : bullets) {
            bullet.update(delta, -Float.MAX_VALUE, Float.MAX_VALUE);
        }

        for (int i = bullets.size - 1; i >= 0; i--) {
            if (!bullets.get(i).isAlive()) bullets.removeIndex(i);
        }
    }

    private void updatePatrol(float delta) {
        float patrolSpeed = baseSpeed;

        if (movingRight) {
            x += patrolSpeed * delta;
            if (x >= patrolMaxX) {
                x = patrolMaxX;
                movingRight = false;
            }
        } else {
            x -= patrolSpeed * delta;
            if (x <= patrolMinX) {
                x = patrolMinX;
                movingRight = true;
            }
        }
    }

    private void updateChase(float delta, Vector2 playerPos, float dx) {
        float chaseSpeed = Constant.PLAYER_SPEED * 0.8f;

        if (Math.abs(dx) > attackDistance) {
            x += (dx < 0 ? -chaseSpeed : chaseSpeed) * delta;
        }

        fireTimer -= delta;
        if (fireTimer <= 0f) {
            fireAtPlayer(playerPos);
            fireTimer = fireCooldown;
        }
    }

    public void checkHitByExplosion(float explosionX, float explosionY, float radius, float damage) {
        if (!isAlive) return;

        float centerX = x + width / 2f;
        float centerY = y + height / 2f;

        float distanceSq = Vector2.dst2(explosionX, explosionY, centerX, centerY);
        float radiusSq = radius * radius;

        if (distanceSq <= radiusSq) {
            System.out.println("Enemy hit by explosion! Damage: " + damage); // Debug
            takeExplosionDamage(damage);
        }
    }

    private void takeExplosionDamage(float dmg) {
        if (!isAlive || dmg <= 0f) return;

        // Pastikan damage dikonversi ke int dengan benar
        int damage = Math.round(dmg);

        System.out.println("Enemy taking " + damage + " damage, current health: " + currentHealth); // Debug

        currentHealth -= damage;
        if (currentHealth <= 0) {
            currentHealth = 0;
            isAlive = false;
            System.out.println("Enemy killed by explosion!"); // Debug
        }
    }

    private void fireAtPlayer(Vector2 playerPos) {
        float centerX = x + width / 2f;
        float centerY = y + height / 2f;

        float dx = playerPos.x - centerX;
        float angle = dx > 0 ? 0f : (float) Math.PI;

        bullets.add(new Bullet(centerX, centerY, angle, true));
    }

    public void takeHit() {
        if (!isAlive) return;
        currentHealth--;
        if (currentHealth <= 0) {
            currentHealth = 0;
            isAlive = false;
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (!isAlive) return;

        float barY = y + height + 4f;
        shapeRenderer.setColor(0.5f, 0, 0, 1);
        shapeRenderer.rect(x, barY, width, 4f);
        shapeRenderer.setColor(1f, 0f, 0f, 1);
        shapeRenderer.rect(x, barY, width * ((float) currentHealth / maxHealth), 4f);

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

