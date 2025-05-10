package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Projectile {
    private final Texture tex;
    private final Vector2 pos, vel;
    private final float scaledW, scaledH;
    private final Rectangle bounds;
    private final float speed = 400f;
    private static final float SCALE = 0.2f;

    public Projectile(Texture tex, float startX, float startY, float targetX, float targetY) {
        this.tex = tex;
        this.pos = new Vector2(startX, startY);
        // ukuran ter‐skala
        this.scaledW = tex.getWidth() * SCALE;
        this.scaledH = tex.getHeight() * SCALE;
        // arah ke target
        Vector2 dir = new Vector2(targetX - startX, targetY - startY).nor();
        this.vel = dir.scl(speed);
        // bounds berpusat
        this.bounds = new Rectangle(
            pos.x - scaledW/2,
            pos.y - scaledH/2,
            scaledW,
            scaledH
        );
    }

    public void update(float delta) {
        pos.mulAdd(vel, delta);
        // update posisi bounds agar collision tepat
        bounds.setPosition(pos.x - scaledW/2, pos.y - scaledH/2);
    }

    public void drawBatch(SpriteBatch batch) {
        if (tex != null) {
            batch.draw(
                tex,
                pos.x - scaledW/2,
                pos.y - scaledH/2,
                scaledW,
                scaledH
            );
        }
    }

    public void drawShape(ShapeRenderer shapes) {
        if (tex == null) {
            shapes.setColor(Color.YELLOW);
            shapes.circle(pos.x, pos.y, scaledW/2);
        }
    }

    public float getX() {
        return pos.x;
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
