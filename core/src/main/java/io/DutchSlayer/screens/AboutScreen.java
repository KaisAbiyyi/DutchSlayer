package io.DutchSlayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color; // Import Color untuk button style
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap; // Import Pixmap untuk button style
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont; // Import BitmapFont
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.defend.utils.AudioManager;
import io.DutchSlayer.utils.Constant;

// NEW IMPORTS for NinePatchDrawable
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;


public class AboutScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private final FitViewport viewport;
    private final Skin skin;

    private final Texture background;
    // Removed titleTexture declaration as it's replaced by a Label

    // Add properties for custom button style
    private TextButton.TextButtonStyle customButtonStyle;
    private Texture buttonUpTexture; // This will be the base texture for NinePatch
    private Texture buttonDownTexture; // This will be the base texture for NinePatch

    // Constants for BACK button specific size (consistent with SettingScreen)
    private static final int BACK_BUTTON_WIDTH = 180;
    private static final int BACK_BUTTON_HEIGHT = 70;
    private static final float BACK_BUTTON_FONT_SCALE = 1.2f;

    private static final int OUTER_BORDER_THICKNESS = 5;
    private static final int INNER_PADDING = 10;
    private static final int INNER_BORDER_THICKNESS = 3;

    // NEW CONSTANT: Size for the NinePatch source image. This smaller, square texture
    // will be used to define the stretchable background pattern.
    private static final int NINE_PATCH_PIXMAP_SIZE = 70; // A square size for the patch source

    public AboutScreen(Main game) {
        this.game = game;
        this.viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage = new Stage(viewport);
        this.skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        this.background       = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));
        // Removed titleTexture initialization

        // Initialize custom button style
        initializeCustomButtonStyle();
        createUI();
    }

    // Method to initialize custom button style with nested shapes
    private void initializeCustomButtonStyle() {
        // Define custom colors for the button layers, consistent with MainMenuScreen
        Color brownBorderOuter = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownOuterFillUp = new Color(0.6f, 0.4f, 0.18f, 1.0f);
        Color brownOuterFillDown = new Color(0.48f, 0.32f, 0.12f, 1.0f);
        Color brownBorderInner = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownInnerFillUp = new Color(0.57f, 0.38f, 0.16f, 1.0f);
        Color brownInnerFillDown = new Color(0.45f, 0.28f, 0.1f, 1.0f);
        Color textColor = new Color(0.25f, 0.15f, 0.05f, 1.0f);

        // --- Create Pixmap for the 'up' state (normal button) ---
        // Use NINE_PATCH_PIXMAP_SIZE for creating the base texture for NinePatch.
        // This will be a small, square texture that defines how the borders should stretch.
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

        // --- Create Pixmap for the 'down' state (pressed button) ---
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

        // Calculate the split values for the NinePatch.
        // These values represent the non-stretchable border regions (corners and edges).
        // This is the total pixel thickness from the edge of the pixmap that should NOT be stretched.
        int splitValue = OUTER_BORDER_THICKNESS + INNER_PADDING + INNER_BORDER_THICKNESS;

        // Create NinePatchDrawables from these Textures.
        // The NinePatch constructor takes the texture, and then left, right, top, bottom split values.
        NinePatchDrawable upDrawable = new NinePatchDrawable(new NinePatch(new TextureRegion(buttonUpTexture),
            splitValue, splitValue, splitValue, splitValue));
        NinePatchDrawable downDrawable = new NinePatchDrawable(new NinePatch(new TextureRegion(buttonDownTexture),
            splitValue, splitValue, splitValue, splitValue));

        // Get the default font from the skin
        BitmapFont defaultFont = skin.getFont("default-font");
        if (defaultFont == null) {
            Gdx.app.error("AboutScreen", "Default font not found in uiskin.json. Please ensure 'default-font' is defined.");
        }
        // Set font filter for sharper text.
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

        // Baris 1: Tombol Back di sudut kiri atas
        Table topBarTable = new Table();
        TextButton backButton = new TextButton("BACK", customButtonStyle);
        // Terapkan skala font spesifik untuk tombol kembali
        backButton.getLabel().setFontScale(BACK_BUTTON_FONT_SCALE);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        topBarTable.add(backButton)
            // Terapkan ukuran spesifik untuk tombol kembali
            .size(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT)
            .pad(10)
            .left();

        rootTable.add(topBarTable)
            .expandX()
            .left();
        rootTable.row();

        // Container utama untuk konten "About Us" (akan menampung judul dan kotak anggota individu)
        Table aboutUsMainContainer = new Table();
        aboutUsMainContainer.defaults().center().padBottom(20); // Default untuk elemen di dalam container ini

        // Judul "About Us" (tetap sebagai Label)
        Label titleLabel = new Label("About Us", skin);
        titleLabel.setFontScale(2.5f);
        titleLabel.setColor(customButtonStyle.fontColor);
        aboutUsMainContainer.add(titleLabel).padBottom(40).row(); // Padding bawah untuk memisahkan dari kotak anggota pertama

        // Buat kotak individual untuk setiap anggota
        // Anggota 1
        Table kaisBox = new Table();
        // Use the NinePatchDrawable here
        kaisBox.setBackground(customButtonStyle.up); // Apply the button background
        // The NinePatchDrawable handles the padding internally based on its splits.
        // No need for kaisBox.pad() if the NinePatch is defined correctly for padding.
        Label kaisLabel = new Label("Kais Abiyyi - 2350081061 - Attack Mode", skin);
        kaisLabel.setFontScale(1.0f);
        kaisLabel.setColor(customButtonStyle.fontColor);
        kaisBox.add(kaisLabel).pad(10).center(); // Padding for the label inside its cell
        aboutUsMainContainer.add(kaisBox).width(Constant.SCREEN_WIDTH * 0.7f).height(BACK_BUTTON_HEIGHT + 20).padBottom(30).row(); // Set a reasonable width and height for the box

        // Anggota 2
        Table alvinBox = new Table();
        alvinBox.setBackground(customButtonStyle.up);
        Label alvinLabel = new Label("M. Alvin Pratama - 2350081076 - UI Design Implementation, Assets", skin);
        alvinLabel.setFontScale(1.0f);
        alvinLabel.setColor(customButtonStyle.fontColor);
        alvinBox.add(alvinLabel).pad(10).center();
        aboutUsMainContainer.add(alvinBox).width(Constant.SCREEN_WIDTH * 0.7f).height(BACK_BUTTON_HEIGHT + 20).padBottom(30).row();

        // Anggota 3
        Table haerulBox = new Table();
        haerulBox.setBackground(customButtonStyle.up);
        Label haerulLabel = new Label("Haerul Rahman Nuryadin - 2350081089 - Defend Mode", skin);
        haerulLabel.setFontScale(1.0f);
        haerulLabel.setColor(customButtonStyle.fontColor);
        haerulBox.add(haerulLabel).pad(10).center();
        aboutUsMainContainer.add(haerulBox).width(Constant.SCREEN_WIDTH * 0.7f).height(BACK_BUTTON_HEIGHT + 20).padBottom(30).row();


        // Add the main container for About Us content to the rootTable
        rootTable.add(aboutUsMainContainer)
            .expand() // Allow the container to expand
            .center() // Center the container horizontally and vertically
            .padTop(50) // Adjust top padding for the entire section
            .padBottom(50); // Adjust bottom padding for the entire section
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
        // Important: Dispose programmatically created textures
        if (buttonUpTexture != null) buttonUpTexture.dispose();
        if (buttonDownTexture != null) buttonDownTexture.dispose();
    }
}
