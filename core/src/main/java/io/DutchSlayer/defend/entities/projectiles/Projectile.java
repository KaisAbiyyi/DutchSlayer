package io.DutchSlayer.defend.entities.projectiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.enemies.Enemy;

public class Projectile {
    public final Texture tex;
    private final float scaledW;
    private final float scaledH;
    public final Rectangle bounds;

    private final Vector2 pos;
    private final Vector2 vel;
    private final float halfWidth;
    private final float halfHeight;

    protected float speed;
    protected int damage;

    private boolean active = true;

    public Projectile(Texture tex, float startX, float startY, float targetX, float scale, float customSpeed, int damage) {
        this.tex = tex;
        this.speed = customSpeed;
        this.damage = damage;
        this.pos = new Vector2(startX, startY);

        this.scaledW = tex.getWidth() * scale;
        this.scaledH = tex.getHeight() * scale;

        this.halfWidth = scaledW / 2f;
        this.halfHeight = scaledH / 2f;

        float direction = Math.signum(targetX - startX);
        this.vel = new Vector2(direction * speed, 0f);

        this.bounds = new Rectangle(
            pos.x - halfWidth,
            pos.y - halfHeight,
            scaledW,
            scaledH
        );
    }

    public void update(float delta) {
        if (!active) return;

        pos.mulAdd(vel, delta);

        bounds.x = pos.x - halfWidth;
        bounds.y = pos.y - halfHeight;
    }

    public void drawBatch(SpriteBatch batch) {
        if (!active || tex == null) return;

        batch.draw(
            tex,
            pos.x - halfWidth,
            pos.y - halfHeight,
            scaledW,
            scaledH
        );
    }

    public void drawShape(ShapeRenderer shapes) {
        if (!active) return;

        if (tex == null) {
            shapes.setColor(Color.YELLOW);
            shapes.circle(pos.x, pos.y, halfWidth);
        }
    }

    public void onHit(Array<Enemy> enemies) {
        if (!active) return;

        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            if (e.isDestroyed()) continue;

            if (bounds.overlaps(e.getBounds())) {
                e.takeDamage(this.damage);
                this.active = false;
                return;
            }
        }
    }

    public void reset(float startX, float startY, float targetX, float customSpeed, int damage) {
        this.pos.set(startX, startY);
        this.speed = customSpeed;
        this.damage = damage;
        this.active = true;

        float direction = Math.signum(targetX - startX);
        this.vel.set(direction * speed, 0f);

        bounds.setPosition(pos.x - halfWidth, pos.y - halfHeight);
    }

    public float getX() { return pos.x; }
    public float getY() { return pos.y; }
    public Rectangle getBounds() { return bounds; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getDamage() { return damage; }
}
