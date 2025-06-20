package io.DutchSlayer.attack.player; // Sesuaikan dengan struktur paket Anda

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import io.DutchSlayer.utils.Constant; // Pastikan import Constant sudah benar

public class PlayerMovement {

    private final Sound dashSound;
    private final Sound jumpSound;

    public PlayerMovement() {
        this.dashSound = Gdx.audio.newSound(Gdx.files.internal("player/dash.mp3"));
        this.jumpSound = Gdx.audio.newSound(Gdx.files.internal("player/player_jump.mp3"));
    }

    public void update(float delta, PlayerState state) {
        if (state.isDead && !state.isWaitingToRespawn) {
            state.vx = 0;
            return;
        }

        float GRAVITY = -2500f;
        if (state.isWaitingToRespawn) {
            if (state.isJumping) {
                state.vy += GRAVITY * delta;
                state.y += state.vy * delta;

                if (state.y <= Constant.TERRAIN_HEIGHT) {
                    state.y = Constant.TERRAIN_HEIGHT;
                    if (state.vy < 0) state.vy = 0;
                    state.isJumping = false;
                }
            } else {
                state.vy = 0;
            }
            return;
        }

        if (state.dashActiveTimer > 0f) state.dashActiveTimer -= delta;
        if (state.dashCooldown > 0f) state.dashCooldown -= delta;

        if (state.duckCooldownTimer > 0f) {
            state.duckCooldownTimer -= delta;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && state.dashCooldown <= 0f) {
            dashSound.play(0.6f);
            float DASH_DURATION = 0.2f;
            state.dashActiveTimer = DASH_DURATION;
            float DASH_COOLDOWN_PER_DASH = 1.5f;
            state.dashCooldown = DASH_COOLDOWN_PER_DASH;
        }

        if (state.dashActiveTimer > 0f) {
            state.isDashing = true;
        } else {
            state.isDashing = false;
        }

        float currentSpeed = Constant.PLAYER_SPEED;
        if (state.isDashing) {
            currentSpeed *= 3.0f;
        }

        state.vx = 0;
        boolean wantsToDuck = !state.isJumping && Gdx.input.isKeyPressed(Input.Keys.S);

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            state.vx = -currentSpeed;
            state.facingRight = false;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            state.vx = currentSpeed;
            state.facingRight = true;
        }
        if (state.isDucking) {
            state.vx = 0;
        }

        if (wantsToDuck && state.duckCooldownTimer <= 0f) {
            state.duckHoldTime += delta;

            if (!state.isDucking) {
                state.isDucking = true;
                float DUCK_DURATION = 5f;
                state.duckTimer = DUCK_DURATION;
            }

            if (state.duckTimer > 0f) {
                state.duckTimer -= delta;
            } else {
                state.isDucking = false;
                float DUCK_COOLDOWN = 2f;
                state.duckCooldownTimer = DUCK_COOLDOWN;
                state.duckHoldTime = 0f;
            }
        } else {
            state.isDucking = false;
            state.duckHoldTime = 0f;
        }

        if (state.isDucking) {
            state.playerHeight = state.duckPlayerHeight;
            state.vx *= 0.5f;
        } else {
            state.playerHeight = state.normalPlayerHeight;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !state.isJumping && !state.isDucking) {
            float JUMP_FORCE = 1000f;
            state.vy = JUMP_FORCE;
            state.isJumping = true;
            if (jumpSound != null) {
                jumpSound.play(0.7f);
            }
        }

        state.vy += GRAVITY * delta;
        state.x += state.vx * delta;
        state.y += state.vy * delta;

        if (state.leftBoundX != null && state.x < state.leftBoundX) {
            state.x = state.leftBoundX;
            state.vx = 0;
        }
        if (state.rightBoundX != null && state.x + state.playerWidth > state.rightBoundX) {
            state.x = state.rightBoundX - state.playerWidth;
            state.vx = 0;
        }

        if (state.y <= Constant.TERRAIN_HEIGHT) {
            state.y = Constant.TERRAIN_HEIGHT;
            if (state.vy < 0) {
                state.vy = 0;
            }
            state.isJumping = false;
        }

        state.x = MathUtils.clamp(state.x, Constant.WALL_WIDTH, Constant.MAP_WIDTH - state.playerWidth);
    }

    public void dispose() {
        if (dashSound != null) {
            dashSound.dispose();
        }
    }
}
