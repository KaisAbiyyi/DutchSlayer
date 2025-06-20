package io.DutchSlayer.attack.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.RandomXS128;
import io.DutchSlayer.utils.Constant;

public class Tree {

    private final float x;
    private final float y;
    private float width;
    private float height;
    private final Texture texture;

    public Tree(float x, float y, float width, float height, Texture texture) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.texture = texture;
    }

    public void render(SpriteBatch batch, float offsetX) {
        batch.draw(texture, x - offsetX, y, width, height);
    }

    public void setSize(float newWidth, float newHeight) {
        this.width = newWidth;
        this.height = newHeight;
    }

    public static Tree generateFixed(float mapWidth, RandomXS128 rng, Texture texture) {
        float width = 200f;
        float height = 350f;
        float x = rng.nextFloat() * (mapWidth - width);
        float y = Constant.TERRAIN_HEIGHT + 8f;
        return new Tree(x, y, width, height, texture);
    }

    public float getX() {
        return x;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
