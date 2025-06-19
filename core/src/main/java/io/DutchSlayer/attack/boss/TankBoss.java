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
    private SpriteBatch batch;

    private Texture turretTexture;
    private Texture chargingTexture;
    private Texture grenadeTexture;
    private Texture grenadeProjectileTexture;
    private Texture explosionTexture;
    private Texture destroyedTexture;
    private Texture currentTexture;
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

        if (facingRight && !currentRegion.isFlipX()) {
            currentRegion.flip(true, false);
        }
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
        if (destroyedSound != null) destroyedSound.play(1.0f);
    }

    public void update(float delta) {
        fsm.update(delta);
        if (fsm.getCurrentState() != TankBossState.CHARGE) {
            if (player.getX() + player.getWidth() / 2f > position.x + width / 2f) {
                facingRight = true;
            } else {
                facingRight = false;
            }
        }

        updateVisualsBasedOnState();
        for (int i = bullets.size - 1; i >= 0; i--) {
            BossBullet b = bullets.get(i);
            b.update(delta);
            if (!b.isAlive()) {
                b.dispose();
                bullets.removeIndex(i);
            }
        }
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
                grenades.removeIndex(i);
            }
        }
    }

    public void onStateChanged() {
        updateVisualsBasedOnState();
    }
    public void render(ShapeRenderer renderer, SpriteBatch spriteBatch) {
        fsm.renderDebug(renderer);
        spriteBatch.begin();
        if (currentRegion != null) {
            spriteBatch.draw(currentRegion, position.x, position.y, width, height);
        }
        for (BossBullet b : bullets) {
            b.render(spriteBatch);
        }
        for (BossGrenade g : grenades) {
            g.render(spriteBatch);
        }

        spriteBatch.end();
    }
    public float getHealthRatio() {
        if (this.fsm != null) {
            return this.fsm.getHealthRatio();
        }
        return 0f;
    }
    public void fireBurst(int numberOfBullets) {
        float directionToPlayer = Math.signum(player.getX() - (position.x + width / 2f));
        float spawnX = position.x + width / 2f;
        float spawnY = position.y + height / 2f;

        for (int i = 0; i < numberOfBullets; i++) {
            bullets.add(new BossBullet(spawnX, spawnY, directionToPlayer, facingRight));
        }
        playBulletSound();
        System.out.println("[Boss] Fired burst of " + numberOfBullets + " bullets.");
    }
    public void launchGrenadeTowardsPlayerSide(float relativeSide) {
        float launchX = position.x + width / 2f;
        float launchY = position.y + height * 0.8f;
        float playerTargetX = player.getX() + Constant.PLAYER_WIDTH * 1.5f * relativeSide;
        float targetY = Constant.TERRAIN_HEIGHT;

        float distanceX = playerTargetX - launchX;
        float distanceY = targetY - launchY;
        float gravity = 700f;
        float initialVy = 350f;
        float a = 0.5f * gravity;
        float b = -initialVy;
        float c = distanceY;

        float discriminant = b * b - 4 * a * c;
        float timeToTarget;

        if (discriminant < 0) {
            timeToTarget = 1.0f;
            System.err.println("Warning: Cannot reach target Y with given initialVy. Adjusting time.");
        } else {
            float t1 = (-b + (float) Math.sqrt(discriminant)) / (2 * a);
            float t2 = (-b - (float) Math.sqrt(discriminant)) / (2 * a);
            if (t1 > 0 && t2 > 0) {
                timeToTarget = Math.max(t1, t2);
            } else if (t1 > 0) {
                timeToTarget = t1;
            } else {
                timeToTarget = t2;
            }
            if (timeToTarget < 0.1f) {
                timeToTarget = 0.1f;
            }
        }
        float initialVx = distanceX / timeToTarget;

        float maxVx = 600f;
        initialVx = Math.max(-maxVx, Math.min(maxVx, initialVx));


        grenades.add(new BossGrenade(launchX, launchY, initialVx, initialVy, grenadeProjectileTexture, explosionTexture, grenadeExplosionSound));
        playGrenadeThrowSound();
        System.out.println("[Boss] Launched grenade towards player side " + relativeSide +
            " (vx: " + initialVx + ", vy: " + initialVy + ", targetY: " + targetY + ")");
    }


    public void clearAllProjectiles() {
        for (BossBullet b : bullets) {
            b.dispose();
        }
        bullets.clear();
        grenades.clear();
    }
    public String getName() {
        return BossType.TANK.getDisplayName();
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
        if (newTexture != currentTexture) {
            currentTexture = newTexture;
            if (currentTexture != null) {
                currentRegion = new TextureRegion(currentTexture);
            } else {
                currentRegion = null;
            }
        }
        if (currentRegion != null) {
            if (facingRight && !currentRegion.isFlipX()) {
                currentRegion.flip(true, false);
            }
            else if (!facingRight && currentRegion.isFlipX()) {
                currentRegion.flip(true, false);
            }
        }
    }
    public void dispose() {
        if (turretTexture != null) turretTexture.dispose();
        if (chargingTexture != null) chargingTexture.dispose();
        if (grenadeTexture != null) grenadeTexture.dispose();
        if (grenadeProjectileTexture != null) grenadeProjectileTexture.dispose();
        if (explosionTexture != null) explosionTexture.dispose();
        if (destroyedTexture != null) destroyedTexture.dispose();
        if (batch != null) batch.dispose();
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
