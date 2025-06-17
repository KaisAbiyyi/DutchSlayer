package io.DutchSlayer.attack.boss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.attack.boss.fsm.TankBossFSM;
import io.DutchSlayer.attack.boss.fsm.TankBossState;
import io.DutchSlayer.attack.player.Player;
import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.utils.Constant;


public class TankBoss {

    private final Vector2 position;
    private final float width = 250f;
    private final float height = 150f;
    private final Array<BossBullet> bullets = new Array<>();
    private final Array<BossGrenade> grenades = new Array<>(); // Array untuk granat
    private final TankBossFSM fsm;
    private final Player player;
    private final OrthographicCamera camera;
    private SpriteBatch batch; // Deklarasi SpriteBatch untuk menggambar tekstur

    // Aset visual boss
    private Texture turretTexture;      // asset boss/tank_boss_turret.png
    private Texture chargingTexture;    // asset boss/tank_boss_charging.png
    private Texture grenadeTexture;
    private Texture grenadeProjectileTexture;
    private Texture explosionTexture;// asset boss/tank_boss_grenade.png
    private Texture destroyedTexture;     // asset boss/tank_boss_grenade.png
    private Texture currentTexture;     // Tekstur yang sedang aktif
    private TextureRegion currentRegion;

    private boolean facingRight = true; // Region dari tekstur aktif

    private Sound chargeSound;
    private Sound prepareChargeSound;
    private Sound bulletSound;
    private Sound grenadeThrowSound;
    private Sound grenadeExplosionSound;
    private Sound destroyedSound;
    private GameScreen gameScreen;

    public TankBoss(float x, float y, Player player, OrthographicCamera camera, GameScreen gameScreen) {
        this.position = new Vector2(x, y);
        this.player = player;
        this.camera = camera;
        this.fsm = new TankBossFSM(this, position, camera);
        this.fsm.initialize();
        this.gameScreen = gameScreen;

        turretTexture = new Texture("boss/tank_boss_turret.png");
        chargingTexture = new Texture("boss/tank_boss_charging.png");
        grenadeTexture = new Texture("boss/tank_boss_grenade.png");
        grenadeProjectileTexture = new Texture("player/grenade.png");
        destroyedTexture = new Texture("boss/tank_boss_destroyed.png");
        explosionTexture = new Texture("player/explosion.png");
        currentTexture = turretTexture;
        currentRegion = new TextureRegion(currentTexture);

        if (facingRight && !currentRegion.isFlipX()) { // Jika default menghadap kiri, dan kita ingin menghadap kanan
            currentRegion.flip(true, false);
        }

        // Inisialisasi SpriteBatch
        batch = new SpriteBatch();

        chargeSound = Gdx.audio.newSound(Gdx.files.internal("boss/tank_boss_charging.mp3"));
        prepareChargeSound = Gdx.audio.newSound(Gdx.files.internal("boss/tank_boss_prepare_to_charge.mp3"));
        bulletSound = Gdx.audio.newSound(Gdx.files.internal("boss/tank_boss_bullet.mp3"));
        grenadeThrowSound = Gdx.audio.newSound(Gdx.files.internal("boss/tank_boss_grenade.mp3"));
        grenadeExplosionSound = Gdx.audio.newSound(Gdx.files.internal("player/grenade.mp3"));
        destroyedSound = Gdx.audio.newSound(Gdx.files.internal("boss/tank_boss_destroyed.mp3"));

    }

    public Array<BossBullet> getBullets() {
        return bullets;
    }

    public Array<BossGrenade> getGrenades() { // Getter untuk granat
        return grenades;
    }
    public GameScreen getGameScreen() {
        return this.gameScreen;
    }

    public void playChargeSound() {
        if (chargeSound != null) {
            chargeSound.play(0.8f);
            System.out.println("[Sound] Playing charge sound."); // <-- TAMBAHKAN INI
        } else {
            System.out.println("[Sound ERROR] chargeSound is null!"); // <-- TAMBAHKAN INI
        }
    }

    public void playPrepareChargeSound() {
        if (prepareChargeSound != null) prepareChargeSound.play(0.8f);
    }

    public void playBulletSound() {
        if (bulletSound != null) bulletSound.play(0.6f);
    }

