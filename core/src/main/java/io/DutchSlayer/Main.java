package io.DutchSlayer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.Gdx;

import io.DutchSlayer.defend.screens.MainMenuScreen;
import io.DutchSlayer.defend.untils.GameMode;

public class Main extends Game {
    public static GameMode currentMode = GameMode.NONE;
    public SpriteBatch batch;
    public Music bgMusic;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Load dan play musik sekali saja di awal
        bgMusic = Gdx.audio.newMusic(Gdx.files.internal("music/MainSound.mp3"));
        bgMusic.setLooping(true);
        bgMusic.play();

        this.setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.dispose();
        }
        batch.dispose();
        super.dispose();
    }
}
