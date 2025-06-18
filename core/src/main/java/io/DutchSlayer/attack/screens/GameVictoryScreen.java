package io.DutchSlayer.attack.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color; // Import Color
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap; // Import Pixmap
import com.badlogic.gdx.graphics.Texture; // Import Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont; // Import BitmapFont
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener; // Import ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable; // Import TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.FitViewport; // Mengganti ScreenViewport ke FitViewport untuk konsistensi
import io.DutchSlayer.Main;
import io.DutchSlayer.screens.MainMenuScreen;
import io.DutchSlayer.utils.Constant; // Asumsi Anda punya Constant untuk SCREEN_WIDTH/HEIGHT

public class GameVictoryScreen implements Screen {

    private final Main game;
    private Stage stage;
    private Skin skin;
    private Music backgroundMusic;
    private Texture backgroundTexture; // NEW: Background texture
    private TextButton.TextButtonStyle customButtonStyle; // NEW: Custom button style
    private Texture buttonUpTexture; // NEW: Texture for button up state
    private Texture buttonDownTexture; // NEW: Texture for button down state

    // Constants for button styling (copied from SettingScreen for consistency)
    private static final int BUTTON_WIDTH = 380; // Changed from 280 to match general button in SettingScreen
    private static final int BUTTON_HEIGHT = 100; // Changed from 80 to match general button in SettingScreen
    private static final int OUTER_BORDER = 5;
    private static final int INNER_PADDING = 10; // Changed from 8 to match SettingScreen
    private static final int INNER_BORDER = 3;
    private static final float FONT_SCALE = 1.8f; // For general buttons, adjust if needed

    private final int currentStage; // Simpan currentStage dari konstruktor

    public GameVictoryScreen(Main game, int currentStage) {
        this.game = game;
        this.currentStage = currentStage; // Inisialisasi currentStage

        // Menggunakan FitViewport agar tampilan konsisten dengan resolusi target
        stage = new Stage(new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT), game.batch);
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        // Load background texture
        backgroundTexture = new Texture(Gdx.files.internal("backgrounds/Main Menu.png")); // NEW

        // Initialize custom button style
        initializeCustomButtonStyle(); // NEW

        // Load music
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("backgrounds/WinMusic.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f); // Atur volume musik kemenangan

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label congrats = new Label("CONGRATULATIONS!", skin);
        congrats.setFontScale(2.5f); // Perkecil sedikit agar tidak terlalu besar dengan style baru
        // Set color to match the dark brown used in buttons/labels
        congrats.setColor(new Color(0.25f, 0.15f, 0.05f, 1.0f)); // NEW: Set warna teks


        // Main Menu Button
        TextButton mainMenuButton = new TextButton("MAIN MENU", customButtonStyle); // Use custom style
        mainMenuButton.getLabel().setFontScale(FONT_SCALE); // Apply font scale
        mainMenuButton.addListener(new ClickListener() { // Use ClickListener for consistency
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        // Next Stage Button
        TextButton nextStageButton = new TextButton("NEXT STAGE", customButtonStyle); // Use custom style
        nextStageButton.getLabel().setFontScale(FONT_SCALE); // Apply font scale
        nextStageButton.addListener(new ClickListener() { // Use ClickListener for consistency
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, currentStage + 1));
            }
        });

        table.add(congrats).padBottom(40).row();
        table.add(nextStageButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(10).row(); // Apply size
        table.add(mainMenuButton).size(BUTTON_WIDTH, BUTTON_HEIGHT).pad(10); // Apply size
    }

    // NEW: Method to initialize custom button style, copied from SettingScreen
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
            OUTER_BORDER,
            OUTER_BORDER,
            BUTTON_WIDTH - (2 * OUTER_BORDER),
            BUTTON_HEIGHT - (2 * OUTER_BORDER)
        );

        int innerSquareX = OUTER_BORDER + INNER_PADDING;
        int innerSquareY = OUTER_BORDER + INNER_PADDING;
        int innerSquareWidth = BUTTON_WIDTH - (2 * OUTER_BORDER) - (2 * INNER_PADDING);
        int innerSquareHeight = BUTTON_HEIGHT - (2 * OUTER_BORDER) - (2 * INNER_PADDING);

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
                innerSquareX + INNER_BORDER,
                innerSquareY + INNER_BORDER,
                innerSquareWidth - (2 * INNER_BORDER),
                innerSquareHeight - (2 * INNER_BORDER)
            );
        }

        buttonUpTexture = new Texture(pixmapUp);
        pixmapUp.dispose();

        Pixmap pixmapDown = new Pixmap(BUTTON_WIDTH, BUTTON_HEIGHT, Pixmap.Format.RGBA8888);
        pixmapDown.setColor(brownBorderOuter);
        pixmapDown.fill();

        pixmapDown.setColor(brownOuterFillDown);
        pixmapDown.fillRectangle(
            OUTER_BORDER,
            OUTER_BORDER,
            BUTTON_WIDTH - (2 * OUTER_BORDER),
            BUTTON_HEIGHT - (2 * OUTER_BORDER)
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
                innerSquareX + INNER_BORDER,
                innerSquareY + INNER_BORDER,
                innerSquareWidth - (2 * INNER_BORDER),
                innerSquareHeight - (2 * INNER_BORDER)
            );
        }

        buttonDownTexture = new Texture(pixmapDown);
        pixmapDown.dispose();

        TextureRegionDrawable upDrawable = new TextureRegionDrawable(buttonUpTexture);
        TextureRegionDrawable downDrawable = new TextureRegionDrawable(buttonDownTexture);

        BitmapFont defaultFont = skin.getFont("default-font");
        if (defaultFont == null) {
            Gdx.app.error("GameVictoryScreen", "Default font not found in uiskin.json. Please ensure 'default-font' is defined.");
            defaultFont = new BitmapFont();
        }
        defaultFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        customButtonStyle = new TextButton.TextButtonStyle();
        customButtonStyle.up = upDrawable;
        customButtonStyle.down = downDrawable;
        customButtonStyle.font = defaultFont;
        customButtonStyle.fontColor = textColor;
    }

    @Override
    public void show() {
        backgroundMusic.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // NEW: Draw background before stage
        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        backgroundMusic.stop();
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundMusic.dispose();
        backgroundTexture.dispose(); // NEW: Dispose background texture
        buttonUpTexture.dispose(); // NEW: Dispose button textures
        buttonDownTexture.dispose(); // NEW: Dispose button textures
        skin.dispose(); // Dispose skin
    }
}
