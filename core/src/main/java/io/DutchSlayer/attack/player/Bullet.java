package io.DutchSlayer.attack.player;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.DutchSlayer.utils.Constant;

public class Bullet {

    private float x;
    private float y;
    private float vx;
    private float vy;

    private final float width = Constant.BULLET_WIDTH;
    private final float height = Constant.BULLET_HEIGHT;

    private boolean isAlive = true;

    private final boolean fromEnemy;

    public Bullet(float startX, float startY, float angleRad, boolean fromEnemy) {
        this.x = startX;
        this.y = startY;
        this.vx = (float) Math.cos(angleRad) * Constant.BULLET_SPEED;
        this.vy = (float) Math.sin(angleRad) * Constant.BULLET_SPEED;
        this.fromEnemy = fromEnemy;
    }

    public void update(float delta, float camLeft, float camRight) {
        x += vx * delta;
        y += vy * delta;

        if (!fromEnemy) {
            // Bullet player: hanya aktif selama masih di dalam viewport kamera
            if (x + width < camLeft || x > camRight ||
                y < -height || y > Constant.SCREEN_HEIGHT + height) {
                isAlive = false;
            }
        } else {
            // Bullet enemy: hidup selama masih di dalam map
            if (x < -width || x > Constant.MAP_WIDTH + width ||
                y < -height || y > Constant.SCREEN_HEIGHT + height) {
                isAlive = false;
            }
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(1, 0, 0, 1); // merah
        shapeRenderer.rect(x, y, width, height);
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void kill() {
        isAlive = false;
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
