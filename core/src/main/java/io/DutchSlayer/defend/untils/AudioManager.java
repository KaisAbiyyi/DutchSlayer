package io.DutchSlayer.defend.untils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {
    /* ===== SFX ===== */
    private static Sound buildingSound;
    private static Sound trapSound;

    /* ===== MUSIC ===== */
    private static Music mainMenuMusic;
    private static Music towerDefenseMusic;
    private static Music currentMusic;

    /* ===== VOLUMES ===== */
    private static float masterVolume = 1.0f;
    private static float sfxVolume    = 0.8f;
    private static float musicVolume  = 0.6f;

    private static boolean isInitialized = false;

    /** Call once in Main.create() */
    public static void initialize() {
        if (isInitialized) return;
        isInitialized = true;

        try {
            // Load your SFX files (adjust paths as needed)
            buildingSound = loadSoundSafe("SFX/Building.mp3", "Building Sound");
            trapSound     = loadSoundSafe("SFX/trap.mp3",     "Trap Sound");

            // Load background music
            mainMenuMusic     = loadMusicSafe("Music/MainSound.mp3",  "Main Menu Music");
            towerDefenseMusic = loadMusicSafe("Music/Backsound.mp3",  "Tower Defense Music");

            currentMusic = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Sound loadSoundSafe(String filePath, String soundName) {
        try {
            return Gdx.audio.newSound(Gdx.files.internal(filePath));
        } catch (Exception e) {
            System.err.println("Failed to load SFX " + soundName + " from " + filePath);
            return null;
        }
    }

    private static Music loadMusicSafe(String filePath, String musicName) {
        try {
            Music m = Gdx.audio.newMusic(Gdx.files.internal(filePath));
            m.setLooping(true);
            return m;
        } catch (Exception e) {
            System.err.println("Failed to load Music " + musicName + " from " + filePath);
            return null;
        }
    }

    /* ===== PUBLIC METHODS TO PLAY SFX ===== */
    public static void playTowerDeploy() {
        if (buildingSound != null) {
            buildingSound.play(masterVolume * sfxVolume);
        }
    }

    public static void playTrapDeploy() {
        if (trapSound != null) {
            trapSound.play(masterVolume * sfxVolume);
        }
    }

    /* ===== PUBLIC METHODS TO PLAY / STOP MUSIC ===== */
    public static void playMainMenuMusic() {
        playMusic(mainMenuMusic);
    }

    public static boolean isPlayingMainMenuMusic() {
        return currentMusic == mainMenuMusic && currentMusic.isPlaying();
    }

    public static void playTowerDefenseMusic() {
        playMusic(towerDefenseMusic);
    }

    public static boolean isPlayingTowerDefenseMusic() {
        return currentMusic == towerDefenseMusic && currentMusic.isPlaying();
    }

    private static void playMusic(Music m) {
        if (m == null) return;
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
        }
        currentMusic = m;
        currentMusic.setVolume(masterVolume * musicVolume);
        currentMusic.play();
    }

    public static boolean isMusicPlaying() {
        return currentMusic != null && currentMusic.isPlaying();
    }

    public static void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    /* ===== VOLUME SETTERS / GETTERS ===== */
    public static void setMasterVolume(float vol) {
        masterVolume = Math.max(0f, Math.min(1f, vol));
        if (currentMusic != null) {
            currentMusic.setVolume(masterVolume * musicVolume);
        }
    }
    public static float getMasterVolume() {
        return masterVolume;
    }

    public static void setMusicVolume(float vol) {
        musicVolume = Math.max(0f, Math.min(1f, vol));
        if (currentMusic != null) {
            currentMusic.setVolume(masterVolume * musicVolume);
        }
    }
    public static float getMusicVolume() {
        return musicVolume;
    }

    public static void setSfxVolume(float vol) {
        sfxVolume = Math.max(0f, Math.min(1f, vol));
    }
    public static float getSfxVolume() {
        return sfxVolume;
    }

    /** Clean‐up on game exit */
    public static void shutdown() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }
        if (mainMenuMusic != null) {
            mainMenuMusic.dispose();
            mainMenuMusic = null;
        }
        if (towerDefenseMusic != null) {
            towerDefenseMusic.stop();
            towerDefenseMusic.dispose();
            towerDefenseMusic = null;
        }
        if (buildingSound != null) {
            buildingSound.dispose();
            buildingSound = null;
        }
        if (trapSound != null) {
            trapSound.dispose();
            trapSound = null;
        }
        isInitialized = false;
    }
}
