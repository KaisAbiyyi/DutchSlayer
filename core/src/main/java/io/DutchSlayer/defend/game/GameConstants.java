package io.DutchSlayer.defend.game;

/**
 * Contains all game constants and configuration values
 */
public class GameConstants {
    // Layout constants
    public static final float NAVBAR_HEIGHT = 80f;
    public static final float GROUND_Y = 150f;
    public static final float ZONE_OFFSET_Y = 20f;


    // Trap costs
    public static final int TRAP_COST = 10;
    public static final int TRAP_ATTACK_COST = 15;
    public static final int TRAP_SLOW_COST = 20;
    public static final int TRAP_EXPLOSION_COST = 25;

    // Tower costs
    public static final int TOWER1_COST = 50;  // AOE Tower
    public static final int TOWER2_COST = 30;  // Fast Tower
    public static final int TOWER3_COST = 40;  // Slow Tower

    // Economy
    public static final float INCOME_INTERVAL = 2f;
    public static final int INCOME_AMOUNT = 5;

    // Wave system
    public static final int MAX_WAVE = 3;
    public static final int FINAL_STAGE = 4;
    public static final float WAVE_TRANSITION_DELAY = 3f;

    // Upgrade costs
    public static final int BASE_ATTACK_UPGRADE_COST = 20;
    public static final int BASE_DEFENSE_UPGRADE_COST = 15;
    public static final int BASE_SPEED_UPGRADE_COST = 25;
    public static final float UPGRADE_COST_MULTIPLIER = 1.5f;

    // Wave spawn probabilities
    public static final float BASIC_SPAWN_CHANCE = 0.4f;
    public static final float SHOOTER_SPAWN_CHANCE = 0.2f;
    public static final float BOMBER_SPAWN_CHANCE = 0.15f;
    public static final float SHIELD_SPAWN_CHANCE = 0.2f;
    public static final float BOSS_SPAWN_CHANCE = 0.05f;

    // UI dimensions
    public static final float UI_WIDTH = 800f;
    public static final float UI_HEIGHT = 600f;
    public static final float BUTTON_WIDTH = 300f;
    public static final float BUTTON_HEIGHT = 100f;

    // Pause menu
    public static final float PAUSE_W = 300f;
    public static final float PAUSE_H = 150f;

    // Button dimensions untuk pause menu
    public static final float PAUSE_BUTTON_WIDTH = 50f;
    public static final float PAUSE_BUTTON_HEIGHT = 30f;
    public static final float PAUSE_BUTTON_SPACING = 5f;

    // Boss introduction timing
    public static final float BOSS_ZOOM_IN_DURATION = 1.8f;
    public static final float BOSS_PAUSE_DURATION = 2.5f;
    public static final float BOSS_ZOOM_OUT_DURATION = 1.5f;
    public static final float BOSS_MUSIC_TRANSITION_DELAY = 1f;

    // Navbar items
    public static final String[] NAV_TOWERS = {"Tower1","Tower2","Tower3"};
    public static final String[] NAV_TRAPS = {"TrapAtk","TrapSlow","TrapBomb"};

    // Tower cooldowns
    public static final float[] TOWER_MAX_COOLDOWNS = {3f, 2f, 4f};
    public static final float[] TRAP_MAX_COOLDOWNS = {2f, 3f, 5f};
}
