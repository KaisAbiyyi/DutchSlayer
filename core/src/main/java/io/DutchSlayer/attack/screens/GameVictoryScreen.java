package io.DutchSlayer.attack.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.screens.MainMenuScreen;

public class GameVictoryScreen implements Screen {

    private final Main game;
    private Stage stage;
    private Skin skin;
    private Music backgroundMusic;

    public GameVictoryScreen(Main game, int currentStage) {
        this.game = game;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        // Load music
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("backgrounds/WinMusic.mp3"));
        backgroundMusic.setLooping(true);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label congrats = new Label("CONGRATULATIONS!", skin);
        congrats.setFontScale(2f);

        TextButton mainMenuButton = new TextButton("Main Menu", skin);
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        TextButton nextStageButton = new TextButton("Next Stage", skin);
        nextStageButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new GameScreen(game, currentStage + 1));
            }
        });

        table.add(congrats).padBottom(40).row();
        table.add(nextStageButton).pad(10).row();
        table.add(mainMenuButton).pad(10);
    }

    @Override
    public void show() {
        // Play music when the screen is shown
        backgroundMusic.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.1f, 1);
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
        // Stop music when the screen is hidden
        backgroundMusic.stop();
    }

    @Override
    public void dispose() {
        // Dispose of resources
        stage.dispose();
        backgroundMusic.dispose();
    }
}
