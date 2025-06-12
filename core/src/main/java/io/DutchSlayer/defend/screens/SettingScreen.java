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
import io.DutchSlayer.utils.Constant;

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
        // Root table untuk menampung semua elemen, fillParent agar mengisi penuh viewport
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top();  // Sejajarkan konten rootTable ke atas
        stage.addActor(rootTable);

        // ================================================================
        // Baris 1: Tombol Back di pojok kiri atas
        // ================================================================
        Table topBarTable = new Table(); // Buat tabel baru untuk tombol back
        ImageButton backButton = new ImageButton(new TextureRegionDrawable(backButtonTexture));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
                handleBackButton();
            }
        });
        topBarTable.add(backButton) // Tambahkan tombol ke topBarTable
            .size(100, 100)
            .pad(10)    // Padding di sekitar tombol
            .left();    // Sejajarkan tombol ke kiri dalam sel topBarTable

        rootTable.add(topBarTable) // Tambahkan topBarTable ke rootTable
            .expandX() // Izinkan topBarTable untuk memperluas secara horizontal
            .left();   // Sejajarkan topBarTable ke kiri pada baris rootTable
        rootTable.row(); // Pindah ke baris berikutnya di rootTable

        // ================================================================
        // Baris 2: Judul "Setting" (ditengahkan)
        // ================================================================
        Image titleImg = new Image(titleTexture);
        rootTable.add(titleImg)
            .width(800)
            .height(450)
            .padTop(-200)
            .padBottom(-150)// MODIFIKASI: Beri padding positif agar ada jarak dari tombol back.
            .center();
        rootTable.row();

        // ================================================================
        // Baris 3 dst: Kontainer untuk Slider & Toggle
        // ================================================================
        Table contentTable = new Table();
        // Atur default padding untuk sel di dalam contentTable agar ada jarak antar elemen
        contentTable.defaults().padTop(15).padBottom(15).padLeft(10).padRight(10);

        // ---- 1) Master Volume: Label + Slider ----
        Label masterLabel = new Label("Master Volume", skin);
        masterLabel.setFontScale(0.8f);
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

        // ---- 2) Music Volume: Label + Slider ----
        Label musicLabel = new Label("Music Volume", skin);
        musicLabel.setFontScale(0.8f);
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

        // ---- 3) SFX Volume: Label + Slider ----
        Label sfxLabel = new Label("SFX Volume", skin);
        sfxLabel.setFontScale(0.8f);
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

        // ---- Spacer vertikal sebelum tombol Toggle ----
        // contentTable.add().height(20).colspan(3); // Spacer ini mungkin tidak lagi diperlukan dengan default padding
        // contentTable.row();

        // ---- 4) Tombol Toggle Fullscreen (ditengahkan di baris) ----
        ImageButton toggleBtn = new ImageButton(new TextureRegionDrawable(toggleTexture));
        toggleBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (Gdx.graphics.isFullscreen()) {
                    Gdx.graphics.setWindowedMode(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
                } else {
                    Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                }
            }
        });
        contentTable.add(toggleBtn)
            .size(500, 120)
            .colspan(3)
            .center();
        contentTable.row();

        // Tambahkan contentTable ke rootTable
        rootTable.add(contentTable)
            .padTop(30) // MODIFIKASI: Beri padding positif agar ada jarak dari judul.
            .center();
        rootTable.row();

    }

    // ===== HANDLE BACK BUTTON BERDASARKAN CONTEXT =====
    private void handleBackButton() {
        switch (context) {
            case MAIN_MENU:
                System.out.println("⬅️ Back to Main Menu");
                AudioManager.playMainMenuMusic();
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
                    TowerDefenseScreen newScreen = new TowerDefenseScreen(game, currentStage);
                    newScreen.gameState.isPaused = true;
                    game.setScreen(newScreen);
                }
                break;

            case STAGE_SELECTION:
                System.out.println("⬅️ Back to Stage Selection");
                AudioManager.playMainMenuMusic();
                game.setScreen(new StageSelectionScreen(game, true));
                break;

            default:
                System.out.println("⬅️ Unknown context, going to Main Menu");
                AudioManager.playMainMenuMusic();
                game.setScreen(new MainMenuScreen(game));
                break;
        }
    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        switch (context) {
            case MAIN_MENU:
            case STAGE_SELECTION:
                // Hanya play main menu music jika dari main menu
                if (!AudioManager.isMusicPlaying()) {
                    System.out.println("🎵 SettingsScreen: Resuming main menu music...");
                    AudioManager.playMainMenuMusic();
                }
                break;

            case PAUSE_MENU:
                // ⭐ SAMA SEKALI JANGAN UBAH MUSIK
                System.out.println("🎵 SettingsScreen: From pause menu - DO NOT TOUCH MUSIC");
                // JANGAN panggil AudioManager.playMainMenuMusic() atau method musik lainnya
                break;
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
    @Override public void hide() {
        switch (context) {
            case PAUSE_MENU:
                System.out.println("🎵 SettingsScreen: Hiding - preserving tower defense music for pause menu");
                // Jangan stop atau ubah musik
                break;

            case MAIN_MENU:
            case STAGE_SELECTION:
            default:
                // Untuk context lain, biarkan normal behavior
                System.out.println("🎵 SettingsScreen: Hiding - normal behavior");
                break;
        }
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (background != null) background.dispose();
        if (titleTexture != null) titleTexture.dispose();
        if (toggleTexture != null) toggleTexture.dispose();
        if (backButtonTexture != null) backButtonTexture.dispose();
        volumeTexture.dispose(); // Tambahan
    }
}
