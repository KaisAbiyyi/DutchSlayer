package io.DutchSlayer.defend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.defend.utils.AudioManager;
import io.DutchSlayer.defend.utils.TDConstants;

public class SettingScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private final FitViewport viewport;
    private final Skin skin;

    private final Texture background;
    private final Texture titleTexture;
    private final Texture toggleTexture;
    private final Texture backButtonTexture;
    private final Texture volumeTexture; // Tambahan

    // ===== CONTEXT TRACKING =====
    public enum SettingsContext {
        MAIN_MENU,       // Dipanggil dari main menu
        PAUSE_MENU,      // Dipanggil dari pause menu
        STAGE_SELECTION  // Dipanggil dari stage selection (jika ada)
    }

    private final SettingsContext context;
    private final TowerDefenseScreen previousGameScreen; // For pause menu context
    private final int currentStage;

    // ===== CONSTRUCTOR UNTUK MAIN MENU (existing) =====
    public SettingScreen(Main game) {
        this(game, SettingsContext.MAIN_MENU, null, 1);
    }

    // ===== CONSTRUCTOR UNTUK PAUSE MENU =====
    public SettingScreen(Main game, TowerDefenseScreen gameScreen, int stage) {
        this(game, SettingsContext.PAUSE_MENU, gameScreen, stage);
    }

    // ===== MAIN CONSTRUCTOR =====
    private SettingScreen(Main game, SettingsContext context, TowerDefenseScreen gameScreen, int stage) {
        this.game = game;
        this.context = context;
        this.previousGameScreen = gameScreen;
        this.currentStage = stage;

        this.viewport = new FitViewport(TDConstants.SCREEN_WIDTH, TDConstants.SCREEN_HEIGHT);
        this.stage = new Stage(viewport);
        this.skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        this.background = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));
        this.titleTexture = new Texture(Gdx.files.internal("button/SettingScreen.png"));
        this.toggleTexture = new Texture(Gdx.files.internal("button/ToogleButton.png"));
        this.backButtonTexture = new Texture(Gdx.files.internal("button/backbutton.png"));
        this.volumeTexture = new Texture(Gdx.files.internal("button/volume.png"));

        System.out.println("🔧 SettingsScreen created with context: " + context);
        createUI();
    }

    private void createUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top();
        stage.addActor(rootTable);

        // Baris untuk tombol back
        Table topBar = new Table();
        ImageButton backButton = new ImageButton(new TextureRegionDrawable(backButtonTexture));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                handleBackButton();
            }
        });
        topBar.add(backButton).size(100, 100).pad(10).left();
        rootTable.add(topBar).expandX().left().row();

        // Title
        Image titleImg = new Image(titleTexture);
        rootTable.add(titleImg).size(800, 450).padTop(-180).center().row();

        // Content Table (Volume dulu, lalu Toggle)
        Table contentTable = new Table();
        contentTable.defaults().padBottom(10);

        // 1) Volume image (pengganti label)
        Image masterVolumeImage  = new Image(volumeTexture);
        contentTable.add(masterVolumeImage )
            .size(150, 100)
            .padTop(-600)
            .row();

        // 2) Slider di bawah gambar Volume
        Slider masterVolumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        masterVolumeSlider.setValue(AudioManager.getMasterVolume());
        masterVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.setMasterVolume(masterVolumeSlider.getValue());
                // Also update legacy bgMusic if it exists
                if (game.bgMusic != null) {
                    game.bgMusic.setVolume(masterVolumeSlider.getValue());
                }
            }
        });
        contentTable.add(masterVolumeSlider)
            .width(300)
            .padTop(-580)
            .row();

        // ===== MUSIC VOLUME =====
        Label musicLabel = new Label("Music Volume", skin);
        contentTable.add(musicLabel)
            .padTop(-520)
            .row();

        Slider musicVolumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        musicVolumeSlider.setValue(AudioManager.getMusicVolume());
        musicVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.setMusicVolume(musicVolumeSlider.getValue());
            }
        });
        contentTable.add(musicVolumeSlider)
            .width(300)
            .padTop(-500)
            .row();

        // ===== SFX VOLUME =====
        Label sfxLabel = new Label("SFX Volume", skin);
        contentTable.add(sfxLabel)
            .padTop(-460)
            .row();

        Slider sfxVolumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        sfxVolumeSlider.setValue(AudioManager.getSfxVolume());
        sfxVolumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.setSfxVolume(sfxVolumeSlider.getValue());
            }
        });
        contentTable.add(sfxVolumeSlider)
            .width(300)
            .padTop(-440)
            .row();

        // 3) Baru Toggle button di bawah Slider
        ImageButton toggleBtn = new ImageButton(new TextureRegionDrawable(toggleTexture));
        toggleBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                if (Gdx.graphics.isFullscreen()) {
                    Gdx.graphics.setWindowedMode(TDConstants.SCREEN_WIDTH, TDConstants.SCREEN_HEIGHT);
                } else {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                }
            }
        });
        contentTable.add(toggleBtn)
            .size(300, 100)
            .padTop(-350) // sesuaikan agar jaraknya pas
            .padBottom(20)
            .row();

        // Tambahkan contentTable ke rootTable
        rootTable.add(contentTable).expand().center().padTop(80);
    }

    // ===== HANDLE BACK BUTTON BERDASARKAN CONTEXT =====
    private void handleBackButton() {
        switch (context) {
            case MAIN_MENU:
                System.out.println("⬅️ Back to Main Menu");
                game.setScreen(new MainMenuScreen(game));
                break;

            case PAUSE_MENU:
                System.out.println("⬅️ Back to Pause Menu (Game continues)");
                if (previousGameScreen != null) {
                    // Kembali ke game screen dengan pause state
                    game.setScreen(previousGameScreen);
                    // Game akan tetap dalam status pause
                } else {
                    // Fallback jika gameScreen null
                    System.out.println("⚠️ Warning: No previous game screen, creating new one");
                    game.setScreen(new TowerDefenseScreen(game, currentStage));
                }
                break;

            case STAGE_SELECTION:
                System.out.println("⬅️ Back to Stage Selection");
                game.setScreen(new StageSelectionScreen(game, true));
                break;

            default:
                System.out.println("⬅️ Unknown context, going to Main Menu");
                game.setScreen(new MainMenuScreen(game));
                break;
        }
    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        if (!AudioManager.isMusicPlaying()) {
            System.out.println("🎵 SettingsScreen: Resuming main menu music...");
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

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        background.dispose();
        titleTexture.dispose();
        toggleTexture.dispose();
        backButtonTexture.dispose();
        volumeTexture.dispose(); // Tambahan
    }
}
