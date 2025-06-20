package io.DutchSlayer.defend.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    public enum SoundType {
        BUILDING,
        TRAP,

        BUTTON_CLICK,
        NAVBAR_SELECT,

        TOWER_SHOOT,
        AOE_SHOOT,
        SLOW_SHOOT,
        ENEMY_SHOOT,
        BOSS_SHOOT,
        ENEMY_DEATH,

        TRAP_ATTACK,
        TRAP_SLOW,
        TRAP_EXPLOSION,

        TOWER_BREAK
    }

    public enum MusicType {
        MAIN_MENU,
        TOWER_DEFENSE,
        BOSS_BATTLE,
        VICTORY,
        DEFEAT
    }

    private static final Map<SoundType, Sound> sounds = new HashMap<>();
    private static final Map<MusicType, Music> musics = new HashMap<>();

    private static final Map<SoundType, String> soundPaths = new HashMap<>();
    private static final Map<MusicType, String> musicPaths = new HashMap<>();
    private static final Map<SoundType, Float> defaultVolumes = new HashMap<>();

    private static float masterVolume = 0.8f;
    private static float sfxVolume = 0.8f;
    private static float musicVolume = 0.5f;

    private static Music currentMusic;
    private static boolean isInitialized = false;

    private static boolean isFadingOut = false;
    private static boolean isFadingIn = false;
    private static float fadeTimer = 0f;
    private static float fadeDuration = 3f;
    private static float originalVolume = 0f;
    private static Music nextMusic = null;

    static {
        initializeConfigurations();
    }

    private static void initializeConfigurations() {
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
        musicPaths.put(MusicType.MAIN_MENU, "assets/Music/MainSound.mp3");
        musicPaths.put(MusicType.TOWER_DEFENSE, "assets/Music/Backsound.mp3");
        musicPaths.put(MusicType.BOSS_BATTLE, "assets/Music/BossMusic.mp3");
        musicPaths.put(MusicType.VICTORY, "assets/Music/VictoryMusic.mp3");
        musicPaths.put(MusicType.DEFEAT, "assets/Music/DefeatMusic.mp3");

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

    public static void initialize() {
        if (isInitialized) {
            return;
        }

        try {
            for (Map.Entry<SoundType, String> entry : soundPaths.entrySet()) {
                Sound sound = loadSoundSafe(entry.getValue(), entry.getKey().name());
                sounds.put(entry.getKey(), sound);
            }

            for (Map.Entry<MusicType, String> entry : musicPaths.entrySet()) {
                Music music = loadMusicSafe(entry.getValue(), entry.getKey().name());
                musics.put(entry.getKey(), music);
            }

            isInitialized = true;
        } catch (Exception e) {
            System.err.println("❌ AudioManager: Critical error during initialization - " + e.getMessage());
            e.printStackTrace();
            isInitialized = false;
        }
    }

    private static Sound loadSoundSafe(String filePath, String soundName) {
        try {
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(filePath));
            return sound;
        } catch (Exception e) {
            System.err.println("❌ Failed to load " + soundName + " from " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    private static Music loadMusicSafe(String filePath, String musicName) {
        try {
            Music music = Gdx.audio.newMusic(Gdx.files.internal(filePath));
            music.setLooping(true);
            return music;
        } catch (Exception e) {
            System.err.println("❌ Failed to load " + musicName + " from " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    public static void playSound(SoundType soundType) {
        playSound(soundType, defaultVolumes.getOrDefault(soundType, 1.0f));
    }

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
                }
            } catch (Exception e) {
                System.err.println("❌ Error playing " + soundType.name() + ": " + e.getMessage());
            }
        }
    }


    public static void playTowerDeploy() { playSound(SoundType.BUILDING); }
    public static void playTrapDeploy() { playSound(SoundType.TRAP); }
    public static void PlayBtnSound() { playSound(SoundType.BUTTON_CLICK); }
    public static void PlayBtnPaper() { playSound(SoundType.NAVBAR_SELECT); }
    public static void playTowerShootWithVolume(float volume) { playSound(SoundType.TOWER_SHOOT, volume); }
    public static void playAOEShootWithVolume(float volume) { playSound(SoundType.AOE_SHOOT, volume); }
    public static void playSlowProjectileWithVolume(float volume) { playSound(SoundType.SLOW_SHOOT, volume); }
    public static void playEnemyShoot() { playSound(SoundType.ENEMY_SHOOT); }
    public static void playBossShoot() { playSound(SoundType.BOSS_SHOOT); }
    public static void playEnemyDeath() { playSound(SoundType.ENEMY_DEATH); }
    public static void playTrapAttackHit() { playSound(SoundType.TRAP_ATTACK); }
    public static void playTrapSlowHit() { playSound(SoundType.TRAP_SLOW); }
    public static void playTrapExplosionHit() { playSound(SoundType.TRAP_EXPLOSION); }
    public static void playTowerRemoval() { playSound(SoundType.TOWER_BREAK, 5.0f); }

    public static void playEnemyDeath(io.DutchSlayer.defend.entities.enemies.EnemyType enemyType) {
        if (enemyType == io.DutchSlayer.defend.entities.enemies.EnemyType.BOSS) {
            return;
        }
        playEnemyDeath();
    }

    public static void playMusic(MusicType musicType) {
        if (!isInitialized) {
            initialize();
        }
        Music music = musics.get(musicType);
        if (music != null) {
            try {
                if (currentMusic == music && currentMusic.isPlaying()) {
                    return;
                }

                if (currentMusic != null && currentMusic.isPlaying() && currentMusic != music) {
                    currentMusic.stop();
                }

                currentMusic = music;
                float finalVolume = masterVolume * musicVolume;
                currentMusic.setVolume(finalVolume);

                if (!currentMusic.isPlaying() && finalVolume > 0.0f) {
                    currentMusic.play();
                }
            } catch (Exception e) {
                System.err.println("❌ Error playing " + musicType.name() + ": " + e.getMessage());
            }
        }
    }

    public static void playMainMenuMusic() { playMusic(MusicType.MAIN_MENU); }
    public static void playTowerDefenseMusic() { playMusic(MusicType.TOWER_DEFENSE); }

    public static void playVictoryMusic() {
        playMusic(MusicType.VICTORY);
    }

    public static void playDefeatMusic() {
        playMusic(MusicType.DEFEAT);
    }

    public static void stopMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
        }
        currentMusic = null;
    }

    public static void fadeOutCurrentMusic(float duration) {
        if (currentMusic != null && currentMusic.isPlaying()) {
            isFadingOut = true;
            isFadingIn = false;
            fadeTimer = 0f;
            fadeDuration = duration;
            originalVolume = currentMusic.getVolume();
            nextMusic = null;
        }
    }

    public static void fadeToMusic(MusicType musicType, float fadeOutDuration) {
        Music newMusic = musics.get(musicType);
        if (newMusic == null) {
            return;
        }

        if (currentMusic != null && currentMusic.isPlaying()) {
            isFadingOut = true;
            isFadingIn = false;
            fadeTimer = 0f;
            fadeDuration = fadeOutDuration;
            originalVolume = currentMusic.getVolume();
            nextMusic = newMusic;
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
            } else {
                float newVolume = originalVolume * progress;
                currentMusic.setVolume(newVolume);
            }
        }
    }

    public static void setMasterVolume(float volume) {
        masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public static void setSfxVolume(float volume) {
        sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public static void setMusicVolume(float volume) {
        musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(masterVolume * musicVolume);
        }
    }

    public static float getMasterVolume() { return masterVolume; }
    public static float getSfxVolume() { return sfxVolume; }
    public static float getMusicVolume() { return musicVolume; }
    public static boolean isMusicPlaying() { return currentMusic != null && currentMusic.isPlaying(); }

    public static void shutdown() {
        if (!isInitialized) {
            return;
        }

        try {
            sounds.forEach((type, sound) -> {
                if (sound != null) {
                    try {
                        sound.dispose();
                    } catch (Exception e) {
                        System.err.println("❌ Error disposing " + type.name() + ": " + e.getMessage());
                    }
                }
            });
            sounds.clear();

            musics.forEach((type, music) -> {
                if (music != null) {
                    try {
                        if (music.isPlaying()) {
                            music.stop();
                        }
                        music.dispose();
                    } catch (Exception e) {
                        System.err.println("❌ Error disposing " + type.name() + ": " + e.getMessage());
                    }
                }
            });
            musics.clear();
            currentMusic = null;
            isInitialized = false;
        } catch (Exception e) {
            System.err.println("❌ AudioManager: Error during shutdown - " + e.getMessage());
        }
    }

}
