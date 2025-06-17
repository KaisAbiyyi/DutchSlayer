package io.DutchSlayer.attack.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.screens.MainMenuScreen;
import io.DutchSlayer.utils.Constant;

public class StageSelectorScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private final Skin skin;

    private static final int TOTAL_STAGES = 3;
    // Ubah ini sesuai dengan logika game Anda untuk membuka stage
    // Misalnya, bisa dimuat dari SharedPreferences atau disimpan di kelas Main
    private int unlockedStages = TOTAL_STAGES; // Default hanya stage 1 yang terbuka

    public StageSelectorScreen(Main game) {
        this.game = game;

        FitViewport viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        this.skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        createLayout();
    }

    private void createLayout() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("Select Stage", skin);
        table.add(title).colspan(5).padBottom(30).row();

        for (int i = 1; i <= TOTAL_STAGES; i++) {
            final int stageNumber = i;

            // Tampilkan tanda ✔ jika stage sebelumnya sudah diselesaikan
            String buttonText = "Stage " + i;
            // Ini adalah logika placeholder, Anda harus mendapatkan `unlockedStages` yang sebenarnya
            // dari sistem penyimpanan game Anda (misalnya SharedPreferences atau database).
            // Contoh sederhana:
            if (i < unlockedStages) { // Asumsi unlockedStages adalah stage terakhir yang diselesaikan + 1
                buttonText += " ✔"; // Sudah selesai
            }

            TextButton stageButton = new TextButton(buttonText, skin);

            // Hanya buka stage yang sudah unlocked
            // Ini juga logika placeholder. Pastikan `unlockedStages` akurat.
            if (i > unlockedStages) {
                stageButton.setDisabled(true);
            }

            stageButton.addListener(event -> {
                if (stageButton.isPressed() && !stageButton.isDisabled()) {
                    // Ketika tombol diklik, set screen ke LoadingScreen
                    game.setScreen(new LoadingScreen(game, stageNumber));
                }
                return false;
            });

            table.add(stageButton)
                .size(Constant.BUTTON_WIDTH / 1.5f, Constant.BUTTON_HEIGHT)
                .pad(10);

            if (i % 5 == 0) table.row(); // Baris baru setiap 5 tombol
        }

        // Tombol kembali
        table.row().padTop(20);
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(event -> {
            if (backButton.isPressed()) {
                game.setScreen(new MainMenuScreen(game));
            }
            return false;
        });
        table.add(backButton).colspan(5).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padTop(20);
    }


    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
