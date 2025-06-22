package io.DutchSlayer.defend.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.defend.entities.enemies.Enemy;
import io.DutchSlayer.defend.entities.projectiles.BombAsset;
import io.DutchSlayer.defend.entities.projectiles.EnemyProjectile;
import io.DutchSlayer.defend.entities.towers.Tower;
import io.DutchSlayer.defend.entities.towers.TowerType;
import io.DutchSlayer.defend.entities.traps.Trap;
import io.DutchSlayer.defend.entities.traps.TrapType;
import io.DutchSlayer.defend.game.*;
import io.DutchSlayer.defend.ui.ImageLoader;
import io.DutchSlayer.defend.utils.AudioManager;
import io.DutchSlayer.screens.PauseMenu;

public class TowerDefenseScreen implements Screen {
    private final Main game;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    public final GameState gameState;
    private final GameLogic gameLogic;
    private final UIManager uiManager;
    private final InputHandler inputHandler;

    public final PauseMenu pauseMenu;
    public TowerDefenseScreen(final Main game, int stage) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        ImageLoader.load();
        AudioManager.initialize();
        shapes = new ShapeRenderer();
        font = new BitmapFont();

        gameState = new GameState(stage);
        uiManager = new UIManager(camera, font, layout);
        gameLogic = new GameLogic(gameState, uiManager);
        inputHandler = new InputHandler(this, gameState, uiManager, camera, game);
        this.pauseMenu = new PauseMenu(game, new FitViewport(1280, 720), font, this);

        initializeGameWorld();

        Gdx.input.setInputProcessor(inputHandler);
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    private void initializeGameWorld() {

        float tyMain = GameConstants.GROUND_Y + 95f;
        gameState.towers.add(new Tower(
            ImageLoader.maintowertex,
            ImageLoader.projTex,
            100, tyMain,
            0.3f,
            false,
            true,
            TowerType.BASIC,
            10, 0.1f
        ));

        float baseSpacing = 150f;
        float additionalGap = 20f;
        float totalSpacing = baseSpacing + additionalGap;

        float firstCenter = 100f + totalSpacing;
        float[] zoneCenters = {
            firstCenter,
            firstCenter + totalSpacing,
            firstCenter + totalSpacing*2
        };

        for (float cx : zoneCenters) {
            float w = 40f;
            float h = 40f;
            float x0 = cx - w / 2f;
            float y0 = GameConstants.GROUND_Y;
            float skew = 50f;
            float[] verts = new float[]{
                x0, y0,
                x0 + w, y0,
                x0 + w + skew, y0 - h,
                x0 + skew, y0 - h
            };
            gameState.zones.add(new Zone(verts));
        }

        int numTrapZones = 3;
        float trapFirstCx = firstCenter + totalSpacing*3;
        float trapY0 = GameConstants.GROUND_Y;
        float trapWidth = 40f;
        float trapHeight = 40f;
        float skew = 50f;

        for (int i = 0; i < numTrapZones; i++) {
            float cx = trapFirstCx + i * totalSpacing;
            float x0 = cx - trapWidth/2;
            float y0 = trapY0;
            float[] v = {
                x0, y0,
                x0 + trapWidth, y0,
                x0 + trapWidth + skew, y0 - trapHeight,
                x0 + skew, y0 - trapHeight
            };
            gameState.trapVerts.add(v);
            gameState.trapZones.add(new Trap(v, 0.2f, TrapType.ATTACK));
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            gameState.isPaused = !gameState.isPaused;
            pauseMenu.setPaused(gameState.isPaused);

            // --- TAMBAHKAN BLOK INI ---
            // Jika game baru saja dilanjutkan (isPaused menjadi false)
            if (!gameState.isPaused) {
                // Periksa apakah musik boss seharusnya aktif
                if (gameState.isBossMusicActive) {
                    // Putar ulang musik boss untuk memastikan tidak ada yang menimpanya.
                    AudioManager.playMusic(AudioManager.MusicType.BOSS_BATTLE);
                }
            }
            // --- AKHIR BLOK TAMBAHAN ---
        }

        if (pauseMenu.isPaused()) {
            pauseMenu.renderIfActive(delta);
            return;
        }

        gameLogic.update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        renderBackground();
        renderGameWorld();
        renderUI();

        if (gameState.isGameOver) {
            AudioManager.stopMusic();
            game.setScreen(new TowerDefenseGameOverScreen(game, gameState.currentStage, false));
            return;
        } else if (gameState.isGameWon) {
            AudioManager.stopMusic();
            game.setScreen(new TowerDefenseGameOverScreen(game, gameState.currentStage, true));
            return;
        }

        renderTowerUpgradePanel();
    }

