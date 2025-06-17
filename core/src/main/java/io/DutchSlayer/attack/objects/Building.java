package io.DutchSlayer.attack.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.DutchSlayer.utils.Constant;

/**
 * Representasi visual bangunan statis pada latar penjajahan Belanda.
 */
public class Building {

    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final BuildingType type;

    public Building(float x, BuildingType type) {
        this.x = x;
        this.type = type;
        this.width = type.width;
        this.height = type.height;
        this.y = Constant.TERRAIN_HEIGHT - 30f; // Semua bangunan berdiri di atas tanah
    }

    public void render(SpriteBatch spriteBatch, float offsetX) {
        spriteBatch.draw(type.texture, x - offsetX, y, width, height);
    }


    // Getter opsional jika dibutuhkan untuk logika interaksi/collision
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

    public BuildingType getType() {
        return type;
    }
}
