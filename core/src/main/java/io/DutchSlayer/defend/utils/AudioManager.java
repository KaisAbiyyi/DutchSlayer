package io.DutchSlayer.defend.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * AudioManager untuk handle semua sound effects dalam game
 * Mengatur loading, playing, dan disposal audio resources
 */
public class AudioManager {
    /* ===== DEPLOYMENT SOUNDS ===== */
    private static Sound buildingSound;     // SFX untuk deploy tower
    private static Sound trapSound;         // SFX untuk deploy trap

    /* ===== UI SOUNDS ===== */
    private static Sound Paperbtn; // ✅ SFX untuk navbar selection

    /* ===== COMBAT SOUNDS ===== */
    private static Sound towerShootSound;   // SFX untuk tower shooting
    private static Sound shootSound;        // SFX untuk tower shooting (optional)
    private static Sound explosionSound;    // SFX untuk explosion (optional)
    private static Sound hitSound;          // SFX untuk enemy hit (optional)

    private static Sound aoeShootSound;     // SFX untuk AOE tower shooting (mortir launch)
    private static Sound aoeHitSound;       // SFX untuk AOE explosion hit

    private static Sound enemyDeathSound;   // SFX untuk semua enemy death (generic)

    /* ===== BACKGROUND MUSIC ===== */
    private static Music mainMenuMusic;     // Musik untuk Main Menu
    private static Music towerDefenseMusic; // Musik untuk Tower Defense mode
    private static Music bossMusic;         // Musik untuk Boss Battle
    private static Music currentMusic;      // Currently playing music


    /* ===== VOLUME SETTINGS ===== */
    private static float masterVolume = 0.8f;      // Master volume (0.0f - 1.0f)
    private static float sfxVolume = 0.8f;          // SFX volume (0.0f - 1.0f)
    private static float musicVolume = 0.5f;        // Music volume (0.0f - 1.0f)


    /* ===== INITIALIZATION STATE ===== */
    private static boolean isInitialized = false;

    /* ===== MUSIC TRANSITION SYSTEM ===== */
    private static boolean isFadingOut = false;
    private static boolean isFadingIn = false;
    private static float fadeTimer = 0f;
    private static float fadeDuration = 3f;  // 3 detik fade duration
    private static float originalVolume = 0f;
    private static Music nextMusic = null;   // Music yang akan diplay setelah fade

    /**
     * Initialize AudioManager - load semua audio assets
     * Dipanggil sekali saat game startup
     * Automatically handles loading dan error recovery
     */
    public static void initialize() {
        if (isInitialized) {
            System.out.println("⚠️ AudioManager: Already initialized, skipping...");
            return;
        }

        System.out.println("🎵 AudioManager: Initializing audio system...");

        try {
            // ===== LOAD DEPLOYMENT SOUNDS =====
            buildingSound = loadSoundSafe("assets/SFX/Building.mp3", "Building Sound");
            trapSound = loadSoundSafe("assets/SFX/trap.mp3", "Trap Sound");

            Paperbtn = loadSoundSafe("assets/SFX/SelectNavbar.mp3", "SelectNavbar Sound");

            towerShootSound = loadSoundSafe("SFX/TowerShoot.mp3", "Tower Shoot Sound");
            aoeShootSound = loadSoundSafe("assets/SFX/TowerAoe.mp3", "AOE Shoot Sound");

            enemyDeathSound = loadSoundSafe("assets/SFX/EnemyDeath.mp3", "Enemy Death Sound");
            // ===== LOAD OPTIONAL COMBAT SOUNDS =====
            // Uncomment jika kamu ada file audio ini
            // shootSound = loadSoundSafe("assets/SFX/shoot.mp3", "Shoot Sound");
            // explosionSound = loadSoundSafe("assets/SFX/explosion.mp3", "Explosion Sound");
            // hitSound = loadSoundSafe("assets/SFX/hit.mp3", "Hit Sound");

            mainMenuMusic = loadMusicSafe("assets/Music/MainSound.mp3", "Main Menu Music");
            towerDefenseMusic = loadMusicSafe("assets/Music/Backsound.mp3", "Tower Defense Music");
            bossMusic = loadMusicSafe("assets/Music/BossMusic.mp3", "Boss Battle Music");


            isInitialized = true;
            System.out.println("✅ AudioManager: Audio system initialized successfully!");

            // Optional: Test loaded sounds
            testAllSounds();

        } catch (Exception e) {
            System.err.println("❌ AudioManager: Critical error during initialization - " + e.getMessage());
            e.printStackTrace();
            isInitialized = false;
        }
    }

