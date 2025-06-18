package io.DutchSlayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin; // Not used, can be removed
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.screens.MainMenuScreen;
import io.DutchSlayer.defend.screens.TowerDefenseScreen;
import io.DutchSlayer.attack.screens.GameScreen; // Import GameScreen

public class PauseMenu {
    private final Stage stage;
    private final Main game;
    private boolean paused = false;
    private GameScreen gameScreen; // Add a reference to GameScreen

    private static final int BUTTON_WIDTH = 280;
    private static final int BUTTON_HEIGHT = 80;
    private static final int OUTER_BORDER = 5;
    private static final int INNER_PADDING = 8;
    private static final int INNER_BORDER = 3;
    private static final float FONT_SCALE = 1.8f;

    // Modify constructor to accept GameScreen
    public PauseMenu(Main game, Viewport viewport, BitmapFont font) {
        this(game, viewport, font, null); // Call overloaded constructor
    }

    public PauseMenu(Main game, Viewport viewport, BitmapFont font, GameScreen gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen; // Initialize GameScreen reference
        this.stage = new Stage(viewport);

        // Background image
        Texture bgTexture = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));
        Image bgImage = new Image(bgTexture);
        bgImage.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        stage.addActor(bgImage);

        // Overlay background
        Image bgOverlay = new Image(new Texture(Gdx.files.internal("white.png")));
        bgOverlay.setColor(0, 0, 0, 0.6f);
        bgOverlay.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        stage.addActor(bgOverlay);

        Table table = new Table();
        table.setFillParent(true);
        table.center();

        TextButton resumeButton = createStyledButton("RESUME", font);
        TextButton settingsButton = createStyledButton("SETTINGS", font);
        TextButton mainMenuButton = createStyledButton("MAIN MENU", font);

        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setPaused(false); // This hides PauseMenu and sets input processor to null

                // PENTING: Set isPaused di GameState TowerDefenseScreen menjadi false
                // agar render loop di TowerDefenseScreen tidak langsung mempause ulang
                if (game.getScreen() instanceof TowerDefenseScreen) {
                    ((TowerDefenseScreen) game.getScreen()).gameState.isPaused = false;
                }
                // *** NEW: Handle GameScreen specific unpause logic ***
                else if (game.getScreen() instanceof GameScreen) {
                    GameScreen gs = (GameScreen) game.getScreen();
                    gs.setPaused(false); // Synchronize GameScreen's paused state
                    // Resume music based on game state (boss or background)
                    if (gs.getTankBoss() != null && gs.getTankBoss().isAlive()) {
                        gs.switchToBossMusic();
                    } else {
                        // Only play if not boss music or boss is defeated
                        // Ensure background music is set to play after resume
                        if (!gs.getBackgroundMusic().isPlaying()) { // Check if it's not already playing
                            gs.getBackgroundMusic().play();
                        }
                    }
                    Gdx.input.setInputProcessor(null); // Ensure game input is restored
                }
            }
        });

        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Screen currentScreen = game.getScreen();
                if (currentScreen instanceof TowerDefenseScreen) {
                    TowerDefenseScreen current = (TowerDefenseScreen) currentScreen;
                    game.setScreen(new SettingScreen(game, current, current.gameState.currentStage));
                } else if (currentScreen instanceof GameScreen) {
                    GameScreen current = (GameScreen) currentScreen;
                    game.setScreen(new SettingScreen(game, current, current.getStageNumber()));
                }
            }
        });

        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        table.add(resumeButton).pad(10).size(BUTTON_WIDTH, BUTTON_HEIGHT).row();
        table.add(settingsButton).pad(10).size(BUTTON_WIDTH, BUTTON_HEIGHT).row();
        table.add(mainMenuButton).pad(10).size(BUTTON_WIDTH, BUTTON_HEIGHT).row();

        stage.addActor(table);
    }

    public boolean renderIfActive(float delta) {
        if (!paused) return false;
        stage.act(delta);
        stage.draw();
        return true;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        Gdx.input.setInputProcessor(paused ? stage : null);
    }

    private TextButton createStyledButton(String text, BitmapFont font) {
        Color brownBorderOuter = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownOuterFill = new Color(0.6f, 0.4f, 0.18f, 1.0f);
        Color brownOuterDown = new Color(0.48f, 0.32f, 0.12f, 1.0f);
        Color brownBorderInner = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownInnerFill = new Color(0.57f, 0.38f, 0.16f, 1.0f);
        Color brownInnerDown = new Color(0.45f, 0.28f, 0.2f, 1.0f);
        Color textColor = new Color(0.25f, 0.15f, 0.05f, 1.0f);

        Texture upTex = createButtonTexture(brownBorderOuter, brownOuterFill, brownBorderInner, brownInnerFill);
        Texture downTex = createButtonTexture(brownBorderOuter, brownOuterDown, brownBorderInner, brownInnerDown);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = new TextureRegionDrawable(new TextureRegion(upTex));
        style.down = new TextureRegionDrawable(new TextureRegion(downTex));
        style.font = font;
        style.fontColor = textColor;

        TextButton button = new TextButton(text, style);
        button.getLabel().setFontScale(FONT_SCALE);
        return button;
    }

    private Texture createButtonTexture(Color outerBorder, Color outerFill, Color innerBorder, Color innerFill) {
        Pixmap pixmap = new Pixmap(BUTTON_WIDTH, BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        pixmap.setColor(outerBorder);
        pixmap.fill();

        pixmap.setColor(outerFill);
        pixmap.fillRectangle(OUTER_BORDER, OUTER_BORDER, BUTTON_WIDTH - 2 * OUTER_BORDER, BUTTON_HEIGHT - 2 * OUTER_BORDER);

        int ix = OUTER_BORDER + INNER_PADDING;
        int iy = OUTER_BORDER + INNER_PADDING;
        int iw = BUTTON_WIDTH - 2 * OUTER_BORDER - 2 * INNER_PADDING;
        int ih = BUTTON_HEIGHT - 2 * OUTER_BORDER - 2 * INNER_PADDING;

        if (iw > 0 && ih > 0) {
            pixmap.setColor(innerBorder);
            pixmap.fillRectangle(ix, iy, iw, ih);
            pixmap.setColor(innerFill);
            pixmap.fillRectangle(ix + INNER_BORDER, iy + INNER_BORDER, iw - 2 * INNER_BORDER, ih - 2 * INNER_BORDER);
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
