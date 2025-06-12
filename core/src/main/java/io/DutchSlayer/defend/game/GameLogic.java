package io.DutchSlayer.defend.game;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.enemies.Enemy;
import io.DutchSlayer.defend.entities.enemies.EnemyType;
import io.DutchSlayer.defend.entities.projectiles.AoeProjectile;
import io.DutchSlayer.defend.entities.projectiles.BombAsset;
import io.DutchSlayer.defend.entities.projectiles.EnemyProjectile;
import io.DutchSlayer.defend.entities.projectiles.Projectile;
import io.DutchSlayer.defend.entities.towers.Tower;
import io.DutchSlayer.defend.entities.traps.Trap;
import io.DutchSlayer.defend.screens.TowerDefenseScreen;
import io.DutchSlayer.defend.ui.ImageLoader;
import io.DutchSlayer.defend.utils.AudioManager;

/**
 * Contains all game logic and update methods
 */
public class GameLogic {
    private final GameState gameState;
    private final UIManager uiManager;

    private boolean musicFadeStarted = false;
    private boolean bossMusicTriggered = false;

    public GameLogic(GameState gameState, UIManager uiManager) {
        this.gameState = gameState;
        this.uiManager = uiManager;
    }

    public void update(float delta) {
        if (gameState.isPaused || gameState.isGameOver) {
            gameState.updateButtonPressTimer(delta);
            return;
        }
        gameState.updateButtonPressTimer(delta);

        AudioManager.updateMusicTransition(delta);

        updateCooldowns(delta);
        updateTraps(delta);
        updateEnemies(delta);
        updateEnemyProjectiles(delta);
        updateBombs(delta);
        updateTrapCollisions(delta);
        updateTowerShooting(delta);
        updateProjectiles(delta);
        cleanupDeadEnemies();
        updateWaveSpawning(delta);
        updateGoldIncome(delta);

        updateBossMusicTransition();
    }

    private void updateCooldowns(float delta) {
        // Update tower cooldowns
        for (int i = 0; i < 3; i++) {
            if (gameState.towerCooldownActive[i]) {
                gameState.towerCooldowns[i] -= delta;
                if (gameState.towerCooldowns[i] <= 0f) {
                    gameState.towerCooldownActive[i] = false;
                    gameState.towerCooldowns[i] = 0f;
                }
            }
        }

        // Update trap cooldowns
        for (int i = 0; i < 3; i++) {
            if (gameState.trapCooldownActive[i]) {
                gameState.trapCooldowns[i] -= delta;
                if (gameState.trapCooldowns[i] <= 0f) {
                    gameState.trapCooldownActive[i] = false;
                    gameState.trapCooldowns[i] = 0f;
                }
            }
        }
    }

    private void updateTraps(float delta) {
        for (Trap trap : gameState.trapZones) {
            trap.update(delta);
        }
    }

    private void updateEnemies(float delta) {
        for (int i = gameState.enemies.size - 1; i >= 0; i--) {
            Enemy e = gameState.enemies.get(i);
            e.update(delta);

            // Check collision with towers
            for (int j = gameState.towers.size - 1; j >= 0; j--) {
                Tower t = gameState.towers.get(j);
                if (e.getBounds().overlaps(t.getBounds())) {
                    handleEnemyTowerCollision(e, t, i, j);
                    break;
                }
            }

            // Remove enemies that went off-screen
            if (i < gameState.enemies.size && e.getX() < -e.getWidth()/2) {
                gameState.enemies.removeIndex(i);
            }
        }

        // Shield protection mechanism
        for (int i = 0; i < gameState.enemies.size; i++) {
            Enemy e = gameState.enemies.get(i);
            if (e.getType() == EnemyType.BASIC) {
                e.seekProtection(gameState.enemies);
            }
        }
    }

