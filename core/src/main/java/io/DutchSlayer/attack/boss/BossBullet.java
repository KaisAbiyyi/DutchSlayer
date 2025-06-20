package io.DutchSlayer.attack.boss;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.DutchSlayer.utils.Constant;

public class BossBullet {

    private float x;
    private final float y;
    private final float width = 40f;
    private final float height = 40f;
    private boolean isAlive = true;
    private final float vx;
    private final Texture bulletFullTexture;
    private final TextureRegion bulletRegion;

    public BossBullet(float startX, float startY, float directionX, boolean bossFacingRight) {
        this.x = startX;
        this.y = startY;
        this.vx = 400f * directionX;

        this.bulletFullTexture = new Texture("boss/boss_bullet.png");
        this.bulletRegion = new TextureRegion(bulletFullTexture);

        if (bossFacingRight && !bulletRegion.isFlipX()) {
            bulletRegion.flip(true, false);
        }
        else if (!bossFacingRight && bulletRegion.isFlipX()) {
            bulletRegion.flip(true, false);
        }
    }

    public void update(float delta) {
        x += vx * delta;
        if (x + width < 0 || x > Constant.MAP_WIDTH) {
            isAlive = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!isAlive) return;
        batch.draw(bulletRegion, x, y, width, height);
    }

    public void render(ShapeRenderer renderer) {
        if (!isAlive) return;
        renderer.rect(x, y, width, height);
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

    public void dispose() {
        if (bulletFullTexture != null) {
            bulletFullTexture.dispose();
        }
    }
}
