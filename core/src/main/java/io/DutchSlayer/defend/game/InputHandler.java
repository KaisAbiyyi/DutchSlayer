package io.DutchSlayer.defend.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import io.DutchSlayer.Main;
import io.DutchSlayer.defend.entities.towers.Tower;
import io.DutchSlayer.defend.entities.towers.TowerType;
import io.DutchSlayer.defend.entities.traps.Trap;
import io.DutchSlayer.defend.entities.traps.TrapType;
import io.DutchSlayer.defend.screens.ModeSelectionScreen;
import io.DutchSlayer.defend.screens.SettingScreen;
import io.DutchSlayer.defend.screens.StageSelectionScreen;
import io.DutchSlayer.defend.screens.TowerDefenseScreen;
import io.DutchSlayer.defend.ui.ImageLoader;
import io.DutchSlayer.defend.utils.AudioManager;

/**
 * Handles all input processing for the tower defense game
 */
public class InputHandler extends InputAdapter {
    private final TowerDefenseScreen screen;
    private final GameState gameState;
    private final UIManager uiManager;
    private final OrthographicCamera camera;
    private final Main game;

    public InputHandler(TowerDefenseScreen screen, GameState gameState, UIManager uiManager,
                        OrthographicCamera camera, Main game) {
        this.screen = screen;
        this.gameState = gameState;
        this.uiManager = uiManager;
        this.camera = camera;
        this.game = game;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Vector3 v = new Vector3(screenX, screenY, 0);
        camera.unproject(v);
        gameState.mouseX = v.x;
        gameState.mouseY = v.y;

        gameState.isRemoveButtonHovered = uiManager.btnRemove.contains(gameState.mouseX, gameState.mouseY);
        gameState.isPauseButtonHovered = uiManager.btnPause.contains(gameState.mouseX, gameState.mouseY);

        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 v = new Vector3(screenX, screenY, 0);
        camera.unproject(v);
        float x = v.x, y = v.y;

        // Win screen handling
        if (gameState.isGameWon && !gameState.isPaused) {
            return handleWinScreen(x, y);
        }

        // Lose screen handling
        if (gameState.isGameOver && !gameState.isGameWon && !gameState.isPaused) {
            return handleLoseScreen(x, y);
        }

        // Pause handling
        if (!gameState.isPaused) {
            if (uiManager.btnPause.contains(x, y)) {
                gameState.isPaused = true;
                uiManager.setupPauseMenu(camera);
                return true;
            }
        } else {
            return handlePauseMenu(x, y);
        }

        if (gameState.isPaused) return true;

        // Tower upgrade panel handling
        if (gameState.selectedTowerUI != null) {
            if (handleTowerUpgrade(x, y)) {
                return true;
            }
        }

        // Remove mode handling
        if (gameState.selectedType == TowerDefenseScreen.NavItem.REMOVE) {
            return handleRemoveMode(x, y);
        }

        // Tower selection for UI
        for (Tower t : gameState.towers) {
            if (t.getBounds().contains(x, y)) {
                // ⭐ CHECK: Jangan tampilkan upgrade UI untuk main tower
                if (t.isMain) {
                    System.out.println("🏰 Main tower clicked - no upgrade available");
                    gameState.selectedTowerUI = null; // Clear any existing selection
                    return true;
                } else {
                    // Hanya set selectedTowerUI untuk tower yang bukan main
                    gameState.selectedTowerUI = t;
                    System.out.println("🔧 Tower selected for upgrade: " + t.type);
                    return true;
                }
            }
        }

        gameState.selectedTowerUI = null;

        // Navbar selection
        if (y > camera.viewportHeight - GameConstants.NAVBAR_HEIGHT) {
            return handleNavbarSelection(x, y);
        }

        // Deployment handling
        if (gameState.selectedType != null) {
            return handleDeployment(x, y);
        }

        return false;
    }

