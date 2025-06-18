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
    private Sound deathSound;
    private Sound shootSound; // Deklarasi shootSound di sini

    private final float awarenessRadius = Constant.SCREEN_WIDTH / 2f;
    private final float attackDistance = 800f;
    public static final float FIXED_DELTA = 1f / 60f;
    private Vector2 playerRef;

    private float reloadTime;
    private float reloadTimer = 0f;
    private int shotsFired = 0;
    private int maxShotsBeforeReload;
    private float lastDelta;
    private float chaseDelay = 2.0f;
    private float chaseDelayTimer = 0f;
    private boolean isChasePrepared = false;


    private EnemyVisuals visuals;

    private AttackType[] additionalAttackTypes;
    private GameScreen gameScreenRef;
    private float deathTimer = 0f;
    private int currentAttackPhaseIndex = 0;
    private float attackPhaseTimer = 0f;
    private int shotsInPhase = 0;
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

        // --- PERBAIKAN: INISIALISASI shootSound UNTUK SEMUA TIPE YANG MUNGKIN MENEMBAK ---
        // Jika AttackType adalah STRAIGHT_SHOOT, BURST_FIRE, ATAU ARC_GRENADE
        // Karena ARC_GRENADE juga melakukan STRAIGHT_SHOOT dalam polanya
        if (attackType == AttackType.STRAIGHT_SHOOT || attackType == AttackType.BURST_FIRE || attackType == AttackType.ARC_GRENADE) {
            shootSound = Gdx.audio.newSound(Gdx.files.internal("player/pistol.mp3"));
        }

        if (attackType == AttackType.ARC_GRENADE || attackType == AttackType.BURST_FIRE) {
            this.throwGrenadeSound = Gdx.audio.newSound(Gdx.files.internal("player/grenade_throw.mp3"));
        }

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
                fireCooldown = 0.5f;
                reloadTime = 3.0f;
                maxShotsBeforeReload = 2;
                this.additionalAttackTypes = new AttackType[]{AttackType.ARC_GRENADE};
            }
            case ARC_GRENADE -> {
                fireCooldown = 2.5f; // Ini adalah cooldown utama untuk memulai pola granat
                reloadTime = 3.0f;
                maxShotsBeforeReload = 2; // Ini mungkin tidak relevan untuk pola yang lebih kompleks
                this.additionalAttackTypes = new AttackType[]{AttackType.STRAIGHT_SHOOT};
            }
        }
        currentAttackPhaseIndex = 0;
        attackPhaseTimer = 0f;
        shotsInPhase = 0;
        enemyBurstIndex = 0;
        enemyBurstTimer = 0f;
    }

    public void update(float delta, Vector2 playerPos) {
        this.lastDelta = delta;
        this.playerRef = playerPos;
        fsm.update();

        if (fsm.getCurrentState() == EnemyState.DYING) {
            deathTimer -= delta;
            if (deathTimer <= 0) {
                isAlive = false;
                if (visuals != null) {
                    visuals.dispose();
                    visuals = null;
                }
            }
        }

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

        if (playerRef != null && Math.abs(playerRef.x - x) <= Constant.PLAYER_WIDTH * 10f) {
            fsm.changeState(EnemyState.CHASE);
        }
    }

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
            fsm.changeState(EnemyState.SHOOT);
        } else if (distance > width * 15f) {
            fsm.changeState(EnemyState.PATROL);
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

        if (attackType == AttackType.BURST_FIRE && enemyBurstIndex > 0 && enemyBurstIndex <= 3) {
            enemyBurstTimer += lastDelta;
            if (enemyBurstTimer >= ENEMY_BURST_DELAY) {
                enemyBurstTimer -= ENEMY_BURST_DELAY;
                shootStraight();
                enemyBurstIndex++;
            }
            if (enemyBurstIndex > 3) {
                enemyBurstIndex = 0;
                attackPhaseTimer = fireCooldown;
            }
            return;
        }

        attackPhaseTimer -= lastDelta;
        if (attackPhaseTimer <= 0f) {
            performAttack();
        }
    }

    private void performAttack() {
        switch (attackType) {
            case STRAIGHT_SHOOT -> {
                if (shotsInPhase < 3) {
                    shootStraight();
                    shotsInPhase++;
                    attackPhaseTimer = 0.5f;
                } else {
                    reloadTimer = 3.0f;
                    shotsInPhase = 0;
                    attackPhaseTimer = 0f;
                }
            }
            case ARC_GRENADE -> {
                if (currentAttackPhaseIndex == 0) {
                    if (shotsInPhase < 2) {
                        throwArcGrenade();
                        shotsInPhase++;
                        attackPhaseTimer = 2.5f;
                    } else {
                        currentAttackPhaseIndex = 1;
                        shotsInPhase = 0;
                        attackPhaseTimer = 0f;
                    }
                } else if (currentAttackPhaseIndex == 1) { // Fase 2: Straight Shoot
                    if (shotsInPhase < 3) {
                        shootStraight(); // Panggil shootStraight di sini
                        shotsInPhase++;
                        attackPhaseTimer = 1.0f;
                    } else {
                        reloadTimer = 3.0f;
                        currentAttackPhaseIndex = 0;
                        shotsInPhase = 0;
                        attackPhaseTimer = 0f;
                    }
                }
            }
            case BURST_FIRE -> {
                if (currentAttackPhaseIndex == 0) {
                    if (shotsInPhase < 2) {
                        enemyBurstIndex = 1;
                        enemyBurstTimer = 0f;
                        shotsInPhase++;
                    } else {
                        currentAttackPhaseIndex = 1;
                        shotsInPhase = 0;
                        attackPhaseTimer = 1.0f;
                    }
                } else if (currentAttackPhaseIndex == 1) {
                    throwArcGrenade();
                    currentAttackPhaseIndex = 0;
                    shotsInPhase = 0;
                    reloadTimer = 3.0f;
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
        if (fsm.getCurrentState() == EnemyState.DYING || !isAlive) return;

        int damage = Math.round(dmg);
        currentHealth -= damage;

        if (currentHealth <= 0) {
            currentHealth = 0;
            fsm.changeState(EnemyState.DYING);
            deathSound.play(0.35f);
            System.out.println("Enemy killed by explosion!");
        }
    }

    private void shootStraight() {
        // --- PERBAIKAN: Pastikan shootSound dimainkan di sini ---
        if (shootSound != null) {
            shootSound.play(0.5f); // Volume 0.5f (opsional, sesuaikan)
        }
        float cx = x + width / 2f;
        float cy = y + height / 1.75f;
        boolean shootRight = playerRef.x > x;
        float angle = shootRight ? 0f : (float) Math.PI;

        Texture bulletTex = new Texture(Gdx.files.internal("player/bullet.png"));
        TextureRegion region = new TextureRegion(bulletTex);

        if (!shootRight) {
            region.flip(true, false);
        }

        Bullet bullet = new Bullet(cx, cy, angle, true);
        bullet.setTextureRegion(region);
        bullets.add(bullet);
    }

    private void burstFire() {
        // This method is not called directly in performAttack anymore for BURST_FIRE
        // The individual shoots are handled by shootStraight() in updateShoot() loop.
        // So, the sound should primarily be in shootStraight().
        // If you want a *different* sound for the *start* of a burst, you'd add it where enemyBurstIndex is set to 1.
    }

    private void throwArcGrenade() {
        if (gameScreenRef == null) {
            System.err.println("BasicEnemy: GameScreen reference is null, cannot throw grenade.");
            return;
        }

        Texture grenadeTex = gameScreenRef.getGrenadeTexture();
        Texture explosionTex = gameScreenRef.getExplosionTexture();

        if (throwGrenadeSound != null) {
            throwGrenadeSound.play(0.6f);
        }

        float startX = x + width / 2f;
        float startY = y + height * 0.75f;

        float targetX = playerRef.x;
        float targetY = playerRef.y;

        float angleRad;
        float power = 600f;

        if (movingRight) {
            angleRad = MathUtils.degreesToRadians * 45f;
        } else {
            angleRad = MathUtils.degreesToRadians * (180f - 45f);
        }

        Grenade grenade = new Grenade(startX, startY, angleRad, power, true, grenadeTex, explosionTex);

        gameScreenRef.getGrenades().add(grenade);

        System.out.println("Enemy throwing arc grenade!");
    }

    private void dashTowardsPlayer() {
        float dx = playerRef.x - x;
        float dashSpeed = Constant.PLAYER_SPEED * 2.5f;
        x += (dx < 0 ? -dashSpeed : dashSpeed) * FIXED_DELTA;
    }

    private void jumpSmash() {
        System.out.println("Performing jump attack");
    }

    public void takeHit() {
        if (fsm.getCurrentState() == EnemyState.DYING || !isAlive) return;

        currentHealth--;
        if (currentHealth <= 0) {
            currentHealth = 0;
            fsm.changeState(EnemyState.DYING);
            deathSound.play(0.35f);
        }
    }

    public void render(ShapeRenderer shapeRenderer) {
        if (isAlive && fsm.getCurrentState() != EnemyState.DYING) {
            float barY = y + height + 4f;
            shapeRenderer.setColor(0.5f, 0, 0, 1);
            shapeRenderer.rect(x, barY, width, 4f);
            shapeRenderer.setColor(1f, 0f, 0f, 1);
            shapeRenderer.rect(x, barY, width * ((float) currentHealth / maxHealth), 4f);
        }
    }

    public void render(SpriteBatch spriteBatch, float delta) {
        for (Bullet bullet : bullets) {
            bullet.render(spriteBatch);
        }

        if (isAlive) {
            TextureRegion frame = visuals.getFrameToRender(fsm.getCurrentState(), movingRight, delta);

            float renderWidth = this.width;
            float renderHeight = this.height;

            if (fsm.getCurrentState() == EnemyState.DYING) {
                renderHeight = this.height / 2f;
            }

            spriteBatch.draw(frame, x, (y - 20f), renderWidth, renderHeight);
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

        if (deathSound != null) {
            deathSound.dispose();
        }

        if (visuals != null) {
            visuals.dispose();
        }
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
