package io.DutchSlayer.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.utils.Constant;
import io.DutchSlayer.controllers.ControllerHandler;

public class Player {

    private float x;
    private float y;
    private float vx;
    private float vy;

    private boolean isJumping = false;
    private boolean facingRight = true;

    private final float width = Constant.PLAYER_WIDTH;
    private final float height = Constant.PLAYER_HEIGHT;

    private final float gravity = -2000f;
    private final float jumpForce = 660f;

    private final Array<Bullet> bullets;

    public Player() {
        this.x = Constant.PLAYER_START_X;
        this.y = Constant.TERRAIN_HEIGHT;
        this.bullets = new Array<>();
    }

    public void update(float delta) {
        ControllerHandler ctrl = ControllerHandler.getInstance();

        float speed = Constant.PLAYER_SPEED;

        // Dash (tombol B)
        if (ctrl.isDashPressed()) {
            speed *= 2.0f;
        }

        // Movement: keyboard A/D atau analog kiri
        vx = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || ctrl.getMoveAxis() < -0.2f) {
            vx = -speed;
            facingRight = false;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D) || ctrl.getMoveAxis() > 0.2f) {
            vx = speed;
            facingRight = true;
        }

        // Jump
        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || ctrl.isJumpPressed()) && !isJumping) {
            vy = jumpForce;
            isJumping = true;
        }

        vy += gravity * delta;
        x += vx * delta;
        y += vy * delta;

        if (y <= Constant.TERRAIN_HEIGHT) {
            y = Constant.TERRAIN_HEIGHT;
            vy = 0;
            isJumping = false;
        }

        if (x < Constant.WALL_WIDTH) {
            x = Constant.WALL_WIDTH;
        }

        float maxX = Constant.MAP_WIDTH - Constant.PLAYER_WIDTH;
        if (x > maxX) {
            x = maxX;
        }

        if (x < 0) {
            x = 0;
        }

        // Fire: tombol X (controller) atau tombol K (keyboard)
        if (ctrl.consumeFirePressedOnce() || Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            fireCardinal(ctrl);
        }

        // Grenade: tombol Y (once)
        if (ctrl.consumeGrenadePressedOnce()) {
            // TODO: implement grenade
        }
        float camLeft = x - Constant.SCREEN_WIDTH / 2f;
        float camRight = x + Constant.SCREEN_WIDTH / 2f;

        for (Bullet bullet : bullets) {
            bullet.update(delta, camLeft, camRight);
        }


        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            if (!bullet.isAlive()) {
                bullets.removeIndex(i);
            }
        }
    }

    private void fireCardinal(ControllerHandler ctrl) {
        float centerX = x + width / 2;
        float fireY = y + height * 0.65f; // Default: 65% tinggi player

        float angle = 0;
        boolean hasInput = false;

        // === Keyboard Input ===
        boolean keyLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean keyRight = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean keyUp = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean keyDown = Gdx.input.isKeyPressed(Input.Keys.S); // untuk offset

        // === Analog Input ===
        float analogX = ctrl.getAnalogX();
        float analogY = ctrl.getAnalogY();

        boolean analogLeft = analogX < -0.4f;
        boolean analogRight = analogX > 0.4f;
        boolean analogUp = analogY > 0.4f;
        boolean analogDown = analogY < -0.4f;

        // === Deteksi Arah Tembakan ===
        if (keyUp || analogUp) {
            angle = (float) Math.PI / 2; // Atas
            hasInput = true;
        } else if ((keyDown || analogDown) && isJumping) {
            angle = (float) -Math.PI / 2; // Bawah (saat loncat)
            hasInput = true;
        } else if (keyLeft || analogLeft) {
            angle = (float) Math.PI; // Kiri
            hasInput = true;
        } else if (keyRight || analogRight) {
            angle = 0f; // Kanan
            hasInput = true;
        }

        // Turunkan posisi tembak jika analog/tombol bawah ditekan (tidak harus melompat)
        if (keyDown || analogDown) {
            fireY = y + height * 0.25f;
        }

        float direction = hasInput ? angle : (facingRight ? 0f : (float) Math.PI);

        bullets.add(new Bullet(centerX, fireY, direction, false)); // false = bullet player

    }

    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(0, 0.4f, 1f, 1);
        shapeRenderer.rect(x, y, width, height);

        for (Bullet bullet : bullets) {
            bullet.render(shapeRenderer);
        }
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
