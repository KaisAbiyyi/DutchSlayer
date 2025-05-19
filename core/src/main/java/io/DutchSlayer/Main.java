package io.DutchSlayer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.DutchSlayer.defend.GameScreen;
import io.DutchSlayer.screens.MainMenuScreen;

public class Main extends Game {
    public SpriteBatch batch;
//    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        this.setScreen(new GameScreen(this)); // Set awal ke menu utama
//        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render(); // Render screen aktif
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
    }
}
