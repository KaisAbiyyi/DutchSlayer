package io.DutchSlayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.defend.utils.AudioManager;
import io.DutchSlayer.utils.Constant;

public class AboutScreen implements Screen {
    private final Main game;
    private final Stage stage;
    private final FitViewport viewport;
    private final Skin skin;
    private final Texture background;

    private TextButton.TextButtonStyle customButtonStyle;
    private Texture buttonUpTexture;
    private Texture buttonDownTexture;

    private static final int BACK_BUTTON_WIDTH = 180;
    private static final int BACK_BUTTON_HEIGHT = 70;
    private static final float BACK_BUTTON_FONT_SCALE = 1.2f;

    private static final int OUTER_BORDER_THICKNESS = 5;
    private static final int INNER_PADDING = 10;
    private static final int INNER_BORDER_THICKNESS = 3;

    private static final int NINE_PATCH_PIXMAP_SIZE = 70;

    public AboutScreen(Main game) {
        this.game = game;
        this.viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage = new Stage(viewport);
        this.skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        this.background       = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));

        initializeCustomButtonStyle();
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

        Pixmap pixmapUp = new Pixmap(NINE_PATCH_PIXMAP_SIZE, NINE_PATCH_PIXMAP_SIZE, Pixmap.Format.RGBA8888);
        pixmapUp.setColor(brownBorderOuter);
        pixmapUp.fill();

        pixmapUp.setColor(brownOuterFillUp);
        pixmapUp.fillRectangle(
            OUTER_BORDER_THICKNESS,
            OUTER_BORDER_THICKNESS,
            NINE_PATCH_PIXMAP_SIZE - (2 * OUTER_BORDER_THICKNESS),
            NINE_PATCH_PIXMAP_SIZE - (2 * OUTER_BORDER_THICKNESS)
        );

        int innerSquareX = OUTER_BORDER_THICKNESS + INNER_PADDING;
        int innerSquareY = OUTER_BORDER_THICKNESS + INNER_PADDING;
        int innerSquareWidth = NINE_PATCH_PIXMAP_SIZE - (2 * OUTER_BORDER_THICKNESS) - (2 * INNER_PADDING);
        int innerSquareHeight = NINE_PATCH_PIXMAP_SIZE - (2 * OUTER_BORDER_THICKNESS) - (2 * INNER_PADDING);

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

        Pixmap pixmapDown = new Pixmap(NINE_PATCH_PIXMAP_SIZE, NINE_PATCH_PIXMAP_SIZE, Pixmap.Format.RGBA8888);
        pixmapDown.setColor(brownBorderOuter);
        pixmapDown.fill();

        pixmapDown.setColor(brownOuterFillDown);
        pixmapDown.fillRectangle(
            OUTER_BORDER_THICKNESS,
            OUTER_BORDER_THICKNESS,
            NINE_PATCH_PIXMAP_SIZE - (2 * OUTER_BORDER_THICKNESS),
            NINE_PATCH_PIXMAP_SIZE - (2 * OUTER_BORDER_THICKNESS)
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


        int splitValue = OUTER_BORDER_THICKNESS + INNER_PADDING + INNER_BORDER_THICKNESS;
        NinePatchDrawable upDrawable = new NinePatchDrawable(new NinePatch(new TextureRegion(buttonUpTexture),
            splitValue, splitValue, splitValue, splitValue));
        NinePatchDrawable downDrawable = new NinePatchDrawable(new NinePatch(new TextureRegion(buttonDownTexture),
            splitValue, splitValue, splitValue, splitValue));

        BitmapFont defaultFont = skin.getFont("default-font");
        if (defaultFont == null) {
            Gdx.app.error("AboutScreen", "Default font not found in uiskin.json. Please ensure 'default-font' is defined.");
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

        Table topBarTable = new Table();
        TextButton backButton = new TextButton("BACK", customButtonStyle);
        backButton.getLabel().setFontScale(BACK_BUTTON_FONT_SCALE);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        topBarTable.add(backButton)
            .size(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT)
            .pad(10)
            .left();
        rootTable.add(topBarTable)
            .expandX()
            .left();
        rootTable.row();

        Table aboutUsMainContainer = new Table();
        aboutUsMainContainer.defaults().center().padBottom(20);

        Label titleLabel = new Label("About Us", skin);
        titleLabel.setFontScale(2.5f);
        titleLabel.setColor(customButtonStyle.fontColor);
        aboutUsMainContainer.add(titleLabel).padBottom(40).row();

        Table kaisBox = new Table();
        kaisBox.setBackground(customButtonStyle.up);

        Label kaisLabel = new Label("Kais Abiyyi - 2350081061 - Attack Mode", skin);
        kaisLabel.setFontScale(1.0f);
        kaisLabel.setColor(customButtonStyle.fontColor);
        kaisBox.add(kaisLabel).pad(10).center();
        aboutUsMainContainer.add(kaisBox).width(Constant.SCREEN_WIDTH * 0.7f).height(BACK_BUTTON_HEIGHT + 20).padBottom(30).row();

        Table alvinBox = new Table();
        alvinBox.setBackground(customButtonStyle.up);
        Label alvinLabel = new Label("M. Alvin Pratama - 2350081076 - UI Design Implementation, Assets", skin);
        alvinLabel.setFontScale(1.0f);
        alvinLabel.setColor(customButtonStyle.fontColor);
        alvinBox.add(alvinLabel).pad(10).center();
        aboutUsMainContainer.add(alvinBox).width(Constant.SCREEN_WIDTH * 0.7f).height(BACK_BUTTON_HEIGHT + 20).padBottom(30).row();

        Table haerulBox = new Table();
        haerulBox.setBackground(customButtonStyle.up);
        Label haerulLabel = new Label("Haerul Rahman Nuryadin - 2350081089 - Defend Mode", skin);
        haerulLabel.setFontScale(1.0f);
        haerulLabel.setColor(customButtonStyle.fontColor);
        haerulBox.add(haerulLabel).pad(10).center();
        aboutUsMainContainer.add(haerulBox).width(Constant.SCREEN_WIDTH * 0.7f).height(BACK_BUTTON_HEIGHT + 20).padBottom(30).row();

        rootTable.add(aboutUsMainContainer)
            .expand()
            .center()
            .padTop(50)
            .padBottom(50);
        rootTable.row();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        if (!AudioManager.isMusicPlaying()) {
            AudioManager.playMainMenuMusic();
        }
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
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (background != null) background.dispose();

        if (buttonUpTexture != null) buttonUpTexture.dispose();
        if (buttonDownTexture != null) buttonDownTexture.dispose();
    }
}
