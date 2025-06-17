package io.DutchSlayer.attack.boss.fsm;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import io.DutchSlayer.attack.boss.TankBoss;
import io.DutchSlayer.utils.Constant;
import io.DutchSlayer.utils.Timer;

public class TankBossFSM {

    private TankBossState currentState;
    private float health;
    private final float maxHealth = 200f;
    // private final Vector2 position; // Bisa didapat dari owner.getPosition()
    private final Timer stateTimer = new Timer(); // Timer untuk durasi state atau jeda
    private boolean isPhaseTwo = false;
    private final TankBoss owner;
    private final OrthographicCamera camera;

    // Variabel Charge (sudah ada dan baik)
    private float remainingCharge = 0f;
    private final float chargeSpeed = 800f;
    private Vector2 chargeDirection = new Vector2();
    private final float prepareChargeTime = 1f;

    // Variabel untuk Attack Pattern Baru
    private int currentBurstCount = 0; // Jumlah burst yang sudah dilakukan dalam satu seri
    private final int totalBurstsInSeries = 3;
    private final int bulletsPerBurst = 3; // Jumlah peluru per burst
    private final float burstCooldownTime = 1f; // Jeda antar burst

    private final float preGrenadeDelayTime = 2.5f; // Jeda sebelum serangan granat

    private int currentGrenadeTossCount = 0; // Jumlah granat yang sudah dilempar
    private final int totalGrenadesToToss = 3;
    private final float grenadeTossCooldownTime = 0.5f;
    private int grenadeTargetSide = 0; // 0: kiri, 1: kanan, untuk alternating

    private final float entrySpeed = 400f;

    private float targetArenaX;
    private boolean isInvincible = false;

    public TankBossFSM(TankBoss owner, Vector2 startPos, OrthographicCamera camera) {
        this.owner = owner;
        // this.position = startPos; // Gunakan owner.getPosition() jika memungkinkan
        this.camera = camera;
        this.health = maxHealth;
        stateTimer.reset();
        // Inisialisasi state awal ke IDLE, lalu dari IDLE akan memulai pola serangan
    }

    public void initialize() {
        changeState(TankBossState.ENTERING_ARENA);
    }

    public void update(float delta) {
        stateTimer.update(delta);

        switch (currentState) {
            case ENTERING_ARENA:
                owner.move(-entrySpeed * delta, 0);

                // Periksa apakah boss sudah mencapai posisi targetArenaX
                if (owner.getPosition().x <= targetArenaX) {
                    owner.move(targetArenaX - owner.getPosition().x, 0); // Pastikan berhenti tepat di target
                    changeState(TankBossState.IDLE); // Transisi ke IDLE setelah mencapai posisi
                }
                break;

            case IDLE:
                // Setelah jeda singkat di IDLE, mulai pola serangan
                if (stateTimer.elapsed(2.0f)) {
                    currentBurstCount = 0;
                    changeState(TankBossState.PERFORMING_BURST);
                }
                break;

            case PERFORMING_BURST:
                // Aksi menembak burst dipicu saat masuk state ini (di changeState)
                // State ini hanya menunggu untuk transisi ke cooldown atau state berikutnya
                // Jika aksi menembak terjadi seketika, langsung ubah state.
                // Jika menembak butuh waktu (misal ada animasi), tunggu animasi selesai.
                // Untuk kasus ini, asumsikan menembak terjadi di changeState.
                if (currentBurstCount < totalBurstsInSeries) {
                    changeState(TankBossState.BURST_COOLDOWN);
                } else {
                    // Selesai semua burst, masuk ke jeda sebelum granat
                    changeState(TankBossState.PRE_GRENADE_DELAY);
                }
                break;

            case BURST_COOLDOWN:
                if (stateTimer.elapsed(burstCooldownTime)) {
                    changeState(TankBossState.PERFORMING_BURST); // Kembali menembak burst berikutnya
                }
                break;

            case PRE_GRENADE_DELAY:
                if (stateTimer.elapsed(preGrenadeDelayTime)) {
                    currentGrenadeTossCount = 0;
                    grenadeTargetSide = 0; // Reset arah lemparan granat pertama
                    changeState(TankBossState.PERFORMING_GRENADE_TOSS);
                }
                break;

            case PERFORMING_GRENADE_TOSS:
                // Aksi melempar granat dipicu saat masuk state ini (di changeState)
                if (currentGrenadeTossCount < totalGrenadesToToss) {
                    changeState(TankBossState.GRENADE_TOSS_COOLDOWN);
                } else {
                    // Selesai semua granat, masuk ke persiapan charge
                    changeState(TankBossState.PREPARE_CHARGE);
                }
                break;

            case GRENADE_TOSS_COOLDOWN:
                if (stateTimer.elapsed(grenadeTossCooldownTime)) {
                    changeState(TankBossState.PERFORMING_GRENADE_TOSS); // Kembali melempar granat berikutnya
                }
                break;

            case PREPARE_CHARGE:
                if (stateTimer.elapsed(prepareChargeTime)) {
                    changeState(TankBossState.CHARGE);
                }
                break;

            case CHARGE:
                if (remainingCharge > 0) {
                    float step = chargeSpeed * delta;
                    float move = Math.min(step, remainingCharge);
                    owner.move(chargeDirection.x * move, chargeDirection.y * move);
                    remainingCharge -= move;
                } else {
                    // Selesai charge, kembali ke IDLE untuk memulai siklus dari awal (setelah jeda di IDLE)
                    changeState(TankBossState.IDLE);
                }
                break;

            case DEAD:
                // Do nothing
                break;
        }
    }

