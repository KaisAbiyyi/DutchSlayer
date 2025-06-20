package io.DutchSlayer.defend.game;

import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.enemies.Enemy;
import io.DutchSlayer.defend.entities.projectiles.BombAsset;
import io.DutchSlayer.defend.entities.projectiles.EnemyProjectile;
import io.DutchSlayer.defend.entities.projectiles.Projectile;
import io.DutchSlayer.defend.entities.towers.Tower;
import io.DutchSlayer.defend.entities.traps.Trap;
import io.DutchSlayer.defend.screens.TowerDefenseScreen;

public class GameState {
    public final Array<EnemyProjectile> enemyProjectiles = new Array<>();
    public final Array<BombAsset> bombs = new Array<>();
    public final Array<Tower> towers = new Array<>();
    public final Array<Enemy> enemies = new Array<>();
    public final Array<Projectile> projectiles = new Array<>();
    public final Array<Trap> trapZones = new Array<>();
    public final Array<float[]> trapVerts = new Array<>();

    public final Array<TowerDefenseScreen.Zone> zones = new Array<>();
    public final Array<TowerDefenseScreen.Zone> deployedTowerZones = new Array<>();

    public int gold = 80;
    public int currentWave = 1;
    public int spawnCount = 0;
    public int enemiesThisWave = 5;
    public boolean bossSpawned = false;
    public boolean isGameOver = false;
    public boolean isGameWon = false;
    public boolean isPaused = false;
    public boolean isWaveTransition = false;
    public boolean waveCompleteBonusGiven = false;

    public float spawnTimer = 0f;
    public float goldTimer = 0f;
    public float waveTransitionTimer = 0f;

    public TowerDefenseScreen.NavItem selectedType;
    public Tower selectedTowerUI;

    public boolean isBossIntroduction = false;
    public float bossIntroTimer = 0f;
    public Enemy currentBoss = null;

    public boolean isRemoveButtonHovered = false;
    public boolean isPauseButtonHovered = false;
    public float mouseX = 0f, mouseY = 0f;

    public final int currentStage;

    public final float[] towerCooldowns = new float[3];
    public final float[] trapCooldowns = new float[3];
    public final boolean[] towerCooldownActive = new boolean[3];
    public final boolean[] trapCooldownActive = new boolean[3];

    public boolean isResumeButtonPressed = false;
    public boolean isSettingButtonPressed = false;
    public boolean isMenuButtonPressed = false;
    public boolean isNextButtonPressed = false;
    public boolean isRetryButtonPressed = false;
    public boolean isContinueButtonPressed = false;
    public boolean isRestartButtonPressed = false;
    public boolean isQuitButtonPressed = false;

    public float buttonPressTimer = 0f;
    public static final float BUTTON_PRESS_DURATION = 0.15f;

    public GameState(int stage) {
        this.currentStage = stage;
        initializeStageSettings(stage);
    }

    private void initializeStageSettings(int stage) {
        switch(stage) {
            case 1:
                gold = 100;
                enemiesThisWave = 4;
                break;
            case 2:
                gold = 80;
                enemiesThisWave = 5;
                break;
            case 3:
                gold = 60;
                enemiesThisWave = 6;
                break;
            case 4:
                gold = 50;
                enemiesThisWave = 8;
                break;
            default:
                gold = 80;
                enemiesThisWave = 5;
                break;
        }
    }

    public void clearBossReference() {
        currentBoss = null;
        isBossIntroduction = false;
        bossIntroTimer = 0f;
    }

    public void resetButtonStates() {
        isMenuButtonPressed = false;
        isNextButtonPressed = false;
        isRetryButtonPressed = false;
        isContinueButtonPressed = false;
        isRestartButtonPressed = false;
        isQuitButtonPressed = false;
        isResumeButtonPressed = false;
        isSettingButtonPressed = false;
        buttonPressTimer = 0f;
    }

    public void updateButtonPressTimer(float delta) {
        if (buttonPressTimer > 0f) {
            buttonPressTimer -= delta;
            if (buttonPressTimer <= 0f) {
                resetButtonStates();
            }
        }
    }

    public void pressButton(String buttonType) {
        resetButtonStates();

        switch(buttonType.toLowerCase()) {
            case "menu":
                isMenuButtonPressed = true;
                break;
            case "next":
                isNextButtonPressed = true;
                break;
            case "retry":
                isRetryButtonPressed = true;
                break;
            case "continue":
            case "resume":
                isResumeButtonPressed = true;
                break;
            case "restart":
                isRestartButtonPressed = true;
                break;
            case "quit":
                isQuitButtonPressed = true;
                break;
            case "setting":
                isSettingButtonPressed = true;
                break;
        }
        buttonPressTimer = BUTTON_PRESS_DURATION;
    }
}
