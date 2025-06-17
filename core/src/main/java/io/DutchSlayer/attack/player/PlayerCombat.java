package io.DutchSlayer.attack.player; // Sesuaikan dengan struktur paket Anda

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import io.DutchSlayer.attack.player.weapon.AssaultRifle;
import io.DutchSlayer.attack.player.weapon.Grenade;
import io.DutchSlayer.attack.player.weapon.Pistol;
import io.DutchSlayer.attack.player.weapon.Weapon;
import io.DutchSlayer.attack.screens.GameScreen;
// import io.DutchSlayer.utils.Constant; // Tidak secara langsung digunakan di sini

public class PlayerCombat {

    private Weapon currentWeapon;
    private Player playerRef;         // Referensi ke objek Player utama
    private GameScreen gameScreenRef; // Referensi ke GameScreen untuk spawn granat
    private final Sound throwSound;
    private final Sound playerDiesSound;
    // Konstanta yang sebelumnya ada di Player.java terkait combat
    // Ini adalah cooldown yang dimiliki Player, bukan senjata.
    private final float PLAYER_FIRE_COOLDOWN = 0.25f; // private float fireCooldown = 0.25f; dari Player.java
    private final float GRENADE_COOLDOWN_PLAYER = 1.0f; // private float grenadeCooldown = 1.0f; dari Player.java
    private final float RESPAWN_DELAY_PLAYER = 2.0f; // private float respawnDelay = 2.0f; dari Player.java
    private final float INVINCIBILITY_DURATION_PLAYER = 3.0f; // private float invincibilityDuration = 3.0f; dari Player.java
    private final int MAX_GRENADE_AMMO_PLAYER = 10; // private final int maxGrenadeAmmo = 10; dari Player.java


    public PlayerCombat(Player playerReference, GameScreen gameScreenReference) {
        this.playerRef = playerReference;
        this.gameScreenRef = gameScreenReference;
        this.currentWeapon = new Pistol(); // Sesuai inisialisasi di Player.java
        this.throwSound = Gdx.audio.newSound(Gdx.files.internal("player/grenade_throw.mp3"));
        this.playerDiesSound = Gdx.audio.newSound(Gdx.files.internal("player/player_dies.mp3")); //
    }

    /**
     * Metode update utama untuk PlayerCombat.
     * Input state (seperti fireInput, grenadeInput) dan delta time akan diteruskan dari Player.java.
     * playerState akan menjadi objek yang menyimpan semua variabel kondisi pemain.
     */
    public void update(float delta, PlayerState playerState, boolean fireInput, boolean grenadeInput) {
        // Timers dari Player.java (akan direferensikan melalui playerState)
        if (playerState.invincibilityTimer > 0f)
            playerState.invincibilityTimer -= delta; // Sesuai bagian 2 di Player.update()
        if (playerState.grenadeTimer > 0f) playerState.grenadeTimer -= delta; // Sesuai bagian 2 di Player.update()

        // Logika Respawn (hanya bagian timer dan pemanggilan performCombatRespawn)
        if (playerState.isWaitingToRespawn) { // Mengacu pada isWaitingToRespawn di PlayerState
            playerState.respawnTimer -= delta; // Mengacu pada respawnTimer di PlayerState
            if (playerState.respawnTimer <= 0f) {
                playerState.isWaitingToRespawn = false;
                // Pemanggilan respawn fisik (posisi, kecepatan) akan tetap di Player.java
                // playerRef.respawn(newX, newY); // Ini akan dipanggil dari Player.java seperti di bagian 1 Player.update()
                performCombatRespawn(playerState); // Menangani status combat setelah respawn
            }
            return; // Jika sedang menunggu respawn, jangan lakukan aksi combat lain
        }

        if (playerState.isDead) return; // Jika mati, jangan lakukan aksi

        // Fire logic dari Player.java (bagian 7)
        if (playerState.fireTimer > 0f) playerState.fireTimer -= delta; // Sesuai bagian 7 di Player.update()

        if (fireInput && playerState.fireTimer <= 0f) {
            fireWeapon(playerState);
            playerState.fireTimer = PLAYER_FIRE_COOLDOWN; // Menggunakan player-level cooldown
        }

        // Update burst dari Player.java
        if (currentWeapon != null) {
            currentWeapon.updateBurst(this.playerRef, delta); // Sesuai bagian 7 dan 10 di Player.update()
        }

        // Throw grenade logic dari Player.java (bagian 8)
        if (grenadeInput && playerState.grenadeTimer <= 0f) {
            throwGrenade(playerState);
            playerState.grenadeTimer = GRENADE_COOLDOWN_PLAYER; // Sesuai bagian 8 di Player.update()
        }
    }

    private void fireWeapon(PlayerState playerState) {
        // Logika dari fireCardinal() di Player.java
        if (currentWeapon == null || currentWeapon.isOutOfAmmo()) {
            setWeapon(new Pistol());
        }
        currentWeapon.fire(this.playerRef);
    }

