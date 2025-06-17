package io.DutchSlayer.attack.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.attack.enemy.fsm.EnemyFSM;
import io.DutchSlayer.attack.enemy.fsm.EnemyState;
import io.DutchSlayer.attack.player.weapon.Bullet;
import io.DutchSlayer.attack.player.weapon.Grenade;
import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.utils.Constant;

/**
 * Enemy dasar dengan FSM dan satu jenis serangan tetap berdasarkan AttackType.
 */
public class BasicEnemy {

    private float x, y;
    private final float width, height;
    private final float baseSpeed;

    private final AttackType attackType;
    private final EnemyFSM fsm;

    private boolean isAlive = true;
    private int maxHealth = 3;
    private int currentHealth = maxHealth;

    private final Array<Bullet> bullets = new Array<>();
    private float fireCooldown = 1.5f;
    private float fireTimer = 0f;

    private float patrolMinX, patrolMaxX;
    private boolean movingRight = true;

    private Sound throwGrenadeSound;
    private Sound deathSound; // Tambahkan Sound untuk suara kematian

    private final float awarenessRadius = Constant.SCREEN_WIDTH / 2f;
    private final float attackDistance = 800f;
    public static final float FIXED_DELTA = 1f / 60f;
    private Vector2 playerRef;

    private float reloadTime;      // Waktu reload setelah beberapa tembakan
    private float reloadTimer = 0f;
    private int shotsFired = 0;
    private int maxShotsBeforeReload;
    private float lastDelta;
    private float chaseDelay = 2.0f;
    private float chaseDelayTimer = 0f;
    private boolean isChasePrepared = false;
    private Sound shootSound;

    private EnemyVisuals visuals;

    private AttackType[] additionalAttackTypes; // NEW: Jenis serangan tambahan yang bisa dilakukan
    private GameScreen gameScreenRef;
    private float deathTimer = 0f;
    private int currentAttackPhaseIndex = 0; // Melacak fase saat ini dalam pola
    private float attackPhaseTimer = 0f;     // Timer untuk delay antar aksi dalam satu fase
    private int shotsInPhase = 0;            // Melacak tembakan/lemparan dalam satu fase
    private float enemyBurstTimer = 0f;
    private int enemyBurstIndex = 0;
    private static final float ENEMY_BURST_DELAY = 0.08f;
    private boolean dropChecked = false;

    public BasicEnemy(AttackType type, float spawnX, float spawnY, GameScreen gameScreen) {
        this.attackType = type;
        this.x = spawnX;
        this.y = spawnY;
        this.width = Constant.PLAYER_WIDTH * 1.75f;
        this.height = Constant.PLAYER_HEIGHT * 1.25f;
        this.baseSpeed = Constant.PLAYER_SPEED * 0.4f;
        this.gameScreenRef = gameScreen;

        this.patrolMinX = Math.max(0, spawnX - 80);
        this.patrolMaxX = Math.min(Constant.MAP_WIDTH, spawnX + 80);

        this.fsm = new EnemyFSM(this);
        configureWeaponByType();

        if (attackType == AttackType.STRAIGHT_SHOOT || attackType == AttackType.BURST_FIRE) {
            shootSound = Gdx.audio.newSound(Gdx.files.internal("player/pistol.mp3"));
        }

        if (attackType == AttackType.ARC_GRENADE || attackType == AttackType.BURST_FIRE) {
            this.throwGrenadeSound = Gdx.audio.newSound(Gdx.files.internal("player/grenade_throw.mp3"));
        }

        // Inisialisasi suara kematian
        deathSound = Gdx.audio.newSound(Gdx.files.internal("enemy/enemy_death.mp3"));

        this.visuals = new EnemyVisuals(this.attackType);
    }

