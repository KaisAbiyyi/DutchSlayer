package io.DutchSlayer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.DutchSlayer.defend.untils.AudioManager;
import io.DutchSlayer.defend.untils.GameMode;
import io.DutchSlayer.defend.screens.MainMenuScreen;

public class Main extends Game {
    public SpriteBatch batch;

    // Agar ModeSelectionScreen bisa men‐set Main.currentMode tanpa error:
    public static GameMode currentMode = GameMode.NONE;

    @Override
    public void create() {
        batch = new SpriteBatch();
        AudioManager.initialize();
        AudioManager.playMainMenuMusic();

        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        AudioManager.shutdown();
    }
}
