package io.DutchSlayer.defend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

/**
 * AudioManager untuk handle semua sound effects dalam game
 * Mengatur loading, playing, dan disposal audio resources
 */
public class AudioManager {
    /* ===== DEPLOYMENT SOUNDS ===== */
    private static Sound buildingSound;     // SFX untuk deploy tower
    private static Sound trapSound;         // SFX untuk deploy trap

    /* ===== COMBAT SOUNDS ===== */
    private static Sound shootSound;        // SFX untuk tower shooting (optional)
    private static Sound explosionSound;    // SFX untuk explosion (optional)
    private static Sound hitSound;          // SFX untuk enemy hit (optional)

    /* ===== VOLUME SETTINGS ===== */
    private static float masterVolume = 1.0f;      // Master volume (0.0f - 1.0f)
    private static float sfxVolume = 0.8f;          // SFX volume (0.0f - 1.0f)

    /* ===== INITIALIZATION STATE ===== */
    private static boolean isInitialized = false;

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

            // ===== LOAD OPTIONAL COMBAT SOUNDS =====
            // Uncomment jika kamu ada file audio ini
            // shootSound = loadSoundSafe("assets/SFX/shoot.mp3", "Shoot Sound");
            // explosionSound = loadSoundSafe("assets/SFX/explosion.mp3", "Explosion Sound");
            // hitSound = loadSoundSafe("assets/SFX/hit.mp3", "Hit Sound");

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
     * Play tower shooting sound (optional)
     */
    public static void playTowerShoot() {
        playSound(shootSound, "Tower Shoot");
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

            disposeSoundSafe(shootSound, "Shoot Sound");
            shootSound = null;

            disposeSoundSafe(explosionSound, "Explosion Sound");
            explosionSound = null;

            disposeSoundSafe(hitSound, "Hit Sound");
            hitSound = null;

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
