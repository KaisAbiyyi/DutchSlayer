package io.DutchSlayer.defend.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import io.DutchSlayer.defend.ui.ImageLoader;

/**
 * Manages all UI elements and rendering
 */
public class UIManager {
    private final OrthographicCamera camera;
    private final BitmapFont font;
    private final GlyphLayout layout;

    // Button bounds
    public Rectangle btnPause, btnRemove;
    public Rectangle btnNext, btnMenuWin;
    public Rectangle btnRetryLose, btnMenuLose;
    public Rectangle pausePanel, btnResume, btnSetting, btnMenuPause;
    public Rectangle btnMode;

    // Navbar positions
    public final float[] navTowerX = new float[3];
    public final float[] navTowerW = new float[3];
    public final float[] navTrapX = new float[3];
    public final float[] navTrapW = new float[3];

    public UIManager(OrthographicCamera camera, BitmapFont font, GlyphLayout layout) {
        this.camera = camera;
        this.font = font;
        this.layout = layout;

        calculateNavButtonPositions();
        setupStaticButtons();
    }

    private void calculateNavButtonPositions() {
        float vw = camera.viewportWidth;
        float buttonSize = 80f;
        float spacing = 18f;
        float totalWidth = (buttonSize * 6) + (spacing * 5);
        float startX = (vw - totalWidth) / 2f;

        for (int i = 0; i < 3; i++) {
            navTowerX[i] = startX + i * (buttonSize + spacing);
            navTowerW[i] = buttonSize;
        }

        for (int i = 0; i < 3; i++) {
            navTrapX[i] = startX + (3 + i) * (buttonSize + spacing);
            navTrapW[i] = buttonSize;
        }
    }

    private void setupStaticButtons() {
        float vw = camera.viewportWidth;
        float yNav = camera.viewportHeight - GameConstants.NAVBAR_HEIGHT / 2 + 10f;

        // Pause button
        float pauseSize = 60f;
        float pauseX = vw - 20f - pauseSize;
        float pauseY = yNav - pauseSize / 2 - 8f;
        btnPause = new Rectangle(pauseX, pauseY, pauseSize, pauseSize);

        // Remove button
        float removeSize = 60f;
        float removeX = pauseX - 20f - removeSize;
        float removeY = yNav - removeSize / 2 - 8f;
        btnRemove = new Rectangle(removeX, removeY, removeSize, removeSize);
    }

    public void setupWinUI(int currentStage) {
        float centerX = camera.viewportWidth / 2f;
        float centerY = camera.viewportHeight / 2f;

        float buttonWidth = 203f;   // Sesuaikan dengan proporsi texture
        float buttonHeight = 132f;   // Sesuaikan dengan proporsi texture
        float buttonSpacing = 150f;  // Jarak antar button

        float buttonY = centerY - GameConstants.UI_HEIGHT / 2f + 60f; // Posisi vertikal

        // Posisi horizontal
        float leftButtonX = centerX - buttonWidth - buttonSpacing / 2f;
        float rightButtonX = centerX + buttonSpacing / 2f;

        btnMenuWin = new Rectangle(leftButtonX, buttonY, buttonWidth, buttonHeight);
        if (currentStage == GameConstants.FINAL_STAGE) {
            // ===== STAGE 4 (FINAL): Mode Selection Button =====
            btnMode = new Rectangle(rightButtonX, buttonY, buttonWidth, buttonHeight);
            btnNext = null;
        } else {
            // ===== STAGE 1-3: Next Stage Button =====
            btnNext = new Rectangle(rightButtonX, buttonY, buttonWidth, buttonHeight);
            btnMode = null; // Clear mode button
        }
    }

    public void setupLoseUI() {
        float centerX = camera.viewportWidth / 2f;
        float centerY = camera.viewportHeight / 2f;

        float buttonWidth = 203f;
        float buttonHeight = 132f;
        float buttonSpacing = 150f;

        float buttonY = centerY - GameConstants.UI_HEIGHT / 2f + 60f;

        float leftButtonX = centerX - buttonWidth - buttonSpacing / 2f;
        float rightButtonX = centerX + buttonSpacing / 2f;

        btnMenuLose = new Rectangle(leftButtonX, buttonY, buttonWidth, buttonHeight);
        btnRetryLose = new Rectangle(rightButtonX, buttonY, buttonWidth, buttonHeight);
    }

