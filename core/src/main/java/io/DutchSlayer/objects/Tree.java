package io.DutchSlayer.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import io.DutchSlayer.assets.AssetLoader;
import io.DutchSlayer.utils.Constant;

public class Tree {

    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final Texture texture;

    public Tree(float x, float width, float height, Texture texture) {
        this.x = x;
        this.width = width;
        this.height = height;
        this.y = Constant.TERRAIN_HEIGHT + 8f;
        this.texture = texture;
    }

    public static Tree generateRandom(float mapWidth) {
        Texture texture = AssetLoader.getRandomTreeTexture();

        float height;
        if (texture == null) {
            height = Constant.TREE1_HEIGHT; // fallback
        } else if (texture.toString().contains("tree1")) {
            height = Constant.TREE1_HEIGHT;
        } else if (texture.toString().contains("tree2")) {
            height = Constant.TREE2_HEIGHT;
        } else if (texture.toString().contains("tree3")) {
            height = Constant.TREE3_HEIGHT;
        } else if (texture.toString().contains("tree4")) {
            height = Constant.TREE4_HEIGHT;
        } else {
            height = Constant.TREE1_HEIGHT;
        }

        float textureRatio = (float) texture.getWidth() / texture.getHeight();
        float width = height * textureRatio;

        float x = MathUtils.random(Constant.WALL_WIDTH + 100, mapWidth - width);

        return new Tree(x, width, height, texture);
    }

    public void render(SpriteBatch batch, boolean drawShadow) {
        if (drawShadow) {
            batch.setColor(0f, 0f, 0f, 0.3f);
            batch.draw(texture, x + 4f, y - 4f, width, height);
            batch.setColor(Color.WHITE);
        }
        batch.draw(texture, x, y, width, height);
    }

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
}
