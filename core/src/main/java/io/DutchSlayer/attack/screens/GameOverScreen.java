package io.DutchSlayer.attack.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.utils.Constant;

public class GameOverScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private final Viewport viewport;
    private final int stageNumber;
    private final Music backgroundMusic;

    public GameOverScreen(Main game, int stageNumber) {
        this.game = game;
        this.viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage = new Stage(viewport);
        this.stageNumber = stageNumber;
        Skin skin = new Skin(Gdx.files.internal("uiskin/uiskin.json")); // pastikan ada uiskin.json

        // Load music
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("backgrounds/LoseMusic.mp3"));
        backgroundMusic.setLooping(true);

        Table table = new Table();
        table.setFillParent(true);

        Label title = new Label("GAME OVER", skin);

        TextButton retryButton = new TextButton("Retry Mission", skin);

        retryButton.addListener(event -> {
            if (retryButton.isPressed()) {
                game.setScreen(new GameScreen(game, stageNumber));
                return true;
            }
            return false;
        });

        table.add(title).padBottom(20f).row();
        table.add(retryButton).width(200).height(50);

        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1); // Background hitam
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void show() {
        // Play music when the screen is shown
        backgroundMusic.play();
    }

    @Override
    public void hide() {
        // Stop music when the screen is hidden
        backgroundMusic.stop();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        // Dispose of resources
        stage.dispose();
        backgroundMusic.dispose();
    }
}