    public void playGrenadeThrowSound() {
        if (grenadeThrowSound != null) grenadeThrowSound.play(0.7f);
    }

    public void playDestroyedSound() {
        if (destroyedSound != null) destroyedSound.play(1.0f); // Full volume untuk kematian
    }

    public void update(float delta) {
        fsm.update(delta);

        // Tentukan arah hadap boss berdasarkan posisi pemain
        if (fsm.getCurrentState() != TankBossState.CHARGE) {
            // Tentukan arah hadap boss berdasarkan posisi pemain
            // Boss menghadap kanan jika pemain di kanannya
            if (player.getX() + player.getWidth() / 2f > position.x + width / 2f) {
                facingRight = true;
            } else { // Pemain di kiri, boss menghadap kiri
                facingRight = false;
            }
        }
        // Jika boss sedang CHARGE, nilai 'facingRight' akan tetap seperti sebelum charge dimulai.

        updateVisualsBasedOnState();

        // Update bullets
        for (int i = bullets.size - 1; i >= 0; i--) {
            BossBullet b = bullets.get(i);
            b.update(delta);
            if (!b.isAlive()) {
                b.dispose(); // Panggil dispose saat peluru tidak lagi hidup
                bullets.removeIndex(i);
            }
        }

        // Update grenades
        for (int i = grenades.size - 1; i >= 0; i--) {
            BossGrenade g = grenades.get(i);
            g.update(delta);
            if (g.hasExploded() && g.isDamagePhase()) {
                if (player.isHitByExplosion(g.getX(), g.getY(), g.getExplosionRadius())) {
                    player.takeDeath();
                }
                g.markDamagePhaseDone();
            }
            if (!g.isAlive()) {
                // g.dispose(); // Jika BossGrenade memiliki tekstur sendiri, panggil dispose di sini
                grenades.removeIndex(i);
            }
        }
    }

    public void onStateChanged() {
        updateVisualsBasedOnState();
    }

    // Metode render yang menerima ShapeRenderer dan SpriteBatch
    // Ini memungkinkan Anda menggambar shape dan tekstur dalam satu panggilan render loop
    public void render(ShapeRenderer renderer, SpriteBatch spriteBatch) {
        // Hanya gunakan ShapeRenderer untuk debug (jika perlu), jangan untuk granat
        fsm.renderDebug(renderer);

        // Gunakan SpriteBatch yang sama dari GameScreen untuk semua gambar
        spriteBatch.begin();

        // 1. Gambar badan boss
        if (currentRegion != null) {
            spriteBatch.draw(currentRegion, position.x, position.y, width, height);
        }

        // 2. Gambar peluru
        for (BossBullet b : bullets) {
            b.render(spriteBatch);
        }

        // 3. Gambar granat & ledakannya
        for (BossGrenade g : grenades) {
            g.render(spriteBatch);
        }

        spriteBatch.end();
    }

    /**
     * Mendapatkan rasio kesehatan bos saat ini (0.0 hingga 1.0).
     * Metode ini akan mendelegasikan panggilan ke FSM.
     *
     * @return Rasio kesehatan bos.
     */
    public float getHealthRatio() {
        if (this.fsm != null) {
            return this.fsm.getHealthRatio(); // Delegasikan ke FSM
        }
        return 0f; // Nilai default jika FSM null (sebaiknya tidak terjadi)
    }


    // Method baru untuk menembakkan burst peluru
    public void fireBurst(int numberOfBullets) {
        float directionToPlayer = Math.signum(player.getX() - (position.x + width / 2f));
        float spawnX = position.x + width / 2f;
        float spawnY = position.y + height / 2f;

        for (int i = 0; i < numberOfBullets; i++) {
            bullets.add(new BossBullet(spawnX, spawnY, directionToPlayer, facingRight));
        }
        playBulletSound(); // <-- BARU: Panggil suara tembakan
        System.out.println("[Boss] Fired burst of " + numberOfBullets + " bullets.");
    }

