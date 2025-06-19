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
    private final Timer stateTimer = new Timer();
    private boolean isPhaseTwo = false;
    private final TankBoss owner;
    private final OrthographicCamera camera;

    private float remainingCharge = 0f;
    private final float chargeSpeed = 800f;
    private Vector2 chargeDirection = new Vector2();
    private final float prepareChargeTime = 1f;

    private int currentBurstCount = 0;
    private final int totalBurstsInSeries = 3;
    private final int bulletsPerBurst = 3;
    private final float burstCooldownTime = 1f;

    private final float preGrenadeDelayTime = 2.5f;

    private int currentGrenadeTossCount = 0;
    private final int totalGrenadesToToss = 3;
    private final float grenadeTossCooldownTime = 0.5f;
    private int grenadeTargetSide = 0;

    private final float entrySpeed = 400f;

    private float targetArenaX;
    private boolean isInvincible = false;

    public TankBossFSM(TankBoss owner, Vector2 startPos, OrthographicCamera camera) {
        this.owner = owner;
        this.camera = camera;
        this.health = maxHealth;
        stateTimer.reset();
    }

    public void initialize() {
        changeState(TankBossState.ENTERING_ARENA);
    }

    public void update(float delta) {
        stateTimer.update(delta);

        switch (currentState) {
            case ENTERING_ARENA:
                owner.move(-entrySpeed * delta, 0);

                if (owner.getPosition().x <= targetArenaX) {
                    owner.move(targetArenaX - owner.getPosition().x, 0);
                    changeState(TankBossState.IDLE);
                }
                break;

            case IDLE:
                if (stateTimer.elapsed(2.0f)) {
                    currentBurstCount = 0;
                    changeState(TankBossState.PERFORMING_BURST);
                }
                break;

            case PERFORMING_BURST:
                if (currentBurstCount < totalBurstsInSeries) {
                    changeState(TankBossState.BURST_COOLDOWN);
                } else {
                    changeState(TankBossState.PRE_GRENADE_DELAY);
                }
                break;

            case BURST_COOLDOWN:
                if (stateTimer.elapsed(burstCooldownTime)) {
                    changeState(TankBossState.PERFORMING_BURST);
                }
                break;

            case PRE_GRENADE_DELAY:
                if (stateTimer.elapsed(preGrenadeDelayTime)) {
                    currentGrenadeTossCount = 0;
                    grenadeTargetSide = 0;
                    changeState(TankBossState.PERFORMING_GRENADE_TOSS);
                }
                break;

            case PERFORMING_GRENADE_TOSS:
                if (currentGrenadeTossCount < totalGrenadesToToss) {
                    changeState(TankBossState.GRENADE_TOSS_COOLDOWN);
                } else {
                    changeState(TankBossState.PREPARE_CHARGE);
                }
                break;

            case GRENADE_TOSS_COOLDOWN:
                if (stateTimer.elapsed(grenadeTossCooldownTime)) {
                    changeState(TankBossState.PERFORMING_GRENADE_TOSS);
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
                    changeState(TankBossState.IDLE);
                }
                break;

            case DEAD:
                break;
        }
    }

    public void takeDamage(float dmg) {
        if (currentState == TankBossState.DEAD) return;
        if (isInvincible) return;

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
        if (newState == TankBossState.ENTERING_ARENA) {
            isInvincible = true;
            targetArenaX = camera.position.x + Constant.SCREEN_WIDTH / 2f - owner.getWidth() - (Constant.SCREEN_WIDTH * 0.2f);
            System.out.println("Boss targetArenaX set to: " + targetArenaX);
            owner.playChargeSound();
        } else {
            isInvincible = false;
        }

        if (newState == TankBossState.PERFORMING_BURST) {
            if (currentBurstCount < totalBurstsInSeries) {
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
                owner.launchGrenadeTowardsPlayerSide(relativeDirection);
                currentGrenadeTossCount++;
                grenadeTargetSide = (grenadeTargetSide + 1) % 2;
            }
        } else if (newState == TankBossState.PREPARE_CHARGE) {
            owner.playPrepareChargeSound();

            Vector2 playerCenter = new Vector2(owner.getPlayerPosition());
            playerCenter.x += Constant.PLAYER_WIDTH / 2f;
            float bossCenterX = owner.getPosition().x + owner.getWidth() / 2f;
            float directionX = Math.signum(playerCenter.x - bossCenterX);

            owner.move(-directionX * 30f, 0);

            chargeDirection.set(directionX, 0);
            remainingCharge = Math.abs(playerCenter.x - bossCenterX) + Constant.PLAYER_WIDTH * 2f;
        } else if (newState == TankBossState.CHARGE) {
            owner.playChargeSound();
        } else if (newState == TankBossState.DEAD) {
            owner.clearAllProjectiles();
            isInvincible = true;
            owner.playDestroyedSound();
            owner.getGameScreen().setupAndTriggerBossDefeatedVN();
        }
        owner.onStateChanged();
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
    }

    public boolean isDead() {
        return currentState == TankBossState.DEAD;
    }
}