    private void configureWeaponByType() {
        switch (attackType) {
            case STRAIGHT_SHOOT -> {
                fireCooldown = 0.5f;
                reloadTime = 3.0f;
                maxShotsBeforeReload = 3;
                this.additionalAttackTypes = new AttackType[]{};
            }
            case BURST_FIRE -> {
                fireCooldown = 0.5f; // Ini akan jadi interval antar burst
                reloadTime = 3.0f;
                maxShotsBeforeReload = 2;
                this.additionalAttackTypes = new AttackType[]{AttackType.ARC_GRENADE};
            }
            case ARC_GRENADE -> {
                fireCooldown = 2.5f;
                reloadTime = 3.0f;
                maxShotsBeforeReload = 2;
                this.additionalAttackTypes = new AttackType[]{AttackType.STRAIGHT_SHOOT};
            }
        }
        currentAttackPhaseIndex = 0;
        attackPhaseTimer = 0f;
        shotsInPhase = 0;
        // NEW: Reset burst fire musuh
        enemyBurstIndex = 0;
        enemyBurstTimer = 0f;
    }

    public void update(float delta, Vector2 playerPos) {
        this.lastDelta = delta;
        this.playerRef = playerPos;
        fsm.update();

        if (fsm.getCurrentState() == EnemyState.DYING) {
            deathTimer -= delta; // Kurangi timer
            if (deathTimer <= 0) {
                isAlive = false; // Setelah timer habis, baru benar-benar "mati"
                if (visuals != null) {
                    visuals.dispose();
                    visuals = null;
                }
            }
        }

        // Update bullet logic
        for (Bullet bullet : bullets) {
            bullet.update(delta, -Float.MAX_VALUE, Float.MAX_VALUE);
        }
        for (int i = bullets.size - 1; i >= 0; i--) {
            if (!bullets.get(i).isAlive()) bullets.removeIndex(i);
        }
    }

    public boolean hasDropBeenChecked() {
        return this.dropChecked;
    }

    public void setDropChecked(boolean value) {
        this.dropChecked = value;
    }

    // FSM: PATROL
    public void updatePatrol() {
        float patrolSpeed = baseSpeed;
        if (movingRight) {
            x += patrolSpeed * FIXED_DELTA;
            if (x >= patrolMaxX) {
                x = patrolMaxX;
                movingRight = false;
            }
        } else {
            x -= patrolSpeed * FIXED_DELTA;
            if (x <= patrolMinX) {
                x = patrolMinX;
                movingRight = true;
            }
        }

        // Transisi jika player dekat
        if (playerRef != null && Math.abs(playerRef.x - x) <= Constant.PLAYER_WIDTH * 10f) {
            fsm.changeState(EnemyState.CHASE);
        }
    }

    // FSM: CHASEs
    public void updateChase() {
        if (playerRef == null) return;

        float dx = playerRef.x - x;
        float distance = Math.abs(dx);
        float chaseSpeed = Constant.PLAYER_SPEED * 1.2f;

        this.movingRight = dx > 0;

        if (distance > width / 2f) {
            x += (dx < 0 ? -chaseSpeed : chaseSpeed) * lastDelta;
        }

        if (distance <= width * 8f) {
            fsm.changeState(EnemyState.SHOOT); // baru nembak saat cukup dekat
        } else if (distance > width * 15f) {
            fsm.changeState(EnemyState.PATROL); // terlalu jauh
        }
    }


    public void setChasePrepared(boolean b) {
        this.isChasePrepared = b;
    }

    public void setChaseDelayTimer(float t) {
        this.chaseDelayTimer = t;
    }

    public EnemyState getCurrentState() {
        return fsm.getCurrentState();
    }

