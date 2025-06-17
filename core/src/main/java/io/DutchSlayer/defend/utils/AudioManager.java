package io.DutchSlayer.defend.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

/**
 * AudioManager optimized untuk handle semua sound effects dalam game
 * Menggunakan Map untuk mengurangi redundancy dan meningkatkan maintainability
 */
public class AudioManager {

    /* ===== SOUND IDENTIFIERS ===== */
    public enum SoundType {
        // Deployment sounds
        BUILDING,
        TRAP,

        // UI sounds
        BUTTON_CLICK,
        NAVBAR_SELECT,

        // Combat sounds
        TOWER_SHOOT,
        AOE_SHOOT,
        SLOW_SHOOT,
        ENEMY_SHOOT,
        BOSS_SHOOT,
        ENEMY_DEATH,

        // Trap collision sounds
        TRAP_ATTACK,
        TRAP_SLOW,
        TRAP_EXPLOSION,

        // Tower destruction sounds
        TOWER_BREAK,

        // Optional sounds (dapat null)
        EXPLOSION,
        HIT
    }

    public enum MusicType {
        MAIN_MENU,
        TOWER_DEFENSE,
        BOSS_BATTLE,
        VICTORY,
        DEFEAT
    }

    /* ===== AUDIO STORAGE ===== */
    private static final Map<SoundType, Sound> sounds = new HashMap<>();
    private static final Map<MusicType, Music> musics = new HashMap<>();

    /* ===== SOUND CONFIGURATIONS ===== */
    private static final Map<SoundType, String> soundPaths = new HashMap<>();
    private static final Map<MusicType, String> musicPaths = new HashMap<>();
    private static final Map<SoundType, Float> defaultVolumes = new HashMap<>();

    /* ===== VOLUME SETTINGS ===== */
    private static float masterVolume = 0.8f;
    private static float sfxVolume = 0.8f;
    private static float musicVolume = 0.5f;

    /* ===== MUSIC STATE ===== */
    private static Music currentMusic;
    private static boolean isInitialized = false;

    /* ===== MUSIC TRANSITION SYSTEM ===== */
    private static boolean isFadingOut = false;
    private static boolean isFadingIn = false;
    private static float fadeTimer = 0f;
    private static float fadeDuration = 3f;
    private static float originalVolume = 0f;
    private static Music nextMusic = null;

    static {
        initializeConfigurations();
    }

