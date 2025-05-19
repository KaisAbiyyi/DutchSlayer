package io.DutchSlayer.defend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.DutchSlayer.Main;
import io.DutchSlayer.defend.untils.GameMode;

public class ModeSelectionScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final Skin skin;

    public ModeSelectionScreen(Main game) {
        this.game = game;
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(800, 480, camera); // Sesuaikan jika perlu
        this.stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        this.skin = new Skin(Gdx.files.internal("uiskin/uiskin.json")); // Pastikan ada di assets

        createUI();
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextButton towerDefenseButton = new TextButton("Tower Defense", skin);
        TextButton platformerButton = new TextButton("Platformer", skin);
        TextButton backButton = new TextButton("Back", skin);

        towerDefenseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Main.currentMode = GameMode.TOWER_DEFENSE;
                game.setScreen(new StageSelectionScreen(game,true));
            }
        });

        platformerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Main.currentMode = GameMode.PLATFORMER;
                game.setScreen(new StageSelectionScreen(game,false));
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        table.add(towerDefenseButton).pad(10).row();
        table.add(platformerButton).pad(10).row();
        table.add(backButton).pad(10).row();
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