    // FSM: SHOOT
    public void updateShoot() {
        if (playerRef == null) return;

        float dx = playerRef.x - x;
        float distanceToPlayer = Math.abs(dx);

        this.movingRight = dx > 0;

        if (distanceToPlayer > Constant.PLAYER_WIDTH * 10f) {
            fsm.changeState(EnemyState.CHASE);
            return;
        }

        float cameraLeft = playerRef.x - Constant.SCREEN_WIDTH / 2f;
        float cameraRight = playerRef.x + Constant.SCREEN_WIDTH / 2f;
        if (x + width < cameraLeft || x > cameraRight) return;

        if (reloadTimer > 0f) {
            reloadTimer -= lastDelta;
            return;
        }

        // NEW: Kelola Burst Fire Musuh secara terpisah
        if (attackType == AttackType.BURST_FIRE && enemyBurstIndex > 0 && enemyBurstIndex <= 3) {
            enemyBurstTimer += lastDelta;
            if (enemyBurstTimer >= ENEMY_BURST_DELAY) {
                enemyBurstTimer -= ENEMY_BURST_DELAY;
                shootStraight(); // Gunakan shootStraight untuk menembak 1 peluru
                enemyBurstIndex++; // Tingkatkan index peluru dalam burst
            }
            if (enemyBurstIndex > 3) { // Burst selesai
                enemyBurstIndex = 0; // Reset
                attackPhaseTimer = fireCooldown; // Set cooldown antar burst
            }
            return; // Penting: Jangan lanjutkan ke logika performAttack() jika sedang dalam burst
        }

        attackPhaseTimer -= lastDelta;
        if (attackPhaseTimer <= 0f) {
            performAttack();
        }
    }


    // NEW: Metode untuk memilih dan melakukan serangan berdasarkan pola
    private void performAttack() {
        switch (attackType) {
            case STRAIGHT_SHOOT -> {
                // Pola: 3x STRAIGHT_SHOOT (0.5s interval), lalu 3s delay
                if (shotsInPhase < 3) {
                    shootStraight();
                    shotsInPhase++;
                    attackPhaseTimer = 0.5f; // Interval antar tembakan
                } else {
                    // Selesai 3 tembakan, mulai delay penuh (reload)
                    reloadTimer = 3.0f;
                    shotsInPhase = 0; // Reset untuk pola berikutnya
                    attackPhaseTimer = 0f; // Pastikan timer tidak negatif
                }
            }
            case ARC_GRENADE -> {
                // Pola: 2x ARC_GRENADE (2.5s interval) -> 3x STRAIGHT_SHOOT (1s interval) -> 3s delay
                if (currentAttackPhaseIndex == 0) { // Fase 1: Granat
                    if (shotsInPhase < 2) {
                        throwArcGrenade();
                        shotsInPhase++;
                        attackPhaseTimer = 2.5f; // Interval antar granat
                    } else {
                        // Selesai 2 granat, pindah ke fase 2 (Straight Shoot)
                        currentAttackPhaseIndex = 1;
                        shotsInPhase = 0; // Reset untuk fase baru
                        attackPhaseTimer = 0f; // Langsung mulai fase berikutnya
                    }
                } else if (currentAttackPhaseIndex == 1) { // Fase 2: Straight Shoot
                    if (shotsInPhase < 3) {
                        shootStraight();
                        shotsInPhase++;
                        attackPhaseTimer = 1.0f; // Interval antar tembakan lurus
                    } else {
                        // Selesai 3 tembakan lurus, kembali ke awal pola (reload)
                        reloadTimer = 3.0f; // Delay penuh
                        currentAttackPhaseIndex = 0; // Kembali ke fase granat
                        shotsInPhase = 0; // Reset untuk pola berikutnya
                        attackPhaseTimer = 0f;
                    }
                }
            }
            case BURST_FIRE -> {
                // Pola: 2x BURST_FIRE (0.5s interval) -> 1s delay -> 1x ARC_GRENADE -> 3s delay
                if (currentAttackPhaseIndex == 0) { // Fase 1: Burst Fire
                    if (shotsInPhase < 2) {
                        // NEW: Mulai burst (akan ditangani di updateShoot())
                        enemyBurstIndex = 1; // Set ke 1 untuk memulai burst
                        enemyBurstTimer = 0f; // Reset timer burst
                        shotsInPhase++;
                        // attackPhaseTimer akan diatur setelah burst selesai (di updateShoot)
                    } else {
                        // Selesai 2 burst, pindah ke fase 2 (delay sebelum granat)
                        currentAttackPhaseIndex = 1;
                        shotsInPhase = 0; // Reset
                        attackPhaseTimer = 1.0f; // Delay 1 detik sebelum granat
                    }
                } else if (currentAttackPhaseIndex == 1) { // Fase 2: Lempar Granat
                    throwArcGrenade();
                    currentAttackPhaseIndex = 0; // Kembali ke fase 0 untuk pola berikutnya
                    shotsInPhase = 0; // Reset
                    reloadTimer = 3.0f; // Delay penuh setelah granat
                    attackPhaseTimer = 0f;
                }
            }
        }
    }

