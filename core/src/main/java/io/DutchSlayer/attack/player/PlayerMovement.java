package io.DutchSlayer.attack.player; // Sesuaikan dengan struktur paket Anda

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import io.DutchSlayer.utils.Constant; // Pastikan import Constant sudah benar

public class PlayerMovement {

    // Konstanta Gerakan (diambil dari Player.java sebelumnya)
    private final float GRAVITY = -2500f;
    private final float JUMP_FORCE = 1000f;

    // Konstanta Ducking
    private final float DUCK_DURATION = 5f;     // maksimal durasi duck
    private final float DUCK_COOLDOWN = 2f;     // cooldown setelah duck habis

    // Konstanta Dashing
    private final float DASH_DURATION = 0.2f;
    private final float DASH_COOLDOWN_PER_DASH = 1.5f;
    private final Sound dashSound;
    private final Sound jumpSound;

    public PlayerMovement() {
        // BARU: Muat suara dash sekali saat objek dibuat
        this.dashSound = Gdx.audio.newSound(Gdx.files.internal("player/dash.mp3"));
        this.jumpSound = Gdx.audio.newSound(Gdx.files.internal("player/player_jump.mp3"));
    }

    public void update(float delta, PlayerState state) {
        if (state.isDead && !state.isWaitingToRespawn) {
            state.vx = 0;
            return;
        }

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

        // === 1. Update Timers Terkait Gerakan dari PlayerState ===
        if (state.dashActiveTimer > 0f) state.dashActiveTimer -= delta;
        if (state.dashCooldown > 0f) state.dashCooldown -= delta; // <--- Update timer cooldown dash

        if (state.duckCooldownTimer > 0f) {
            state.duckCooldownTimer -= delta;
        }

        // === 2. Logika Dash ===
        // Dash dipicu jika tombol SHIFT ditekan DAN cooldown dash sudah habis
        if (Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT) && state.dashCooldown <= 0f) {
            dashSound.play(0.6f); // <-- BARU: Mainkan suara dash dengan volume 60%
            state.dashActiveTimer = DASH_DURATION; // Aktifkan dash
            state.dashCooldown = DASH_COOLDOWN_PER_DASH; // Mulai cooldown dash
        }

        if (state.dashActiveTimer > 0f) {
            state.isDashing = true;
        } else {
            state.isDashing = false;
        }

        // === 3. Tentukan Kecepatan Dasar ===
        float currentSpeed = Constant.PLAYER_SPEED;
        if (state.isDashing) {
            currentSpeed *= 3.0f; // Kecepatan dash
        }
        // Logika kecepatan merunduk akan diterapkan saat menghitung state.vx


        // === 4. Input Gerakan Horizontal ===
        state.vx = 0; // Reset kecepatan horizontal setiap frame
        boolean wantsToDuck = !state.isJumping && Gdx.input.isKeyPressed(Input.Keys.S); // Cek apakah mau merunduk

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

        // === 5. Logika Merunduk (Ducking) ===
        // (Ini disesuaikan dari logika di Player.java Anda)
        if (wantsToDuck && state.duckCooldownTimer <= 0f) {
            state.duckHoldTime += delta; // Akumulasi waktu tombol S ditekan

            if (!state.isDucking) { // Jika belum merunduk, mulai merunduk
                state.isDucking = true;
                state.duckTimer = DUCK_DURATION; // Set durasi merunduk
            }

            if (state.duckTimer > 0f) { // Jika sedang dalam durasi merunduk
                state.duckTimer -= delta;
            } else { // Durasi merunduk habis
                state.isDucking = false;
                state.duckCooldownTimer = DUCK_COOLDOWN; // Mulai cooldown
                state.duckHoldTime = 0f; // Reset hold time
            }
        } else {
            // Tombol S dilepas atau sedang cooldown duck
            if (state.isDucking && state.duckHoldTime >= 0.1f && state.duckCooldownTimer <= 0f) { // Jika tombol S dilepas setelah ditekan cukup lama
                // Jika ingin cooldown setelah S dilepas, uncomment baris di bawah
                // state.duckCooldownTimer = DUCK_COOLDOWN;
            }
            state.isDucking = false; // Tidak lagi merunduk
            // state.duckTimer = DUCK_DURATION; // Reset sisa durasi duck (opsional, tergantung behavior yg diinginkan)
            state.duckHoldTime = 0f; // Reset hold time
        }

        // Update tinggi pemain berdasarkan status merunduk
        if (state.isDucking) {
            state.playerHeight = state.duckPlayerHeight;
            state.vx *= 0.5f; // Kurangi kecepatan jika merunduk
        } else {
            state.playerHeight = state.normalPlayerHeight;
        }

        // === 6. Input Lompat ===
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !state.isJumping && !state.isDucking) {
            state.vy = JUMP_FORCE;
            state.isJumping = true;
            // --- BARU: Mainkan suara lompat ---
            if (jumpSound != null) { //
                jumpSound.play(0.7f); // Mainkan dengan volume 70%
            }
        }


        // === 7. Terapkan Gravitasi & Perbarui Posisi ===
        state.vy += GRAVITY * delta;
        state.x += state.vx * delta;
        state.y += state.vy * delta;

        // === 8. Penanganan Batas Dinding Dinamis (jika ada) ===
        if (state.leftBoundX != null && state.x < state.leftBoundX) {
            state.x = state.leftBoundX;
            state.vx = 0; // Hentikan gerakan horizontal jika menabrak
        }
        if (state.rightBoundX != null && state.x + state.playerWidth > state.rightBoundX) {
            state.x = state.rightBoundX - state.playerWidth;
            state.vx = 0; // Hentikan gerakan horizontal jika menabrak
        }

        // === 9. Penanganan Tabrakan dengan Tanah ===
        if (state.y <= Constant.TERRAIN_HEIGHT) {
            state.y = Constant.TERRAIN_HEIGHT;
            if (state.vy < 0) { // Hanya hentikan vy jika bergerak ke bawah
                state.vy = 0;
            }
            state.isJumping = false;
        }

        // === 10. Batasi Posisi Pemain pada Peta Keseluruhan ===
        // Pastikan state.playerWidth sudah benar merefleksikan lebar pemain
        state.x = MathUtils.clamp(state.x, Constant.WALL_WIDTH, Constant.MAP_WIDTH - state.playerWidth);
    }

    public void dispose() {
        if (dashSound != null) {
            dashSound.dispose();
        }
    }
}
