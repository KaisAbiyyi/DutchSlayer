// File: io/DutchSlayer/defend/utils/TDConstants.java
package io.DutchSlayer.defend.utils;

// Diadaptasi dari Constant.java dan nilai-nilai di TowerDefenseScreen.java asli Anda
public class TDConstants {
    // Screen (dari Constant.java Anda, jika relevan untuk TD, atau dari TDS asli)
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    // Game Logic (dari TowerDefenseScreen.java asli)
    public static final float GROUND_Y = 150f;
    public static final int MAX_WAVE = 3;
    public static final int FINAL_STAGE_NUMBER = 4;
    public static final float ZONE_OFFSET_Y = 20f;

    // UI Navbar (dari TowerDefenseScreen.java asli)
    public static final float NAVBAR_HEIGHT = 80f;
    public static final float NAVBAR_ITEM_BUTTON_SIZE = 80f; // Ukuran tombol deploy di navbar (navTowerX/W)
    public static final float NAVBAR_ITEM_BUTTON_SPACING = 18f;
    public static final float NAVBAR_ACTION_BUTTON_SIZE = 60f; // Untuk Pause, Remove

    // UI Panel (dari TowerDefenseScreen.java asli)
    public static final float UPGRADE_PANEL_WIDTH = 160f;
    public static final float UPGRADE_PANEL_HEIGHT = 160f; // Disesuaikan dari 100f di TDS untuk 3 tombol
    public static final float UPGRADE_PANEL_MARGIN = 8f;
    public static final float UPGRADE_PANEL_BUTTON_HEIGHT = 24f;

    public static final float PAUSE_MENU_WIDTH = 200f; // PAUSE_W di TDS
    public static final float PAUSE_MENU_HEIGHT = 120f; // PAUSE_H di TDS
    public static final float PAUSE_MENU_BUTTON_HEIGHT = 30f; // Dari logika TDS

    public static final float END_SCREEN_UI_WIDTH = 800f;
    public static final float END_SCREEN_UI_HEIGHT = 600f;
    public static final float END_SCREEN_BUTTON_WIDTH = 300f;
    public static final float END_SCREEN_BUTTON_HEIGHT = 100f;

    // Biaya (dari TowerDefenseScreen.java asli)
    public static final int TOWER_AOE_COST = 50;  // TOWER1_COST
    public static final int TOWER_FAST_COST = 30; // TOWER2_COST
    public static final int TOWER_SLOW_COST = 40; // TOWER3_COST

    public static final int TRAP_ATTACK_COST = 15;
    public static final int TRAP_SLOW_COST = 20;
    public static final int TRAP_EXPLOSION_COST = 25;
    public static final int TRAP_DEFAULT_COST = 10; // TRAP_COST di TDS

    public static final int BASE_ATTACK_UPGRADE_COST = 20;
    public static final int BASE_DEFENSE_UPGRADE_COST = 15;
    public static final int BASE_SPEED_UPGRADE_COST = 25;
    public static final float UPGRADE_COST_MULTIPLIER = 1.5f;

    // Cooldowns (dari TowerDefenseScreen.java asli)
    public static final float[] TOWER_MAX_COOLDOWNS = {3f, 2f, 4f}; // AOE, Fast, Slow (T1, T2, T3)
    public static final float[] TRAP_MAX_COOLDOWNS = {2f, 3f, 5f}; // Attack, Slow, Explosion (TRAP1, TRAP2, TRAP3)

    // Wave System (dari TowerDefenseScreen.java asli)
    public static final float ENEMY_SPAWN_INTERVAL = 2f; // spawnTimer > 2f di TDS
    public static final float WAVE_TRANSITION_DELAY = 3f;
    public static final float INCOME_INTERVAL = 2f;
    public static final int INCOME_AMOUNT = 5;

    // Zone Sizing & Positioning (dari TowerDefenseScreen.java asli)
    public static final float ZONE_TOWER_WIDTH_FACTOR = 0.2f; // Berdasarkan ImageLoader.towerTex.getWidth() * 0.2f
    public static final float ZONE_TOWER_HEIGHT_FACTOR = 0.1f;
    public static final float ZONE_TRAP_WIDTH_FACTOR = 0.2f;
    public static final float ZONE_TRAP_HEIGHT_FACTOR = 0.1f;
    public static final float ZONE_SKEW = 50f;
    public static final float ZONE_SPACING = 150f;
    public static final float FIRST_TOWER_CENTER_X_CALC = 100f + ZONE_SPACING; // firstCenter di TDS

    // Probabilitas Spawn Musuh (dari TowerDefenseScreen.java asli)
    public static final float BASIC_SPAWN_CHANCE = 0.4f;
    public static final float SHOOTER_SPAWN_CHANCE = 0.2f;
    public static final float BOMBER_SPAWN_CHANCE = 0.15f;
    public static final float SHIELD_SPAWN_CHANCE = 0.2f;
    public static final float BOSS_SPAWN_CHANCE = 0.05f; // Digunakan secara kondisional di TDS
}