    public void checkHitByExplosion(float explosionX, float explosionY, float radius, float damage) {
        if (!isAlive) return;

        float centerX = x + width / 2f;
        float centerY = y + height / 2f;

        float distanceSq = Vector2.dst2(explosionX, explosionY, centerX, centerY);
        float radiusSq = radius * radius;

        if (distanceSq <= radiusSq) {
            System.out.println("Enemy hit by explosion! Damage: " + damage);
            takeExplosionDamage(damage);
        }
    }

    private void takeExplosionDamage(float dmg) {
        // --- MODIFIKASI: Logika saat terkena ledakan ---
        if (fsm.getCurrentState() == EnemyState.DYING || !isAlive) return;

        int damage = Math.round(dmg);
        currentHealth -= damage;

        if (currentHealth <= 0) {
            currentHealth = 0;
            fsm.changeState(EnemyState.DYING); // Ubah state, jangan langsung set isAlive = false
            deathSound.play(0.35f); // Mainkan suara kematian dengan volume 50% lebih kecil (0.7 * 0.5 = 0.35)
            System.out.println("Enemy killed by explosion!");
        }
    }


    private void shootStraight() {
        if (shootSound != null) { // Mainkan suara jika sudah dimuat
            shootSound.play(0.5f); // Volume 0.5f (opsional, sesuaikan)
        }
        float cx = x + width / 2f;
        float cy = y + height / 1.75f;
        boolean shootRight = playerRef.x > x;
        float angle = shootRight ? 0f : (float) Math.PI;

        Texture bulletTex = new Texture(Gdx.files.internal("player/bullet.png"));
        TextureRegion region = new TextureRegion(bulletTex);

        if (!shootRight) {
            region.flip(true, false); // flip horizontal jika ke kiri
        }

        Bullet bullet = new Bullet(cx, cy, angle, true);
        bullet.setTextureRegion(region);
        bullets.add(bullet);
    }


    private void burstFire() {
        float cx = x + width / 2f;
        float cy = y + height / 2f;
        boolean shootRight = playerRef.x > x;
        float angle = shootRight ? 0f : (float) Math.PI;

        Texture bulletTex = new Texture(Gdx.files.internal("player/bullet.png"));
        TextureRegion region = new TextureRegion(bulletTex);

        if (!shootRight) {
            region.flip(true, false);
        }

        for (int i = 0; i < 3; i++) {
            Bullet bullet = new Bullet(cx, cy, angle, true);
            bullet.setTextureRegion(region);
            bullets.add(bullet);
        }
    }


    private void throwArcGrenade() {
        if (gameScreenRef == null) {
            System.err.println("BasicEnemy: GameScreen reference is null, cannot throw grenade.");
            return;
        }

        Texture grenadeTex = gameScreenRef.getGrenadeTexture();
        Texture explosionTex = gameScreenRef.getExplosionTexture();

        if (throwGrenadeSound != null) { // <-- BARU
            throwGrenadeSound.play(0.6f); // Mainkan suara dengan volume lebih rendah untuk musuh
        }

        // Tentukan posisi awal granat
        float startX = x + width / 2f;
        float startY = y + height * 0.75f; // Sedikit di atas musuh

        // Tentukan arah lemparan granat berdasarkan arah hadap musuh
        // Anda perlu menentukan sudut dan kekuatan. Ini bisa disesuaikan.
        // Contoh: Melempar ke arah player dengan sudut tertentu (misal, 45 derajat)
        float targetX = playerRef.x;
        float targetY = playerRef.y;

        // Hitung sudut awal dan kekuatan
        // Ini adalah contoh sederhana, Anda mungkin ingin menggunakan fisika parabola yang lebih akurat
        // Untuk "arc", Anda bisa menggunakan sudut tetap (misal, 45 derajat) dan menyesuaikan kekuatan
        float angleRad;
        float power = 600f; // Kekuatan lemparan, sesuaikan

        if (movingRight) {
            // Lempar ke kanan dengan sudut ke atas
            angleRad = MathUtils.degreesToRadians * 45f;
        } else {
            // Lempar ke kiri dengan sudut ke atas
            angleRad = MathUtils.degreesToRadians * (180f - 45f);
        }

        // Buat objek Grenade baru
        Grenade grenade = new Grenade(startX, startY, angleRad, power, true, grenadeTex, explosionTex);

        // Tambahkan granat ke daftar granat di GameScreen
        gameScreenRef.getGrenades().add(grenade);

        System.out.println("Enemy throwing arc grenade!");
    }

