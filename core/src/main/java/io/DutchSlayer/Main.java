package io.DutchSlayer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.DutchSlayer.defend.screens.TowerDefenseScreen;
import io.DutchSlayer.defend.screens.MainMenuScreen; // Menggunakan MainMenuScreen dari package defend
import io.DutchSlayer.defend.untils.GameMode;

public class Main extends Game {
    public static GameMode currentMode = GameMode.NONE;
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        this.setScreen(new MainMenuScreen(this)); // Memanggil MainMenuScreen dari defend package
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