    private void handleEnemyTowerCollision(Enemy e, Tower t, int enemyIndex, int towerIndex) {
        if (e.getType() == EnemyType.BOMBER) {
            // Bomber drops bomb
            float dropX = e.getX();
            float dropY = e.getBounds().y + e.getBounds().height/2;

            BombAsset bomb = new BombAsset(
                ImageLoader.bombAssetTex != null ? ImageLoader.bombAssetTex : ImageLoader.trapTex,
                dropX, dropY
            );

            if (bomb != null) {
                gameState.bombs.add(bomb);
            }

            gameState.enemies.removeIndex(enemyIndex);
        } else if (e.canAttack()) {
            int damage = (e.getType() == EnemyType.SHIELD) ? 0 : 1;

            if (damage > 0) {
                t.takeDamage(damage);
                System.out.println("⚔️ Enemy attacked tower! Tower HP: " + t.getHealth());
            }
            e.knockback();

            if (t.isDestroyed()) {
                resetTowerZone(towerIndex);
                gameState.towers.removeIndex(towerIndex);
                if (t.isMain) {
                    gameState.isGameOver = true;
                    gameState.isGameWon = false;
                    uiManager.setupLoseUI();
                    System.out.println("💀 MAIN TOWER DESTROYED! GAME OVER!");
                }
            }
        }
    }

    private void updateEnemyProjectiles(float delta) {
        for (int i = gameState.enemyProjectiles.size - 1; i >= 0; i--) {
            EnemyProjectile ep = gameState.enemyProjectiles.get(i);
            ep.update(delta);

            boolean hit = false;
            for (int j = gameState.towers.size - 1; j >= 0; j--) {
                Tower t = gameState.towers.get(j);
                if (ep.getBounds().overlaps(t.getBounds())) {
                    t.takeDamage(ep.getDamage());
                    hit = true;

                    if (t.isDestroyed()) {
                        resetTowerZone(j);
                        gameState.towers.removeIndex(j);
                        if (t.isMain) gameState.isGameOver = true;
                    }
                    break;
                }
            }

            if (hit || ep.getX() < -50f) {
                gameState.enemyProjectiles.removeIndex(i);
            }
        }
    }

    private void updateBombs(float delta) {
        for (int i = gameState.bombs.size - 1; i >= 0; i--) {
            BombAsset bomb = gameState.bombs.get(i);
            bomb.update(delta);

            if (bomb.shouldExplode()) {
                handleBombExplosion(bomb);
            }

            if (bomb.hasExploded()) {
                gameState.bombs.removeIndex(i);
            }
        }
    }

    private void updateTrapCollisions(float delta) {
        for (int i = gameState.enemies.size - 1; i >= 0; i--) {
            Enemy e = gameState.enemies.get(i);

            for (int trapIdx = 0; trapIdx < gameState.trapZones.size; trapIdx++) {
                Trap trap = gameState.trapZones.get(trapIdx);

                if (!trap.occupied || trap.isUsed()) continue;

                if (e.getBounds().overlaps(trap.bounds)) {
                    switch (trap.getType()) {
                        case ATTACK:
                            AudioManager.playTrapAttackHit();
                            e.takeDamage(1);
                            e.slow(2f);
                            break;
                        case SLOW:
                            AudioManager.playTrapSlowHit();
                            e.slowHeavy(5f, 0.1f);
                            break;
                        case EXPLOSION:
                            AudioManager.playTrapExplosionHit();
                            handleExplosionTrap(trap);
                            break;
                    }

                    trap.occupied = false;
                    System.out.println("💥 Trap consumed!");
                    break;
                }
            }
        }
    }

    /**
     * Handle bomb explosion dengan proper zone reset
     */
    private void handleBombExplosion(BombAsset bomb) {
        // Get towers yang akan di-explode sebelum bomb.explode() dipanggil
        Array<Integer> towersToDestroy = new Array<>();

        for (int j = gameState.towers.size - 1; j >= 0; j--) {
            Tower t = gameState.towers.get(j);
            if (bomb.willDamageTower(t)) {
                towersToDestroy.add(j);
            }
        }

        // Explode bomb (ini akan damage towers)
        bomb.explode(gameState.towers);

        // Check dan reset zones untuk towers yang hancur
        for (int j = towersToDestroy.size - 1; j >= 0; j--) {
            int towerIndex = towersToDestroy.get(j);
            if (towerIndex < gameState.towers.size) {
                Tower t = gameState.towers.get(towerIndex);
                if (t.isDestroyed()) {
                    resetTowerZone(towerIndex);
                    gameState.towers.removeIndex(towerIndex);
                    if (t.isMain) {
                        gameState.isGameOver = true;
                        gameState.isGameWon = false;
                        uiManager.setupLoseUI();
                    }
                }
            }
        }
    }

