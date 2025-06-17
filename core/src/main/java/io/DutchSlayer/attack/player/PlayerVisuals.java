package io.DutchSlayer.attack.player; // Sesuaikan dengan struktur paket Anda

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class PlayerVisuals {

    // Tekstur dan Region untuk Idle
    private Texture idleTexture;
    private TextureRegion idleFrame;

    // Tekstur dan Animasi untuk Berjalan/Lari
    private Array<Texture> runTextures; // Untuk menyimpan referensi Texture agar bisa di-dispose
    private Animation<TextureRegion> walkAnimation;

    private Texture deadTexture;
    private TextureRegion deadFrame;


    private Texture arIdleTexture;
    private TextureRegion arIdleFrame;
    private Array<Texture> arRunTextures;
    private Animation<TextureRegion> arWalkAnimation;


    private Texture duckTexture; //
    private TextureRegion duckFrame; //

    // NEW: Tekstur dan Region untuk Ducking dengan Assault Rifle
    private Texture arDuckTexture; //
    private TextureRegion arDuckFrame;

    // Variabel internal untuk state visual
    private float stateTime = 0f; // Akan di-update oleh Player.java

    private Texture dashTexture; // <-- BARU
    private TextureRegion dashFrame;

    private Texture jumpTexture; // New Texture for jumping
    private TextureRegion jumpFrame;

    private float blinkTimer = 0f; //
    private final float BLINK_INTERVAL = 0.1f; // Seberapa cepat berkedip
    private boolean isVisible = true; //

    public PlayerVisuals() {
        runTextures = new Array<>();
        arRunTextures = new Array<>();
        loadAssets();
    }

    private void loadAssets() {
        // === Memuat Texture IDLE Default ===
        idleTexture = new Texture(Gdx.files.internal("player/player_idle.png"));
        idleFrame = new TextureRegion(idleTexture);

        // === Memuat Texture RUN Default ===
        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 1; i <= 5; i++) {
            Texture runTex = new Texture(Gdx.files.internal("player/player_run" + i + ".png"));
            runTextures.add(runTex);
            TextureRegion runRegion = new TextureRegion(runTex);
            runFrames.add(runRegion);
        }
        walkAnimation = new Animation<>(0.1f, runFrames, Animation.PlayMode.LOOP);

        deadTexture = new Texture(Gdx.files.internal("player/player_dead.png"));
        deadFrame = new TextureRegion(deadTexture);

        // === NEW: Memuat Texture IDLE untuk Assault Rifle ===
        arIdleTexture = new Texture(Gdx.files.internal("player/player_run_ar1.png")); // Using ar1 for idle as per request
        arIdleFrame = new TextureRegion(arIdleTexture);

        // === NEW: Memuat Texture RUN untuk Assault Rifle ===
        Array<TextureRegion> arRunFrames = new Array<>();
        for (int i = 1; i <= 8; i++) { // From ar1 to ar8
            Texture arRunTex = new Texture(Gdx.files.internal("player/player_run_ar" + i + ".png"));
            arRunTextures.add(arRunTex);
            TextureRegion arRunRegion = new TextureRegion(arRunTex);
            arRunFrames.add(arRunRegion);
        }
        arWalkAnimation = new Animation<>(0.1f, arRunFrames, Animation.PlayMode.LOOP);

        duckTexture = new Texture(Gdx.files.internal("player/player_duck.png"));
        duckFrame = new TextureRegion(duckTexture);

        // NEW: Memuat Texture DUCKING untuk Assault Rifle
        arDuckTexture = new Texture(Gdx.files.internal("player/player_duck_ar.png"));
        arDuckFrame = new TextureRegion(arDuckTexture);

        dashTexture = new Texture(Gdx.files.internal("player/player_dash.png"));
        dashFrame = new TextureRegion(dashTexture);

        jumpTexture = new Texture(Gdx.files.internal("player/player_jump.png"));
        jumpFrame = new TextureRegion(jumpTexture);
    }

    /**
     * Mendapatkan TextureRegion yang sesuai untuk dirender berdasarkan state pemain.
     *
     * @param playerState Objek PlayerState yang berisi kondisi pemain saat ini.
     * @param isRunning   Boolean yang menandakan apakah pemain sedang berlari/bergerak horizontal.
     * @param deltaTime   Waktu delta dari frame terakhir, untuk mengupdate stateTime internal.
     * @return TextureRegion yang siap untuk dirender.
     */
    public TextureRegion getFrameToRender(PlayerState playerState, boolean isRunning, float deltaTime, String currentWeaponName) {
        this.stateTime += deltaTime; // Update stateTime internal

        // NEW: Logika berkedip untuk invincibility
        if (playerState.isInvincible()) { //
            blinkTimer += deltaTime; //
            if (blinkTimer >= BLINK_INTERVAL) { //
                isVisible = !isVisible; // Toggle visibilitas
                blinkTimer = 0f; //
            }
            if (!isVisible) { //
                return null; // Mengembalikan null agar tidak dirender jika tidak terlihat
            }
        } else { //
            isVisible = true; // Pastikan terlihat saat tidak invincible
            blinkTimer = 0f; //
        }


        TextureRegion region;

        // ** MODIFIKASI: Prioritaskan state mati **
        if (playerState.isDead && playerState.isWaitingToRespawn) { //
            region = deadFrame; //
        } else if (playerState.isJumping) { // <-- NEW: Prioritize jumping state
            region = jumpFrame; //
        } else if (playerState.isDashing) { // Prioritize dashing state after jumping
            region = dashFrame; //
        } else if (playerState.isDucking) { // Handle ducking state
            if ("Assault Rifle".equals(currentWeaponName)) { //
                region = arDuckFrame; // Use AR duck asset
            } else { //
                region = duckFrame; // Use default duck asset
            }
        } else { //
            // Check weapon and apply appropriate visuals
            if ("Assault Rifle".equals(currentWeaponName)) { //
                if (isRunning) { //
                    region = arWalkAnimation.getKeyFrame(this.stateTime, true); //
                } else { //
                    region = arIdleFrame; //
                }
            } else { // Default weapon visual
                if (isRunning) { //
                    region = walkAnimation.getKeyFrame(this.stateTime, true); //
                } else { //
                    region = idleFrame; //
                }
            }
        }


        // Atur flip TextureRegion berdasarkan arah hadap pemain
        // Jika menghadap kanan (facingRight = true) dan region saat ini ter-flip ke kiri (isFlipX() = true), maka flip kembali.
        if (region != deadFrame) { // Hanya terapkan flip ke animasi berjalan/idle
            if (playerState.facingRight && region.isFlipX()) { //
                region.flip(true, false); //
            } else if (!playerState.facingRight && !region.isFlipX()) { //
                region.flip(true, false); //
            }
        }
        return region; //
    }
    /**
     * Panggil metode ini ketika game ditutup untuk melepaskan memori dari tekstur.
     */
    public void dispose() {
        if (idleTexture != null) {
            idleTexture.dispose();
        }
        for (Texture tex : runTextures) {
            if (tex != null) {
                tex.dispose();
            }
        }
        runTextures.clear();
        if (deadTexture != null) {
            deadTexture.dispose();
        }
        // NEW: Dispose AR textures
        if (arIdleTexture != null) {
            arIdleTexture.dispose();
        }
        for (Texture tex : arRunTextures) {
            if (tex != null) {
                tex.dispose();
            }
        }
        arRunTextures.clear();

        if (duckTexture != null) {
            duckTexture.dispose();
        }
        if (arDuckTexture != null) {
            arDuckTexture.dispose();
        }

        if (dashTexture != null) { // <-- BARU
            dashTexture.dispose();
        }
        if (jumpTexture != null) {
            jumpTexture.dispose();
        }

    }
}
