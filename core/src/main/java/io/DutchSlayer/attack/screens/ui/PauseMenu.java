package io.DutchSlayer.attack.screens.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.screens.MainMenuScreen;

public class PauseMenu {
    private final Stage stage;
    private final Main game;
    private boolean paused = false;

    public PauseMenu(Main game, Skin uiSkin, Viewport viewport) {
        this.game = game;
        this.stage = new Stage(viewport);

        // Overlay background
        Image bgOverlay = new Image(new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("white.png")));
        bgOverlay.setColor(0, 0, 0, 0.5f);
        bgOverlay.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        stage.addActor(bgOverlay);

        // Menu buttons
        Table table = new Table(uiSkin);
        table.setFillParent(true);
        table.center();

        TextButton resumeButton = new TextButton("Resume", uiSkin);
        TextButton settingsButton = new TextButton("Settings", uiSkin); // Placeholder
        TextButton mainMenuButton = new TextButton("Main Menu", uiSkin);

        resumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setPaused(false);
                Gdx.input.setInputProcessor(null);
            }
        });

        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        table.add(resumeButton).pad(10).row();
        table.add(settingsButton).pad(10).row();
        table.add(mainMenuButton).pad(10).row();

        stage.addActor(table);
    }

    public boolean renderIfActive(float delta) {
        if (!paused) return false;
        stage.act(delta);
        stage.draw();
        return true;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        Gdx.input.setInputProcessor(paused ? stage : null);
    }
}