    public void takeDamage(float dmg) {
        if (currentState == TankBossState.DEAD) return;
        if (isInvincible) return; // <--- Boss invincible jika flag ini true

        health -= dmg;
        if (health <= 0) {
            health = 0;
            changeState(TankBossState.DEAD);
            owner.clearAllProjectiles();
        }
    }

    private void changeState(TankBossState newState) {
        System.out.println("[FSM] Changing from " + currentState + " to " + newState);
        currentState = newState;
        stateTimer.reset();

        // Atur invincibility berdasarkan state
        if (newState == TankBossState.ENTERING_ARENA) {
            isInvincible = true; // Boss invincible saat masuk arena
            // Hitung targetArenaX di sini
            targetArenaX = camera.position.x + Constant.SCREEN_WIDTH / 2f - owner.getWidth() - (Constant.SCREEN_WIDTH * 0.2f);
            System.out.println("Boss targetArenaX set to: " + targetArenaX);
            owner.playChargeSound(); // <-- BARU: Panggil suara charging di sini
        } else {
            isInvincible = false; // Boss tidak invincible di state lain (kecuali DEAD)
        }

        if (newState == TankBossState.PERFORMING_BURST) {
            if (currentBurstCount < totalBurstsInSeries) {
                // Suara tembakan sudah dipicu di `fireBurst()` di TankBoss.java
                owner.fireBurst(bulletsPerBurst);
                currentBurstCount++;
            }
        } else if (newState == TankBossState.PERFORMING_GRENADE_TOSS) {
            if (currentGrenadeTossCount < totalGrenadesToToss) {
                float relativeDirection;
                if (grenadeTargetSide == 0) {
                    relativeDirection = -1f;
                } else {
                    relativeDirection = 1f;
                }
                // Suara lempar granat sudah dipicu di `launchGrenadeTowardsPlayerSide()` di TankBoss.java
                owner.launchGrenadeTowardsPlayerSide(relativeDirection);
                currentGrenadeTossCount++;
                grenadeTargetSide = (grenadeTargetSide + 1) % 2;
            }
        } else if (newState == TankBossState.PREPARE_CHARGE) {
            // BARU: Panggil suara prepare to charge
            owner.playPrepareChargeSound();

            Vector2 playerCenter = new Vector2(owner.getPlayerPosition());
            playerCenter.x += Constant.PLAYER_WIDTH / 2f;
            float bossCenterX = owner.getPosition().x + owner.getWidth() / 2f;
            float directionX = Math.signum(playerCenter.x - bossCenterX);

            owner.move(-directionX * 30f, 0);

            chargeDirection.set(directionX, 0);
            remainingCharge = Math.abs(playerCenter.x - bossCenterX) + Constant.PLAYER_WIDTH * 2f;
        } else if (newState == TankBossState.CHARGE) {
            // BARU: Panggil suara charging
            owner.playChargeSound();
        } else if (newState == TankBossState.DEAD) {
            owner.clearAllProjectiles();
            isInvincible = true;
            // BARU: Panggil suara kematian
            owner.playDestroyedSound();
            owner.getGameScreen().setupAndTriggerBossDefeatedVN();
        }
        owner.onStateChanged();
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    public TankBossState getCurrentState() {
        return currentState;
    }

    public float getHealthRatio() {
        return Math.max(0, health / maxHealth);
    }

    public boolean isCharging() {
        return currentState == TankBossState.CHARGE;
    }

    public void renderDebug(ShapeRenderer renderer) {
        // Optional
    }

    public boolean isDead() {
        return currentState == TankBossState.DEAD;
    }
}