    private void handleExplosionTrap(Trap trap) {
        float trapX = trap.getCenterX();
        float trapY = trap.getCenterY();
        float explosionRadius = 250f;
        int explosionDamage = 2;

        for (int enemyIdx = gameState.enemies.size - 1; enemyIdx >= 0; enemyIdx--) {
            Enemy target = gameState.enemies.get(enemyIdx);
            if (target.isDestroyed()) continue;

            float targetX = target.getX();
            float targetY = target.getBounds().y + target.getBounds().height / 2;

            float distance = (float) Math.sqrt(
                Math.pow(trapX - targetX, 2) + Math.pow(trapY - targetY, 2)
            );

            if (distance <= explosionRadius) {
                target.takeDamage(explosionDamage);
            }
        }
    }

    private void updateTowerShooting(float delta) {
        for (Tower t : gameState.towers) {
            t.update(delta, gameState.enemies, gameState.projectiles);
        }
    }

    private void updateProjectiles(float delta) {
        for (int i = gameState.projectiles.size - 1; i >= 0; i--) {
            Projectile p = gameState.projectiles.get(i);
            p.update(delta);

            boolean shouldRemove = false;

            // ===== SPECIAL HANDLING UNTUK AOE PROJECTILE =====
            if (p instanceof AoeProjectile) {
                AoeProjectile aoeProj = (AoeProjectile) p;

                // Check jika AOE sudah exploded
                if (aoeProj.hasExploded()) {
                    // Trigger AOE damage manual
                    aoeProj.triggerAOEDamage(gameState.enemies);
                    shouldRemove = true;
                    System.out.println("🎯 AOE Projectile exploded and triggered damage");
                }
                // Check jika AOE keluar bounds
                else if (aoeProj.getX() > 1280 + aoeProj.getBounds().width/2 ||
                    aoeProj.getX() < -aoeProj.getBounds().width/2) {
                    shouldRemove = true;
                    System.out.println("AOE Projectile removed - out of bounds");
                }
            }
            // ===== STANDARD PROJECTILE COLLISION =====
            else {
                boolean hit = false;
                for (int j = gameState.enemies.size - 1; j >= 0; j--) {
                    Enemy e = gameState.enemies.get(j);
                    if (!e.isDestroyed() && p.getBounds().overlaps(e.getBounds())) {
                        p.onHit(gameState.enemies);
                        hit = true;
                        break;
                    }
                }

                // Remove jika hit atau keluar bounds
                if (hit || p.getX() > 1280 + p.getBounds().width/2) {
                    shouldRemove = true;
                }
            }

            // Remove projectile dari array
            if (shouldRemove) {
                gameState.projectiles.removeIndex(i);
            }
        }
    }

    private void cleanupDeadEnemies() {
        for (int j = gameState.enemies.size - 1; j >= 0; j--) {
            Enemy e = gameState.enemies.get(j);
            if (e.isDestroyed()) {
                if (e.getType() == EnemyType.BOSS) {
                    System.out.println("👑 BOSS DEFEATED! Returning to normal music...");

                    // Transition back to tower defense music

                    gameState.clearBossReference();

                    // Reset boss music flags for potential future waves
                    bossMusicTriggered = false;
                    // Keep musicFadeStarted true untuk prevent re-triggering di wave yang sama
                } else {
                    AudioManager.playEnemyDeath(e.getType());
                }

                gameState.enemies.removeIndex(j);
                gameState.gold += getGoldReward(e.getType());
            }
        }
    }

    private int getGoldReward(EnemyType enemyType) {
        switch(enemyType) {
            case BASIC: return 10;
            case SHOOTER: return 15;
            case BOMBER: return 12;
            case SHIELD: return 20;
            case BOSS: return 50;
            default: return 10;
        }
    }

