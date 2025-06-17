package io.DutchSlayer.attack.player; // Sesuaikan dengan struktur paket Anda

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.attack.boss.TankBoss;
import io.DutchSlayer.attack.player.weapon.Bullet;
import io.DutchSlayer.attack.player.weapon.Weapon;
import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.utils.Constant;

public class Player {

    // Komponen yang di-refactor
    private PlayerState playerState;
    private PlayerVisuals visualsHandler;
    private PlayerMovement movementHandler;
    private PlayerCombat combatHandler;

    // Variabel yang masih relevan di Player.java
    private OrthographicCamera camera; // Untuk update peluru
    private GameScreen gameScreen;     // Untuk PlayerCombat (misal, spawn granat)
    private TankBoss boss;

    private final Array<Bullet> bullets;

    private Rectangle bounds; // Bounds pemain untuk kolisi

    public Player(OrthographicCamera camera) {
        this.camera = camera;

        this.playerState = new PlayerState();
        // Inisialisasi nilai awal PlayerState dari field Player.java lama
        this.playerState.x = Constant.PLAYER_START_X;
        this.playerState.y = Constant.TERRAIN_HEIGHT;
        this.playerState.lives = 5;
        this.playerState.grenadeAmmo = 5;

        // Inisialisasi dimensi di PlayerState
        this.playerState.playerWidth = Constant.PLAYER_WIDTH;
        this.playerState.normalPlayerHeight = Constant.PLAYER_HEIGHT;
        this.playerState.duckPlayerHeight = Constant.PLAYER_HEIGHT / 2.5f; // Sesuai `duckHeight = this.height / 2.5f;`
        this.playerState.playerHeight = this.playerState.normalPlayerHeight; // Mulai dengan tinggi normal

        // Inisialisasi timer dan state lain di PlayerState jika ada nilai default spesifik dari Player.java lama
        // Contoh:
        // this.playerState.duckTimer = DUCK_DURATION; // Jika DUCK_DURATION adalah konstanta di PlayerMovement/PlayerState
        // this.playerState.duckCooldownTimer = 0f;

        this.visualsHandler = new PlayerVisuals();
        this.movementHandler = new PlayerMovement();
        // combatHandler akan diinisialisasi di setGameScreen

        this.bullets = new Array<>();

        this.bounds = new Rectangle(
            this.playerState.x,
            this.playerState.y,
            this.playerState.playerWidth,
            this.playerState.playerHeight
        );
    }

