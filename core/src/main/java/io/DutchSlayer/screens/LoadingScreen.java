package io.DutchSlayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont; // Import BitmapFont
import com.badlogic.gdx.graphics.Color; // Import Color
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;

import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.defend.screens.TowerDefenseScreen;

import io.DutchSlayer.utils.Constant;

public class LoadingScreen implements Screen {

    private final Main game;
    private final int stageNumber;
    private final boolean isDefendMode;

    private Stage stage;
    private Skin skin;
    private Label loadingLabel;
    private Texture backgroundTexture;

    private boolean gameScreenInitialized = false;

    public LoadingScreen(Main game, int stageNumber, boolean isDefendMode) {
        this.game = game;
        this.stageNumber = stageNumber;
        this.isDefendMode = isDefendMode;
    }

    @Override
    public void show() {
        FitViewport viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        backgroundTexture = new Texture(Gdx.files.internal("backgrounds/Main Menu.png"));

        // Mengambil font default dari skin untuk diubah warnanya
        BitmapFont defaultFont = skin.getFont("default-font");
        if (defaultFont == null) {
            Gdx.app.error("LoadingScreen", "Default font not found in uiskin.json. Using fallback.");
            defaultFont = new BitmapFont(); // Fallback if not found
        }
        // Pastikan font difilter untuk kualitas yang lebih baik saat diskalakan
        defaultFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);


        // Definisikan warna coklat gelap yang konsisten dengan tema tombol Anda
        Color darkBrown = new Color(0.25f, 0.15f, 0.05f, 1.0f); // Warna yang sama dengan textColor di PauseMenu/SettingScreen

        // Buat LabelStyle baru dengan font dan warna yang diinginkan
        Label.LabelStyle loadingLabelStyle = new Label.LabelStyle(defaultFont, darkBrown);

        // Ubah teks menjadi uppercase dan terapkan gaya baru
        loadingLabel = new Label("LOADING...", loadingLabelStyle); // Teks UPPERCASE
        loadingLabel.setFontScale(3.0f); // Perbesar ukuran font (sesuaikan sesuai kebutuhan Anda)

        // Kembalikan posisi ke tengah layar
        loadingLabel.setPosition(
            (Constant.SCREEN_WIDTH - loadingLabel.getWidth()) / 2 -75f, // Tengah horizontal
            (Constant.SCREEN_HEIGHT - loadingLabel.getHeight()) / 2 // Tengah vertikal
        );
        stage.addActor(loadingLabel);

        gameScreenInitialized = false;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(backgroundTexture, 0, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        game.batch.end();

        stage.act(delta);
        stage.draw();

        if (!gameScreenInitialized) {
            gameScreenInitialized = true;
            Gdx.app.postRunnable(() -> {
                if (isDefendMode) {
                    game.setScreen(new TowerDefenseScreen(game, stageNumber));
                } else {
                    game.setScreen(new GameScreen(game, stageNumber));
                }
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
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
    }
}