    private void throwGrenade(PlayerState playerState) {
        // Logika dari throwGrenade() di Player.java
        if (playerState.grenadeAmmo <= 0) return;


        Texture grenadeTex = gameScreenRef.getGrenadeTexture();
        Texture explosionTex = gameScreenRef.getExplosionTexture();
        throwSound.play(0.8f);
        // Variabel x, y, width, height, facingRight akan diambil dari playerState
        float centerX = playerState.x + playerState.playerWidth / 2;
        float centerY = playerState.y + playerState.playerHeight * 0.5f;
        float angle = playerState.facingRight ? 0.5f : (float) Math.PI - 0.5f;
        float power = 800f;

        if (gameScreenRef != null) {
            gameScreenRef.getGrenades().add(new Grenade(centerX, centerY, angle, power, false, grenadeTex, explosionTex));
        } else {
            System.err.println("PlayerCombat: GameScreen reference is null, cannot throw grenade.");
        }
        playerState.grenadeAmmo--;
    }

    public void takeDeath(PlayerState playerState) {
        // Hindari memicu kematian berkali-kali jika sudah dalam proses atau benar-benar mati
        if ((playerState.isDead && playerState.isWaitingToRespawn) || (playerState.isDead && playerState.lives < 0)) {
            return;
        }
        if (playerState.invincibilityTimer > 0f) return; // Jangan proses kematian jika sedang invincible

        if (playerDiesSound != null) { //
            playerDiesSound.play(0.8f); // Mainkan dengan volume 80%
        }

        playerState.lives--; // Kurangi nyawa dulu

        if (playerState.lives < 0) { // Kondisi Game Over Sebenarnya
            playerState.isDead = true;             // Tandai pemain mati permanen
            playerState.isWaitingToRespawn = false;  // Tidak ada lagi siklus respawn
            // Pindahkan posisi logis pemain sangat jauh agar tidak ditarget musuh
            playerState.x = -10000f; // Jauh di luar layar
            playerState.y = -10000f;
            setWeapon(new Pistol()); // Reset senjata
            System.out.println("PlayerCombat: Player GAME OVER. Final death.");
            // GameScreen.setGameOver(true) akan diurus oleh GameLogicHandler
            return; // Keluar, tidak ada proses respawn
        }

        // Jika masih ada nyawa, lanjutkan dengan siklus kematian dan respawn normal
        playerState.isDead = true; // Pemain "mati" selama hitungan mundur respawn
        playerState.respawnTimer = RESPAWN_DELAY_PLAYER; // Atur timer 2 detik
        playerState.isWaitingToRespawn = true;           // Masuk ke state menunggu respawn
        setWeapon(new Pistol()); // Reset senjata saat mati
        System.out.println("PlayerCombat: Player died! Remaining lives: " + playerState.lives);
    }

    public void performCombatRespawn(PlayerState playerState) {
        // Bagian dari Player.respawn() yang terkait status combat
        playerState.isDead = false;
        playerState.invincibilityTimer = INVINCIBILITY_DURATION_PLAYER;

        setWeapon(new Pistol()); // Pastikan senjata direset saat respawn
        playerState.grenadeAmmo = 5;
        System.out.println("PlayerCombat: Player combat state respawned. Lives: " + playerState.lives);
    }

    // === Metode Getter & Setter ===

    public void setWeapon(Weapon weapon) {
        this.currentWeapon = weapon;
        // System.out.println("PlayerCombat: Weapon set to " + (weapon != null ? weapon.getName() : "None"));
    }

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    public String getCurrentWeaponName() {
        return currentWeapon != null ? currentWeapon.getName() : "None"; // Sesuai Player.getCurrentWeaponName()
    }

    public int getCurrentAmmo(PlayerState playerState) {
        // Sesuai Player.getCurrentAmmo()
        if (currentWeapon instanceof AssaultRifle rifle) {
            return rifle.getAmmo();
        }
        return -1;
    }

    public float getInvincibilityDurationPlayer() {
        return INVINCIBILITY_DURATION_PLAYER;
    }

    public void pickupGrenade(PlayerState playerState, int amount) {
        // Sesuai Player.pickupGrenade()
        playerState.grenadeAmmo += amount;
        // System.out.println("PlayerCombat: Picked up " + amount + " grenades. Total: " + playerState.grenadeAmmo);
    }

    public int getGrenadeAmmo(PlayerState playerState) {
        return playerState.grenadeAmmo; // Sesuai Player.getGrenadeAmmo()
    }

    public boolean isInvincible(PlayerState playerState) {
        return playerState.invincibilityTimer > 0f; // Sesuai Player.isInvincible()
    }

    public void dispose() {
        if (throwSound != null) {
            throwSound.dispose();
        }
        if (playerDiesSound != null) {
            playerDiesSound.dispose();
        }
    }

}
