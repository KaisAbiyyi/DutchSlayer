package io.DutchSlayer.attack.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import io.DutchSlayer.utils.Constant;

/**
 * Tree adalah objek statis dekoratif di environment.
 * Dapat digenerate secara random maupun deterministik.
 */
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
        float width = 200f;   // fixed size
        float height = 350f; // fixed size
        float x = rng.nextFloat() * (mapWidth - width);
        float y = Constant.TERRAIN_HEIGHT + 8f;
        return new Tree(x, y, width, height, texture);
    }


    /**
     * Deteksi jika dua pohon saling tumpang tindih horizontal.
     */
    public boolean overlaps(Tree other) {
        return this.x < other.x + other.width &&
            this.x + this.width > other.x;
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