    /**
     * Initialize semua konfigurasi audio paths dan volumes
     */
    private static void initializeConfigurations() {
        // Sound file paths
        soundPaths.put(SoundType.BUILDING, "assets/SFX/Building.mp3");
        soundPaths.put(SoundType.TRAP, "assets/SFX/trap.mp3");
        soundPaths.put(SoundType.BUTTON_CLICK, "assets/SFX/BtnClick.mp3");
        soundPaths.put(SoundType.NAVBAR_SELECT, "assets/SFX/SelectNavbar.mp3");
        soundPaths.put(SoundType.TOWER_SHOOT, "SFX/TowerShoot.mp3");
        soundPaths.put(SoundType.AOE_SHOOT, "assets/SFX/TowerAoe.mp3");
        soundPaths.put(SoundType.SLOW_SHOOT, "assets/SFX/WindPassing.mp3");
        soundPaths.put(SoundType.ENEMY_SHOOT, "assets/SFX/EnemyShoot.mp3");
        soundPaths.put(SoundType.BOSS_SHOOT, "assets/SFX/BossShoot.mp3");
        soundPaths.put(SoundType.ENEMY_DEATH, "assets/SFX/EnemyDeath.mp3");
        soundPaths.put(SoundType.TRAP_ATTACK, "assets/SFX/TrapSpike.mp3");
        soundPaths.put(SoundType.TRAP_SLOW, "assets/SFX/TrapSlow.mp3");
        soundPaths.put(SoundType.TRAP_EXPLOSION, "assets/SFX/TrapBomb.mp3");
        soundPaths.put(SoundType.TOWER_BREAK, "assets/SFX/TowerBreak.mp3");

        // Music file paths
        musicPaths.put(MusicType.MAIN_MENU, "assets/Music/MainSound.mp3");
        musicPaths.put(MusicType.TOWER_DEFENSE, "assets/Music/Backsound.mp3");
        musicPaths.put(MusicType.BOSS_BATTLE, "assets/Music/BossMusic.mp3");
        musicPaths.put(MusicType.VICTORY, "assets/Music/VictoryMusic.mp3");
        musicPaths.put(MusicType.DEFEAT, "assets/Music/DefeatMusic.mp3");


        // Default volume multipliers
        defaultVolumes.put(SoundType.BUILDING, 1.0f);
        defaultVolumes.put(SoundType.TRAP, 1.0f);
        defaultVolumes.put(SoundType.BUTTON_CLICK, 1.0f);
        defaultVolumes.put(SoundType.NAVBAR_SELECT, 1.0f);
        defaultVolumes.put(SoundType.TOWER_SHOOT, 1.0f);
        defaultVolumes.put(SoundType.AOE_SHOOT, 1.0f);
        defaultVolumes.put(SoundType.SLOW_SHOOT, 0.6f);
        defaultVolumes.put(SoundType.ENEMY_SHOOT, 0.5f);
        defaultVolumes.put(SoundType.BOSS_SHOOT, 0.7f);
        defaultVolumes.put(SoundType.ENEMY_DEATH, 0.6f);
        defaultVolumes.put(SoundType.TRAP_ATTACK, 0.7f);
        defaultVolumes.put(SoundType.TRAP_SLOW, 0.6f);
        defaultVolumes.put(SoundType.TRAP_EXPLOSION, 0.8f);
        defaultVolumes.put(SoundType.TOWER_BREAK, 0.7f);
    }

    /**
     * Initialize AudioManager - load semua audio assets
     */
    public static void initialize() {
        if (isInitialized) {
            System.out.println("⚠️ AudioManager: Already initialized, skipping...");
            return;
        }

        System.out.println("🎵 AudioManager: Initializing audio system...");

        try {
            // Load all sounds
            for (Map.Entry<SoundType, String> entry : soundPaths.entrySet()) {
                Sound sound = loadSoundSafe(entry.getValue(), entry.getKey().name());
                sounds.put(entry.getKey(), sound);
            }

            // Load all musics
            for (Map.Entry<MusicType, String> entry : musicPaths.entrySet()) {
                Music music = loadMusicSafe(entry.getValue(), entry.getKey().name());
                musics.put(entry.getKey(), music);
            }

            isInitialized = true;
            System.out.println("✅ AudioManager: Audio system initialized successfully!");

        } catch (Exception e) {
            System.err.println("❌ AudioManager: Critical error during initialization - " + e.getMessage());
            e.printStackTrace();
            isInitialized = false;
        }
    }

