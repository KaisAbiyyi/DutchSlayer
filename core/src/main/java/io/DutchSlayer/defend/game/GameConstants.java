package io.DutchSlayer.defend.game;

/**
 * Contains all game constants and configuration values
 */
public class GameConstants {
    public static final float NAVBAR_HEIGHT = 80f;
    public static final float GROUND_Y = 150f;

    public static final int TRAP_ATTACK_COST = 15;
    public static final int TRAP_SLOW_COST = 20;
    public static final int TRAP_EXPLOSION_COST = 25;

    public static final int TOWER1_COST = 50;
    public static final int TOWER2_COST = 30;
    public static final int TOWER3_COST = 40;

    public static final float INCOME_INTERVAL = 2f;
    public static final int INCOME_AMOUNT = 5;

    public static final int MAX_WAVE = 3;
    public static final int FINAL_STAGE = 4;
    public static final float WAVE_TRANSITION_DELAY = 3f;

    public static final int BASE_ATTACK_UPGRADE_COST = 20;
    public static final int BASE_DEFENSE_UPGRADE_COST = 15;
    public static final int BASE_SPEED_UPGRADE_COST = 25;
    public static final float UPGRADE_COST_MULTIPLIER = 1.5f;

    public static final float BASIC_SPAWN_CHANCE = 0.4f;
    public static final float SHOOTER_SPAWN_CHANCE = 0.2f;
    public static final float BOMBER_SPAWN_CHANCE = 0.15f;

    public static final float UI_HEIGHT = 600f;
    public static final String[] NAV_TOWERS = {"Tower1","Tower2","Tower3"};
    public static final String[] NAV_TRAPS = {"TrapAtk","TrapSlow","TrapBomb"};

    public static final float[] TOWER_MAX_COOLDOWNS = {3f, 2f, 4f};
    public static final float[] TRAP_MAX_COOLDOWNS = {2f, 3f, 5f};

    public static final int TRAP_ATTACK_INDEX = 0;
    public static final int TRAP_SLOW_INDEX = 1;
    public static final int TRAP_EXPLOSION_INDEX = 2;
}