    public void setupPauseMenu(OrthographicCamera camera) {
        float panelWidth = 590f;
        float panelHeight = 500f;
        float panelX = (camera.viewportWidth - panelWidth) / 2f;
        float panelY = (camera.viewportHeight - panelHeight) / 2f;

        pausePanel = new Rectangle(panelX, panelY, panelWidth, panelHeight);

        float buttonWidth = 200f;
        float buttonHeight = 90f;
        float buttonSpacing = 20f;

        float buttonX = panelX + (panelWidth - buttonWidth) / 2f;

        float centerY = panelY + panelHeight / 2f;
        float totalButtonsHeight = (buttonHeight * 3) + (buttonSpacing * 2);
        float startY = centerY + (totalButtonsHeight / 2f) - 40f;

        btnResume = new Rectangle(buttonX, startY - buttonHeight, buttonWidth, buttonHeight);
        btnSetting = new Rectangle(buttonX, startY - (buttonHeight * 2) - buttonSpacing, buttonWidth, buttonHeight);
        btnMenuPause = new Rectangle(buttonX, startY - (buttonHeight * 3) - (buttonSpacing * 2), buttonWidth, buttonHeight);
    }

    public void drawGoldUI(SpriteBatch batch, GameState gameState) {
        float vy = camera.viewportHeight;
        float yNav = vy - GameConstants.NAVBAR_HEIGHT / 2 + 10f;

        float goldIconSize = 80f;
        float goldX = 20f;
        float goldY = yNav - 50f;

        if (ImageLoader.goldIconTex != null) {
            batch.draw(ImageLoader.goldIconTex, goldX, goldY, goldIconSize, goldIconSize);
        }

        String goldText = String.valueOf(gameState.gold);
        layout.setText(font, goldText);
        float textX = goldX + (goldIconSize - layout.width) / 2f;
        float textY = goldY + 20f;

        font.setColor(Color.YELLOW);
        font.draw(batch, goldText, textX, textY);
        font.setColor(Color.WHITE);
    }

    public void drawNavbarButtons(SpriteBatch batch, ShapeRenderer shapes, GameState gameState) {
        float yNav = camera.viewportHeight - GameConstants.NAVBAR_HEIGHT / 2 + 10f;
        float buttonSize = 80f;
        float buttonY = yNav - buttonSize / 2 - 5f;

        Texture[] towerTextures = {
            ImageLoader.UITowerAOE,
            ImageLoader.UITowerSpeed,
            ImageLoader.UITowerDefensif
        };

        Texture[] trapTextures = {
            ImageLoader.UITrapAttack,
            ImageLoader.UITrapSlow,
            ImageLoader.UITrapBomb
        };

        int[] towerCosts = {GameConstants.TOWER1_COST, GameConstants.TOWER2_COST, GameConstants.TOWER3_COST};
        int[] trapCosts = {GameConstants.TRAP_ATTACK_COST, GameConstants.TRAP_SLOW_COST, GameConstants.TRAP_EXPLOSION_COST};

        for (int i = 0; i < 3; i++) {
            float x = navTowerX[i];
            boolean isOnCooldown = gameState.towerCooldownActive[i];
            boolean canAfford = gameState.gold >= towerCosts[i];
            boolean isSelected = gameState.selectedType != null && gameState.selectedType.ordinal() == i;

            // Button color logic
            if (isOnCooldown) {
                batch.setColor(0.7f, 0.7f, 0.7f, 0.9f);
            } else if (isSelected) {
                batch.setColor(1f, 1f, 0.8f, 1f);
            } else if (!canAfford) {
                batch.setColor(1f, 0.7f, 0.7f, 1f);
            } else {
                batch.setColor(Color.WHITE);
            }

            // Draw button image
            if (towerTextures[i] != null) {
                batch.draw(towerTextures[i], x, buttonY, buttonSize, buttonSize);
            } else {
                batch.end();
                shapes.setProjectionMatrix(camera.combined);
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(Color.GRAY);
                shapes.rect(x, buttonY, buttonSize, buttonSize);
                shapes.end();
                batch.begin();
            }

            // Cooldown overlay
            if (isOnCooldown) {
                float cooldownProgress = 1f - (gameState.towerCooldowns[i] / GameConstants.TOWER_MAX_COOLDOWNS[i]);
                float overlayHeight = buttonSize * (1f - cooldownProgress);

                batch.end();
                shapes.setProjectionMatrix(camera.combined);
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(0f, 0f, 0f, 0.5f);
                shapes.rect(x, buttonY + buttonSize - overlayHeight, buttonSize, overlayHeight);
                shapes.end();
                batch.begin();
            }
        }

        // Draw trap buttons
        for (int i = 0; i < 3; i++) {
            float x = navTrapX[i];
            boolean isOnCooldown = gameState.trapCooldownActive[i];
            boolean canAfford = gameState.gold >= trapCosts[i];
            boolean isSelected = gameState.selectedType != null && gameState.selectedType.ordinal() == (3 + i);

            // Button color logic
            if (isOnCooldown) {
                batch.setColor(0.5f, 0.5f, 0.5f, 0.7f);
            } else if (isSelected) {
                batch.setColor(1f, 1f, 0.8f, 1f);
            } else if (!canAfford) {
                batch.setColor(1f, 0.7f, 0.7f, 1f);
            } else {
                batch.setColor(Color.WHITE);
            }

            // Draw button image
            if (trapTextures[i] != null) {
                batch.draw(trapTextures[i], x, buttonY, buttonSize, buttonSize);
            } else {
                batch.end();
                shapes.setProjectionMatrix(camera.combined);
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(Color.ORANGE);
                shapes.rect(x, buttonY, buttonSize, buttonSize);
                shapes.end();
                batch.begin();
            }

            // Cooldown overlay for traps
            if (isOnCooldown) {
                float cooldownProgress = 1f - (gameState.trapCooldowns[i] / GameConstants.TRAP_MAX_COOLDOWNS[i]);
                float overlayHeight = buttonSize * (1f - cooldownProgress);

                batch.end();
                shapes.setProjectionMatrix(camera.combined);
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(0f, 0f, 0f, 0.5f);
                shapes.rect(x, buttonY + buttonSize - overlayHeight, buttonSize, overlayHeight);
                shapes.end();
                batch.begin();
            }
        }

        batch.setColor(Color.WHITE);
        font.setColor(Color.WHITE);
    }