    private void updateWaveSpawning(float delta) {
        if (gameState.spawnCount < gameState.enemiesThisWave) {
            gameState.spawnTimer += delta;
            if (gameState.spawnTimer > 2f) {
                gameState.spawnTimer = 0f;
                spawnEnemy();
                gameState.spawnCount++;
            }
        }

        // Wave completion check
        if (gameState.spawnCount >= gameState.enemiesThisWave && gameState.enemies.size == 0) {
            if (!gameState.isWaveTransition && !gameState.waveCompleteBonusGiven) {
                int waveBonus = 50 + (gameState.currentWave * 10);
                gameState.gold += waveBonus;
                gameState.waveCompleteBonusGiven = true;

                if (gameState.currentWave < GameConstants.MAX_WAVE) {
                    gameState.isWaveTransition = true;
                    gameState.waveTransitionTimer = 0f;
                } else {
                    gameState.isGameWon = true;
                    uiManager.setupWinUI(gameState.currentStage);
                    if (gameState.currentStage == GameConstants.FINAL_STAGE) {
                        System.out.println("🏆 CONGRATULATIONS! YOU COMPLETED THE FINAL STAGE! 🏆");
                        AudioManager.stopMusic();
                    } else {
                        System.out.println("🎉 Stage " + gameState.currentStage + " completed! Ready for Stage " + (gameState.currentStage + 1));
                    }
                }
            }

            if (gameState.isWaveTransition) {
                gameState.waveTransitionTimer += delta;

                if (gameState.waveTransitionTimer >= GameConstants.WAVE_TRANSITION_DELAY) {
                    gameState.currentWave++;
                    gameState.enemiesThisWave += 5;
                    gameState.spawnCount = 0;
                    gameState.bossSpawned = false;
                    gameState.isWaveTransition = false;
                    gameState.waveTransitionTimer = 0f;
                    gameState.waveCompleteBonusGiven = false;

                    System.out.println("🚀 Starting Wave " + gameState.currentWave + " with " + gameState.enemiesThisWave + " enemies!");
                }
            }
        }
    }

    private void spawnEnemy() {
        EnemyType enemyType = determineEnemyType();
        float enemyY = GameConstants.GROUND_Y + getEnemyYOffset(enemyType);
        Enemy newEnemy = new Enemy(enemyType, 1280, enemyY);
        newEnemy.setReferences(gameState.towers, gameState.enemyProjectiles, gameState.bombs);
        gameState.enemies.add(newEnemy);

        if (enemyType == EnemyType.BOSS) {
            gameState.currentBoss = newEnemy; // Set boss reference immediately
        }
    }

    private EnemyType determineEnemyType() {
        switch(gameState.currentStage) {
            case 1:
                return Math.random() < 0.7f ? EnemyType.BASIC : EnemyType.SHIELD;
            case 2:
                float rand2 = (float) Math.random();
                if (rand2 < 0.5f) return EnemyType.BASIC;
                else if (rand2 < 0.8f) return EnemyType.SHIELD;
                else return EnemyType.SHOOTER;
            case 3:
                float rand3 = (float) Math.random();
                if (rand3 < 0.3f) return EnemyType.BASIC;
                else if (rand3 < 0.5f) return EnemyType.SHOOTER;
                else if (rand3 < 0.7f) return EnemyType.BOMBER;
                else return EnemyType.SHIELD;
            case 4:
                if (gameState.currentWave == 3 && !gameState.bossSpawned) {
                    if (gameState.spawnCount >= gameState.enemiesThisWave - 1) {
                        gameState.bossSpawned = true;
                        return EnemyType.BOSS;
                    }
                    if (Math.random() < 0.8f) {
                        gameState.bossSpawned = true;
                        return EnemyType.BOSS;
                    }
                }
                float rand4 = (float) Math.random();
                if (rand4 < GameConstants.BASIC_SPAWN_CHANCE) {
                    return EnemyType.BASIC;
                } else if (rand4 < GameConstants.BASIC_SPAWN_CHANCE + GameConstants.SHOOTER_SPAWN_CHANCE) {
                    return EnemyType.SHOOTER;
                } else if (rand4 < GameConstants.BASIC_SPAWN_CHANCE + GameConstants.SHOOTER_SPAWN_CHANCE + GameConstants.BOMBER_SPAWN_CHANCE) {
                    return EnemyType.BOMBER;
                } else {
                    return EnemyType.SHIELD;
                }
            default:
                return EnemyType.BASIC;
        }
    }

    private void updateGoldIncome(float delta) {
        gameState.goldTimer += delta;
        if (gameState.goldTimer >= GameConstants.INCOME_INTERVAL) {
            gameState.gold += GameConstants.INCOME_AMOUNT;
            gameState.goldTimer -= GameConstants.INCOME_INTERVAL;
        }
    }

