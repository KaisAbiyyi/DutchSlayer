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
import io.DutchSlayer.screens.ModeSelectionScreen;
import io.DutchSlayer.screens.SettingScreen;
import io.DutchSlayer.screens.StageSelectionScreen;
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
                if (t.isMain) {
                    gameState.selectedTowerUI = null;
                    return true;
                } else {
                    gameState.selectedTowerUI = t;
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
                AudioManager.stopMusic();
                AudioManager.playMainMenuMusic();
                game.setScreen(new StageSelectionScreen(game, true));
            });
            return true;
        }

        if (uiManager.btnMode != null && uiManager.btnMode.contains(x, y)) {
            AudioManager.PlayBtnPaper();
            gameState.pressButton("mode");
            scheduleAction(() -> {
                AudioManager.stopMusic();
                AudioManager.playMainMenuMusic();
                game.setScreen(new ModeSelectionScreen(game));
            });
            return true;
        }

        // HANDLE NEXT STAGE BUTTON (STAGE 1-3)
        if (uiManager.btnNext != null && uiManager.btnNext.contains(x, y)) {
            AudioManager.PlayBtnPaper();
            gameState.pressButton("next");

            scheduleAction(() -> {
                if (gameState.currentStage < GameConstants.FINAL_STAGE) {
                    AudioManager.stopMusic();
                    game.setScreen(new TowerDefenseScreen(game, gameState.currentStage + 1));
                } else {
                    AudioManager.stopMusic();
                    AudioManager.playMainMenuMusic();
                    game.setScreen(new ModeSelectionScreen(game));
                }
            });
            return true;
        }
        return true;
    }

    private boolean handleLoseScreen(float x, float y) {
        if (uiManager.btnMenuLose != null && uiManager.btnMenuLose.contains(x, y)) {
            AudioManager.PlayBtnPaper();
            gameState.pressButton("menu");

            scheduleAction(() -> {
                AudioManager.stopMusic();
                game.setScreen(new StageSelectionScreen(game, true));
            });
            return true;
        }
        if (uiManager.btnRetryLose != null && uiManager.btnRetryLose.contains(x, y)) {
            AudioManager.PlayBtnPaper();
            gameState.pressButton("retry");

            scheduleAction(() -> {
                AudioManager.stopMusic();
                screen.restartGame();
            });
            return true;
        }
        return true;
    }

    private boolean handlePauseMenu(float x, float y) {
        if (uiManager.btnResume != null && uiManager.btnResume.contains(x, y)) {
            AudioManager.PlayBtnSound();
            gameState.pressButton("resume");

            scheduleAction(() -> gameState.isPaused = false);
            return true;
        }

        if (uiManager.btnSetting != null && uiManager.btnSetting.contains(x, y)) {
            AudioManager.PlayBtnSound();
            gameState.pressButton("setting");

            scheduleAction(() -> game.setScreen(new SettingScreen(game, screen, gameState.currentStage)));
            return true;
        }

        if (uiManager.btnMenuPause != null && uiManager.btnMenuPause.contains(x, y)) {
            AudioManager.PlayBtnSound();
            gameState.pressButton("menu");

            scheduleAction(() -> {
                AudioManager.playMainMenuMusic();
                game.setScreen(new StageSelectionScreen(game, true));
            });
            return true;
        }

        return true;
    }

    private boolean handleTowerUpgrade(float x, float y) {
        if (gameState.selectedTowerUI == null || gameState.selectedTowerUI.isMain) {
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
                }
                return true;
            }
            if (btnDefense.contains(x, y)) {
                int cost = screen.getDefenseUpgradeCost(gameState.selectedTowerUI);
                if (gameState.selectedTowerUI.canUpgrade() && gameState.gold >= cost) {
                    gameState.gold -= cost;
                    gameState.selectedTowerUI.upgradeDefense();
                }
                return true;
            }
            if (btnSpeed.contains(x, y)) {
                int cost = screen.getSpeedUpgradeCost(gameState.selectedTowerUI);
                if (gameState.selectedTowerUI.canUpgrade() && gameState.gold >= cost) {
                    gameState.gold -= cost;
                    gameState.selectedTowerUI.upgradeSpeed();
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
                if (t.isMain) {
                    gameState.selectedType = null;
                    return true;
                }

                AudioManager.playTowerRemoval();
                int refund = switch (t.type) {
                    case AOE -> GameConstants.TOWER1_COST / 2;
                    case FAST -> GameConstants.TOWER2_COST / 2;
                    case SLOW -> GameConstants.TOWER3_COST / 2;
                    default -> 0;
                };
                gameState.gold += refund;
                gameState.towers.removeIndex(i);

                // Remove dari deployed zones juga
                if (i - 1 < gameState.deployedTowerZones.size && i > 0) {
                    gameState.deployedTowerZones.get(i - 1).occupied = false;
                }

                gameState.selectedType = null;
                return true;
            }
        }

        // Remove trap
        for (int i = gameState.trapZones.size - 1; i >= 0; i--) {
            Trap tz = gameState.trapZones.get(i);
            if (Intersector.isPointInPolygon(gameState.trapVerts.get(i), 0, gameState.trapVerts.get(i).length, x, y)
                && tz.occupied) {

                int refund = switch (tz.getType()) {
                    case ATTACK -> GameConstants.TRAP_ATTACK_COST / 2;
                    case SLOW -> GameConstants.TRAP_SLOW_COST / 2;
                    case EXPLOSION -> GameConstants.TRAP_EXPLOSION_COST / 2;
                };

                gameState.gold += refund;
                tz.occupied = false;
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
            return true;
        }

        int cost = screen.getTowerCost(gameState.selectedType);
        for (int zoneIdx = 0; zoneIdx < gameState.zones.size; zoneIdx++) {
            TowerDefenseScreen.Zone z = gameState.zones.get(zoneIdx);
            if (!z.occupied && z.contains(x, y)) {
                if (gameState.gold >= cost) {
                    gameState.gold -= cost;
                    float cx = (z.verts[0] + z.verts[2] + z.verts[4] + z.verts[6]) / 4f;

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
                }
                return true;
            }
        }
        return false;
    }

    private boolean deployTrap(float x, float y, TrapType trapType) {
        int trapIndex = screen.getTrapIndex(gameState.selectedType);
        if (!screen.canDeployTrap(trapIndex)) {
            return true;
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
                return true;
            }
        }
        return false;
    }

    private int getTowerHP(TowerType type) {
        return switch (type) {
            case AOE -> 5;
            case FAST -> 3;
            case SLOW -> 10;
            default -> 5;
        };
    }

    private float getProjectileScaleForTowerType(TowerType type) {
        return switch (type) {
            case AOE -> 0.05f;
            case FAST -> 0.015f;
            case SLOW -> 0.5f;
            default -> 0.1f;
        };
    }

    private float getTowerScale(TowerType type) {
        return switch (type) {
            case AOE -> 0.45f;
            case FAST -> 0.55f;
            case SLOW -> 0.2f;
            case BASIC -> 0.20f;
        };
    }

    private float getProperTowerY(TowerType type) {
        float baseY = GameConstants.GROUND_Y + 15f;  // 15px di atas ground
        return switch (type) {
            case AOE -> baseY + 15f;
            case FAST -> baseY + 5f;
            case SLOW -> baseY + 40f;
            default -> baseY;
        };
    }

    private void scheduleAction(Runnable action) {
        new Thread(() -> {
            try {
                Thread.sleep((int)((float) 0.1 * 1000));
                Gdx.app.postRunnable(action);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
