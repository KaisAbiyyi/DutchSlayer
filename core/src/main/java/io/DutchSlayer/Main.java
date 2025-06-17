package io.DutchSlayer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.DutchSlayer.screens.MainMenuScreen;

public class Main extends Game {

    public SpriteBatch batch;
    public Skin uiSkin;

    @Override
    public void create() {
        batch = new SpriteBatch();
        uiSkin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));

        this.setScreen(new MainMenuScreen(this)); // Set awal ke menu utama
    }

    @Override
    public void render() {
        super.render(); // Render screen aktif
    }

    @Override
    public void dispose() {
        super.dispose();
        uiSkin.dispose();
        batch.dispose();
    }
}
