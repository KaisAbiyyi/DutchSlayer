package io.DutchSlayer.attack.player.weapon;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.DutchSlayer.utils.Constant;

public class Bullet {

    private float x;
    private float y;
    private final float vx;
    private final float vy;

    private final float width = Constant.BULLET_WIDTH;
    private final float height = Constant.BULLET_HEIGHT;

    private boolean isAlive = true;

    private final boolean fromEnemy;
    private Texture texture;
    private TextureRegion textureRegion;

    public void setTextureRegion(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
    }


    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Bullet(float startX, float startY, float angleRad, boolean fromEnemy) {
        this.x = startX;
        this.y = startY;
        this.vx = (float) Math.cos(angleRad) * (fromEnemy ? Constant.BULLET_SPEED - 300f : Constant.BULLET_SPEED);
        this.vy = (float) Math.sin(angleRad) * (fromEnemy ? Constant.BULLET_SPEED - 300f : Constant.BULLET_SPEED);
        this.fromEnemy = fromEnemy;
    }

    public void update(float delta, float camLeft, float camRight) {
        x += vx * delta;
        y += vy * delta;

        if (!fromEnemy) {
            if (x + width < camLeft || x > camRight ||
                y < -height || y > Constant.SCREEN_HEIGHT + height) {
                isAlive = false;
            }
        } else {
            if (x < -width || x > Constant.MAP_WIDTH + width ||
                y < -height || y > Constant.SCREEN_HEIGHT + height) {
                isAlive = false;
            }
        }

        if (isOutOfBounds(camLeft, camRight)) {
            isAlive = false;
        }
    }

    private boolean isOutOfBounds(float camLeft, float camRight) {
        if (!fromEnemy) {
            return x + width < camLeft || x > camRight ||
                y < -height || y > Constant.SCREEN_HEIGHT + height;
        } else {
            return x < -width || x > Constant.MAP_WIDTH + width ||
                y < -height || y > Constant.SCREEN_HEIGHT + height;
        }
    }

    public void render(SpriteBatch spriteBatch) {
        if (!isAlive) return;

        if (textureRegion != null) {
            spriteBatch.draw(textureRegion, x, y, width, height);
        } else if (texture != null) {
            spriteBatch.draw(texture, x, y, width, height);
        }
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