    public void drawWaveProgressBar(SpriteBatch batch, ShapeRenderer shapes, GameState gameState) {
        float vw = camera.viewportWidth;
        float barWidth = 200f;
        float barHeight = 15f;
        float barX = vw - barWidth - 20f;
        float barY = 20f;

        float spawnProgress = (float) gameState.spawnCount / (float) gameState.enemiesThisWave;
        int enemiesKilled = gameState.spawnCount - gameState.enemies.size;
        float killProgress = (float) enemiesKilled / (float) gameState.enemiesThisWave;

        // Background bar
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.3f, 0.3f, 0.3f, 0.8f);
        shapes.rect(barX, barY, barWidth, barHeight);
        shapes.end();

        // Spawn progress (Blue)
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.2f, 0.6f, 1f, 0.8f);
        shapes.rect(barX, barY, barWidth * spawnProgress, barHeight);
        shapes.end();

        // Kill progress (Green)
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.2f, 1f, 0.2f, 0.8f);
        shapes.rect(barX, barY, barWidth * killProgress, barHeight);
        shapes.end();

        // Border
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.WHITE);
        shapes.rect(barX, barY, barWidth, barHeight);
        shapes.end();

        // Text labels
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Wave title
        String waveTitle = "Wave " + gameState.currentWave + " / " + GameConstants.MAX_WAVE;
        layout.setText(font, waveTitle);
        float titleX = barX;
        float titleY = barY + barHeight + 20f;
        font.setColor(Color.WHITE);
        font.draw(batch, waveTitle, titleX, titleY);

        // Progress text
        String progressText = enemiesKilled + "/" + gameState.enemiesThisWave;
        layout.setText(font, progressText);
        float progressX = barX + (barWidth - layout.width) / 2f;
        float progressY = barY + (barHeight + layout.height) / 2f;
        font.setColor(Color.WHITE);
        font.draw(batch, progressText, progressX, progressY);

        // Next wave indicator
        if (gameState.spawnCount >= gameState.enemiesThisWave && gameState.enemies.size == 0 && gameState.currentWave < GameConstants.MAX_WAVE) {
            String nextWaveText = "Preparing Wave " + (gameState.currentWave + 1) + "...";
            layout.setText(font, nextWaveText);
            float nextX = barX;
            float nextY = barY - 8f;
            font.setColor(Color.CYAN);
            font.draw(batch, nextWaveText, nextX, nextY);
        }

        font.setColor(Color.WHITE);
        batch.end();
    }

    public void drawStageInfo(SpriteBatch batch, GameState gameState) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        String stageText = "STAGE " + gameState.currentStage;
        if (gameState.currentStage == GameConstants.FINAL_STAGE) {
            stageText += " - FINAL BOSS STAGE";
        }

        layout.setText(font, stageText);
        float stageX = (camera.viewportWidth - layout.width) / 2f; // Center horizontal

        // Calculate position below navbar
        float navbarY = camera.viewportHeight - GameConstants.NAVBAR_HEIGHT;
        float stageY = navbarY - 20f;

        if (gameState.currentStage == GameConstants.FINAL_STAGE) {
            font.setColor(Color.GOLD);
        } else {
            font.setColor(Color.WHITE);
        }

        font.draw(batch, stageText, stageX, stageY);
        font.setColor(Color.WHITE);
        batch.end();
    }

}
