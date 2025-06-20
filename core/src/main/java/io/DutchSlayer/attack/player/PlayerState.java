package io.DutchSlayer.attack.player;

import io.DutchSlayer.utils.Constant;

public class PlayerState {
    public float x;
    public float y;
    public float vx;
    public float vy;
    public boolean isDead = false;
    public boolean facingRight = true;
    public int lives = 5;

    public boolean isJumping = false;
    public boolean isDucking = false;
    public boolean isDashing = false;

    public float invincibilityTimer = 0f;
    public float fireTimer = 0f;
    public float grenadeTimer = 0f;

    public float duckTimer = 0f;
    public float duckCooldownTimer = 0f;
    public float duckHoldTime = 0f;

    public float dashActiveTimer = 0f;
    public float dashCooldown = 0f;

    public float respawnTimer = 0f;
    public boolean isWaitingToRespawn = false;

    public int grenadeAmmo = 5;

    public float playerWidth = Constant.PLAYER_WIDTH;
    public float playerHeight = Constant.PLAYER_HEIGHT;
    public float normalPlayerHeight = Constant.PLAYER_HEIGHT;
    public float duckPlayerHeight = Constant.PLAYER_HEIGHT / 2.5f;

    public Float leftBoundX = null;
    public Float rightBoundX = null;

    public boolean isInvincible() {
        return invincibilityTimer > 0f;
    }
}
