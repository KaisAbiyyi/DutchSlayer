package io.DutchSlayer.defend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.utils.Constant;

public class AboutScreen implements Screen {

    private final Main game;
    private final Stage stage;

    public AboutScreen(Main game) {
        this.game = game;

        // === Gunakan FitViewport untuk autoscale UI ===
        FitViewport viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
        this.stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        // === UI Skin dan Table ===
        Skin skin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // === Konten ===
        Label title = new Label("About Us", skin, "default");
        Label member1 = new Label("Kais Abiyyi - 2350081061", skin);
        Label member2 = new Label("M. Alvin Pratama - 2350081076", skin);
        Label member3 = new Label("Haerul Rahman Nuryadin - 2350081089", skin);

        TextButton backButton = new TextButton("Back to Menu", skin);
        backButton.addListener(event -> {
            if (backButton.isPressed()) {
                game.setScreen(new MainMenuScreen(game));
            }
            return false;
        });

        // === Tata Letak ===
        table.add(title).padBottom(30).row();
        table.add(member1).padBottom(10).row();
        table.add(member2).padBottom(10).row();
        table.add(member3).padBottom(30).row();
        table.add(backButton).size(Constant.BUTTON_WIDTH, Constant.BUTTON_HEIGHT);
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
    }
}
