package io.DutchSlayer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.Gdx;

import io.DutchSlayer.defend.screens.MainMenuScreen;
import io.DutchSlayer.defend.utils.GameMode;
import io.DutchSlayer.defend.utils.AudioManager;

public class Main extends Game {
    public static GameMode currentMode = GameMode.NONE;
    public SpriteBatch batch;
    public Music bgMusic;
    public Music defendModeMusic;

    @Override
    public void create() {
        batch = new SpriteBatch();
        AudioManager.initialize();
        // Load dan play musik sekali saja di awal
        // Load legacy bgMusic for compatibility (optional, bisa dihapus nanti)
        try {
            bgMusic = Gdx.audio.newMusic(Gdx.files.internal("Music/MainSound.mp3"));
            bgMusic.setLooping(true);
            bgMusic.setVolume(0.6f);
        } catch (Exception e) {
            System.err.println("⚠️ Main: Legacy bgMusic not found, using AudioManager only");
            bgMusic = null;
        }
        this.setScreen(new MainMenuScreen(this)); // Set awal ke menu utamathis.setScreen(new MainMenuScreen(this)); // Set awal ke menu utamathis.setScreen(new MainMenuScreen(this)); // Set awal ke menu utama
    }

    @Override
    public void render() {
        super.render(); // Render screen aktif
    }

    @Override
    public void dispose() {
        batch.dispose();
        super.dispose();
        AudioManager.shutdown();
        // Dispose legacy music if exists
        if (bgMusic != null) {
            bgMusic.dispose();
        }
    }
}