    public void setBoss(TankBoss boss) {
        this.boss = boss;
    }

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        if (this.combatHandler == null && this.gameScreen != null) {
            this.combatHandler = new PlayerCombat(this, this.gameScreen);
        }
    }

    // Di dalam kelas Player.java

    public void update(float delta) {
        if (combatHandler == null) { // Guard jika combatHandler belum siap
            if (movementHandler != null) {
                movementHandler.update(delta, playerState);
            }
            if (this.bounds != null) {
                this.bounds.set(playerState.x, playerState.y, playerState.playerWidth, playerState.playerHeight);
            }
            return;
        }

        // 1. Ambil input untuk combat
        boolean fireInput = Gdx.input.isKeyJustPressed(Input.Keys.J);
        boolean grenadeInput = Gdx.input.isKeyJustPressed(Input.Keys.K);

        // 2. Update Movement Handler
        movementHandler.update(delta, playerState);

        // 3. Update Combat Handler (termasuk timer respawn dan status combat)
        combatHandler.update(delta, playerState, fireInput, grenadeInput);

        // 4. Logika untuk memicu respawn fisik pemain
        // Ini terjadi setelah PlayerCombat menyelesaikan siklus tunggunya dan mengaktifkan invincibility.
        if (!playerState.isDead && // Pemain tidak lagi ditandai mati oleh PlayerCombat
            playerState.invincibilityTimer == combatHandler.getInvincibilityDurationPlayer() && // Invincibility baru saja dimulai
            playerState.lives > 0) { // Pastikan masih ada nyawa

            // Hitung posisi X baru: tepi kiri layar kamera
            float newRespawnX = camera.position.x - (camera.viewportWidth / 2f);

            // Batasi posisi respawn X dalam area peta
            newRespawnX = Math.max(newRespawnX, Constant.WALL_WIDTH);
            newRespawnX = Math.min(newRespawnX, Constant.MAP_WIDTH - playerState.playerWidth);

            respawn(newRespawnX, Constant.TERRAIN_HEIGHT); // Panggil metode respawn fisik
        }

        if (boss != null) {
            checkBossCollision(boss);
        }

        // 5. Update Peluru pemain
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

        // 6. Update Bounds pemain
        this.bounds.set(
            playerState.x,
            playerState.y,
            playerState.playerWidth,
            playerState.playerHeight
        );

        // 7. Hentikan update lebih lanjut jika pemain mati permanen (Game Over)
        if (playerState.isDead && !playerState.isWaitingToRespawn) {
            playerState.y = -500f; // Atur posisi Y ke -5f
            playerState.vx = 0;  // Hentikan gerakan horizontal
            playerState.vy = 0;  // Hentikan gerakan vertikal
            // Perbarui bounds agar sesuai dengan posisi baru
            this.bounds.set(playerState.x, playerState.y, playerState.playerWidth, playerState.playerHeight);
            // Tetap panggil combatHandler.update untuk menangani timer respawn
            combatHandler.update(delta, playerState, false, false);


            return;
        }
    }

    private void checkBossCollision(TankBoss boss) {
        // Jangan proses jika pemain sedang invincible, sudah mati, atau sedang menunggu respawn
        if (isInvincible() || isDead() || playerState.isWaitingToRespawn) {
            return;
        }

        // Dapatkan bounds (area tabrakan) pemain dan boss
        Rectangle playerRect = getBounds();
        Rectangle bossRect = new Rectangle(boss.getPosition().x, boss.getPosition().y, boss.getWidth(), boss.getHeight());

        // Periksa apakah ada tabrakan (overlap) antara pemain dan boss
        if (playerRect.overlaps(bossRect)) {
            // Periksa apakah boss sedang dalam state CHARGE
            if (boss.isCharging()) {
                System.out.println("[Player] Collided with charging boss! Taking death.");
                takeDeath(); // Panggil metode takeDeath() pemain
            }
        }
    }

    public void render(SpriteBatch spriteBatch, float delta) {
        if (visualsHandler == null) return; //

        // Tentukan isRunning
        boolean isRunning = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D); //
        if (playerState.isDashing) isRunning = true; //

        // Dapatkan frame/region yang sesuai dari PlayerVisuals
        String currentWeaponName = getCurrentWeaponName(); //
        TextureRegion region = visualsHandler.getFrameToRender(playerState, isRunning, delta, currentWeaponName); //

        // NEW: Jika region null, jangan render pemain (untuk efek berkedip)
        if (region == null) { //
            // Render peluru meskipun pemain tidak terlihat
            for (Bullet bullet : bullets) { //
                bullet.render(spriteBatch); //
            }
            return; //
        }

        // Tentukan posisi Y default
        float renderY = playerState.y; //

        // Logika untuk render pemain mati di bawah tanah (tidak berubah)
        if (playerState.isDead && playerState.isWaitingToRespawn) { //
            renderY -= 25f; //
        }

        // Tentukan tinggi render default
        float renderHeight = playerState.playerHeight; //
        if (playerState.isDucking) { //
            renderHeight = playerState.normalPlayerHeight - 40f; //
        }

        // --- AWAL PERUBAHAN UNTUK LEBAR DASH ---

        // Tentukan lebar dan posisi X render default
        float renderWidth = playerState.playerWidth; //
        float renderX = playerState.x; //

        // Jika sedang dash, buat gambarnya lebih lebar dan geser posisi X agar tetap di tengah
        if (playerState.isDashing) { //
            renderWidth *= 1.15f; // Buat 50% lebih lebar (Anda bisa sesuaikan nilai 1.5f ini)
            // Geser posisi render ke kiri sebesar setengah dari lebar tambahan agar tetap simetris
            renderX -= (renderWidth - playerState.playerWidth) / 2f; //
        }

        // --- AKHIR PERUBAHAN ---

        // Skala visual (tidak berubah)
        float scaleHorizontal = 1.25f + 0.3f; //
        float scaleVertical = 1.25f; //

        // Panggil draw dengan variabel render yang sudah disesuaikan
        spriteBatch.draw(
            region,
            renderX, // Gunakan renderX yang sudah disesuaikan
            renderY,
            0, 0,
            renderWidth,  // Gunakan renderWidth yang sudah disesuaikan
            renderHeight,
            scaleHorizontal, scaleVertical,
            0
        );

        // Render peluru (tidak berubah)
        for (Bullet bullet : bullets) { //
            bullet.render(spriteBatch); //
        }
    }

    /**
     * Menangani aspek fisik dari respawn.
     * Status combat (invincibility, reset senjata) diurus oleh PlayerCombat.performCombatRespawn().
     */
    public void respawn(float newXBase, float newYRespawnPlatform) {
        // Logika fisik dari Player.respawn() lama, menggunakan PlayerState
        playerState.x = newXBase;
        // Atur Y jauh di atas platform agar terlihat jatuh (sesuai Player.java lama)
        playerState.y = newYRespawnPlatform + 400f;
        playerState.vx = 0;
        // Atur kecepatan Y awal untuk jatuh (sesuai Player.java lama)
        // Anda bisa juga set playerState.vy = 0f; jika ingin jatuh murni karena gravitasi dari posisi diam di udara.
        playerState.vy = -300f;
        playerState.isJumping = true; // Penting agar PlayerMovement menerapkan gravitasi

        // Status isDead dan invincibilityTimer sudah diatur oleh combatHandler.performCombatRespawn()

        // Reset status gerakan yang mungkin masih aktif
        playerState.isDucking = false;
        playerState.isDashing = false; // Hentikan dash jika sedang aktif saat mati
        playerState.dashActiveTimer = 0f;

        // Reset timer duck yang mungkin relevan jika ingin perilaku konsisten setelah respawn
        // playerState.duckTimer = nilai_default_durasi_duck; // jika ada konstanta ini
        // playerState.duckCooldownTimer = 0f;

        System.out.println("Player physically respawned at: " + playerState.x + ", " + playerState.y + " and falling.");
    }


    public void dispose() {
        if (visualsHandler != null) {
            visualsHandler.dispose();
        }
        if (combatHandler != null) { // <-- TAMBAHKAN BLOK INI
            combatHandler.dispose();
        }

        if (movementHandler != null) { // <-- TAMBAHKAN BLOK INI
            movementHandler.dispose();
        }
        // Tidak ada tekstur lain yang di-dispose langsung di Player.java ini
    }

    // === GETTERS & SETTERS (mendelegasikan ke PlayerState atau Handler) ===

    public PlayerState getPlayerState() { // Untuk akses internal jika dibutuhkan, atau oleh GameLogicHandler
        return playerState;
    }

    public Rectangle getBounds() {
        return bounds; // bounds diupdate dari playerState
    }

    public void setX(float newX) { // Digunakan oleh GameLogicHandler untuk collision response
        playerState.x = newX;
        if (bounds != null) bounds.setX(newX);
    }

    public float getX() {
        return playerState.x;
    }

    public float getY() {
        return playerState.y;
    }

    public float getWidth() {
        return playerState.playerWidth;
    }

    public float getHeight() { // Ini adalah tinggi visual/kolisi saat ini
        return playerState.playerHeight;
    }

    public boolean isDead() {
        return playerState.isDead;
    }

    public int getLives() {
        return playerState.lives;
    }

    public boolean isInvincible() {
        return combatHandler != null ? combatHandler.isInvincible(playerState) : playerState.invincibilityTimer > 0f;
    }

    public boolean isHitByExplosion(float explosionX, float explosionY, float explosionRadius) {
        if (isInvincible()) { // Jika player sedang invincible
            return false;
        }
        // Jika player sudah mati atau sedang menunggu respawn, dia tidak bisa terkena lagi
        if (playerState.isDead || playerState.isWaitingToRespawn) {
            return false;
        }

        Rectangle playerBounds = getBounds(); // Dapatkan bounds pemain saat ini

        // Temukan titik terdekat pada persegi panjang pemain ke pusat lingkaran ledakan
        float closestX = Math.max(playerBounds.x, Math.min(explosionX, playerBounds.x + playerBounds.width));
        float closestY = Math.max(playerBounds.y, Math.min(explosionY, playerBounds.y + playerBounds.height));

        // Hitung jarak kuadrat antara titik terdekat dan pusat lingkaran
        float distanceX = explosionX - closestX;
        float distanceY = explosionY - closestY;
        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);

        // Jika jarak kuadrat kurang dari radius kuadrat ledakan, maka terjadi tabrakan
        return distanceSquared < (explosionRadius * explosionRadius);
    }

    public void takeDeath() { // Dipanggil oleh GameLogicHandler
        if (combatHandler != null) {
            combatHandler.takeDeath(playerState);
        } else { // Fallback jika combatHandler belum siap
            if (playerState.isDead || playerState.lives <= 0) return;
            playerState.isDead = true;
            playerState.lives--;
            playerState.isWaitingToRespawn = true;
            // playerState.respawnTimer = RESPAWN_DELAY_PLAYER; // konstanta ini ada di PlayerCombat
        }
    }

    public void setWeapon(Weapon weapon) {
        if (combatHandler != null) {
            combatHandler.setWeapon(weapon);
        }
    }

    public Weapon getCurrentWeapon() {
        return combatHandler != null ? combatHandler.getCurrentWeapon() : null;
    }

    public String getCurrentWeaponName() {
        return combatHandler != null ? combatHandler.getCurrentWeaponName() : "None";
    }

    public int getCurrentAmmo() {
        return combatHandler != null ? combatHandler.getCurrentAmmo(playerState) : -1;
    }

    public void pickupGrenade(int amount) {
        if (combatHandler != null) {
            combatHandler.pickupGrenade(playerState, amount);
        }
    }

    public int getGrenadeAmmo() {
        return playerState.grenadeAmmo; // Bisa juga combatHandler.getGrenadeAmmo(playerState)
    }

    public boolean isFacingRight() {
        return playerState.facingRight;
    }

    // Metode yang dibutuhkan oleh kelas Weapon (dipanggil via playerRef)
    public float getFireAngle() {
        // Logika dari Player.java lama
        boolean keyLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean keyRight = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean keyUp = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean keyDown = Gdx.input.isKeyPressed(Input.Keys.S);

        if (keyUp) return (float) Math.PI / 2;
        if (keyDown && playerState.isJumping) return (float) -Math.PI / 2; // Gunakan playerState.isJumping
        // Jika tidak ada input arah spesifik, gunakan arah hadap pemain
        // Ini perlu disesuaikan dengan bagaimana senjata Anda ingin targetting bekerja
        // Versi Player.java lama memiliki prioritas: Atas, Bawah (saat lompat), Kiri, Kanan, lalu facingRight
        if (keyLeft) return (float) Math.PI;
        if (keyRight) return 0f;

        return playerState.facingRight ? 0f : (float) Math.PI;
    }

    public float getFireY() {
        // Logika dari Player.java lama
        boolean keyDown = Gdx.input.isKeyPressed(Input.Keys.S);
        // Gunakan playerState.y dan playerState.playerHeight
        return keyDown ? playerState.y + playerState.playerHeight * 0.725f : playerState.y + playerState.playerHeight * 0.80f;
    }

    public Array<Bullet> getBullets() {
        return bullets;
    }


    public void setWallBounds(float leftX, float rightX) { // Digunakan oleh GameLogicHandler
        playerState.leftBoundX = leftX;
        playerState.rightBoundX = rightX;
    }

    // Getter untuk UI (HUD) dari Player.java lama
    public int getDashCount() {
        // Karena dash sekarang unlimited, kita bisa mengembalikan 1 jika cooldown habis, 0 jika tidak.
        // Atau, jika Anda ingin menampilkan "Unlimited", Anda bisa mengubah tipe return ke String.
        // Untuk tujuan HUD, kita bisa mengembalikan 1 jika bisa dash, 0 jika tidak.
        return playerState.dashCooldown <= 0f ? 1 : 0;
    }

    public float getDashCooldownRemaining() {
        return playerState.dashCooldown; // <--- Mengembalikan sisa cooldown dash
    }

    public float getDuckCooldownRemaining() {
        return playerState.duckCooldownTimer;
    }

    public float getDuckTimeRemaining() {
        return playerState.duckTimer;
    }

    public boolean isDucking() {
        return playerState.isDucking;
    }

    public Vector2 getPosition() { // Untuk kompatibilitas jika ada yang menggunakan
        return new Vector2(playerState.x, playerState.y); // Kembalikan salinan baru agar state internal aman
    }
}