    /**
     * Safe sound loading dengan error handling
     * @param filePath Path ke audio file
     * @param soundName Nama sound untuk logging
     * @return Sound object atau null jika gagal load
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
     * @param filePath Path ke music file
     * @param musicName Nama music untuk logging
     * @return Music object atau null jika gagal load
     */
    private static Music loadMusicSafe(String filePath, String musicName) {
        try {
            Music music = Gdx.audio.newMusic(Gdx.files.internal(filePath));
            music.setLooping(true); // Set to loop by default
            System.out.println("✅ Loaded: " + musicName + " from " + filePath);
            return music;
        } catch (Exception e) {
            System.err.println("❌ Failed to load " + musicName + " from " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Play navbar selection sound
     */
    public static void PlayBtnPaper() {
        playSound(Paperbtn, "Navbar Select");
    }

    /**
     * Play tower deployment sound
     */
    public static void playTowerDeploy() {
        playSound(buildingSound, "Tower Deploy");
    }

    /**
     * Play trap deployment sound
     */
    public static void playTrapDeploy() {
        playSound(trapSound, "Trap Deploy");
    }

    /**
     * Play tower shooting sound
     */
    public static void playTowerShoot() {
        playSound(towerShootSound, "Tower Shoot");
    }

    /**
     * Play AOE tower shooting sound (mortir launch)
     */
    public static void playAOEShoot() {
        playSound(aoeShootSound, "AOE Shoot");
    }

    /**
     * Play explosion sound (optional)
     */
    public static void playExplosion() {
        playSound(explosionSound, "Explosion");
    }

    /**
     * Play enemy hit sound (optional)
     */
    public static void playEnemyHit() {
        playSound(hitSound, "Enemy Hit");
    }

    /**
     * Play enemy death sound langsung (tanpa enemy type check)
     */
    public static void playEnemyDeath() {
        playEnemyDeathWithVolume(0.6f);
    }

    /**
     * Helper method untuk play sound dengan error handling dan initialization check
     * @param sound Sound object yang akan diplay
     * @param soundName Nama sound untuk debugging
     */
    private static void playSound(Sound sound, String soundName) {
        // Auto-initialize jika belum initialized
        if (!isInitialized) {
            System.out.println("⚠️ AudioManager not initialized, initializing now...");
            initialize();
        }

        if (sound != null) {
            try {
                float finalVolume = masterVolume * sfxVolume;
                if (finalVolume > 0.0f) {  // Skip jika volume 0
                    sound.play(finalVolume);
                    System.out.println("🔊 Playing: " + soundName + " (Volume: " + String.format("%.2f", finalVolume) + ")");
                }
            } catch (Exception e) {
                System.err.println("❌ Error playing " + soundName + ": " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ " + soundName + " sound is null - skipping playback");
        }
    }

    /* ===== BACKGROUND MUSIC METHODS ===== */

    /**
     * Play Main Menu background music
     */
    public static void playMainMenuMusic() {
        playMusic(mainMenuMusic, "Main Menu Music");
    }

    /**
     * Play Tower Defense background music
     */
    public static void playTowerDefenseMusic() {
        playMusic(towerDefenseMusic, "Tower Defense Music");
    }

    /**
     * Play generic enemy death sound untuk semua enemy types (kecuali boss)
     * @param enemyType Jenis enemy yang mati (untuk check boss)
     */
    public static void playEnemyDeath(io.DutchSlayer.defend.entities.enemies.EnemyType enemyType) {
        // Skip jika boss (sesuai request)
        if (enemyType == io.DutchSlayer.defend.entities.enemies.EnemyType.BOSS) {
            return;
        }

        // Play generic death sound untuk semua enemy
        playEnemyDeathWithVolume(0.6f);
    }

    /**
     * Play Boss Battle background music
     */
    public static void playBossMusic() {
        playMusic(bossMusic, "Boss Battle Music");
    }

    /**
     * Stop currently playing music
     */
    public static void stopMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
            System.out.println("🛑 Music stopped");
        }
        currentMusic = null;
    }

    /**
     * Pause currently playing music
     */
    public static void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
            System.out.println("⏸️ Music paused");
        }
    }

    /**
     * Resume currently paused music
     */
    public static void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
            System.out.println("▶️ Music resumed");
        }
    }

    /**
     * Helper method untuk play music dengan smooth transition
     * @param music Music object yang akan diplay
     * @param musicName Nama music untuk debugging
     */
    private static void playMusic(Music music, String musicName) {
        // Auto-initialize jika belum initialized
        if (!isInitialized) {
            System.out.println("⚠️ AudioManager not initialized, initializing now...");
            initialize();
        }

        if (music != null) {
            try {
                // Stop current music if playing
                if (currentMusic != null && currentMusic.isPlaying()) {
                    currentMusic.stop();
                }

                // Set new music as current
                currentMusic = music;

                // Calculate final volume
                float finalVolume = masterVolume * musicVolume;
                currentMusic.setVolume(finalVolume);

                // Start playing
                if (finalVolume > 0.0f) {
                    currentMusic.play();
                    System.out.println("🎵 Playing: " + musicName + " (Volume: " + String.format("%.2f", finalVolume) + ")");
                }
            } catch (Exception e) {
                System.err.println("❌ Error playing " + musicName + ": " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ " + musicName + " is null - skipping playback");
        }
    }


    /**
     * Set master volume (affects all sounds)
     * @param volume Volume level (0.0f = mute, 1.0f = full volume)
     */
    public static void setMasterVolume(float volume) {
        masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        System.out.println("🔊 Master volume set to: " + masterVolume);
    }

    /**
     * Set SFX volume (affects only sound effects)
     * @param volume Volume level (0.0f = mute, 1.0f = full volume)
     */
    public static void setSfxVolume(float volume) {
        sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
        System.out.println("🔊 SFX volume set to: " + sfxVolume);
    }

    /**
     * Set Music volume (affects only background music)
     * @param volume Volume level (0.0f = mute, 1.0f = full volume)
     */
    public static void setMusicVolume(float volume) {
        musicVolume = Math.max(0.0f, Math.min(1.0f, volume));

        // Update current music volume if playing
        if (currentMusic != null) {
            currentMusic.setVolume(masterVolume * musicVolume);
        }

        System.out.println("🔊 Music volume set to: " + musicVolume);
    }

    /**
     * Get current master volume
     * @return Current master volume (0.0f - 1.0f)
     */
    public static float getMasterVolume() {
        return masterVolume;
    }

    /**
     * Get current SFX volume
     * @return Current SFX volume (0.0f - 1.0f)
     */
    public static float getSfxVolume() {
        return sfxVolume;
    }

    /**
     * Get current Music volume
     * @return Current music volume (0.0f - 1.0f)
     */
    public static float getMusicVolume() {
        return musicVolume;
    }

    /**
     * Mute all sounds
     */
    public static void muteAll() {
        setMasterVolume(0.0f);
    }

    /**
     * Unmute sounds (restore to previous volume)
     */
    public static void unmuteAll() {
        setMasterVolume(1.0f);
    }

    /**
     * Check if audio is muted
     * @return true if master volume is 0
     */
    public static boolean isMuted() {
        return masterVolume == 0.0f;
    }

    /**
     * Check if music is currently playing
     * @return true if music is playing
     */
    public static boolean isMusicPlaying() {
        return currentMusic != null && currentMusic.isPlaying();
    }

    /**
     * Play tower shooting sound dengan volume custom
     * @param volumeMultiplier Multiplier volume (0.0f - 1.0f)
     */
    public static void playTowerShootWithVolume(float volumeMultiplier) {
        if (!isInitialized) {
            initialize();
        }

        if (towerShootSound != null) {
            try {
                // Calculate volume dengan custom multiplier
                float finalVolume = masterVolume * sfxVolume * volumeMultiplier;
                if (finalVolume > 0.0f) {
                    towerShootSound.play(finalVolume);
                    System.out.println("🔊 Tower Shoot (Custom Volume: " + String.format("%.2f", finalVolume) + ")");
                }
            } catch (Exception e) {
                System.err.println("❌ Error playing Tower Shoot with custom volume: " + e.getMessage());
            }
        }
    }

    /**
     * Play AOE tower shooting sound dengan volume custom
     * @param volumeMultiplier Multiplier volume (0.0f - 1.0f)
     */
    public static void playAOEShootWithVolume(float volumeMultiplier) {
        if (!isInitialized) {
            initialize();
        }

        if (aoeShootSound != null) {
            try {
                float finalVolume = masterVolume * sfxVolume * volumeMultiplier;
                if (finalVolume > 0.0f) {
                    aoeShootSound.play(finalVolume);
                    System.out.println("🚀 AOE Shoot (Volume: " + String.format("%.2f", finalVolume) + ")");
                }
            } catch (Exception e) {
                System.err.println("❌ Error playing AOE Shoot: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ AOE Shoot sound is null - using fallback");
            // Fallback ke explosion sound jika tersedia
            playSound(explosionSound, "AOE Shoot (Fallback)");
        }
    }

    /**
     * Play generic enemy death sound dengan volume custom
     * @param volumeMultiplier Multiplier volume (0.0f - 1.0f)
     */
    public static void playEnemyDeathWithVolume(float volumeMultiplier) {
        if (!isInitialized) {
            initialize();
        }

        if (enemyDeathSound != null) {
            try {
                float finalVolume = masterVolume * sfxVolume * volumeMultiplier;
                if (finalVolume > 0.0f) {
                    enemyDeathSound.play(finalVolume);
                    System.out.println("💀 Enemy Death (Volume: " + String.format("%.2f", finalVolume) + ")");
                }
            } catch (Exception e) {
                System.err.println("❌ Error playing enemy death sound: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Enemy death sound is null - no sound played");
        }
    }



    /**
        * Fade out current music perlahan
     * @param duration Durasi fade dalam detik
     */
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

    /**
     * Fade out current music dan transition ke music baru
     * @param newMusic Music yang akan diplay setelah fade out
     * @param fadeOutDuration Durasi fade out dalam detik
     */
    public static void fadeToMusic(Music newMusic, float fadeOutDuration) {
        if (newMusic == null) {
            System.out.println("⚠️ Cannot fade to null music");
            return;
        }

        if (currentMusic != null && currentMusic.isPlaying()) {
            isFadingOut = true;
            isFadingIn = false;
            fadeTimer = 0f;
            fadeDuration = fadeOutDuration;
            originalVolume = currentMusic.getVolume();
            nextMusic = newMusic;

            System.out.println("🔄 Starting music transition over " + fadeOutDuration + " seconds...");
        } else {
            // Langsung play music baru jika tidak ada current music
            playMusicDirect(newMusic, "Transition Music");
        }
    }

    /**
     * Update music transition (dipanggil setiap frame)
     * @param delta Delta time
     */
    public static void updateMusicTransition(float delta) {
        if (isFadingOut && currentMusic != null) {
            fadeTimer += delta;
            float progress = fadeTimer / fadeDuration;

            if (progress >= 1f) {
                // Fade out selesai
                currentMusic.stop();
                isFadingOut = false;

                if (nextMusic != null) {
                    // Start fade in ke music baru
                    currentMusic = nextMusic;
                    nextMusic = null;

                    float finalVolume = masterVolume * musicVolume;
                    currentMusic.setVolume(0f);  // Start dari volume 0
                    currentMusic.play();

                    // Start fade in
                    isFadingIn = true;
                    fadeTimer = 0f;
                    originalVolume = finalVolume;

                    System.out.println("🔊 Starting fade in for new music...");
                } else {
                    System.out.println("🔇 Fade out completed - music stopped");
                }
            } else {
                // Update volume saat fade out
                float newVolume = originalVolume * (1f - progress);
                currentMusic.setVolume(newVolume);
            }
        }

        if (isFadingIn && currentMusic != null) {
            fadeTimer += delta;
            float progress = fadeTimer / fadeDuration;

            if (progress >= 1f) {
                // Fade in selesai
                currentMusic.setVolume(originalVolume);
                isFadingIn = false;
                System.out.println("✅ Music transition completed!");
            } else {
                // Update volume saat fade in
                float newVolume = originalVolume * progress;
                currentMusic.setVolume(newVolume);
            }
        }
    }

    /**
     * Play music langsung tanpa transition
     */
    private static void playMusicDirect(Music music, String musicName) {
        if (music != null) {
            try {
                // Stop current music if playing
                if (currentMusic != null && currentMusic.isPlaying()) {
                    currentMusic.stop();
                }

                currentMusic = music;
                float finalVolume = masterVolume * musicVolume;
                currentMusic.setVolume(finalVolume);
                currentMusic.play();

                System.out.println("🎵 Playing: " + musicName + " (Direct)");
            } catch (Exception e) {
                System.err.println("❌ Error playing " + musicName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Play Boss Battle background music dengan transition
     */
    public static void playBossMusicWithTransition(float fadeOutDuration) {
        fadeToMusic(bossMusic, fadeOutDuration);
    }

    /**
     * Check apakah sedang dalam music transition
     */
    public static boolean isTransitioning() {
        return isFadingOut || isFadingIn;
    }

    /**
     * Get fade progress (0.0f - 1.0f)
     */
    public static float getFadeProgress() {
        if (isFadingOut || isFadingIn) {
            return Math.min(1f, fadeTimer / fadeDuration);
        }
        return 0f;
    }


    /**
     * Shutdown AudioManager - dispose semua audio resources
     * Dipanggil saat game shutdown untuk prevent memory leaks
     */
    public static void shutdown() {
        if (!isInitialized) {
            System.out.println("⚠️ AudioManager: Not initialized, nothing to shutdown");
            return;
        }

        System.out.println("🔄 AudioManager: Shutting down audio system...");

        try {
            disposeSoundSafe(buildingSound, "Building Sound");
            buildingSound = null;

            disposeSoundSafe(trapSound, "Trap Sound");
            trapSound = null;

            disposeSoundSafe(Paperbtn, "Navbar Select Sound");
            Paperbtn = null;

            disposeSoundSafe(towerShootSound, "Tower Shoot Sound");
            towerShootSound = null;

            disposeSoundSafe(aoeShootSound, "AOE Shoot Sound");
            aoeShootSound = null;

            disposeSoundSafe(explosionSound, "Explosion Sound");
            explosionSound = null;

            disposeSoundSafe(hitSound, "Hit Sound");
            hitSound = null;

            disposeSoundSafe(enemyDeathSound, "Enemy Death Sound");
            enemyDeathSound = null;

            disposeMusicSafe(mainMenuMusic, "Main Menu Music");
            mainMenuMusic = null;

            disposeMusicSafe(towerDefenseMusic, "Tower Defense Music");
            towerDefenseMusic = null;

            disposeMusicSafe(bossMusic, "Boss Music");
            bossMusic = null;

            isInitialized = false;
            System.out.println("✅ AudioManager: Audio system shutdown successfully!");

        } catch (Exception e) {
            System.err.println("❌ AudioManager: Error during shutdown - " + e.getMessage());
        }
    }

    /**
     * Safe sound disposal dengan error handling
     * @param sound Sound object yang akan di-dispose
     * @param soundName Nama sound untuk logging
     */
    private static void disposeSoundSafe(Sound sound, String soundName) {
        if (sound != null) {
            try {
                sound.dispose();
                System.out.println("✅ Disposed: " + soundName);
            } catch (Exception e) {
                System.err.println("❌ Error disposing " + soundName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Safe music disposal dengan error handling
     * @param music Music object yang akan di-dispose
     * @param musicName Nama music untuk logging
     */
    private static void disposeMusicSafe(Music music, String musicName) {
        if (music != null) {
            try {
                if (music.isPlaying()) {
                    music.stop();
                }
                music.dispose();
                System.out.println("✅ Disposed: " + musicName);
            } catch (Exception e) {
                System.err.println("❌ Error disposing " + musicName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Check apakah AudioManager sudah diinisialisasi
     * @return true jika sudah initialized
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Test method untuk check apakah semua sounds loaded dengan benar
     */
    public static void testAllSounds() {
        System.out.println("🎵 AudioManager: Testing loaded sounds...");

        if (buildingSound != null) {
            System.out.println("✅ Building sound: LOADED");
        } else {
            System.out.println("❌ Building sound: NULL");
        }

        if (trapSound != null) {
            System.out.println("✅ Trap sound: LOADED");
        } else {
            System.out.println("❌ Trap sound: NULL");
        }

        // Test optional sounds
        System.out.println("ℹ️ Shoot sound: " + (shootSound != null ? "LOADED" : "NULL"));
        System.out.println("ℹ️ Explosion sound: " + (explosionSound != null ? "LOADED" : "NULL"));
        System.out.println("ℹ️ Hit sound: " + (hitSound != null ? "LOADED" : "NULL"));

        System.out.println("🎵 Sound test complete. Initialization status: " + (isInitialized ? "READY" : "NOT READY"));
    }


}
