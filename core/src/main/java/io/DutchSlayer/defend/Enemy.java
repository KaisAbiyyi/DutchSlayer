package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy {
    public static final float SCALE = 0.2f;           // bisa diakses luar
    private final Texture tex;
    private final Vector2 pos;
    private final Rectangle bounds;
    private final float scaledWidth;
    private final float scaledHeight;
    private float speed = 100f;  // piksel per detik

    public Enemy(Texture tex, float xCenter, float yCenter) {
        this.tex    = tex;
        // hitung ukuran ter‐skala
        this.scaledWidth  = tex.getWidth() * SCALE;
        this.scaledHeight = tex.getHeight() * SCALE;
        // simpan posisi sebagai center
        this.pos = new Vector2(xCenter, yCenter);
        this.bounds = new Rectangle(
            xCenter - scaledWidth/2,
            yCenter - scaledHeight/2,
            scaledWidth,
            scaledHeight
        );
    }

    public void update(float delta) {
        pos.x -= speed * delta;
        bounds.setPosition(pos.x - scaledWidth/2, pos.y - scaledHeight/2);
    }

    public void drawBatch(SpriteBatch batch) {
        if (tex != null) {
            batch.draw(
                tex,
                pos.x - scaledWidth/2,
                pos.y - scaledHeight/2,
                scaledWidth,
                scaledHeight
            );
        }
    }

    public void drawShape(ShapeRenderer shapes) {
        if (tex == null) {
            shapes.setColor(Color.RED);
            float radius = scaledWidth/2f;
            shapes.circle(pos.x, pos.y, radius);
        }
    }

    public float getX()     { return pos.x; }
    public float getWidth() { return scaledWidth; }
    public Rectangle getBounds() { return bounds; }
}
