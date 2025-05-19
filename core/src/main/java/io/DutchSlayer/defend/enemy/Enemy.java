package io.DutchSlayer.defend.enemy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Enemy {
    private float x, y;
    private float speed = 50;
    private Texture texture;
    private boolean dead = false;

    public Enemy(float x, float y) {
        this.x = x;
        this.y = y;
        texture = new Texture("enemy.png"); // pastikan file ini ada di folder assets
    }

    public void update(float delta) {
        x += speed * delta;

        if (x > 800) { // misal tujuan akhir di x=800
            dead = true;
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y);
    }

    public boolean isDead() {
        return dead;
    }

    public boolean hasReachedEnd() {
        return x >= 800;
    }

    public void dispose() {

    }

    public void markDead() {
    }

    public boolean reachedBase() {
        return false;
    }

    public void setDead(boolean b) {
    }

    public boolean isAlive() {
        return false;
    }
}
