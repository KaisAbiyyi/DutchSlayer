package io.DutchSlayer.attack.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.utils.Constant;
import io.DutchSlayer.attack.player.Grenade;
import io.DutchSlayer.attack.screens.GameScreen;

public class Player {

    private float x;
    private float y;
    private float vx;
    private float vy;
    private int lives = 5;
    private boolean isDead = false;

    private boolean isJumping = false;
    private boolean facingRight = true;
    private OrthographicCamera camera;
    private final float width = Constant.PLAYER_WIDTH;
    private final float height = Constant.PLAYER_HEIGHT;

    private final float gravity = -2000f;
    private final float jumpForce = 660f;

    private final Array<Bullet> bullets;

    // === Tambahan: Fire Delay ===
    private float fireCooldown = 0.25f; // detik antar tembakan
    private float fireTimer = 0f;

    // Hapus array lokal grenades dan gunakan referensi ke GameScreen
    private GameScreen gameScreen;

    private float grenadeCooldown = 1.0f;
    private float grenadeTimer = 0f;


    public Player(OrthographicCamera camera) {
        this.camera = camera;
        this.x = Constant.PLAYER_START_X;
        this.y = Constant.TERRAIN_HEIGHT;
        this.bullets = new Array<>();
    }

    // Tambahkan setter untuk GameScreen
    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    public void update(float delta) {
        float speed = Constant.PLAYER_SPEED;

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            speed *= 2.0f;
        }

        vx = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            vx = -speed;
            facingRight = false;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            vx = speed;
            facingRight = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isJumping) {
            vy = jumpForce;
            isJumping = true;
        }

        // Timer granat
        if (grenadeTimer > 0f) {
            grenadeTimer -= delta;
        }

        // Lempar granat dengan tombol G
        if (Gdx.input.isKeyJustPressed(Input.Keys.K) && grenadeTimer <= 0f) {
            throwGrenade();
            grenadeTimer = grenadeCooldown;
        }

        vy += gravity * delta;
        x += vx * delta;
        y += vy * delta;

        if (y <= Constant.TERRAIN_HEIGHT) {
            y = Constant.TERRAIN_HEIGHT;
            vy = 0;
            isJumping = false;
        }

        x = MathUtils.clamp(x, Constant.WALL_WIDTH, Constant.MAP_WIDTH - width);

        if (fireTimer > 0f) {
            fireTimer -= delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.J) && fireTimer <= 0f) {
            fireCardinal();
            fireTimer = fireCooldown;
        }

        // Ambil posisi viewport kamera saat ini
        float camLeft = camera.position.x - Constant.SCREEN_WIDTH / 2f;
        float camRight = camera.position.x + Constant.SCREEN_WIDTH / 2f;

        for (Bullet bullet : bullets) {
            bullet.update(delta, camLeft, camRight);
        }

        for (int i = bullets.size - 1; i >= 0; i--) {
            if (!bullets.get(i).isAlive()) {
                bullets.removeIndex(i);
            }
        }

        if (isDead) return;
    }

    private void throwGrenade() {
        float centerX = x + width / 2;
        float centerY = y + height * 0.5f;

        float angle = facingRight ? 0.5f : (float) Math.PI - 0.5f; // Lemparan setengah diagonal
        float power = 800f;

        // Tambahkan granat ke GameScreen, bukan ke array lokal
        if (gameScreen != null) {
            gameScreen.getGrenades().add(new Grenade(centerX, centerY, angle, power));
        }
    }

    private void fireCardinal() {
        float centerX = x + width / 2;
        float fireY = y + height * 0.65f;

        float angle = 0f;
        boolean hasInput = false;

        boolean keyLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean keyRight = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean keyUp = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean keyDown = Gdx.input.isKeyPressed(Input.Keys.S);

        if (keyUp) {
            angle = (float) Math.PI / 2;
            hasInput = true;
        } else if (keyDown && isJumping) {
            angle = (float) -Math.PI / 2;
            hasInput = true;
        } else if (keyLeft) {
            angle = (float) Math.PI;
            hasInput = true;
        } else if (keyRight) {
            angle = 0f;
            hasInput = true;
        }

        if (keyDown) {
            fireY = y + height * 0.25f;
        }

        float direction = hasInput ? angle : (facingRight ? 0f : (float) Math.PI);
        bullets.add(new Bullet(centerX, fireY, direction, false));
    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0, 0.4f, 1f, 1);
        shapeRenderer.rect(x, y, width, height);

        for (Bullet bullet : bullets) {
            bullet.render(shapeRenderer);
        }
    }

    public void takeDeath() {
        if (isDead || lives <= 0) return;

        isDead = true;
        lives--;

        System.out.println("Player died! Remaining lives: " + lives);
    }

    public void respawn(float x, float y) {
        this.x = x;
        this.y = y + 400f; // naikkan posisi Y agar jatuh
        this.vx = 0;
        this.vy = -300; // beri kecepatan jatuh awal
        this.isJumping = true;
        this.isDead = false;

        System.out.println("Player respawned at: " + x + ", " + y);
    }

    public boolean isDead() {
        return isDead;
    }

    public int getLives() {
        return lives;
    }

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

    public Array<Bullet> getBullets() {
        return bullets;
    }
}