    /**
     * Safe sound loading dengan error handling
     */
    private static Sound loadSoundSafe(String filePath, String soundName) {
        try {
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(filePath));
            System.out.println("✅ Loaded: " + soundName + " from " + filePath);
            return sound;
        } catch (Exception e) {
            System.err.println("❌ Failed to load " + soundName + " from " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Safe music loading dengan error handling
     */
    private static Music loadMusicSafe(String filePath, String musicName) {
        try {
            Music music = Gdx.audio.newMusic(Gdx.files.internal(filePath));
            music.setLooping(true);
            System.out.println("✅ Loaded: " + musicName + " from " + filePath);
            return music;
        } catch (Exception e) {
            System.err.println("❌ Failed to load " + musicName + " from " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    /* ===== GENERIC SOUND PLAYING METHODS ===== */

    /**
     * Play sound dengan default volume
     */
    public static void playSound(SoundType soundType) {
        playSound(soundType, defaultVolumes.getOrDefault(soundType, 1.0f));
    }

    /**
     * Play sound dengan custom volume multiplier
     */
    public static void playSound(SoundType soundType, float volumeMultiplier) {
        if (!isInitialized) {
            initialize();
        }

        Sound sound = sounds.get(soundType);
        if (sound != null) {
            try {
                float finalVolume = masterVolume * sfxVolume * volumeMultiplier;
                if (finalVolume > 0.0f) {
                    sound.play(finalVolume);
                    System.out.println("🔊 Playing: " + soundType.name() + " (Volume: " + String.format("%.2f", finalVolume) + ")");
                }
            } catch (Exception e) {
                System.err.println("❌ Error playing " + soundType.name() + ": " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ " + soundType.name() + " sound is null - skipping playback");
        }
    }

    /* ===== CONVENIENT WRAPPER METHODS ===== */

    // Deployment sounds
    public static void playTowerDeploy() { playSound(SoundType.BUILDING); }
    public static void playTrapDeploy() { playSound(SoundType.TRAP); }

    // UI sounds
    public static void PlayBtnSound() { playSound(SoundType.BUTTON_CLICK); }
    public static void playRemoveButtonClick() { playSound(SoundType.BUTTON_CLICK); }
    public static void playPauseButtonClick() { playSound(SoundType.BUTTON_CLICK); }
    public static void PlayBtnPaper() { playSound(SoundType.NAVBAR_SELECT); }

    // Combat sounds
    public static void playTowerShoot() { playSound(SoundType.TOWER_SHOOT); }
    public static void playTowerShootWithVolume(float volume) { playSound(SoundType.TOWER_SHOOT, volume); }
    public static void playAOEShoot() { playSound(SoundType.AOE_SHOOT); }
    public static void playAOEShootWithVolume(float volume) { playSound(SoundType.AOE_SHOOT, volume); }
    public static void playSlowProjectile() { playSound(SoundType.SLOW_SHOOT); }
    public static void playSlowProjectileWithVolume(float volume) { playSound(SoundType.SLOW_SHOOT, volume); }
    public static void playEnemyShoot() { playSound(SoundType.ENEMY_SHOOT); }
    public static void playEnemyShootWithVolume(float volume) { playSound(SoundType.ENEMY_SHOOT, volume); }
    public static void playBossShoot() { playSound(SoundType.BOSS_SHOOT); }
    public static void playBossShootWithVolume(float volume) { playSound(SoundType.BOSS_SHOOT, volume); }
    public static void playEnemyDeath() { playSound(SoundType.ENEMY_DEATH); }
    public static void playEnemyDeathWithVolume(float volume) { playSound(SoundType.ENEMY_DEATH, volume); }

    // Trap collision sounds
    public static void playTrapAttackHit() { playSound(SoundType.TRAP_ATTACK); }
    public static void playTrapSlowHit() { playSound(SoundType.TRAP_SLOW); }
    public static void playTrapExplosionHit() { playSound(SoundType.TRAP_EXPLOSION); }

    // Tower destruction sounds
    public static void playTowerBreak() { playSound(SoundType.TOWER_BREAK); }
    public static void playTowerRemoval() { playSound(SoundType.TOWER_BREAK, 5.0f); }
    public static void playMainTowerDestroy() { playSound(SoundType.TOWER_BREAK, 1.0f); }

    // Enemy death dengan type check
    public static void playEnemyDeath(io.DutchSlayer.defend.entities.enemies.EnemyType enemyType) {
        if (enemyType == io.DutchSlayer.defend.entities.enemies.EnemyType.BOSS) {
            return;
        }
        playEnemyDeath();
    }

    /* ===== MUSIC METHODS ===== */

    /**
     * Play music dengan smooth transition check
     */
    public static void playMusic(MusicType musicType) {
        if (!isInitialized) {
            initialize();
        }

        Music music = musics.get(musicType);
        if (music != null) {
            try {
                // Check jika musik yang diminta sama dengan yang sedang berjalan
                if (currentMusic == music && currentMusic.isPlaying()) {
                    System.out.println("🎵 " + musicType.name() + " already playing - skipping restart");
                    return;
                }

                // Stop current music if different
                if (currentMusic != null && currentMusic.isPlaying() && currentMusic != music) {
                    currentMusic.stop();
                    System.out.println("🛑 Stopped previous music to play: " + musicType.name());
                }

                currentMusic = music;
                float finalVolume = masterVolume * musicVolume;
                currentMusic.setVolume(finalVolume);

                if (!currentMusic.isPlaying() && finalVolume > 0.0f) {
                    currentMusic.play();
                    System.out.println("🎵 Playing: " + musicType.name() + " (Volume: " + String.format("%.2f", finalVolume) + ")");
                }
            } catch (Exception e) {
                System.err.println("❌ Error playing " + musicType.name() + ": " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ " + musicType.name() + " is null - skipping playback");
        }
    }

    // Music convenience methods
    public static void playMainMenuMusic() { playMusic(MusicType.MAIN_MENU); }
    public static void playTowerDefenseMusic() { playMusic(MusicType.TOWER_DEFENSE); }
    public static void playBossMusic() { playMusic(MusicType.BOSS_BATTLE); }

    public static void playVictoryMusic() {
        playMusic(MusicType.VICTORY);
        System.out.println("🏆 Victory music started!");
    }

    public static void playDefeatMusic() {
        playMusic(MusicType.DEFEAT);
        System.out.println("💀 Defeat music started!");
    }

    public static void stopMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
            System.out.println("🛑 Music stopped");
        }
        currentMusic = null;
    }

    public static void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
            System.out.println("⏸️ Music paused");
        }
    }

    public static void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
            System.out.println("▶️ Music resumed");
        }
    }

    /* ===== MUSIC TRANSITION METHODS ===== */

    public static void fadeOutCurrentMusic(float duration) {
        if (currentMusic != null && currentMusic.isPlaying()) {
            isFadingOut = true;
            isFadingIn = false;
            fadeTimer = 0f;
            fadeDuration = duration;
            originalVolume = currentMusic.getVolume();
            nextMusic = null;
            System.out.println("🔉 Starting music fade out over " + duration + " seconds...");
        }
    }

    public static void fadeToMusic(MusicType musicType, float fadeOutDuration) {
        Music newMusic = musics.get(musicType);
        if (newMusic == null) {
            System.out.println("⚠️ Cannot fade to null music: " + musicType.name());
            return;
        }

        if (currentMusic != null && currentMusic.isPlaying()) {
            isFadingOut = true;
            isFadingIn = false;
            fadeTimer = 0f;
            fadeDuration = fadeOutDuration;
            originalVolume = currentMusic.getVolume();
            nextMusic = newMusic;
            System.out.println("🔄 Starting music transition to " + musicType.name() + " over " + fadeOutDuration + " seconds...");
        } else {
            playMusic(musicType);
        }
    }

    public static void playBossMusicWithTransition(float fadeOutDuration) {
        fadeToMusic(MusicType.BOSS_BATTLE, fadeOutDuration);
    }

    public static void updateMusicTransition(float delta) {
        if (isFadingOut && currentMusic != null) {
            fadeTimer += delta;
            float progress = fadeTimer / fadeDuration;

            if (progress >= 1f) {
                currentMusic.stop();
                isFadingOut = false;

                if (nextMusic != null) {
                    currentMusic = nextMusic;
                    nextMusic = null;

                    float finalVolume = masterVolume * musicVolume;
                    currentMusic.setVolume(0f);
                    currentMusic.play();

                    isFadingIn = true;
                    fadeTimer = 0f;
                    originalVolume = finalVolume;
                    System.out.println("🔊 Starting fade in for new music...");
                } else {
                    System.out.println("🔇 Fade out completed - music stopped");
                }
            } else {
                float newVolume = originalVolume * (1f - progress);
                currentMusic.setVolume(newVolume);
            }
        }

        if (isFadingIn && currentMusic != null) {
            fadeTimer += delta;
            float progress = fadeTimer / fadeDuration;

            if (progress >= 1f) {
                currentMusic.setVolume(originalVolume);
                isFadingIn = false;
                System.out.println("✅ Music transition completed!");
            } else {
                float newVolume = originalVolume * progress;
                currentMusic.setVolume(newVolume);
            }
        }
    }

    /* ===== VOLUME CONTROL METHODS ===== */

    public static void setMasterVolume(float volume) {
        masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        System.out.println("🔊 Master volume set to: " + masterVolume);
    }

    public static void setSfxVolume(float volume) {
        sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
        System.out.println("🔊 SFX volume set to: " + sfxVolume);
    }

    public static void setMusicVolume(float volume) {
        musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(masterVolume * musicVolume);
        }
        System.out.println("🔊 Music volume set to: " + musicVolume);
    }

    // Volume getters
    public static float getMasterVolume() { return masterVolume; }
    public static float getSfxVolume() { return sfxVolume; }
    public static float getMusicVolume() { return musicVolume; }

    // Volume utility methods
    public static void muteAll() { setMasterVolume(0.0f); }
    public static void unmuteAll() { setMasterVolume(1.0f); }
    public static boolean isMuted() { return masterVolume == 0.0f; }
    public static boolean isMusicPlaying() { return currentMusic != null && currentMusic.isPlaying(); }
    public static boolean isTransitioning() { return isFadingOut || isFadingIn; }
    public static float getFadeProgress() {
        if (isFadingOut || isFadingIn) {
            return Math.min(1f, fadeTimer / fadeDuration);
        }
        return 0f;
    }

    /* ===== CLEANUP METHODS ===== */

    public static void shutdown() {
        if (!isInitialized) {
            System.out.println("⚠️ AudioManager: Not initialized, nothing to shutdown");
            return;
        }

        System.out.println("🔄 AudioManager: Shutting down audio system...");

        try {
            // Dispose all sounds
            sounds.forEach((type, sound) -> {
                if (sound != null) {
                    try {
                        sound.dispose();
                        System.out.println("✅ Disposed: " + type.name());
                    } catch (Exception e) {
                        System.err.println("❌ Error disposing " + type.name() + ": " + e.getMessage());
                    }
                }
            });
            sounds.clear();

            // Dispose all musics
            musics.forEach((type, music) -> {
                if (music != null) {
                    try {
                        if (music.isPlaying()) {
                            music.stop();
                        }
                        music.dispose();
                        System.out.println("✅ Disposed: " + type.name());
                    } catch (Exception e) {
                        System.err.println("❌ Error disposing " + type.name() + ": " + e.getMessage());
                    }
                }
            });
            musics.clear();

            currentMusic = null;
            isInitialized = false;
            System.out.println("✅ AudioManager: Audio system shutdown successfully!");

        } catch (Exception e) {
            System.err.println("❌ AudioManager: Error during shutdown - " + e.getMessage());
        }
    }

    /* ===== UTILITY METHODS ===== */

    public static boolean isInitialized() {
        return isInitialized;
    }

    public static void testAllSounds() {
        System.out.println("🎵 AudioManager: Testing loaded sounds...");

        sounds.forEach((type, sound) -> {
            System.out.println((sound != null ? "✅" : "❌") + " " + type.name() + ": " + (sound != null ? "LOADED" : "NULL"));
        });

        musics.forEach((type, music) -> {
            System.out.println((music != null ? "✅" : "❌") + " " + type.name() + ": " + (music != null ? "LOADED" : "NULL"));
        });

        System.out.println("🎵 Sound test complete. Initialization status: " + (isInitialized ? "READY" : "NOT READY"));
    }
}
