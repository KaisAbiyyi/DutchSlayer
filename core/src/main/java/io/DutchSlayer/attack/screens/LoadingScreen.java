package io.DutchSlayer.attack.screens; // Sesuaikan dengan package Anda

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.attack.screens.GameScreen; // Import GameScreen Anda
import io.DutchSlayer.utils.Constant;

public class LoadingScreen implements Screen {

    private final Main game;
    private final int stageNumber;
    private Stage stage;
    private Skin skin;
    private Label loadingLabel;

    // Flag untuk memastikan GameScreen hanya diinisialisasi sekali
    private boolean gameScreenInitialized = false;

    public LoadingScreen(Main game, int stageNumber) {
        this.game = game;
        this.stageNumber = stageNumber;
    }

    @Override
    public void show() {
        FitViewport viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        // Pastikan Anda menggunakan SpriteBatch dari Main untuk Stage jika ada
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);

        // Pastikan Skin dimuat sebelum digunakan
        // Jika skin ini sudah dimuat di Main, Anda bisa meneruskannya dari Main
        // Namun, jika tidak, ini akan memuatnya di sini
        skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        loadingLabel = new Label("Loading...", skin);
        loadingLabel.setFontScale(2.0f); // Sesuaikan ukuran font
        loadingLabel.setPosition(Constant.SCREEN_WIDTH / 2 - loadingLabel.getWidth() / 2,
            Constant.SCREEN_HEIGHT / 2 - loadingLabel.getHeight() / 2);
        stage.addActor(loadingLabel);

        // Reset flag
        gameScreenInitialized = false;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        // Ini adalah bagian kunci:
        // Gunakan postRunnable untuk menunda pembuatan GameScreen
        // Runnable ini akan dieksekusi di thread rendering utama setelah frame saat ini selesai digambar.
        if (!gameScreenInitialized) {
            gameScreenInitialized = true; // Set flag agar hanya dieksekusi sekali
            Gdx.app.postRunnable(() -> {
                // Instansiasi GameScreen (proses berat) terjadi di sini
                game.setScreen(new GameScreen(game, stageNumber));
                dispose(); // Penting untuk membuang LoadingScreen setelah selesai
            });
        }
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
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            // Hanya dispose skin jika tidak berasal dari Main class
            // Jika Anda meneruskan skin dari Main, jangan dispose di sini
            skin.dispose();
        }
    }
}
