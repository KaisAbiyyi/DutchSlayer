package io.DutchSlayer.screens; // Pastikan package ini sesuai dengan struktur proyek Anda

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;

// --- PERUBAHAN ---
// Impor kedua kelas layar permainan yang relevan.
// Pastikan path impor ini benar sesuai lokasi file Anda.
import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.defend.screens.TowerDefenseScreen;

// Asumsi Anda memiliki kelas Constant untuk ukuran layar
import io.DutchSlayer.utils.Constant;

public class LoadingScreen implements Screen {

    private final Main game;
    private final int stageNumber;
    // --- PERUBAHAN ---
    // Tambahkan variabel untuk menyimpan mode permainan
    private final boolean isDefendMode;

    private Stage stage;
    private Skin skin;
    private Label loadingLabel;

    // Flag untuk memastikan layar permainan hanya diinisialisasi sekali
    private boolean gameScreenInitialized = false;

    // --- PERUBAHAN ---
    // Konstruktor diperbarui untuk menerima flag isDefendMode
    public LoadingScreen(Main game, int stageNumber, boolean isDefendMode) {
        this.game = game;
        this.stageNumber = stageNumber;
        this.isDefendMode = isDefendMode; // Simpan flag-nya
    }

    @Override
    public void show() {
        FitViewport viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        stage = new Stage(viewport, game.batch); // Gunakan batch dari Main jika ada
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        loadingLabel = new Label("Loading...", skin);
        loadingLabel.setFontScale(2.0f);
        loadingLabel.setPosition(
            Constant.SCREEN_WIDTH / 2 - loadingLabel.getWidth(), // Disesuaikan agar lebih ke tengah
            Constant.SCREEN_HEIGHT / 2 - loadingLabel.getHeight() / 2
        );
        stage.addActor(loadingLabel);

        gameScreenInitialized = false;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        if (!gameScreenInitialized) {
            gameScreenInitialized = true;
            Gdx.app.postRunnable(() -> {
                // --- PERUBAHAN UTAMA DI SINI ---
                // Gunakan flag isDefendMode untuk menentukan layar mana yang akan dibuat
                if (isDefendMode) {
                    // Jika mode bertahan, muat TowerDefenseScreen
                    game.setScreen(new TowerDefenseScreen(game, stageNumber));
                } else {
                    // Jika bukan, muat GameScreen (untuk mode attack/lainnya)
                    game.setScreen(new GameScreen(game, stageNumber));
                }
                // Panggil dispose setelah layar baru di-set untuk membersihkan resource LoadingScreen
                dispose();
            });
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
    }
}
