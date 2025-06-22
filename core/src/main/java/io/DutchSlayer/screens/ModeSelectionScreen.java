package io.DutchSlayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color; // Import Color for button style
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap; // Import Pixmap for button style
import com.badlogic.gdx.graphics.g2d.BitmapFont; // Import BitmapFont
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label; // Import Label for text title
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton; // Import TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable; // For creating drawables
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.DutchSlayer.Main;
import io.DutchSlayer.defend.utils.AudioManager;
import io.DutchSlayer.defend.utils.GameMode;
import io.DutchSlayer.utils.Constant; // Using Constant.SCREEN_WIDTH/HEIGHT for consistency

public class ModeSelectionScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private final FitViewport viewport;
    private final Skin skin;

    private final Texture background;
    // Custom TextButton style properties, same as MainMenuScreen
    private TextButton.TextButtonStyle customButtonStyle;
    private Texture buttonUpTexture;
    private Texture buttonDownTexture;

    // Constants for general button size, border, and padding - consistent with MainMenuScreen
    private static final int GENERAL_BUTTON_WIDTH = 380; // Adjusted for mode selection buttons
    private static final int GENERAL_BUTTON_HEIGHT = 100;
    private static final float GENERAL_BUTTON_FONT_SCALE = 1.8f; // Adjusted font scale to fit buttons

    // Constants for BACK button specific size (from MainMenuScreen/SettingScreen)
    private static final int BACK_BUTTON_WIDTH = 180;
    private static final int BACK_BUTTON_HEIGHT = 70;
    private static final float BACK_BUTTON_FONT_SCALE = 1.2f;

    // Constants for button styling (from MainMenuScreen)
    private static final int OUTER_BORDER_THICKNESS = 5;
    private static final int INNER_PADDING = 8;
    private static final int INNER_BORDER_THICKNESS = 3;

    public ModeSelectionScreen(Main game) {
        this.game = game;
        // Using Constant.SCREEN_WIDTH/HEIGHT for consistency
        this.viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage = new Stage(viewport);
        this.skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        this.background = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));

        initializeCustomButtonStyle();

        Gdx.input.setInputProcessor(stage);
        createUI();
    }

    // Method to initialize custom button style with nested shapes, copied from MainMenuScreen
    private void initializeCustomButtonStyle() {
        Color brownBorderOuter = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownOuterFillUp = new Color(0.6f, 0.4f, 0.18f, 1.0f);
        Color brownOuterFillDown = new Color(0.48f, 0.32f, 0.12f, 1.0f);
        Color brownBorderInner = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownInnerFillUp = new Color(0.57f, 0.38f, 0.16f, 1.0f);
        Color brownInnerFillDown = new Color(0.45f, 0.28f, 0.1f, 1.0f);
        Color textColor = new Color(0.25f, 0.15f, 0.05f, 1.0f);

        Pixmap pixmapUp = new Pixmap(GENERAL_BUTTON_WIDTH, GENERAL_BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        pixmapUp.setColor(brownBorderOuter);
        pixmapUp.fill();

        pixmapUp.setColor(brownOuterFillUp);
        pixmapUp.fillRectangle(
            OUTER_BORDER_THICKNESS,
            OUTER_BORDER_THICKNESS,
            GENERAL_BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS),
            GENERAL_BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS)
        );

        int innerSquareX = OUTER_BORDER_THICKNESS + INNER_PADDING;
        int innerSquareY = OUTER_BORDER_THICKNESS + INNER_PADDING;
        int innerSquareWidth = GENERAL_BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS) - (2 * INNER_PADDING);
        int innerSquareHeight = GENERAL_BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS) - (2 * INNER_PADDING);

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

        buttonUpTexture = new Texture(pixmapUp);
        pixmapUp.dispose();

        Pixmap pixmapDown = new Pixmap(GENERAL_BUTTON_WIDTH, GENERAL_BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        pixmapDown.setColor(brownBorderOuter);
        pixmapDown.fill();

        pixmapDown.setColor(brownOuterFillDown);
        pixmapDown.fillRectangle(
            OUTER_BORDER_THICKNESS,
            OUTER_BORDER_THICKNESS,
            GENERAL_BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS),
            GENERAL_BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS)
        );

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

        buttonDownTexture = new Texture(pixmapDown);
        pixmapDown.dispose();

        TextureRegionDrawable upDrawable = new TextureRegionDrawable(buttonUpTexture);
        TextureRegionDrawable downDrawable = new TextureRegionDrawable(buttonDownTexture);

        BitmapFont defaultFont = skin.getFont("default-font");
        if (defaultFont == null) {
            Gdx.app.error("ModeSelectionScreen", "Default font not found in uiskin.json. Please ensure 'default-font' is defined.");
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

        // Back Button (top-left corner) - now a TextButton with custom style
        Table topBar = new Table();
        TextButton backButton = new TextButton("BACK", customButtonStyle);
        backButton.getLabel().setFontScale(BACK_BUTTON_FONT_SCALE); // Apply specific font scale
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        topBar.add(backButton).size(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT).pad(10).left();
        rootTable.add(topBar).expandX().left().row();

        // Title (Now a Label with "Dutch Slayer" text, consistent with MainMenuScreen)
        Label titleLabel = new Label("Dutch Slayer", skin);
        titleLabel.setFontScale(4.5f); // Same font scale as MainMenuScreen title
        titleLabel.setColor(customButtonStyle.fontColor); // Same color as MainMenuScreen title
        rootTable.add(titleLabel)
            .padTop(50) // Adjust padding to position the title
            .padBottom(50)
            .center()
            .row();

        // Button Table
        Table buttonTable = new Table();
        buttonTable.defaults().pad(20); // Increased padding between buttons for better spacing

        // Defense Button - now a TextButton with custom style
        TextButton defenseBtn = new TextButton("DEFENSE MODE", customButtonStyle);
        defenseBtn.getLabel().setFontScale(GENERAL_BUTTON_FONT_SCALE); // Apply general font scale
        defenseBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Main.currentMode = GameMode.TOWER_DEFENSE;
                game.setScreen(new StageSelectionScreen(game, true));
            }
        });
        buttonTable.add(defenseBtn).size(GENERAL_BUTTON_WIDTH + 120, GENERAL_BUTTON_HEIGHT + 20).padTop(-100).row(); // Adjusted size

        // Platformer Button - now a TextButton with custom style
        TextButton platformerBtn = new TextButton("PLATFORMER MODE", customButtonStyle);
        platformerBtn.getLabel().setFontScale(GENERAL_BUTTON_FONT_SCALE); // Apply general font scale
        platformerBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Main.currentMode = GameMode.PLATFORMER;
                game.setScreen(new StageSelectionScreen(game, false));
            }
        });
        buttonTable.add(platformerBtn).size(GENERAL_BUTTON_WIDTH + 120, GENERAL_BUTTON_HEIGHT + 20).padBottom(100).row(); // Adjusted size

        rootTable.add(buttonTable).expand().center();
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

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
        // Disposed textures created programmatically
        if (buttonUpTexture != null) buttonUpTexture.dispose();
        if (buttonDownTexture != null) buttonDownTexture.dispose();
    }
}
