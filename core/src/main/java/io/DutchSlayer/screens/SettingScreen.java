package io.DutchSlayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.defend.screens.TowerDefenseScreen;
import io.DutchSlayer.defend.utils.AudioManager;
// import io.DutchSlayer.defend.utils.TDConstants; // Perhatikan jika TDConstants.SCREEN_WIDTH/HEIGHT berbeda dari Constant - TDConstants is not used, can remove
import io.DutchSlayer.utils.Constant; // Menggunakan Constant.SCREEN_WIDTH/HEIGHT
import io.DutchSlayer.attack.screens.GameScreen; // NEW: Import GameScreen

public class SettingScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private final FitViewport viewport;
    private final Skin skin;

    private final Texture background;
    // private final Texture titleTexture; // REMOVED: No longer using an image for the title
    private final Texture volumeTexture;

    public enum SettingsContext {
        MAIN_MENU,
        PAUSE_MENU,
        STAGE_SELECTION
    }

    private final SettingsContext context;
    // Renamed for clarity and generality
    private final Screen gameOrPreviousScreen; // Can be TowerDefenseScreen or GameScreen
    private final int currentStage; // This might be unused for non-game screens but is harmless

    private TextButton.TextButtonStyle customButtonStyle;
    private Texture buttonUpTexture;
    private Texture buttonDownTexture;

    private static final int GENERAL_BUTTON_WIDTH = 380;
    private static final int GENERAL_BUTTON_HEIGHT = 100;
    private static final float GENERAL_BUTTON_FONT_SCALE = 2.5f;
    private static final float TOGGLE_FULLSCREEN_FONT_SCALE = 1.5f;
    private static final int BACK_BUTTON_WIDTH = 180;
    private static final int BACK_BUTTON_HEIGHT = 70;
    private static final float BACK_BUTTON_FONT_SCALE = 1.2f;
    private static final int OUTER_BORDER_THICKNESS = 5;
    private static final int INNER_PADDING = 10;
    private static final int INNER_BORDER_THICKNESS = 3;

    // Default constructor for MainMenu context
    public SettingScreen(Main game) {
        this(game, SettingsContext.MAIN_MENU, null, 1); // Pass null for gameOrPreviousScreen as it's not applicable here
    }

    // Constructor for TowerDefenseScreen coming from PauseMenu
    public SettingScreen(Main game, TowerDefenseScreen gameScreen, int stageNumber) {
        this(game, SettingsContext.PAUSE_MENU, gameScreen, stageNumber); // Pass gameScreen as previous screen
    }

    // NEW: Constructor for GameScreen coming from PauseMenu
    public SettingScreen(Main game, GameScreen gameScreen, int stageNumber) {
        this(game, SettingsContext.PAUSE_MENU, gameScreen, stageNumber); // Pass gameScreen as previous screen
    }

    // Main constructor to handle all contexts
    public SettingScreen(Main game, SettingsContext context, Screen previousScreen, int currentStage) {
        this.game = game;
        this.context = context;
        this.gameOrPreviousScreen = previousScreen; // Now accepts any Screen type
        this.currentStage = currentStage;

        this.viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage = new Stage(viewport);
        this.skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        this.background = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));
        // this.titleTexture = new Texture(Gdx.files.internal("button/SettingScreen.png")); // REMOVED
        this.volumeTexture = new Texture(Gdx.files.internal("button/volume.png"));

        System.out.println("🔧 SettingsScreen created with context: " + context);

        initializeCustomButtonStyle();

        Gdx.input.setInputProcessor(this.stage);
        createUI();
    }

    // Method to initialize our custom button style with nested shapes
    // This method generates the textures for the general-sized buttons.
    // We will apply different sizes in createUI()
    private void initializeCustomButtonStyle() {
        // Define custom colors for the button layers, consistent with MainMenuScreen
        Color brownBorderOuter = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownOuterFillUp = new Color(0.6f, 0.4f, 0.18f, 1.0f);
        Color brownOuterFillDown = new Color(0.48f, 0.32f, 0.12f, 1.0f);
        Color brownBorderInner = new Color(0.3f, 0.15f, 0.05f, 1.0f);
        Color brownInnerFillUp = new Color(0.57f, 0.38f, 0.16f, 1.0f);
        Color brownInnerFillDown = new Color(0.45f, 0.28f, 0.1f, 1.0f);
        Color textColor = new Color(0.25f, 0.15f, 0.05f, 1.0f);

        // --- Create Pixmap for the 'up' state background (normal button) ---
        // Use GENERAL_BUTTON_WIDTH and GENERAL_BUTTON_HEIGHT for the Pixmap generation
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

        // --- Create Pixmap for the 'down' state background (pressed button) ---
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

        // Create TextureRegionDrawables from these Textures
        TextureRegionDrawable upDrawable = new TextureRegionDrawable(buttonUpTexture);
        TextureRegionDrawable downDrawable = new TextureRegionDrawable(buttonDownTexture);

        // Get the default font from the skin
        BitmapFont defaultFont = skin.getFont("default-font");
        if (defaultFont == null) {
            Gdx.app.error("SettingScreen", "Default font not found in uiskin.json. Please ensure 'default-font' is defined.");
            // Fallback strategy: You might want to load a default font directly here if skin fails
            // defaultFont = new BitmapFont(Gdx.files.internal("path/to/your/default/font.fnt"));
        }
        // Set font filter for sharper text.
        // This is crucial for non-blurry text when scaling down or up.
        // Make sure this is applied once after getting the font.
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
        rootTable.top();
        stage.addActor(rootTable);

        // Row 1: Back Button in the top-left corner
        Table topBarTable = new Table();
        TextButton backButton = new TextButton("BACK", customButtonStyle);
        // Apply specific font scale for back button
        backButton.getLabel().setFontScale(BACK_BUTTON_FONT_SCALE);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                // CORRECTED CALL: Explicitly refer to the enclosing class instance
                SettingScreen.this.handleBackButton();
            }
        });
        topBarTable.add(backButton)
            // Apply specific size for back button
            .size(BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT)
            .pad(10)
            .left();

        rootTable.add(topBarTable)
            .expandX()
            .left();
        rootTable.row();

        // Define a new font style for labels that matches the title text color
        // Assuming 'default-font' is used for title, retrieve it and set a new color.
        BitmapFont labelFont = skin.getFont("default-font");
        if (labelFont == null) {
            Gdx.app.error("SettingScreen", "Default font not found for label styling.");
            // Fallback to a default font if 'default-font' is missing
            labelFont = new BitmapFont(); // Consider loading a specific font here
        }
        // Set font filter for sharper text if not already done for this font instance
        labelFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Create a LabelStyle with the desired font and color
        Label.LabelStyle labelStyle = new Label.LabelStyle(labelFont, new Color(0.25f, 0.15f, 0.05f, 1.0f)); // Same color as customButtonStyle.fontColor


        // Row 2: "Setting" Title (centered)
        // Image titleImg = new Image(titleTexture); // REMOVED
        Label titleLabel = new Label("SETTINGS", labelStyle); // Using Label instead of Image
        titleLabel.setFontScale(2.5f); // Adjust font scale to match desired title size
        rootTable.add(titleLabel)
            .padTop(10) // Adjusted padding
            .padBottom(50) // Adjusted padding
            .center();
        rootTable.row();

        // Row 3 onwards: Container for Sliders & Toggle
        Table contentTable = new Table();
        contentTable.defaults().padTop(15).padBottom(15).padLeft(10).padRight(10);

        // Master Volume: Label + Slider
        Label masterLabel = new Label("Master Volume", labelStyle); // Apply the new style
        masterLabel.setFontScale(1.2f); // Atur skala font menjadi lebih besar
        contentTable.add(masterLabel)
            .left()
            .padLeft(0);

        Slider masterSlider = new Slider(0f, 1f, 0.01f, false, skin);
        masterSlider.setValue(AudioManager.getMasterVolume());
        masterSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.setMasterVolume(masterSlider.getValue());
            }
        });
        contentTable.add(masterSlider)
            .colspan(2)
            .width(400)
            .left();
        contentTable.row();

        // Music Volume: Label + Slider
        Label musicLabel = new Label("Music Volume", labelStyle); // Apply the new style
        musicLabel.setFontScale(1.2f); // Atur skala font menjadi lebih besar
        contentTable.add(musicLabel)
            .left()
            .padLeft(0);

        Slider musicSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicSlider.setValue(AudioManager.getMusicVolume());
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.setMusicVolume(musicSlider.getValue());
            }
        });
        contentTable.add(musicSlider)
            .colspan(2)
            .width(400)
            .left();
        contentTable.row();

        // SFX Volume: Label + Slider
        Label sfxLabel = new Label("SFX Volume", labelStyle); // Apply the new style
        sfxLabel.setFontScale(1.2f); // Atur skala font menjadi lebih besar
        contentTable.add(sfxLabel)
            .left()
            .padLeft(0);

        Slider sfxSlider = new Slider(0f, 1f, 0.01f, false, skin);
        sfxSlider.setValue(AudioManager.getSfxVolume());
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.setSfxVolume(sfxSlider.getValue());
            }
        });
        contentTable.add(sfxSlider)
            .colspan(2)
            .width(400)
            .left();
        contentTable.row();

        // Toggle Fullscreen Button (centered in row)
        TextButton toggleBtn = new TextButton("TOGGLE FULLSCREEN", customButtonStyle);
        // Terapkan skala font baru untuk tombol fullscreen
        toggleBtn.getLabel().setFontScale(TOGGLE_FULLSCREEN_FONT_SCALE);
        toggleBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (Gdx.graphics.isFullscreen()) {
                    Gdx.graphics.setWindowedMode(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
                } else {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                }
                Gdx.app.postRunnable(() -> {
                    viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
                    Gdx.input.setInputProcessor(stage);
                });
            }
        });
        contentTable.add(toggleBtn)
            // Apply general button size
            .size(GENERAL_BUTTON_WIDTH, GENERAL_BUTTON_HEIGHT)
            .colspan(3)
            .center();
        contentTable.row();

        // Add contentTable to rootTable
        rootTable.add(contentTable)
            .padTop(30)
            .center();
        rootTable.row();
    }

    // ===== HANDLE BACK BUTTON BASED ON CONTEXT =====
    private void handleBackButton() {
        switch (context) {
            case MAIN_MENU:
                game.setScreen(new MainMenuScreen(game));
                break;
            case PAUSE_MENU:
                if (gameOrPreviousScreen instanceof TowerDefenseScreen) { // Check if it's TowerDefenseScreen
                    TowerDefenseScreen tdScreen = (TowerDefenseScreen) gameOrPreviousScreen;
                    tdScreen.gameState.isPaused = true; // Ensure the game state remains paused
                    game.setScreen(tdScreen); // Go back to the specific TD screen instance
                    Gdx.app.postRunnable(() -> {
                        // Update pause menu viewport
                        Stage pauseStage = tdScreen.pauseMenu.getStage();
                        pauseStage.getViewport().update(
                            Gdx.graphics.getWidth(),
                            Gdx.graphics.getHeight(),
                            true
                        );

                        // Set input processor
                        Gdx.input.setInputProcessor(pauseStage);
                        tdScreen.pauseMenu.setPaused(true);

                    }); // Set input processor for its pause menu
                } else if (gameOrPreviousScreen instanceof GameScreen) { // NEW: Handle GameScreen
                    GameScreen gsScreen = (GameScreen) gameOrPreviousScreen;
                    // GameScreen's pause logic is handled internally in render(), so just setting the screen is usually enough
                    // You might need to set a flag on GameScreen to indicate it was paused before going to settings if its internal state relies on it.
                    // For now, assuming setting the screen back correctly resumes its paused state.
                    gsScreen.setPaused(true); // Explicitly set GameScreen to paused state
                    game.setScreen(gsScreen); // Go back to the specific GameScreen instance
                    Gdx.input.setInputProcessor(gsScreen.getPauseMenu().getStage()); // Set input processor for its pause menu
                }
                // If it's a pause menu context but previousScreen is null or unknown,
                // you might want a fallback, e.g., to MainMenuScreen or create a new game.
                // The existing logic already accounts for previousGameScreen == null for TDScreen,
                // but this might need refinement if more screen types are added.
                break;
            case STAGE_SELECTION:
                game.setScreen(new StageSelectionScreen(game, true));
                break;
            default: // Fallback for any unhandled context
                game.setScreen(new MainMenuScreen(game));
                break;
        }
    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
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
    public void resize(int w, int h) {
        viewport.update(w, h, true);
        Gdx.app.postRunnable(() -> {
            Gdx.input.setInputProcessor(stage);
        });
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (background != null) background.dispose();
        // if (titleTexture != null) titleTexture.dispose(); // REMOVED
        if (buttonUpTexture != null) buttonUpTexture.dispose();
        if (buttonDownTexture != null) buttonDownTexture.dispose();
        if (volumeTexture != null) volumeTexture.dispose();
    }
}
