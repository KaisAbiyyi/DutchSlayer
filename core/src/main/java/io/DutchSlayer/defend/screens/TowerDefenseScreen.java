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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.defend.entities.enemies.Enemy;
import io.DutchSlayer.defend.entities.enemies.EnemyType;
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

/**
 * Refactored TowerDefenseScreen with separated concerns
 * Now uses GameState, GameLogic, UIManager, and InputHandler
 */
public class TowerDefenseScreen implements Screen {

    // Core components
    private final Main game;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    // Refactored components
    public final GameState gameState;
    private final GameLogic gameLogic;
    private final UIManager uiManager;
    private final InputHandler inputHandler;

//    // Camera system for boss introduction
//    private float originalCameraZoom = 1f;
//    private Vector2 originalCameraPosition = new Vector2();
//    private Vector2 targetCameraPosition = new Vector2();

    // Tambahkan flag untuk track apakah sedang ke settings
    public final PauseMenu pauseMenu;
    public TowerDefenseScreen(final Main game, int stage) {
        this.game = game;

        // Initialize core components
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        ImageLoader.load();
        AudioManager.initialize();
        shapes = new ShapeRenderer();
        font = new BitmapFont();

        // Initialize refactored components
        gameState = new GameState(stage);
        uiManager = new UIManager(camera, font, layout);
        gameLogic = new GameLogic(gameState, uiManager);
        inputHandler = new InputHandler(this, gameState, uiManager, camera, game);
        // UBAH BARIS INI: Lewatkan 'this' (instance TowerDefenseScreen) ke PauseMenu
        this.pauseMenu = new PauseMenu(game, new FitViewport(1280, 720), font, this); //

        // Initialize game world
        initializeGameWorld();

        // Set input processor (This is the initial set, and needs to be re-set after pause/resume from menu)
        Gdx.input.setInputProcessor(inputHandler);
    }

    public InputHandler getInputHandler() { //
        return inputHandler; //
    }


    private void initializeGameWorld() {
        // Initialize main tower
        float towerMainH = ImageLoader.maintowertex.getHeight() * 0.6f;
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

        float baseSpacing = 150f;          // Base jarak antar zone (bisa diubah)
        float additionalGap = 20f;         // Extra gap antar tower (bisa diubah)
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

        // Initialize trap zones
        int numTrapZones = 3;
        float trapFirstCx = firstCenter + totalSpacing*3;
        float trapY0 = GameConstants.GROUND_Y;
//        float trapWidth = ImageLoader.towerTex.getWidth() * 0.2f;
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
        // Handle global ESC key for pausing
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) { //
            // Toggle the paused state in both gameState and pauseMenu.
            // This ensures they are always in sync.
            gameState.isPaused = !gameState.isPaused; //
            pauseMenu.setPaused(gameState.isPaused); //
        }

        // --- Core Render Logic ---
        // If the pause menu is active, only render it and return.
        // The pauseMenu.setPaused() method already handles setting the InputProcessor.
        if (pauseMenu.isPaused()) { //
            pauseMenu.renderIfActive(delta); //
            return; // Crucial: Stop processing game logic and rendering if paused
        }

        // If not paused, proceed with game logic and rendering
        gameLogic.update(delta); //

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        renderBackground(); //
        renderGameWorld(); //
        renderUI(); //

        if (gameState.isGameOver) {
            // Hentikan musik TowerDefenseScreen sebelum beralih
            AudioManager.stopMusic(); // Pastikan AudioManager.stopMusic() menghentikan semua musik aktif
            game.setScreen(new TowerDefenseGameOverScreen(game, gameState.currentStage, false)); // Kalah
            return;
        } else if (gameState.isGameWon) {
            // Hentikan musik TowerDefenseScreen sebelum beralih
            AudioManager.stopMusic(); // Pastikan AudioManager.stopMusic() menghentikan semua musik aktif
            game.setScreen(new TowerDefenseGameOverScreen(game, gameState.currentStage, true)); // Menang
            return;
        }

        // REMOVE THE FOLLOWING BLOCK. It's causing the re-pausing.
        /*
        if (gameState.isPaused) {
            pauseMenu.setPaused(true); // Aktifkan pause menu jika gameState mendeteksi pause
            return;
        } else {
            pauseMenu.setPaused(false);
        }
        */

