package io.DutchSlayer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.DutchSlayer.screens.MainMenuScreen;

public class Main extends Game {

    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        this.setScreen(new MainMenuScreen(this)); // Set awal ke menu utama
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
