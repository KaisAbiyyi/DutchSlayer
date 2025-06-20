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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.defend.screens.TowerDefenseScreen;
import io.DutchSlayer.defend.utils.AudioManager;

public class PauseMenu {
    private final Stage stage;
    private final Main game;
    private boolean paused = false;
    private TowerDefenseScreen towerDefenseScreenRef;
    private GameScreen gameScreenRef;

    private static final int BUTTON_WIDTH = 280;
    private static final int BUTTON_HEIGHT = 80;
    private static final int OUTER_BORDER = 5;
    private static final int INNER_PADDING = 8;
    private static final int INNER_BORDER = 3;
    private static final float FONT_SCALE = 1.8f;

    public PauseMenu(Main game, Viewport viewport, BitmapFont font, TowerDefenseScreen tdScreen) {
        this(game, viewport, font, tdScreen, null);
    }

    public PauseMenu(Main game, Viewport viewport, BitmapFont font, GameScreen gameScreen) {
        this(game, viewport, font, null, gameScreen);
    }

    public PauseMenu(Main game, Viewport viewport, BitmapFont font, TowerDefenseScreen tdScreen, GameScreen gameScreen) {
        this.game = game;
        this.towerDefenseScreenRef = tdScreen;
        this.gameScreenRef = gameScreen;
        this.stage = new Stage(viewport);

        Texture bgTexture = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));
        Image bgImage = new Image(bgTexture);
        bgImage.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        stage.addActor(bgImage);

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
                setPaused(false);

                Screen currentScreen = game.getScreen();
                if (currentScreen instanceof TowerDefenseScreen) {
                    TowerDefenseScreen tdScreen = (TowerDefenseScreen) currentScreen;
                    tdScreen.gameState.isPaused = false;
                    Gdx.input.setInputProcessor(tdScreen.getInputHandler());
                    if (!tdScreen.gameState.isBossIntroduction && !tdScreen.gameState.isPaused) {
                        AudioManager.playTowerDefenseMusic();
                    }
                } else if (currentScreen instanceof GameScreen) {
                    GameScreen gs = (GameScreen) currentScreen;
                    gs.setPaused(false);

                    if (gs.getTankBoss() != null && gs.getTankBoss().isAlive()) {
                        gs.switchToBossMusic();
                    } else {
                        if (!gs.getBackgroundMusic().isPlaying()) {
                            gs.getBackgroundMusic().play();
                        }
                    }
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

    public void updateViewport() {
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused) {
            updateViewport();
            Gdx.input.setInputProcessor(stage);
        }
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