    private void renderBackground() {
        float vw = camera.viewportWidth;
        float vy = camera.viewportHeight;

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        if (ImageLoader.skytex != null) {
            game.batch.draw(ImageLoader.skytex, 0, 0, vw, vy);
        }
        game.batch.end();

        Texture grass = ImageLoader.terratex;
        float tileSize = 150f;
        float overlap = 20f;
        float step = tileSize - overlap;
        int cols = 100;

        game.batch.begin();
        for (int c = 0; c < cols; c++) {
            float x = c * step;
            game.batch.draw(grass, x, 0, tileSize, tileSize);
        }
        game.batch.end();
    }

    private void renderGameWorld() {
        game.batch.begin();
        for (Tower t : gameState.towers) {
            if (t.isMain) {
                t.drawBatch(game.batch);
                break;
            }
        }

        if (gameState.currentBoss != null) {
            gameState.currentBoss.drawBatch(game.batch);
        }
        game.batch.end();

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.LIGHT_GRAY);
        for (Zone z : gameState.zones) {
            if (!z.occupied) shapes.polygon(z.verts);
        }
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.ORANGE);
        for (int i = 0; i < gameState.trapZones.size; i++) {
            if (!gameState.trapZones.get(i).occupied) {
                shapes.polygon(gameState.trapVerts.get(i));
            }
        }
        shapes.end();

        game.batch.begin();
        for (Trap t : gameState.trapZones) t.drawBatch(game.batch);
        game.batch.end();
        renderEnemyHealthBars();

        game.batch.begin();
        for (Tower t : gameState.towers) t.drawBatch(game.batch);

        for (Enemy e : gameState.enemies) {
            e.drawBatch(game.batch);
        }

        for (io.DutchSlayer.defend.entities.projectiles.Projectile p : gameState.projectiles) p.drawBatch(game.batch);
        for (EnemyProjectile ep : gameState.enemyProjectiles) ep.drawBatch(game.batch);

        for (BombAsset bomb : gameState.bombs) {
            bomb.drawBatch(game.batch);
        }

        font.setColor(1f, 1f, 1f, 1f);
        game.batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Tower t : gameState.towers) t.drawShape(shapes);
        for (Enemy e : gameState.enemies) e.drawShape(shapes);
        for (io.DutchSlayer.defend.entities.projectiles.Projectile p : gameState.projectiles) p.drawShape(shapes);
        for (EnemyProjectile ep : gameState.enemyProjectiles) ep.drawShape(shapes);
        shapes.end();
    }

    private void renderEnemyHealthBars() {
        shapes.setProjectionMatrix(camera.combined);

        for (Enemy e : gameState.enemies) {
            float enemyX = e.getBounds().x;
            float enemyY = e.getBounds().y;
            float enemyWidth = e.getBounds().width;
            float enemyHeight = e.getBounds().height;

            float barWidth = Math.min(enemyWidth * 0.6f, 30f);
            float barHeight = 3f;
            float barX = enemyX + (enemyWidth - barWidth) / 2f;
            float barY = enemyY + enemyHeight + 4f;

            float healthPercent = (float)e.getHealth() / e.getMaxHealth();
            float filledWidth = barWidth * healthPercent;

            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0.2f, 0.2f, 0.2f, 0.9f);
            shapes.rect(barX, barY, barWidth, barHeight);

            Color healthColor;
            if (healthPercent > 0.6f) {
                healthColor = Color.GREEN;
            } else if (healthPercent > 0.3f) {
                healthColor = Color.YELLOW;
            } else {
                healthColor = Color.RED;
            }

            shapes.setColor(healthColor);
            shapes.rect(barX, barY, filledWidth, barHeight);
            shapes.end();

            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.WHITE);
            shapes.rect(barX, barY, barWidth, barHeight);
            shapes.end();
        }
    }

    private void renderUI() {
        float vw = camera.viewportWidth;
        float vy = camera.viewportHeight;

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(0, vy - GameConstants.NAVBAR_HEIGHT, vw, GameConstants.NAVBAR_HEIGHT);
        shapes.end();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        uiManager.drawGoldUI(game.batch, gameState);

        float rightMargin = 20f;
        float yNav = vy - GameConstants.NAVBAR_HEIGHT/2 + 10f;
        float pauseSize = 80f;
        float removeSize = 60f;

        float pauseX = vw - rightMargin - pauseSize;
        float pauseY = yNav - pauseSize/2 - 8f;
        float removeX = pauseX - 20f - removeSize;
        float removeY = yNav - removeSize/2 - 8f;

        if (ImageLoader.PauseBtntex != null) {
            if (gameState.isPauseButtonHovered) {
                game.batch.setColor(0.8f, 0.8f, 1f, 1f);
            } else {
                game.batch.setColor(Color.WHITE);
            }
            game.batch.draw(ImageLoader.PauseBtntex, pauseX, pauseY, pauseSize, pauseSize);
            game.batch.setColor(Color.WHITE);
            if (Gdx.input.justTouched()) {
                Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(touch);
                if (touch.x >= pauseX && touch.x <= pauseX + pauseSize &&
                    touch.y >= pauseY && touch.y <= pauseY + pauseSize) {
                    gameState.isPaused = true;
                    pauseMenu.setPaused(true);
                }
            }
        }

        if (ImageLoader.removeBtnTex != null) {
            if (gameState.selectedType == NavItem.REMOVE) {
                game.batch.setColor(1f, 0.2f, 0.2f, 1f);
            } else if (gameState.isRemoveButtonHovered) {
                game.batch.setColor(1f, 0.7f, 0.7f, 1f);
            } else {
                game.batch.setColor(Color.WHITE);
            }
            game.batch.draw(ImageLoader.removeBtnTex, removeX, removeY, removeSize, removeSize);
            game.batch.setColor(Color.WHITE);
        }

        uiManager.drawNavbarButtons(game.batch, shapes, gameState);
        game.batch.end();

        if (!gameState.isPaused && !gameState.isGameOver && !gameState.isGameWon) {
            uiManager.drawWaveProgressBar(game.batch, shapes, gameState);
            uiManager.drawStageInfo(game.batch, gameState);
        }
    }

    private void renderTowerUpgradePanel() {
        if (gameState.selectedTowerUI != null) {
            float panelW = 160, panelH = 160;
            float px = gameState.selectedTowerUI.x + (gameState.selectedTowerUI.getBounds().width/2) + 10;
            float py = gameState.selectedTowerUI.y - panelH/2;

            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0, 0, 0, 0.7f);
            shapes.rect(px, py, panelW, panelH);
            shapes.end();

            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.WHITE);
            shapes.rect(px, py, panelW, panelH);
            shapes.end();

            String header = "Upgrade Left " + gameState.selectedTowerUI.getUpgradeRemaining();
            layout.setText(font, header);
            float headerX = px + (panelW - layout.width)/2;
            float headerY = py + panelH - 12;

            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            font.draw(game.batch, header, headerX, headerY);
            game.batch.end();

            float margin = 8f;
            float btnH = 24f;
            float btnW = panelW - margin*2;

            int attackCost = getAttackUpgradeCost(gameState.selectedTowerUI);
            int defenseCost = getDefenseUpgradeCost(gameState.selectedTowerUI);
            int speedCost = getSpeedUpgradeCost(gameState.selectedTowerUI);

            String[] labels = {
                "Attack ($" + attackCost + ")",
                "Defense ($" + defenseCost + ")",
                "Speed ($" + speedCost + ")"
            };

            int[] values = {
                gameState.selectedTowerUI.getAttackLevel(),
                gameState.selectedTowerUI.getDefenseLevel(),
                gameState.selectedTowerUI.getSpeedLevel()
            };

            boolean[] canAfford = {
                gameState.gold >= attackCost && gameState.selectedTowerUI.canUpgrade(),
                gameState.gold >= defenseCost && gameState.selectedTowerUI.canUpgrade(),
                gameState.gold >= speedCost && gameState.selectedTowerUI.canUpgrade()
            };

            float startY = headerY - 8;

            for (int i = 0; i < 3; i++) {
                float y = startY - (i+1)*(btnH + margin);

                shapes.begin(ShapeRenderer.ShapeType.Filled);
                if (canAfford[i]) {
                    shapes.setColor(0.2f, 0.4f, 0.2f, 1f);
                } else {
                    shapes.setColor(0.4f, 0.2f, 0.2f, 1f);
                }
                shapes.rect(px + margin, y - btnH, btnW, btnH);
                shapes.end();

                shapes.begin(ShapeRenderer.ShapeType.Line);
                if (canAfford[i]) {
                    shapes.setColor(Color.GREEN);
                } else {
                    shapes.setColor(Color.RED);
                }
                shapes.rect(px + margin, y - btnH, btnW, btnH);
                shapes.end();

                game.batch.begin();
                if (canAfford[i]) {
                    font.setColor(Color.WHITE);
                } else {
                    font.setColor(Color.LIGHT_GRAY);
                }

                font.draw(game.batch, labels[i], px + margin + 4, y - btnH/2 + 6);

                String valTxt = String.valueOf(values[i]);
                layout.setText(font, valTxt);
                font.draw(game.batch, valTxt, px + margin + btnW - layout.width - 4, y - btnH/2 + 6);

                font.setColor(Color.WHITE);
                game.batch.end();
            }
        }
    }


    public void restartGame() {
        gameState.isGameOver = false;
        gameState.isGameWon = false;
        gameState.isPaused = false;

        gameState.currentWave = 1;
        gameState.spawnCount = 0;
        gameState.enemiesThisWave = 5;
        gameState.bossSpawned = false;
        gameState.isWaveTransition = false;
        gameState.waveTransitionTimer = 0f;
        gameState.waveCompleteBonusGiven = false;

        gameLogic.resetBossMusicState();
        AudioManager.stopMusic();
        AudioManager.resetMusicLock();
        AudioManager.playTowerDefenseMusic();

        gameState.gold = 80;
        gameState.goldTimer = 0f;
        gameState.spawnTimer = 0f;

        gameState.enemies.clear();
        gameState.projectiles.clear();
        gameState.enemyProjectiles.clear();
        gameState.bombs.clear();

        gameState.towers.clear();
        float towerMainH = ImageLoader.maintowertex.getHeight() * 0.6f;
        float tyMain = GameConstants.GROUND_Y + towerMainH/5f;
        gameState.towers.add(new Tower(
            ImageLoader.maintowertex, ImageLoader.projTex, 100, tyMain, 0.3f, false, true, TowerType.BASIC, 10, 0.1f
        ));

        for (Zone z : gameState.zones) {
            z.occupied = false;
        }
        gameState.deployedTowerZones.clear();

        for (Trap trap : gameState.trapZones) {
            trap.occupied = false;
        }

        for (int i = 0; i < 3; i++) {
            gameState.towerCooldowns[i] = 0f;
            gameState.trapCooldowns[i] = 0f;
            gameState.towerCooldownActive[i] = false;
            gameState.trapCooldownActive[i] = false;
        }

        gameState.selectedType = null;
        gameState.selectedTowerUI = null;
        gameState.currentBoss = null;

        gameState.isBossIntroduction = false;
        gameState.bossIntroTimer = 0f;
        gameState.isBossMusicActive = false;

        switch(gameState.currentStage) {
            case 1:
                gameState.gold = 100;
                gameState.enemiesThisWave = 4;
                break;
            case 2:
                gameState.gold = 80;
                gameState.enemiesThisWave = 5;
                break;
            case 3:
                gameState.gold = 60;
                gameState.enemiesThisWave = 6;
                break;
            case 4:
                gameState.gold = 50;
                gameState.enemiesThisWave = 8;
                break;
            default:
                gameState.gold = 80;
                gameState.enemiesThisWave = 5;
                break;
        }
    }

    public int getTowerCost(NavItem towerType) {
        return switch (towerType) {
            case T1 -> GameConstants.TOWER1_COST;
            case T2 -> GameConstants.TOWER2_COST;
            case T3 -> GameConstants.TOWER3_COST;
            default -> 40;
        };
    }

    public int getAttackUpgradeCost(Tower tower) {
        return (int)(GameConstants.BASE_ATTACK_UPGRADE_COST * Math.pow(GameConstants.UPGRADE_COST_MULTIPLIER, tower.getAttackLevel()));
    }

    public int getDefenseUpgradeCost(Tower tower) {
        return (int)(GameConstants.BASE_DEFENSE_UPGRADE_COST * Math.pow(GameConstants.UPGRADE_COST_MULTIPLIER, tower.getDefenseLevel()));
    }

    public int getSpeedUpgradeCost(Tower tower) {
        return (int)(GameConstants.BASE_SPEED_UPGRADE_COST * Math.pow(GameConstants.UPGRADE_COST_MULTIPLIER, tower.getSpeedLevel()));
    }

    public enum NavItem {
        T1, T2, T3,
        TRAP1, TRAP2, TRAP3,
        REMOVE
    }

    public static class Zone {
        public final float[] verts;
        public boolean occupied;

        public Zone(float[] verts) {
            this.verts = verts;
        }

        public boolean contains(float x, float y) {
            return Intersector.isPointInPolygon(verts, 0, verts.length, x, y);
        }
    }

    @Override public void resize(int w, int h) {}
    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputHandler);
        // Memeriksa status musik boss setiap kali layar ditampilkan,
        // tidak peduli status pause.
        if (gameState.isBossMusicActive) {
            AudioManager.playMusic(AudioManager.MusicType.BOSS_BATTLE);
        } else {
            AudioManager.playTowerDefenseMusic();
        }
    }

    public boolean canDeployTower(int towerIndex) {
        return gameLogic.canDeployTower(towerIndex);
    }

    public void startTowerCooldown(int towerIndex) {
        gameLogic.startTowerCooldown(towerIndex);
    }


    public boolean canDeployTrap(int trapIndex) {
        return gameLogic.canDeployTrap(trapIndex);
    }

    public void startTrapCooldown(int trapIndex) {
        gameLogic.startTrapCooldown(trapIndex);
    }

    public int getTrapIndex(NavItem navItem) {
        return switch (navItem) {
            case TRAP1 -> GameConstants.TRAP_ATTACK_INDEX;
            case TRAP2 -> GameConstants.TRAP_SLOW_INDEX;
            case TRAP3 -> GameConstants.TRAP_EXPLOSION_INDEX;
            default -> -1;
        };
    }

    public int getTrapCost(NavItem selectedType) {
        return switch (selectedType) {
            case TRAP1 -> GameConstants.TRAP_ATTACK_COST;
            case TRAP2 -> GameConstants.TRAP_SLOW_COST;
            case TRAP3 -> GameConstants.TRAP_EXPLOSION_COST;
            default -> 0;
        };
    }

    @Override
    public void hide() {
//        if (!gameState.isPaused) {
//            AudioManager.stopMusic();
//        }
    }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        AudioManager.stopMusic();
        shapes.dispose();
        font.dispose();
        ImageLoader.dispose();
        pauseMenu.getStage().dispose();
    }
}