    // Method baru untuk meluncurkan granat
    // relativeDirection: -1 untuk kiri player, 1 untuk kanan player
    public void launchGrenadeTowardsPlayerSide(float relativeSide) {
        float launchX = position.x + width / 2f;
        float launchY = position.y + height * 0.8f; // Posisi peluncuran granat (misal, sedikit di atas)

        // Perkirakan posisi target X relatif terhadap player
        float playerTargetX = player.getX() + (Constant.PLAYER_WIDTH * 1.5f * relativeSide); // Target di sisi player + offset
        float targetY = Constant.TERRAIN_HEIGHT; // **Set target Y ke Constant.TERRAIN_HEIGHT**

        float distanceX = playerTargetX - launchX;
        float distanceY = targetY - launchY;
        float gravity = 700f;
        float initialVy = 350f; // Initial upward velocity, can be tweaked for arc height

        // Solve for time of flight (t) using the quadratic formula for vertical motion:
        // 0.5 * g * t^2 - initialVy * t + distanceY = 0
        // a = 0.5 * g, b = -initialVy, c = distanceY
        float a = 0.5f * gravity;
        float b = -initialVy;
        float c = distanceY;

        float discriminant = b * b - 4 * a * c;
        float timeToTarget;

        if (discriminant < 0) {
            // No real solution, means the grenade can't reach the target Y with this initialVy
            // or gravity. Adjust initialVy or handle error.
            // For now, let's just pick a default time or adjust initialVy.
            timeToTarget = 1.0f; // Fallback time
            System.err.println("Warning: Cannot reach target Y with given initialVy. Adjusting time.");
        } else {
            float t1 = (-b + (float) Math.sqrt(discriminant)) / (2 * a);
            float t2 = (-b - (float) Math.sqrt(discriminant)) / (2 * a);

            // Choose the positive time. If both are positive, choose the one that makes sense
            // (e.g., shorter time for a more direct arc, or longer for a higher arc if desired).
            // Usually, the larger positive root is the time to land if launched upwards.
            if (t1 > 0 && t2 > 0) {
                timeToTarget = Math.max(t1, t2);
            } else if (t1 > 0) {
                timeToTarget = t1;
            } else {
                timeToTarget = t2; // Should be positive if discriminant is non-negative and some solution exists
            }

            // Ensure timeToTarget is not too small to avoid extremely high horizontal velocity
            if (timeToTarget < 0.1f) { // Arbitrary minimum time
                timeToTarget = 0.1f;
            }
        }

        // Calculate initialVx based on desired horizontal distance and time of flight
        float initialVx = distanceX / timeToTarget;

        // Batasi kecepatan Vx agar tidak terlalu ekstrim
        float maxVx = 600f; // Increased maxVx for better range
        initialVx = Math.max(-maxVx, Math.min(maxVx, initialVx));


        grenades.add(new BossGrenade(launchX, launchY, initialVx, initialVy, grenadeProjectileTexture, explosionTexture, grenadeExplosionSound));
        playGrenadeThrowSound();
        System.out.println("[Boss] Launched grenade towards player side " + relativeSide +
            " (vx: " + initialVx + ", vy: " + initialVy + ", targetY: " + targetY + ")");
    }


    // Method untuk membersihkan semua proyektil (berguna saat boss mati)
    public void clearAllProjectiles() {
        // Panggil dispose untuk setiap peluru sebelum membersihkan array
        for (BossBullet b : bullets) {
            b.dispose();
        }
        bullets.clear();
        // Jika BossGrenade memiliki resource yang perlu di-dispose, lakukan juga di sini
        grenades.clear();
    }


    // fireCannon() dan machineGunFire() lama mungkin tidak dipakai jika pola serangan sudah tetap.
    // Bisa dihapus atau disimpan jika akan dipakai di Phase Two atau konteks lain.
    public void fireCannon() {
        float direction = Math.signum(player.getX() - position.x);
        // PERUBAHAN UTAMA DI SINI: Meneruskan `facingRight`
        bullets.add(new BossBullet(position.x + width / 2f, position.y + height / 2f, direction, facingRight));
    }

    public void machineGunFire(int count) {
        float direction = Math.signum(player.getX() - position.x);
        for (int i = 0; i < count; i++) {
            // PERUBAHAN UTAMA DI SINI: Meneruskan `facingRight`
            bullets.add(new BossBullet(position.x + width / 2f, position.y + height / 2f, direction, facingRight));
        }
    }