    private boolean handleWinScreen(float x, float y) {
        if (uiManager.btnMenuWin != null && uiManager.btnMenuWin.contains(x, y)) {
            AudioManager.PlayBtnPaper();
            gameState.pressButton("menu");
            scheduleAction(() -> {
                // ⭐ SELALU PLAY MAIN MENU MUSIC SAAT KEMBALI KE MENU
                System.out.println("🎵 Win Screen: Going to menu - playing main menu music");
                AudioManager.playMainMenuMusic();
                game.setScreen(new StageSelectionScreen(game, true));
            }, 0.1f);
            return true;
        }

        if (uiManager.btnMode != null && uiManager.btnMode.contains(x, y)) {
            AudioManager.PlayBtnPaper();
            gameState.pressButton("mode");
            scheduleAction(() -> {
                System.out.println("🏆 Final stage completed! Going to Mode Selection...");
                AudioManager.playMainMenuMusic();
                game.setScreen(new ModeSelectionScreen(game));
            }, 0.1f);
            return true;
        }

        // ⭐ HANDLE NEXT STAGE BUTTON (STAGE 1-3)
        if (uiManager.btnNext != null && uiManager.btnNext.contains(x, y)) {
            AudioManager.PlayBtnPaper();
            gameState.pressButton("next");

            scheduleAction(() -> {
                if (gameState.currentStage < GameConstants.FINAL_STAGE) {
                    // Lanjut ke stage berikutnya - tetap tower defense music
                    System.out.println("🎵 Win Screen: Going to next stage - keeping tower defense music");
                    game.setScreen(new TowerDefenseScreen(game, gameState.currentStage + 1));
                } else {
                    // ⭐ BACKUP: Kalau somehow masuk sini di final stage, ke mode selection
                    System.out.println("🎵 Win Screen: Final stage backup - playing main menu music");
                    AudioManager.playMainMenuMusic();
                    game.setScreen(new ModeSelectionScreen(game));
                }
            }, 0.1f);
            return true;
        }
        return true;
    }

    private boolean handleLoseScreen(float x, float y) {
        if (uiManager.btnMenuLose != null && uiManager.btnMenuLose.contains(x, y)) {
            System.out.println("Going to Main Menu...");
            AudioManager.PlayBtnPaper();
            gameState.pressButton("menu");

            scheduleAction(() -> {
                System.out.println("Going to Main Menu...");
                game.setScreen(new StageSelectionScreen(game, true));
            }, 0.1f);
            return true;
        }
        if (uiManager.btnRetryLose != null && uiManager.btnRetryLose.contains(x, y)) {
            System.out.println("Restarting game...");
            AudioManager.PlayBtnPaper();
            gameState.pressButton("retry");

            scheduleAction(() -> {
                System.out.println("Restarting game...");
                screen.restartGame();
            }, 0.1f);
            return true;
        }
        return true;
    }

    private boolean handlePauseMenu(float x, float y) {
        if (uiManager.btnResume != null && uiManager.btnResume.contains(x, y)) {
            AudioManager.PlayBtnSound();
            gameState.pressButton("resume");

            scheduleAction(() -> {
                gameState.isPaused = false;
                System.out.println("🎮 Game resumed!");
            }, 0.1f);
            return true;
        }

        if (uiManager.btnSetting != null && uiManager.btnSetting.contains(x, y)) {
            System.out.println("✅ SETTING button clicked!");
            AudioManager.PlayBtnSound();
            gameState.pressButton("setting");

            scheduleAction(() -> {
                System.out.println("⚙️ Opening Settings from Pause Menu...");
                game.setScreen(new SettingScreen(game, screen, gameState.currentStage));
            }, 0.1f);
            return true;
        }

        if (uiManager.btnMenuPause != null && uiManager.btnMenuPause.contains(x, y)) {
            AudioManager.PlayBtnSound();
            gameState.pressButton("menu");

            scheduleAction(() -> {
                System.out.println("🏠 Going to Stage Selection...");
                AudioManager.playMainMenuMusic();
                game.setScreen(new StageSelectionScreen(game, true));
            }, 0.1f);
            return true;
        }

        return true;
    }

    private boolean handleTowerUpgrade(float x, float y) {
        if (gameState.selectedTowerUI == null || gameState.selectedTowerUI.isMain) {
            System.out.println("🚫 Cannot upgrade main tower");
            gameState.selectedTowerUI = null;
            return false;
        }

        float panelW = 160, panelH = 100;
        float px = gameState.selectedTowerUI.x + gameState.selectedTowerUI.getBounds().width/2 + 10;
        float py = gameState.selectedTowerUI.y - panelH/2;
        Rectangle panelRect = new Rectangle(px, py, panelW, panelH);

        float sectionH = panelH / 3f;
        Rectangle btnAttack = new Rectangle(px, py + sectionH*2, panelW, sectionH);
        Rectangle btnDefense = new Rectangle(px, py + sectionH, panelW, sectionH);
        Rectangle btnSpeed = new Rectangle(px, py, panelW, sectionH);

        if (panelRect.contains(x, y)) {
            if (btnAttack.contains(x, y)) {
                int cost = screen.getAttackUpgradeCost(gameState.selectedTowerUI);
                if (gameState.selectedTowerUI.canUpgrade() && gameState.gold >= cost) {
                    gameState.gold -= cost;
                    gameState.selectedTowerUI.upgradeAttack();
                    System.out.println("⚔️ Tower attack upgraded!");
                }
                return true;
            }
            if (btnDefense.contains(x, y)) {
                int cost = screen.getDefenseUpgradeCost(gameState.selectedTowerUI);
                if (gameState.selectedTowerUI.canUpgrade() && gameState.gold >= cost) {
                    gameState.gold -= cost;
                    gameState.selectedTowerUI.upgradeDefense();
                    System.out.println("🛡️ Tower defense upgraded!");
                }
                return true;
            }
            if (btnSpeed.contains(x, y)) {
                int cost = screen.getSpeedUpgradeCost(gameState.selectedTowerUI);
                if (gameState.selectedTowerUI.canUpgrade() && gameState.gold >= cost) {
                    gameState.gold -= cost;
                    gameState.selectedTowerUI.upgradeSpeed();
                    System.out.println("⚡ Tower speed upgraded!");
                }
                return true;
            }
            return true;
        }

        gameState.selectedTowerUI = null;
        return false;
    }

