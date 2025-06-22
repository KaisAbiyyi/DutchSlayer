package io.DutchSlayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.DutchSlayer.Main;
import io.DutchSlayer.defend.utils.AudioManager;
import io.DutchSlayer.utils.Constant;

public class MainMenuScreen implements Screen {
    private final Main game;
    private final Stage stage;
    private final FitViewport viewport;
    private final Skin skin;
    private final Texture background;
    private TextButton.TextButtonStyle customButtonStyle;
    private Texture buttonUpTexture;
    private Texture buttonDownTexture;

    private static final int BUTTON_WIDTH = 280;
    private static final int BUTTON_HEIGHT = 80;
    private static final float BUTTON_FONT_SCALE = 1.8f;

    private static final int OUTER_BORDER_THICKNESS = 5;
    private static final int INNER_PADDING = 8;
    private static final int INNER_BORDER_THICKNESS = 3;

    public MainMenuScreen(Main game) {
        this.game       = game;
        this.viewport   = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage      = new Stage(viewport);
        this.skin       = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        this.background = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));

        initializeCustomButtonStyle();

        Gdx.input.setInputProcessor(stage);
        createUI();
    }

    private void initializeCustomButtonStyle() {
        Color brownBorderOuter = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownOuterFillUp = new Color(0.6f, 0.4f, 0.18f, 1.0f);
        Color brownOuterFillDown = new Color(0.48f, 0.32f, 0.12f, 1.0f);
        Color brownBorderInner = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownInnerFillUp = new Color(0.57f, 0.38f, 0.16f, 1.0f);
        Color brownInnerFillDown = new Color(0.45f, 0.28f, 0.1f, 1.0f);
        Color textColor = new Color(0.25f, 0.15f, 0.05f, 1.0f);

        Pixmap pixmapUp = new Pixmap(BUTTON_WIDTH, BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        pixmapUp.setColor(brownBorderOuter);
        pixmapUp.fill();
        pixmapUp.setColor(brownOuterFillUp);
        pixmapUp.fillRectangle(
            OUTER_BORDER_THICKNESS,
            OUTER_BORDER_THICKNESS,
            BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS),
            BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS)
        );

        int innerSquareX = OUTER_BORDER_THICKNESS + INNER_PADDING;
        int innerSquareY = OUTER_BORDER_THICKNESS + INNER_PADDING;
        int innerSquareWidth = BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS) - (2 * INNER_PADDING);
        int innerSquareHeight = BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS) - (2 * INNER_PADDING);

        if (innerSquareWidth > 0 && innerSquareHeight > 0) {
            pixmapUp.setColor(brownBorderInner);
            pixmapUp.fillRectangle(
                innerSquareX,
                innerSquareY,
                innerSquareWidth,
                innerSquareHeight
            );

            pixmapUp.setColor(brownInnerFillUp);
            pixmapUp.fillRectangle(
                innerSquareX + INNER_BORDER_THICKNESS,
                innerSquareY + INNER_BORDER_THICKNESS,
                innerSquareWidth - (2 * INNER_BORDER_THICKNESS),
                innerSquareHeight - (2 * INNER_BORDER_THICKNESS)
            );
        }

        buttonUpTexture = new Texture(pixmapUp);
        pixmapUp.dispose();

        Pixmap pixmapDown = new Pixmap(BUTTON_WIDTH, BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        pixmapDown.setColor(brownBorderOuter);
        pixmapDown.fill();

        pixmapDown.setColor(brownOuterFillDown);
        pixmapDown.fillRectangle(
            OUTER_BORDER_THICKNESS,
            OUTER_BORDER_THICKNESS,
            BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS),
            BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS)
        );

        if (innerSquareWidth > 0 && innerSquareHeight > 0) {
            pixmapDown.setColor(brownBorderInner);
            pixmapDown.fillRectangle(
                innerSquareX,
                innerSquareY,
                innerSquareWidth,
                innerSquareHeight
            );

            pixmapDown.setColor(brownInnerFillDown);
            pixmapDown.fillRectangle(
                innerSquareX + INNER_BORDER_THICKNESS,
                innerSquareY + INNER_BORDER_THICKNESS,
                innerSquareWidth - (2 * INNER_BORDER_THICKNESS),
                innerSquareHeight - (2 * INNER_BORDER_THICKNESS)
            );
        }

        buttonDownTexture = new Texture(pixmapDown);
        pixmapDown.dispose();
        TextureRegionDrawable upDrawable = new TextureRegionDrawable(buttonUpTexture);
        TextureRegionDrawable downDrawable = new TextureRegionDrawable(buttonDownTexture);

        BitmapFont defaultFont = skin.getFont("default-font");
        if (defaultFont == null) {
            Gdx.app.error("MainMenuScreen", "Default font not found in uiskin.json. Please ensure 'default-font' is defined.");


        }

        defaultFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        customButtonStyle = new TextButton.TextButtonStyle();
        customButtonStyle.up = upDrawable;
        customButtonStyle.down = downDrawable;
        customButtonStyle.font = defaultFont;
        customButtonStyle.fontColor = textColor;
    }

    private void createUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().left();
        stage.addActor(rootTable);

        Label titleLabel = new Label("Merah Putih: The last Stand", skin);
        titleLabel.setFontScale(4.5f);
        titleLabel.setColor(customButtonStyle.fontColor);

        rootTable.add(titleLabel)
            .padTop(50)
            .padBottom(50)
            .center()
            .row();

        Table buttonTable = new Table();
        buttonTable.defaults().padBottom(20);

        TextButton startBtn = new TextButton("START", customButtonStyle);
        startBtn.getLabel().setFontScale(BUTTON_FONT_SCALE);
        startBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new ModeSelectionScreen(game));
            }
        });
        buttonTable.add(startBtn).size(BUTTON_WIDTH, BUTTON_HEIGHT).row();

        TextButton settingsBtn = new TextButton("SETTINGS", customButtonStyle);
        settingsBtn.getLabel().setFontScale(BUTTON_FONT_SCALE);
        settingsBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new SettingScreen(game));
            }
        });
        buttonTable.add(settingsBtn).size(BUTTON_WIDTH, BUTTON_HEIGHT).row();

        TextButton aboutBtn = new TextButton("ABOUT", customButtonStyle);
        aboutBtn.getLabel().setFontScale(BUTTON_FONT_SCALE);
        aboutBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new AboutScreen(game));
            }
        });
        buttonTable.add(aboutBtn).size(BUTTON_WIDTH, BUTTON_HEIGHT).row();

        TextButton exitBtn = new TextButton("EXIT", customButtonStyle);
        exitBtn.getLabel().setFontScale(BUTTON_FONT_SCALE);
        exitBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                Gdx.app.exit();
            }
        });
        buttonTable.add(exitBtn).size(BUTTON_WIDTH, BUTTON_HEIGHT);

        rootTable.add(buttonTable)
            .expand()
            .center()
            .padBottom(10);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        AudioManager.playMainMenuMusic();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();
        game.batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();

        if (buttonUpTexture != null) buttonUpTexture.dispose();
        if (buttonDownTexture != null) buttonDownTexture.dispose();
    }
}
