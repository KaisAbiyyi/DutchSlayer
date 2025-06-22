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

    // Removed titleTexture declaration as it's replaced by a Label
    // private final Texture titleTexture;

    // Custom TextButton style
    private TextButton.TextButtonStyle customButtonStyle;

    // Textures created from Pixmap for button backgrounds (need to be disposed)
    private Texture buttonUpTexture;
    private Texture buttonDownTexture;

    // Constants for button size, border, and padding
    private static final int BUTTON_WIDTH = 280; // Smaller width for buttons
    private static final int BUTTON_HEIGHT = 80; // Smaller height for buttons
    private static final float BUTTON_FONT_SCALE = 1.8f; // Adjusted font scale to fit smaller button and remain sharp

    private static final int OUTER_BORDER_THICKNESS = 5; // Thickness of the outermost border
    private static final int INNER_PADDING = 8; // Slightly reduced padding for smaller button
    private static final int INNER_BORDER_THICKNESS = 3; // Thickness of the inner square's border

    public MainMenuScreen(Main game) {
        this.game       = game;
        this.viewport   = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage      = new Stage(viewport);
        this.skin       = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        this.background = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));

        // Removed titleTexture initialization
        // this.titleTexture    = new Texture(Gdx.files.internal("button/DutchSlayer.png"));

        // Initialize custom button style programmatically
        initializeCustomButtonStyle();

        Gdx.input.setInputProcessor(stage);
        createUI();
    }

    // Method to initialize our custom button style with nested shapes
    private void initializeCustomButtonStyle() {
        // Define custom colors for the button layers
        Color brownBorderOuter = new Color(0.3f, 0.15f, 0.05f, 1.0f); // Very dark brown for outermost border
        Color brownOuterFillUp = new Color(0.6f, 0.4f, 0.18f, 1.0f);  // Lighter brown for main button body (up state)
        Color brownOuterFillDown = new Color(0.48f, 0.32f, 0.12f, 1.0f); // Slightly darker for main button body (down state)
        Color brownBorderInner = new Color(0.3f, 0.15f, 0.05f, 1.0f); // Same dark brown for inner border
        Color brownInnerFillUp = new Color(0.57f, 0.38f, 0.16f, 1.0f); // Mid-tone brown for inner square (up state)
        Color brownInnerFillDown = new Color(0.45f, 0.28f, 0.1f, 1.0f); // Darker mid-tone brown for inner square (down state)
        Color textColor = new Color(0.25f, 0.15f, 0.05f, 1.0f);       // Dark text color

        // --- Create Pixmap for the 'up' state background (normal button) ---
        Pixmap pixmapUp = new Pixmap(BUTTON_WIDTH, BUTTON_HEIGHT, Pixmap.Format.RGBA8888);

        // 1. Draw the outermost border
        pixmapUp.setColor(brownBorderOuter);
        pixmapUp.fill(); // Fill the entire pixmap with the outer border color

        // 2. Draw the outer button's inner fill (applying outer border thickness)
        pixmapUp.setColor(brownOuterFillUp);
        pixmapUp.fillRectangle(
            OUTER_BORDER_THICKNESS,
            OUTER_BORDER_THICKNESS,
            BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS),
            BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS)
        );

        // Calculate dimensions for the inner square (after padding from outer fill)
        int innerSquareX = OUTER_BORDER_THICKNESS + INNER_PADDING;
        int innerSquareY = OUTER_BORDER_THICKNESS + INNER_PADDING;
        int innerSquareWidth = BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS) - (2 * INNER_PADDING);
        int innerSquareHeight = BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS) - (2 * INNER_PADDING);

        // Ensure inner square dimensions are valid (positive)
        if (innerSquareWidth > 0 && innerSquareHeight > 0) {
            // 3. Draw the inner square's border
            pixmapUp.setColor(brownBorderInner);
            pixmapUp.fillRectangle(
                innerSquareX,
                innerSquareY,
                innerSquareWidth,
                innerSquareHeight
            );

            // 4. Draw the inner square's fill
            pixmapUp.setColor(brownInnerFillUp);
            pixmapUp.fillRectangle(
                innerSquareX + INNER_BORDER_THICKNESS,
                innerSquareY + INNER_BORDER_THICKNESS,
                innerSquareWidth - (2 * INNER_BORDER_THICKNESS),
                innerSquareHeight - (2 * INNER_BORDER_THICKNESS)
            );
        }

        buttonUpTexture = new Texture(pixmapUp);
        pixmapUp.dispose(); // Dispose the Pixmap

        // --- Create Pixmap for the 'down' state background (pressed button) ---
        Pixmap pixmapDown = new Pixmap(BUTTON_WIDTH, BUTTON_HEIGHT, Pixmap.Format.RGBA8888);

        // 1. Draw the outermost border
        pixmapDown.setColor(brownBorderOuter);
        pixmapDown.fill();

        // 2. Draw the outer button's inner fill (applying outer border thickness)
        pixmapDown.setColor(brownOuterFillDown); // Use darker color for pressed state
        pixmapDown.fillRectangle(
            OUTER_BORDER_THICKNESS,
            OUTER_BORDER_THICKNESS,
            BUTTON_WIDTH - (2 * OUTER_BORDER_THICKNESS),
            BUTTON_HEIGHT - (2 * OUTER_BORDER_THICKNESS)
        );

        // Use same inner square dimensions
        if (innerSquareWidth > 0 && innerSquareHeight > 0) {
            // 3. Draw the inner square's border
            pixmapDown.setColor(brownBorderInner);
            pixmapDown.fillRectangle(
                innerSquareX,
                innerSquareY,
                innerSquareWidth,
                innerSquareHeight
            );

            // 4. Draw the inner square's fill
            pixmapDown.setColor(brownInnerFillDown); // Use darker color for pressed state
            pixmapDown.fillRectangle(
                innerSquareX + INNER_BORDER_THICKNESS,
                innerSquareY + INNER_BORDER_THICKNESS,
                innerSquareWidth - (2 * INNER_BORDER_THICKNESS),
                innerSquareHeight - (2 * INNER_BORDER_THICKNESS)
            );
        }

        buttonDownTexture = new Texture(pixmapDown);
        pixmapDown.dispose();

        // Create TextureRegionDrawables from these Textures
        TextureRegionDrawable upDrawable = new TextureRegionDrawable(buttonUpTexture);
        TextureRegionDrawable downDrawable = new TextureRegionDrawable(buttonDownTexture);

        // Get the default font from the skin
        BitmapFont defaultFont = skin.getFont("default-font");
        if (defaultFont == null) {
            Gdx.app.error("MainMenuScreen", "Default font not found in uiskin.json. Please ensure 'default-font' is defined.");
            // Fallback strategy: You might want to load a default font directly here if skin fails
            // defaultFont = new BitmapFont(Gdx.files.internal("path/to/your/default/font.fnt"));
        }
        // Set font filter for sharper text.
        // This is crucial for non-blurry text when scaling down or up.
        defaultFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);


        // Create the TextButton.TextButtonStyle
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

        // 1) Title (Now a Label instead of an Image)
        Label titleLabel = new Label("MERAH PUTIH: THE LAST STAND", skin);
        titleLabel.setFontScale(4.5f); // <<-- Ukuran font diperbesar untuk efek bold banget
        titleLabel.setColor(customButtonStyle.fontColor); // <<-- Warna teks diubah menjadi coklat dari button style
        // Tidak ada kode tambahan untuk shadow atau border di sini,
        // jadi jika ada, kemungkinan berasal dari aset font itu sendiri atau uiskin.json.
        rootTable.add(titleLabel)
            .padTop(50) // Adjust padding to position the title
            .padBottom(50)
            .center()
            .row();

        // 2) Button Table
        Table buttonTable = new Table();
        buttonTable.defaults().padBottom(20);

        // All buttons use the customButtonStyle and new size
        TextButton startBtn = new TextButton("START", customButtonStyle);
        startBtn.getLabel().setFontScale(BUTTON_FONT_SCALE); // Apply adjusted font scale
        startBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new ModeSelectionScreen(game));
            }
        });
        buttonTable.add(startBtn).size(BUTTON_WIDTH, BUTTON_HEIGHT).row();

        TextButton settingsBtn = new TextButton("SETTINGS", customButtonStyle);
        settingsBtn.getLabel().setFontScale(BUTTON_FONT_SCALE); // Apply adjusted font scale
        settingsBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new SettingScreen(game));
            }
        });
        buttonTable.add(settingsBtn).size(BUTTON_WIDTH, BUTTON_HEIGHT).row();

        TextButton aboutBtn = new TextButton("ABOUT", customButtonStyle);
        aboutBtn.getLabel().setFontScale(BUTTON_FONT_SCALE); // Apply adjusted font scale
        aboutBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new AboutScreen(game));
            }
        });
        buttonTable.add(aboutBtn).size(BUTTON_WIDTH, BUTTON_HEIGHT).row();

        TextButton exitBtn = new TextButton("EXIT", customButtonStyle);
        exitBtn.getLabel().setFontScale(BUTTON_FONT_SCALE); // Apply adjusted font scale
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
    @Override public void hide()   {
        // Don't stop music here
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
        // Removed titleTexture.dispose(); as it's no longer a Texture
        // titleTexture.dispose();

        if (buttonUpTexture != null) buttonUpTexture.dispose();
        if (buttonDownTexture != null) buttonDownTexture.dispose();
    }
}