    private boolean handleRemoveMode(float x, float y) {
        // Remove tower
        for (int i = gameState.towers.size - 1; i >= 0; i--) {
            Tower t = gameState.towers.get(i);
            if (t.getBounds().contains(x, y)) {
                // ⭐ PROTECTION: Jangan bisa remove main tower
                if (t.isMain) {
                    System.out.println("🚫 Cannot remove main tower!");
                    gameState.selectedType = null; // Clear remove mode
                    return true;
                }

                AudioManager.playTowerRemoval();
                // Remove tower biasa
                int refund = 0;
                switch(t.type) {
                    case AOE:  refund = GameConstants.TOWER1_COST / 2; break;
                    case FAST: refund = GameConstants.TOWER2_COST / 2; break;
                    case SLOW: refund = GameConstants.TOWER3_COST / 2; break;
                }
                gameState.gold += refund;
                gameState.towers.removeIndex(i);

                // Remove dari deployed zones juga
                if (i - 1 < gameState.deployedTowerZones.size && i > 0) {
                    gameState.deployedTowerZones.get(i - 1).occupied = false; // i-1 karena main tower index 0
                }

                System.out.println("🗑️ Tower removed! Refund: " + refund + ", Gold: " + gameState.gold);
                gameState.selectedType = null;
                return true;
            }
        }

        // Remove trap
        for (int i = gameState.trapZones.size - 1; i >= 0; i--) {
            Trap tz = gameState.trapZones.get(i);
            if (Intersector.isPointInPolygon(gameState.trapVerts.get(i), 0, gameState.trapVerts.get(i).length, x, y)
                && tz.occupied) {

                int refund = 0;
                switch(tz.getType()) {
                    case ATTACK:    refund = GameConstants.TRAP_ATTACK_COST / 2; break;
                    case SLOW:      refund = GameConstants.TRAP_SLOW_COST / 2; break;
                    case EXPLOSION: refund = GameConstants.TRAP_EXPLOSION_COST / 2; break;
                }

                gameState.gold += refund;
                tz.occupied = false;
                System.out.println("Trap removed! Refund: " + refund + ", Gold: " + gameState.gold);
                gameState.selectedType = null;
                return true;
            }
        }

        gameState.selectedType = null;
        return true;
    }

    private boolean handleNavbarSelection(float x, float y) {
        if (uiManager.btnRemove.contains(x, y)) {
            gameState.selectedType = TowerDefenseScreen.NavItem.REMOVE;
            return true;
        }

        // Tower selection
        for (int i = 0; i < GameConstants.NAV_TOWERS.length; i++) {
            if (x >= uiManager.navTowerX[i] && x <= uiManager.navTowerX[i] + uiManager.navTowerW[i]) {
                gameState.selectedType = TowerDefenseScreen.NavItem.values()[i];
                AudioManager.PlayBtnPaper();
                return true;
            }
        }

        // Trap selection
        for (int i = 0; i < GameConstants.NAV_TRAPS.length; i++) {
            if (x >= uiManager.navTrapX[i] && x <= uiManager.navTrapX[i] + uiManager.navTrapW[i]) {
                gameState.selectedType = TowerDefenseScreen.NavItem.values()[GameConstants.NAV_TOWERS.length + i];
                AudioManager.PlayBtnPaper();
                return true;
            }
        }

        return false;
    }

    private boolean handleDeployment(float x, float y) {
        switch(gameState.selectedType) {
            case T1:
                return deployTower(x, y, 0, TowerType.AOE, ImageLoader.tower1Tex, ImageLoader.aoeProjTex);
            case T2:
                return deployTower(x, y, 1, TowerType.FAST, ImageLoader.tower2Tex, ImageLoader.projtowtex);
            case T3:
                return deployTower(x, y, 2, TowerType.SLOW, ImageLoader.tower3Tex, ImageLoader.slowProjTex);
            case TRAP1:
                return deployTrap(x, y, TrapType.ATTACK);
            case TRAP2:
                return deployTrap(x, y, TrapType.SLOW);
            case TRAP3:
                return deployTrap(x, y, TrapType.EXPLOSION);
        }
        gameState.selectedType = null;
        return false;
    }

