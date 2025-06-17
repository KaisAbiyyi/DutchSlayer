package io.DutchSlayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.attack.screens.StageSelectorScreen;
import io.DutchSlayer.utils.Constant;

public class MainMenuScreen implements Screen {

    private final Main game;
    private final Stage stage;

    public MainMenuScreen(Main game) {
        this.game = game;

        // Gunakan FitViewport agar tetap scaling
        FitViewport viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage = new Stage(viewport);

        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("Dutch Slayer", skin, "default");
        TextButton startButton = new TextButton("Start Game", skin);
        TextButton aboutButton = new TextButton("About Us", skin);
        TextButton fullscreenButton = new TextButton("Toggle Fullscreen", skin);

        // === Listener Tombol ===
        startButton.addListener(event -> {
            if (startButton.isPressed()) {
                game.setScreen(new StageSelectorScreen(game));
            }
            return false;
        });

        aboutButton.addListener(event -> {
            if (aboutButton.isPressed()) {
                game.setScreen(new AboutScreen(game));
            }
            return false;
        });

        fullscreenButton.addListener(event -> {
            if (fullscreenButton.isPressed()) {
                toggleFullscreen();
            }
            return false;
        });

        // Tambahkan ke layout
        table.add(title).padBottom(40).row();
        table.add(startButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padBottom(20).row();
        table.add(aboutButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padBottom(20).row();
        table.add(fullscreenButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT);
    }

    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        } else {
            Graphics.DisplayMode currentMode = Gdx.graphics.getDisplayMode();
            Gdx.graphics.setFullscreenMode(currentMode);
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
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
    }
}
