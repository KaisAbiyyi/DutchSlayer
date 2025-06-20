package io.DutchSlayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.defend.utils.AudioManager;
import io.DutchSlayer.utils.Constant;

public class StageSelectionScreen implements Screen {
    private final Main game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Stage stage;
    private final Skin skin;
    private Texture background;
    private final boolean isDefendMode;

    private TextButton.TextButtonStyle customButtonStyle;
    private Texture buttonUpTexture;
    private Texture buttonDownTexture;

    private TextButton.TextButtonStyle goldButtonStyle;
    private Texture goldButtonUpTexture;
    private Texture goldButtonDownTexture;

    private static final int GENERAL_BUTTON_WIDTH = 280;
    private static final int GENERAL_BUTTON_HEIGHT = 80;
    private static final float GENERAL_BUTTON_FONT_SCALE = 1.8f;

    private static final int BACK_BUTTON_WIDTH = 180;
    private static final int BACK_BUTTON_HEIGHT = 70;
    private static final float BACK_BUTTON_FONT_SCALE = 1.2f;

    private static final int OUTER_BORDER_THICKNESS = 5;
    private static final int INNER_PADDING = 8;
    private static final int INNER_BORDER_THICKNESS = 3;

    public StageSelectionScreen(Main game, boolean isDefendMode) {
        this.game = game;
        this.isDefendMode = isDefendMode;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, camera);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        background = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));

        initializeCustomButtonStyles();
        createUI();
    }

    private void initializeCustomButtonStyles() {
        Color brownBorderOuter = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownOuterFillUp = new Color(0.6f, 0.4f, 0.18f, 1.0f);
        Color brownOuterFillDown = new Color(0.48f, 0.32f, 0.12f, 1.0f);
        Color brownBorderInner = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownInnerFillUp = new Color(0.57f, 0.38f, 0.16f, 1.0f);
        Color brownInnerFillDown = new Color(0.45f, 0.28f, 0.1f, 1.0f);
        Color textColor = new Color(0.25f, 0.15f, 0.05f, 1.0f);

        BitmapFont defaultFont = skin.getFont("default-font");
        if (defaultFont == null) {
            Gdx.app.error("StageSelectionScreen", "Default font not found in uiskin.json. Please ensure 'default-font' is defined.");
        }
        defaultFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        Pixmap pixmapUp = new Pixmap(GENERAL_BUTTON_WIDTH, GENERAL_BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        pixmapUp.setColor(brownBorderOuter);
        pixmapUp.fill();
        pixmapUp.setColor(brownOuterFillUp);
        pixmapUp.fillRectangle(OUTER_BORDER_THICKNESS, OUTER_BORDER_THICKNESS,
            GENERAL_BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS), GENERAL_BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS));
        int innerSquareX = OUTER_BORDER_THICKNESS + INNER_PADDING;
        int innerSquareY = OUTER_BORDER_THICKNESS + INNER_PADDING;
        int innerSquareWidth = GENERAL_BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS) - (2 * INNER_PADDING);
        int innerSquareHeight = GENERAL_BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS) - (2 * INNER_PADDING);
        if (innerSquareWidth > 0 && innerSquareHeight > 0) {
            pixmapUp.setColor(brownBorderInner);
            pixmapUp.fillRectangle(innerSquareX, innerSquareY, innerSquareWidth, innerSquareHeight);
            pixmapUp.setColor(brownInnerFillUp);
            pixmapUp.fillRectangle(innerSquareX + INNER_BORDER_THICKNESS, innerSquareY + INNER_BORDER_THICKNESS,
                innerSquareWidth - (2 * INNER_BORDER_THICKNESS), innerSquareHeight - (2 * INNER_BORDER_THICKNESS));
        }
        buttonUpTexture = new Texture(pixmapUp);
        pixmapUp.dispose();


        Pixmap pixmapDown = new Pixmap(GENERAL_BUTTON_WIDTH, GENERAL_BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        pixmapDown.setColor(brownBorderOuter);
        pixmapDown.fill();
        pixmapDown.setColor(brownOuterFillDown);
        pixmapDown.fillRectangle(OUTER_BORDER_THICKNESS, OUTER_BORDER_THICKNESS,
            GENERAL_BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS), GENERAL_BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS));
        if (innerSquareWidth > 0 && innerSquareHeight > 0) {
            pixmapDown.setColor(brownBorderInner);
            pixmapDown.fillRectangle(innerSquareX, innerSquareY, innerSquareWidth, innerSquareHeight);
            pixmapDown.setColor(brownInnerFillDown);
            pixmapDown.fillRectangle(innerSquareX + INNER_BORDER_THICKNESS, innerSquareY + INNER_BORDER_THICKNESS,
                innerSquareWidth - (2 * INNER_BORDER_THICKNESS), innerSquareHeight - (2 * INNER_BORDER_THICKNESS));
        }
        buttonDownTexture = new Texture(pixmapDown);
        pixmapDown.dispose();

        customButtonStyle = new TextButton.TextButtonStyle();
        customButtonStyle.up = new TextureRegionDrawable(buttonUpTexture);
        customButtonStyle.down = new TextureRegionDrawable(buttonDownTexture);
        customButtonStyle.font = defaultFont;
        customButtonStyle.fontColor = textColor;

        Color goldBorderOuter = new Color(0.4f, 0.3f, 0.0f, 1.0f);
        Color goldOuterFillUp = new Color(0.8f, 0.6f, 0.2f, 1.0f);
        Color goldOuterFillDown = new Color(0.64f, 0.48f, 0.16f, 1.0f);
        Color goldBorderInner = new Color(0.4f, 0.3f, 0.0f, 1.0f);
        Color goldInnerFillUp = new Color(0.75f, 0.55f, 0.15f, 1.0f);
        Color goldInnerFillDown = new Color(0.6f, 0.45f, 0.12f, 1.0f);
        Color goldTextColor = new Color(0.2f, 0.1f, 0.0f, 1.0f);

        Pixmap goldPixmapUp = new Pixmap(GENERAL_BUTTON_WIDTH, GENERAL_BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        goldPixmapUp.setColor(goldBorderOuter);
        goldPixmapUp.fill();
        goldPixmapUp.setColor(goldOuterFillUp);
        goldPixmapUp.fillRectangle(OUTER_BORDER_THICKNESS, OUTER_BORDER_THICKNESS,
            GENERAL_BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS), GENERAL_BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS));
        if (innerSquareWidth > 0 && innerSquareHeight > 0) {
            goldPixmapUp.setColor(goldBorderInner);
            goldPixmapUp.fillRectangle(innerSquareX, innerSquareY, innerSquareWidth, innerSquareHeight);
            goldPixmapUp.setColor(goldInnerFillUp);
            goldPixmapUp.fillRectangle(innerSquareX + INNER_BORDER_THICKNESS, innerSquareY + INNER_BORDER_THICKNESS,
                innerSquareWidth - (2 * INNER_BORDER_THICKNESS), innerSquareHeight - (2 * INNER_BORDER_THICKNESS));
        }
        goldButtonUpTexture = new Texture(goldPixmapUp);
        goldPixmapUp.dispose();

        Pixmap goldPixmapDown = new Pixmap(GENERAL_BUTTON_WIDTH, GENERAL_BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        goldPixmapDown.setColor(goldBorderOuter);
        goldPixmapDown.fill();
        goldPixmapDown.setColor(goldOuterFillDown);
        goldPixmapDown.fillRectangle(OUTER_BORDER_THICKNESS, OUTER_BORDER_THICKNESS,
            GENERAL_BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS), GENERAL_BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS));
        if (innerSquareWidth > 0 && innerSquareHeight > 0) {
            goldPixmapDown.setColor(goldBorderInner);
            goldPixmapDown.fillRectangle(innerSquareX, innerSquareY, innerSquareWidth, innerSquareHeight);
            goldPixmapDown.setColor(goldInnerFillDown);
            goldPixmapDown.fillRectangle(innerSquareX + INNER_BORDER_THICKNESS, innerSquareY + INNER_BORDER_THICKNESS,
                innerSquareWidth - (2 * INNER_BORDER_THICKNESS), innerSquareHeight - (2 * INNER_BORDER_THICKNESS));
        }
        goldButtonDownTexture = new Texture(goldPixmapDown);
        goldPixmapDown.dispose();

        goldButtonStyle = new TextButton.TextButtonStyle();
        goldButtonStyle.up = new TextureRegionDrawable(goldButtonUpTexture);
        goldButtonStyle.down = new TextureRegionDrawable(goldButtonDownTexture);
        goldButtonStyle.font = defaultFont;
        goldButtonStyle.fontColor = goldTextColor;
    }


    private void createUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().left();
        stage.addActor(rootTable);

        Table topBar = new Table();
        TextButton backButton = new TextButton("BACK", customButtonStyle);
        backButton.getLabel().setFontScale(BACK_BUTTON_FONT_SCALE);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new ModeSelectionScreen(game));
            }
        });
        topBar.add(backButton)
            .size(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT)
            .pad(10)
            .left();
        rootTable.add(topBar).expandX().left().row();

        String titleText = isDefendMode ? "DEFEND MODE" : "ATTACK MODE";
        Label titleLabel = new Label(titleText, skin);
        titleLabel.setFontScale(4.5f);
        titleLabel.setColor(customButtonStyle.fontColor);
        rootTable.add(titleLabel)
            .padTop(10)
            .padBottom(10)
            .center()
            .row();

        Table levelTable = new Table();
        levelTable.defaults().pad(10);

        int numberOfStages = isDefendMode ? 4 : 3;
        for (int i = 0; i < numberOfStages; i++) {
            final int levelNum = i + 1;

            TextButton levelBtn;
            if (levelNum == numberOfStages) {
                levelBtn = new TextButton("Level " + levelNum, goldButtonStyle);
            } else {
                levelBtn = new TextButton("Level " + levelNum, customButtonStyle);
            }
            levelBtn.getLabel().setFontScale(GENERAL_BUTTON_FONT_SCALE);
            levelBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.setScreen(new LoadingScreen(game, levelNum, isDefendMode));
                }
            });

            Table btnContainer = new Table();

            btnContainer.add(levelBtn).size(GENERAL_BUTTON_WIDTH, GENERAL_BUTTON_HEIGHT);
            levelTable.add(btnContainer).row();
        }

        rootTable.add(levelTable)
            .expand()
            .center()
        ;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        AudioManager.playMainMenuMusic();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.batch.draw(background,
            0, 0,
            viewport.getWorldWidth(),
            viewport.getWorldHeight());
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
        if (background != null) background.dispose();

        if (buttonUpTexture != null) buttonUpTexture.dispose();
        if (buttonDownTexture != null) buttonDownTexture.dispose();
        if (goldButtonUpTexture != null) goldButtonUpTexture.dispose();
        if (goldButtonDownTexture != null) goldButtonDownTexture.dispose();
    }
}
