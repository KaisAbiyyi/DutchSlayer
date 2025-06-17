package io.DutchSlayer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.DutchSlayer.screens.MainMenuScreen;
import io.DutchSlayer.defend.utils.AudioManager;
import io.DutchSlayer.defend.utils.GameMode;
import com.badlogic.gdx.audio.Music;
public class Main extends Game {

    public SpriteBatch batch;
    public Skin uiSkin;
    public Music bgMusic;
    public Music defendModeMusic;
    public static GameMode currentMode = GameMode.NONE;
    @Override
    public void create() {
        batch = new SpriteBatch();
        AudioManager.initialize();
        uiSkin = new Skin(Gdx.files.internal("uiskin/uiskin.json"));
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
