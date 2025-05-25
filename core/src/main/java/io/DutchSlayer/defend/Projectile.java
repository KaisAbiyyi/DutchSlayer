package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Projectile {
    private final Texture tex;
    private final Vector2 pos;
    private final Vector2 vel;
    private final float scaledW;
    private final float scaledH;
    private final Rectangle bounds;
    protected float speed = 400f;
    protected int damage = 1;

    public Projectile(Texture tex, float startX, float startY, float targetX, float targetY, float scale, float customSpeed, int damage) {
        this.tex = tex;
        this.speed = customSpeed;
        this.pos = new Vector2(startX, startY);

        // ukuran ter‐skala
        this.scaledW = tex.getWidth() * scale;
        this.scaledH = tex.getHeight() * scale;
        // arah ke target
        float direction = Math.signum(targetX - startX); // +1 kalau ke kanan, -1 kalau ke kiri
        Vector2 dir = new Vector2(direction, 0f); // nol di sumbu Y
        this.vel = dir.scl(speed);
        // bounds berpusat
        this.bounds = new Rectangle(
            pos.x - scaledW/2,
            pos.y - scaledH/2,
            scaledW,
            scaledH
        );
    }

    // Constructor dengan speed kustom
    public Projectile(Texture tex, float startX, float startY, float targetX, float targetY, float scale, float customSpeed) {
        this(tex, startX, startY, targetX, targetY, scale, customSpeed, 1);
    }

    // Constructor dengan damage kustom
    public Projectile(Texture tex, float startX, float startY, float targetX, float targetY, float scale, int damage) {
        this(tex, startX, startY, targetX, targetY, scale, 400f, damage);
    }

    // Constructor default
    public Projectile(Texture tex, float startX, float startY, float targetX, float targetY, float scale) {
        this(tex, startX, startY, targetX, targetY, scale, 400f, 1);
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

    public void onHit(Array<Enemy> enemies) {
        // default: beri damage ke 1 musuh saja
        for (Enemy e : enemies) {
            if (getBounds().overlaps(e.getBounds())) {
                e.takeDamage(1);
                System.out.println("Basic projectile hit! Damage: " + damage);
                break;
            }
        }
    }

    public float getX() {
        return pos.x;
    }

    public float getY() {return pos.y;}

    public Rectangle getBounds() {
        return bounds;
    }
}