        renderTowerUpgradePanel(); //
    }

    private void renderBackground() {
        float vw = camera.viewportWidth;
        float vy = camera.viewportHeight;

        // Background sky
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        if (ImageLoader.skytex != null) {
            game.batch.draw(ImageLoader.skytex, 0, 0, vw, vy);
        }
        game.batch.end();

        // Ground tiles
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
        // Draw main tower
        game.batch.begin();
        for (Tower t : gameState.towers) {
            if (t.isMain) {
                t.drawBatch(game.batch);
                break;
            }
        }

        // Draw boss if present
        if (gameState.currentBoss != null) {
            gameState.currentBoss.drawBatch(game.batch);
        }
        game.batch.end();

        // Draw deploy zones
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.LIGHT_GRAY);
        for (Zone z : gameState.zones) {
            if (!z.occupied) shapes.polygon(z.verts);
        }
        shapes.end();

        // Draw trap zones
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.ORANGE);
        for (int i = 0; i < gameState.trapZones.size; i++) {
            if (!gameState.trapZones.get(i).occupied) {
                shapes.polygon(gameState.trapVerts.get(i));
            }
        }
        shapes.end();

        // Draw traps
        game.batch.begin();
        for (Trap t : gameState.trapZones) t.drawBatch(game.batch);
        game.batch.end();

        renderEnemyHealthBars();

        // Draw game entities
        game.batch.begin();
        for (Tower t : gameState.towers) t.drawBatch(game.batch);

        // Draw enemies with health indicators
        for (Enemy e : gameState.enemies) {
            e.drawBatch(game.batch);
        }

        for (Enemy e : gameState.enemies) {
            drawEnemyTypeAndState(e);
        }

        for (io.DutchSlayer.defend.entities.projectiles.Projectile p : gameState.projectiles) p.drawBatch(game.batch);
        for (EnemyProjectile ep : gameState.enemyProjectiles) ep.drawBatch(game.batch);

        // Draw bombs with status indicators
        for (BombAsset bomb : gameState.bombs) {
            bomb.drawBatch(game.batch);
            drawBombStatus(bomb);
        }

        font.setColor(1f, 1f, 1f, 1f);
        game.batch.end();

        // Draw fallback shapes for missing textures
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

            // ===== HP BAR CONFIGURATION =====
            float barWidth = Math.min(enemyWidth * 0.6f, 30f);  // Max 30px, 60% dari enemy width
            float barHeight = 3f;                               // Lebih tipis: 3px
            float barX = enemyX + (enemyWidth - barWidth) / 2f; // Center horizontal
            float barY = enemyY + enemyHeight + 4f;             // Lebih dekat: 4px di atas enemy

            // ===== CALCULATE HP PERCENTAGE =====
            float healthPercent = (float)e.getHealth() / e.getMaxHealth();
            float filledWidth = barWidth * healthPercent;

            // ===== DRAW HP BAR BACKGROUND =====
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0.2f, 0.2f, 0.2f, 0.9f);
            shapes.rect(barX, barY, barWidth, barHeight);

            // ===== DRAW HP BAR FILL =====
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

            // ===== DRAW HP BAR BORDER =====
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.WHITE);
            shapes.rect(barX, barY, barWidth, barHeight);
            shapes.end();
        }
    }

    // ===== SEPARATE METHOD UNTUK TEXT (BATCH ONLY) =====
    private void drawEnemyTypeAndState(Enemy e) {
        // ⚠️ ASSUMES SpriteBatch is already active!
        float enemyX = e.getBounds().x;
        float enemyY = e.getBounds().y;
        float enemyWidth = e.getBounds().width;
        float enemyHeight = e.getBounds().height;

        // Calculate HP bar position to place text above it
        float barHeight = 6f;
        float barY = enemyY + enemyHeight + 8f;

        // ===== ENEMY TYPE INDICATOR =====
        String typeText = getEnemyTypeDisplay(e.getType());
        layout.setText(font, typeText);
        float typeX = enemyX + enemyWidth/2f - layout.width/2f;
        float typeY = barY + barHeight + 15f;

        font.setColor(getEnemyTypeColor(e.getType()));
        font.draw(game.batch, typeText, typeX, typeY);

        // ===== STATE INDICATOR untuk special enemies =====
        if (e.getType() == EnemyType.SHOOTER || e.getType() == EnemyType.BOMBER || e.getType() == EnemyType.BOSS) {
            String stateText = getStateDisplay(e);
            if (!stateText.isEmpty()) {
                layout.setText(font, stateText);
                float stateX = enemyX + enemyWidth/2f - layout.width/2f;
                float stateY = typeY + 15f;
                font.setColor(Color.CYAN);
                font.draw(game.batch, stateText, stateX, stateY);
            }
        }

        font.setColor(Color.WHITE);  // Reset font color
    }

    private void drawBombStatus(BombAsset bomb) {
        if (bomb.isFalling()) {
            String statusText = "↑Flying";
            layout.setText(font, statusText);
            float textX = bomb.getX() - layout.width/2f;
            float textY = bomb.getY() + 35f;

            font.setColor(1f, 1f, 0f, 0.8f);
            font.draw(game.batch, statusText, textX, textY);

            String altText = (int)bomb.getAltitude() + "px";
            layout.setText(font, altText);
            textX = bomb.getX() - layout.width/2f;
            textY = bomb.getY() + 20f;

            font.setColor(1f, 1f, 1f, 0.6f);
            font.draw(game.batch, altText, textX, textY);
        }
        else if (bomb.isLanded() && !bomb.hasExploded()) {
            float timeLeft = bomb.getTimeLeft();
            String countdownText = String.format("%.1f", timeLeft);

            layout.setText(font, countdownText);
            float textX = bomb.getX() - layout.width/2f;
            float textY = bomb.getY() + 35f;

            if (timeLeft < 1f) {
                font.setColor(1f, 0.2f, 0.2f, 1f);
            } else if (timeLeft < 2f) {
                font.setColor(1f, 0.8f, 0.2f, 1f);
            } else {
                font.setColor(1f, 1f, 1f, 1f);
            }

            font.draw(game.batch, countdownText, textX, textY);

            String statusText = "ARMED";
            layout.setText(font, statusText);
            textX = bomb.getX() - layout.width/2f;
            textY = bomb.getY() + 20f;

            font.setColor(1f, 0.4f, 0.4f, 0.8f);
            font.draw(game.batch, statusText, textX, textY);
        }
    }

    private void renderUI() {
        float vw = camera.viewportWidth;
        float vy = camera.viewportHeight;

        // Navbar background
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(0, vy - GameConstants.NAVBAR_HEIGHT, vw, GameConstants.NAVBAR_HEIGHT);
        shapes.end();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Draw UI components
        uiManager.drawGoldUI(game.batch, gameState);

        // Draw Remove & Pause buttons
        float rightMargin = 20f;
        float yNav = vy - GameConstants.NAVBAR_HEIGHT/2 + 10f;
        float pauseSize = 80f;
        float removeSize = 60f;

        float pauseX = vw - rightMargin - pauseSize;
        float pauseY = yNav - pauseSize/2 - 8f;
        float removeX = pauseX - 20f - removeSize;
        float removeY = yNav - removeSize/2 - 8f;

        // Pause button
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
                    // Ketika tombol pause UI ditekan, lakukan tindakan yang sama seperti tombol ESC
                    // yaitu, toggle state pause dan minta pauseMenu untuk menampilkan dirinya
                    gameState.isPaused = true; // Set to true since it's a pause button
                    pauseMenu.setPaused(true); // Memastikan pauseMenu ditampilkan dan mengambil input
                }
            }
        }

        // Remove button
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

        // Draw progress indicators
        if (!gameState.isPaused && !gameState.isGameOver && !gameState.isGameWon) {
            uiManager.drawWaveProgressBar(game.batch, shapes, gameState);
            uiManager.drawStageInfo(game.batch, gameState);
        }