    /**
     * Handle music transition untuk Stage 4 Boss
     */
    private void updateBossMusicTransition() {
        // Hanya untuk Stage 4
        if (gameState.currentStage != 4) return;

        // ===== TRIGGER 1: Wave 2 Complete -> Start Fade Out =====
        Enemy currentBoss = null;
        for (Enemy enemy : gameState.enemies) {
            if (enemy.getType() == EnemyType.BOSS && !enemy.isDestroyed()) {
                currentBoss = enemy;
                gameState.currentBoss = enemy; // Update reference
                break;
            }
        }

        // ===== TRIGGER 2: Boss Detection and Position Monitoring =====
        // ===== STEP 2: Jika ada boss, mulai music transition =====
        if (currentBoss != null) {
            System.out.println("👑 Boss detected at X=" + currentBoss.getX() + ", State=" + currentBoss.getState());

            // ===== TRIGGER FADE OUT: Boss masuk ke range tertentu =====
            if (!musicFadeStarted && currentBoss.getX() <= 1200f) {
                System.out.println("🎵 Boss approaching! Starting fade out...");
                AudioManager.fadeOutCurrentMusic(2f); // 3 detik fade out
                musicFadeStarted = true;
            }

            // ===== TRIGGER BOSS MUSIC: Boss sudah sampai target position =====
            if (musicFadeStarted && !bossMusicTriggered) {
                // Check multiple conditions untuk ensure boss sudah dalam "battle position"
                boolean bossInPosition = currentBoss.getX() <= 1100f ||
                    currentBoss.hasReachedTarget() ||
                    currentBoss.getState().name().equals("STATIONARY");

                if (bossInPosition) {
                    System.out.println("👑 BOSS IN BATTLE POSITION! Starting Boss Music!");
                    AudioManager.playBossMusicWithTransition(2f); // 2 detik transition ke boss music
                    bossMusicTriggered = true;

                    // Stop the waiting message
                    System.out.println("🎵 Boss battle music activated!");
                }
            }
        } else {
            // ===== STEP 3: Boss tidak ada, tapi masih dalam wave 3 =====
            if (gameState.currentWave == 3 && musicFadeStarted && !bossMusicTriggered) {
                System.out.println("🔍 Waiting for boss to spawn...");
            }
        }
    }

    /**
     * ⭐ NEW METHOD: Reset zone ketika tower hancur
     * @param towerIndex Index tower yang hancur
     */
    private void resetTowerZone(int towerIndex) {
        // Skip main tower (index 0)
        if (towerIndex == 0) {
            System.out.println("🏰 Main tower destroyed - no zone to reset");
            return;
        }

        // Calculate deployed tower index (main tower tidak masuk deployedTowerZones)
        int deployedIndex = towerIndex - 1;

        if (deployedIndex >= 0 && deployedIndex < gameState.deployedTowerZones.size) {
            TowerDefenseScreen.Zone destroyedZone = gameState.deployedTowerZones.get(deployedIndex);
            destroyedZone.occupied = false;
            gameState.deployedTowerZones.removeIndex(deployedIndex);

            System.out.println("🔄 Zone reset for tower index " + towerIndex + " (deployed index " + deployedIndex + ")");
        } else {
            System.out.println("⚠️ Could not find deployed zone for tower index " + towerIndex);
        }
    }

    // ===== RESET BOSS MUSIC FLAGS SAAT RESTART =====
    public void resetBossMusicState() {
        musicFadeStarted = false;
        bossMusicTriggered = false;
        gameState.clearBossReference();
        System.out.println("🔄 Boss music state reset");
    }

    public void startTowerCooldown(int towerIndex) {
        gameState.towerCooldowns[towerIndex] = GameConstants.TOWER_MAX_COOLDOWNS[towerIndex];
        gameState.towerCooldownActive[towerIndex] = true;
    }

    public void startTrapCooldown(int trapIndex) {
        gameState.trapCooldowns[trapIndex] = GameConstants.TRAP_MAX_COOLDOWNS[trapIndex];
        gameState.trapCooldownActive[trapIndex] = true;
    }

    public boolean canDeployTower(int towerIndex) {
        return !gameState.towerCooldownActive[towerIndex];
    }

    public boolean canDeployTrap(int trapIndex) {
        return !gameState.trapCooldownActive[trapIndex];
    }

    private float getEnemyYOffset(EnemyType enemyType) {
        switch(enemyType) {
            case BASIC:   return 40f;  // Normal height
            case SHOOTER: return 45f;  // Slightly higher (better aim)
            case BOMBER:  return 50f;  // Higher (diving attack)
            case SHIELD:  return 70f;  // Lower (heavy armor)
            case BOSS:    return 60f;  // Elevated (imposing)
            default:      return 40f;
        }
    }
}
