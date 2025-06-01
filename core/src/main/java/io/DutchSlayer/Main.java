package io.DutchSlayer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.Gdx;
import io.DutchSlayer.defend.AudioManager;
import io.DutchSlayer.defend.GameScreen;

public class Main extends Game {
    public SpriteBatch batch;
//    public Music bgMusic;

    @Override
    public void create() {
        batch = new SpriteBatch();
        AudioManager.initialize();
//        bgMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/Backsound.mp3"));
        this.setScreen(new GameScreen(this)); // Set awal ke menu utama
//        bgMusic.setLooping(true);
//        bgMusic.play();
    }

    @Override
    public void render() {
        super.render(); // Render screen aktif
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        AudioManager.shutdown();
    }
}