    private boolean deployTower(float x, float y, int towerIndex, TowerType type,
                                com.badlogic.gdx.graphics.Texture texture,
                                com.badlogic.gdx.graphics.Texture projTexture) {
        if (!screen.canDeployTower(towerIndex)) {
            System.out.println("Tower " + type + " masih cooldown!");
            return true;
        }

        int cost = screen.getTowerCost(gameState.selectedType);
        for (int zoneIdx = 0; zoneIdx < gameState.zones.size; zoneIdx++) {
            TowerDefenseScreen.Zone z = gameState.zones.get(zoneIdx);
            if (!z.occupied && z.contains(x, y)) {
                if (gameState.gold >= cost) {
                    gameState.gold -= cost;
                    float cx = (z.verts[0] + z.verts[2] + z.verts[4] + z.verts[6]) / 4f;

                    float zoneY = (z.verts[1] + z.verts[3] + z.verts[5] + z.verts[7]) / 4f;
                    float cy = getProperTowerY(type);
                    float towerScale = getTowerScale(type);

                    Tower tower = new Tower(texture, projTexture, cx, cy, towerScale, true, false,
                        type, getTowerHP(type), getProjectileScaleForTowerType(type));
                    gameState.towers.add(tower);
                    gameState.deployedTowerZones.add(z);
                    z.occupied = true;
                    gameState.selectedType = null;
                    screen.startTowerCooldown(towerIndex);
                    AudioManager.playTowerDeploy();
                    return true;
                } else {
                    System.out.println("Not enough gold! Need " + cost + ", have " + gameState.gold);
                }
                return true;
            }
        }
        return false;
    }

    private boolean deployTrap(float x, float y, TrapType trapType) {
        int trapIndex = screen.getTrapIndex(gameState.selectedType);
        if (!screen.canDeployTrap(trapIndex)) {
            System.out.println("Trap " + trapType + " masih cooldown!");
            return true; // Consume input tapi don't clear selection
        }

        int cost = screen.getTrapCost(gameState.selectedType);

        for (int trapIdx = 0; trapIdx < gameState.trapZones.size; trapIdx++) {
            Trap tz = gameState.trapZones.get(trapIdx);
            float[] trapVert = gameState.trapVerts.get(trapIdx);

            if (!tz.occupied &&
                Intersector.isPointInPolygon(trapVert, 0, trapVert.length, x, y) &&
                gameState.gold >= cost) {

                gameState.gold -= cost;
                gameState.trapZones.set(trapIdx, new Trap(trapVert, 0.2f, trapType));
                gameState.trapZones.get(trapIdx).occupied = true;
                gameState.selectedType = null;
                screen.startTrapCooldown(trapIndex);
                AudioManager.playTrapDeploy();
                System.out.println(trapType + " trap deployed!");
                return true;
            }
        }
        return false;
    }

    private int getTowerHP(TowerType type) {
        switch(type) {
            case AOE: return 5;
            case FAST: return 3;
            case SLOW: return 10;
            default: return 5;
        }
    }

    private float getProjectileScaleForTowerType(TowerType type) {
        switch(type) {
            case AOE: return 0.05f;
            case FAST: return 0.015f;
            case SLOW: return 0.5f;
            default: return 0.1f;
        }
    }

    private float getTowerScale(TowerType type) {
        switch(type) {
            case AOE:   return 0.45f;  // Sedikit lebih besar karena AOE
            case FAST:  return 0.55f;  // Kecil, karena fast attack
            case SLOW:  return 0.2f;  // Sedang
            case BASIC: return 0.20f;  // Main tower
            default:    return 0.15f;
        }
    }

    private float getProperTowerY(TowerType type) {
        float baseY = GameConstants.GROUND_Y + 15f;  // 15px di atas ground
        switch(type) {
            case AOE:   return baseY + 15f;        // AOE tower di ground level
            case FAST:  return baseY + 5f;   // Fast tower sedikit lebih rendah
            case SLOW:  return baseY + 40f;   // Slow tower sedikit lebih tinggi
            default:    return baseY;
        }
    }

    private void scheduleAction(Runnable action, float delay) {
        // Simple timer-based delayed action
        new Thread(() -> {
            try {
                Thread.sleep((int)(delay * 1000));
                // Execute on main thread
                Gdx.app.postRunnable(action);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