//        if (gameState.currentStage == 4) {
//             uiManager.drawBossMusicDebugInfo(game.batch, gameState); // Uncomment untuk debug
//        }
    }



    private void renderPauseMenu() {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.6f); // Semi-transparent overlay
        shapes.rect(0, 0, camera.viewportWidth, camera.viewportHeight);
        shapes.end();

        // Panel background
        float panelW = GameConstants.PAUSE_W;
        float panelH = GameConstants.PAUSE_H;
        float px = (camera.viewportWidth - panelW) / 2f;
        float py = (camera.viewportHeight - panelH) / 2f;

        if (uiManager.pausePanel == null || uiManager.btnResume == null ||
            uiManager.btnSetting == null || uiManager.btnMenuPause == null) {
            uiManager.setupPauseMenu(camera);
            return; // Return early jika setup baru saja dilakukan
        }

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Buttons
        if (ImageLoader.PauseUI != null && uiManager.pausePanel != null) {
            game.batch.draw(ImageLoader.PauseUI,
                uiManager.pausePanel.x, uiManager.pausePanel.y,
                uiManager.pausePanel.width, uiManager.pausePanel.height);
        }

        if (ImageLoader.ResumeBtn != null && uiManager.btnResume != null) {
            // Apply press effect
            if (gameState.isResumeButtonPressed) {
                game.batch.setColor(0.7f, 0.7f, 0.7f, 1f); // Darker when pressed
            } else {
                game.batch.setColor(Color.WHITE); // Normal
            }

            game.batch.draw(ImageLoader.ResumeBtn,
                uiManager.btnResume.x, uiManager.btnResume.y,
                uiManager.btnResume.width, uiManager.btnResume.height);
        }

        if (ImageLoader.SettingBtn != null && uiManager.btnSetting != null) {
            // Apply press effect
            if (gameState.isSettingButtonPressed) {
                game.batch.setColor(0.7f, 0.7f, 0.7f, 1f); // Darker when pressed
            } else {
                game.batch.setColor(Color.WHITE); // Normal
            }

            game.batch.draw(ImageLoader.SettingBtn,
                uiManager.btnSetting.x, uiManager.btnSetting.y,
                uiManager.btnSetting.width, uiManager.btnSetting.height);
        }

        if (ImageLoader.MenuBtn != null && uiManager.btnMenuPause != null) {
            // Apply press effect
            if (gameState.isMenuButtonPressed) {
                game.batch.setColor(0.7f, 0.7f, 0.7f, 1f); // Darker when pressed
            } else {
                game.batch.setColor(Color.WHITE); // Normal
            }

            game.batch.draw(ImageLoader.MenuBtn,
                uiManager.btnMenuPause.x, uiManager.btnMenuPause.y,
                uiManager.btnMenuPause.width, uiManager.btnMenuPause.height);
        }

        game.batch.setColor(Color.WHITE);
        game.batch.end();
    }

    private void renderTowerUpgradePanel() {
        if (gameState.selectedTowerUI != null) {
            float panelW = 160, panelH = 160;
            float px = gameState.selectedTowerUI.x + (gameState.selectedTowerUI.getBounds().width/2) + 10;
            float py = gameState.selectedTowerUI.y - panelH/2;

            // Panel background
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0, 0, 0, 0.7f);
            shapes.rect(px, py, panelW, panelH);
            shapes.end();

            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.WHITE);
            shapes.rect(px, py, panelW, panelH);
            shapes.end();

            // Header text
            String header = "Upgrade Left " + gameState.selectedTowerUI.getUpgradeRemaining();
            layout.setText(font, header);
            float headerX = px + (panelW - layout.width)/2;
            float headerY = py + panelH - 12;

            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            font.draw(game.batch, header, headerX, headerY);
            game.batch.end();

            // Upgrade buttons
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

    // Helper methods for enemy display
    private String getEnemyTypeDisplay(EnemyType type) {
        switch(type) {
            case BASIC: return "⚔️";
            case SHOOTER: return "🏹";
            case BOMBER: return "💣";
            case SHIELD: return "🛡️";
            case BOSS: return "👑";
            default: return "❓";
        }
    }

    private Color getEnemyTypeColor(EnemyType type) {
        switch(type) {
            case BASIC: return Color.WHITE;
            case SHOOTER: return Color.GREEN;
            case BOMBER: return Color.ORANGE;
            case SHIELD: return Color.CYAN;
            case BOSS: return Color.PURPLE;
            default: return Color.WHITE;
        }
    }

    private String getStateDisplay(Enemy enemy) {
        switch(enemy.getType()) {
            case SHOOTER:
                return enemy.getState().name().equals("ATTACKING") ? "🎯 Shooting" : "";
            case BOMBER:
                String stateName = enemy.getState().name();
                if (stateName.equals("BOMBING")) return "💣 Planting";
                if (stateName.equals("RETREATING")) return "🏃 Fleeing";
                return "";
            case BOSS:
                if (enemy.hasReachedTarget()) return "🔥 Attacking";
                return "📍 Moving";
            default:
                return "";
        }
    }

    // Public methods for InputHandler
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
        AudioManager.playTowerDefenseMusic();

        gameState.gold = 80;
        gameState.goldTimer = 0f;
        gameState.spawnTimer = 0f;

        gameState.enemies.clear();
        gameState.projectiles.clear();
        gameState.enemyProjectiles.clear();
        gameState.bombs.clear();

        // Reset towers
        gameState.towers.clear();
        float towerMainH = ImageLoader.maintowertex.getHeight() * 0.6f;
        float tyMain = GameConstants.GROUND_Y + towerMainH/5f;
        gameState.towers.add(new Tower(
            ImageLoader.maintowertex, ImageLoader.projTex, 100, tyMain, 0.3f, false, true, TowerType.BASIC, 10, 0.1f
        ));

        // Reset zones
        for (Zone z : gameState.zones) {
            z.occupied = false;
        }
        gameState.deployedTowerZones.clear();

        // Reset traps
        for (Trap trap : gameState.trapZones) {
            trap.occupied = false;
        }

        // Reset Cooldown
        for (int i = 0; i < 3; i++) {
            gameState.towerCooldowns[i] = 0f;
            gameState.trapCooldowns[i] = 0f;
            gameState.towerCooldownActive[i] = false;
            gameState.trapCooldownActive[i] = false;
        }

        // ===== RESET UI SELECTIONS =====
        gameState.selectedType = null;
        gameState.selectedTowerUI = null;
        gameState.currentBoss = null;

        // ===== RESET CAMERA =====
        gameState.isBossIntroduction = false;
        gameState.bossIntroTimer = 0f;

        // ===== RESET STAGE-SPECIFIC SETTINGS =====
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
        System.out.println("🔄 Game restarted!");
    }

    public int getTowerCost(NavItem towerType) {
        switch(towerType) {
            case T1: return GameConstants.TOWER1_COST;
            case T2: return GameConstants.TOWER2_COST;
            case T3: return GameConstants.TOWER3_COST;
            default: return 40;
        }
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

    // Navigation enum
    public enum NavItem {
        T1, T2, T3,
        TRAP1, TRAP2, TRAP3,
        REMOVE
    }

    // Zone class
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
        if (!gameState.isPaused) {
            AudioManager.playTowerDefenseMusic();
        }
    }

    // ===== EXISTING TOWER METHODS =====
    public boolean canDeployTower(int towerIndex) {
        return gameLogic.canDeployTower(towerIndex);
    }

    public void startTowerCooldown(int towerIndex) {
        gameLogic.startTowerCooldown(towerIndex);
    }

    // ===== NEW: TRAP METHODS (SAME PATTERN) =====
    public boolean canDeployTrap(int trapIndex) {
        return gameLogic.canDeployTrap(trapIndex);
    }

    public void startTrapCooldown(int trapIndex) {
        gameLogic.startTrapCooldown(trapIndex);
    }

    // ===== TRAP INDEX MAPPING HELPER =====
    public int getTrapIndex(NavItem navItem) {
        switch(navItem) {
            case TRAP1: return GameConstants.TRAP_ATTACK_INDEX;     // 0
            case TRAP2: return GameConstants.TRAP_SLOW_INDEX;       // 1
            case TRAP3: return GameConstants.TRAP_EXPLOSION_INDEX;  // 2
            default: return -1;
        }
    }

    // ===== TRAP COST HELPER (if needed) =====
    public int getTrapCost(NavItem selectedType) {
        switch(selectedType) {
            case TRAP1: return GameConstants.TRAP_ATTACK_COST;
            case TRAP2: return GameConstants.TRAP_SLOW_COST;
            case TRAP3: return GameConstants.TRAP_EXPLOSION_COST;
            default: return 0;
        }
    }

    @Override
    public void hide() {
        if (!gameState.isPaused) {
            AudioManager.stopMusic();
        }
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
