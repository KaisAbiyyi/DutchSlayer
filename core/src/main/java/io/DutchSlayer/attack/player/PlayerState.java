package io.DutchSlayer.attack.player; // Sesuaikan dengan struktur paket Anda

import io.DutchSlayer.utils.Constant;

public class PlayerState {

    // Kondisi Dasar
    public float x;
    public float y;
    public float vx; // Kecepatan horizontal
    public float vy; // Kecepatan vertikal
    public boolean isDead = false;
    public boolean facingRight = true;
    public int lives = 5; // Contoh nilai awal

    // Kondisi Pergerakan
    public boolean isJumping = false;
    public boolean isDucking = false;
    public boolean isDashing = false;

    // Timer dan Cooldown (contoh, bisa ditambahkan lebih banyak sesuai kebutuhan)
    public float invincibilityTimer = 0f;
    public float fireTimer = 0f;
    public float grenadeTimer = 0f;

    public float duckTimer = 0f;         // Sisa waktu duck aktif
    public float duckCooldownTimer = 0f; // Sisa waktu cooldown duck
    public float duckHoldTime = 0f;      // Berapa lama tombol duck ditekan

    public float dashActiveTimer = 0f;   // Sisa waktu dash aktif
    public float dashCooldown = 0f; // Sisa waktu cooldown untuk batch dash

    public float respawnTimer = 0f;
    public boolean isWaitingToRespawn = false;

    // Status Senjata & Amunisi (bisa juga di PlayerCombat jika lebih detail)
    public int grenadeAmmo = 5; // Contoh nilai awal

    // Lain-lain
    public float stateTime = 0f; // Untuk animasi

    public float playerWidth = Constant.PLAYER_WIDTH; // Lebar tetap
    public float playerHeight = Constant.PLAYER_HEIGHT; // Tinggi saat ini, bisa berubah
    public float normalPlayerHeight = Constant.PLAYER_HEIGHT; // Tinggi normal
    public float duckPlayerHeight = Constant.PLAYER_HEIGHT / 2.5f; // Tinggi saat merunduk

    // Batas pergerakan dinamis (jika ada, misal untuk perangkap dinding)
    public Float leftBoundX = null;
    public Float rightBoundX = null;

    // Anda bisa menambahkan konstruktor jika perlu nilai awal yang lebih spesifik
    // public PlayerState() {
    //     // Inisialisasi nilai default jika diperlukan
    // }

    // Metode helper sederhana bisa ditambahkan di sini jika ada logika state murni
    public boolean isInvincible() {
        return invincibilityTimer > 0f;
    }
}
