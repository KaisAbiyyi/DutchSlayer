package io.DutchSlayer.defend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.DutchSlayer.Main;
import io.DutchSlayer.utils.Constant;

public class MainMenuScreen implements Screen {

    private final Main game;
    private final Stage stage;
    private final OrthographicCamera camera;

    public MainMenuScreen(Main game) {
        this.game = game;

        // Setup camera
        this.camera = new OrthographicCamera();
        camera.setToOrtho(false, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);

        // Setup viewport and stage
        FitViewport viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, camera);
        this.stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        // Load UI skin
        Skin skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        // Main table for layout
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Title
        Label title = new Label("Dutch Slayer", skin);
        title.setFontScale(2f);

        // Tombol mode game
        TextButton platformerButton = new TextButton("Platformer Mode", skin);
        TextButton towerDefenseButton = new TextButton("Tower Defense Mode", skin);
        TextButton aboutButton = new TextButton("About", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        // Listener tombol
        platformerButton.addListener(event -> {
            if (platformerButton.isPressed()) {
                // Menggunakan GameScreen dari package defend.screens
                game.setScreen(new GameScreen(game));
                return true;
            }
            return false;
        });

        towerDefenseButton.addListener(event -> {
            if (towerDefenseButton.isPressed()) {
                // Menggunakan TowerDefenseScreen dari folder defend
                game.setScreen(new TowerDefenseScreen(game));
                return true;
            }
            return false;
        });

        aboutButton.addListener(event -> {
            if (aboutButton.isPressed()) {
                // Menggunakan AboutScreen dari package defend.screens
                game.setScreen(new AboutScreen(game));
                return true;
            }
            return false;
        });

        exitButton.addListener(event -> {
            if (exitButton.isPressed()) {
                Gdx.app.exit();
                return true;
            }
            return false;
        });

        // Layout
        mainTable.add(title).padBottom(50).row();
        mainTable.add(platformerButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padBottom(15).row();
        mainTable.add(towerDefenseButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padBottom(15).row();
        mainTable.add(aboutButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT).padBottom(15).row();
        mainTable.add(exitButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT);
    }

    @Override
    public void show() {
        // Reset input processor when screen becomes active
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // Clear screen with a dark background
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera
        camera.update();

        // Update and draw stage
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
        // Remove input processor when screen is hidden
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