    private void dashTowardsPlayer() {
        float dx = playerRef.x - x;
        float dashSpeed = Constant.PLAYER_SPEED * 2.5f;
        x += (dx < 0 ? -dashSpeed : dashSpeed) * FIXED_DELTA;
    }

    private void jumpSmash() {
        // Placeholder: lompat dan AoE smash
        System.out.println("Performing jump attack");
    }

    public void takeHit() {
        // --- MODIFIKASI: Logika saat terkena tembakan ---
        if (fsm.getCurrentState() == EnemyState.DYING || !isAlive) return;

        currentHealth--;
        if (currentHealth <= 0) {
            currentHealth = 0;
            fsm.changeState(EnemyState.DYING); // Ubah state ke DYING
            deathSound.play(0.35f); // Mainkan suara kematian dengan volume 50% lebih kecil (0.7 * 0.5 = 0.35)
        }
    }


    public void render(ShapeRenderer shapeRenderer) {
        // --- MODIFIKASI: Jangan render health bar jika sudah mati/sekarat ---
        if (isAlive && fsm.getCurrentState() != EnemyState.DYING) {
            float barY = y + height + 4f;
            shapeRenderer.setColor(0.5f, 0, 0, 1);
            shapeRenderer.rect(x, barY, width, 4f);
            shapeRenderer.setColor(1f, 0f, 0f, 1);
            shapeRenderer.rect(x, barY, width * ((float) currentHealth / maxHealth), 4f);
        }
    }

    public void render(SpriteBatch spriteBatch, float delta) {
        // Render peluru di belakang musuh jika diinginkan, atau di depan (setelahnya)
        for (Bullet bullet : bullets) {
            bullet.render(spriteBatch);
        }

        // Hanya render musuh jika isAlive (termasuk selama animasi kematian)
        if (isAlive) {
            TextureRegion frame = visuals.getFrameToRender(fsm.getCurrentState(), movingRight, delta);

            // --- KODE BARU DIMULAI DI SINI ---

            // Tentukan dimensi render default
            float renderWidth = this.width;
            float renderHeight = this.height;

            // Jika musuh dalam state DYING, sesuaikan tinggi rendernya
            // agar tidak terentang secara vertikal.
            // Nilai this.height / 2f adalah titik awal yang baik,
            // Anda bisa menyesuaikannya agar pas dengan aset Anda.
            if (fsm.getCurrentState() == EnemyState.DYING) {
                renderHeight = this.height / 2f;
            }

            // Gunakan dimensi render yang sudah disesuaikan
            spriteBatch.draw(frame, x, (y - 20f), renderWidth, renderHeight);

            // --- KODE BARU BERAKHIR DI SINI ---
        }
    }


    public void setDeathTimer(float duration) {
        this.deathTimer = duration;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void dispose() {
        if (shootSound != null) {
            shootSound.dispose();
        }

        if (throwGrenadeSound != null) {
            throwGrenadeSound.dispose();
        }

        if (deathSound != null) { // Buang suara kematian
            deathSound.dispose();
        }

        if (visuals != null) {
            visuals.dispose();
        }
        // Dispose bullet textures jika ada yang dibuat di shootStraight/burstFire
        // Saat ini, Bullet membuat Texture-nya sendiri, jadi pastikan Bullet dispose dirinya.
    }

    public Array<Bullet> getBullets() {
        return bullets;
    }

    public boolean isMovingRight() {
        return movingRight;
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
}
