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
//    private float speed = 100f;  // piksel per detik
    private final float baseSpeed = 100f;
    private float currentSpeed = baseSpeed;
    private float slowTimer   = 0f;       // waktu tersisa slow

    private int health;     // HP

    public Enemy(Texture tex, float xCenter, float yCenter) {
        this.tex    = tex;
        this.health = 3;
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

    public void takeDamage(int dmg) {
        health = Math.max(0, health - dmg);
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    /** Panggil untuk menerapkan slow pada musuh */
    public void slow(float duration) {
        slowTimer = duration;
    }

    public void update(float delta) {
        // kelola slow
        if (slowTimer > 0f) {
            slowTimer -= delta;
            currentSpeed = baseSpeed * 0.5f;   // misal 50% speed
        } else {
            currentSpeed = baseSpeed;
        }

        pos.x -= currentSpeed * delta;
        bounds.setPosition(pos.x - bounds.width/2, pos.y - bounds.height/2);
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

    public int getHealth() {
        return health;
    }
}
