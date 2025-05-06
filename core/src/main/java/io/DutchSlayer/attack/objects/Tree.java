package io.DutchSlayer.attack.objects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.DutchSlayer.utils.Constant;

public class Tree {

    private final float x;
    private final float y;
    private final float width;
    private final float height;

    public Tree(float x, float width, float height) {
        this.x = x;
        this.width = width;
        this.height = height;
        this.y = Constant.TERRAIN_HEIGHT + 8f;
    }

    public static Tree generateRandom(float mapWidth) {
        float[] possibleHeights = {
            Constant.TREE1_HEIGHT,
            Constant.TREE2_HEIGHT,
            Constant.TREE3_HEIGHT,
            Constant.TREE4_HEIGHT
        };

        float height = possibleHeights[MathUtils.random(possibleHeights.length - 1)];
        float width = height * 0.6f; // asumsi rasio w/h default

        float x = MathUtils.random(Constant.WALL_WIDTH + 100, mapWidth - width);

        return new Tree(x, width, height);
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0.1f, 0.5f, 0.2f, 1f); // hijau daun
        shapeRenderer.rect(x, y, width, height);
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