    public void checkHitByExplosion(float x, float y, float radius, float damage) {
        float centerX = position.x + width / 2f;
        float centerY = position.y + height / 2f;

        float dx = x - centerX;
        float dy = y - centerY;
        float dist2 = dx * dx + dy * dy;

        if (dist2 <= radius * radius) {
            takeHit(damage);
        }
    }

    public String getName() {
        return BossType.TANK.getDisplayName();
    }

    public void charge(float distance) { // Kemungkinan tidak dipakai langsung jika FSM yang menggerakkan
        position.x += distance;
    }

    public void spawnTurretMinion() {
        System.out.println("[Boss] Turret minion spawned at " + position);
        // TODO: implement spawning of turret minion entity
    }

    public void move(float dx, float dy) {
        position.add(dx, dy);
    }

    public Vector2 getPlayerPosition() {
        return player.getPosition();
    }

    public boolean isCharging() {
        return fsm.isCharging();
    }

    public void takeHit(float damage) {
        fsm.takeDamage(damage);
    }

    public boolean isAlive() {
        return !fsm.isDead();
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean isInvincible() {
        return fsm.isInvincible();
    }

    public TankBossState getCurrentState() {
        return fsm.getCurrentState();
    }

    private void updateVisualsBasedOnState() {
        TankBossState currentState = fsm.getCurrentState();
        Texture newTexture = null;

        switch (currentState) {
            case IDLE:
            case PERFORMING_BURST:
            case PREPARE_CHARGE:
            case BURST_COOLDOWN:
            case PRE_GRENADE_DELAY:
                newTexture = turretTexture;
                break;
            case ENTERING_ARENA:
            case CHARGE:
                newTexture = chargingTexture;
                break;
            case PERFORMING_GRENADE_TOSS:
            case GRENADE_TOSS_COOLDOWN:
                newTexture = grenadeTexture;
                break;
            case DEAD:
                newTexture = destroyedTexture;
                break;
        }

        // Jika tekstur berubah, buat TextureRegion baru
        if (newTexture != currentTexture) {
            currentTexture = newTexture;
            if (currentTexture != null) {
                currentRegion = new TextureRegion(currentTexture);
            } else {
                currentRegion = null;
            }
        }

        // <--- Logika flipping yang harus ada di sini
        if (currentRegion != null) {
            // Asumsi: Gambar aset default menghadap KIRI.
            // Jika boss seharusnya menghadap kanan (facingRight = true)
            // DAN currentRegion saat ini TIDAK ter-flip (berarti dia menghadap kiri), maka flip.
            if (facingRight && !currentRegion.isFlipX()) {
                currentRegion.flip(true, false);
            }
            // Jika boss seharusnya menghadap kiri (facingRight = false)
            // DAN currentRegion saat ini ter-flip (berarti dia menghadap kanan), maka flip kembali.
            else if (!facingRight && currentRegion.isFlipX()) {
                currentRegion.flip(true, false);
            }
        }
    }

    // Getter baru untuk TextureRegion yang aktif
    public TextureRegion getCurrentRegion() {
        return currentRegion;
    }

    // Metode dispose untuk membebaskan memori tekstur
    // Penting untuk memanggil dispose pada semua Texture dan SpriteBatch
    public void dispose() {
        if (turretTexture != null) turretTexture.dispose();
        if (chargingTexture != null) chargingTexture.dispose();
        if (grenadeTexture != null) grenadeTexture.dispose();
        if (grenadeProjectileTexture != null) grenadeProjectileTexture.dispose();
        if (explosionTexture != null) explosionTexture.dispose();
        if (destroyedTexture != null) destroyedTexture.dispose();
        if (batch != null) batch.dispose(); // Dispose SpriteBatch juga
        // Pastikan juga memanggil dispose untuk setiap peluru yang masih hidup
        for (BossBullet b : bullets) {
            b.dispose();
        }

        if (chargeSound != null) chargeSound.dispose();
        if (prepareChargeSound != null) prepareChargeSound.dispose();
        if (bulletSound != null) bulletSound.dispose();
        if (grenadeThrowSound != null) grenadeThrowSound.dispose();
        if (grenadeExplosionSound != null) grenadeExplosionSound.dispose();
        if (destroyedSound != null) destroyedSound.dispose();
    }
}
