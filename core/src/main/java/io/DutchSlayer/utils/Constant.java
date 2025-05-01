package io.DutchSlayer.utils;

public class Constant {

    // === Screen ===
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    // === Player ===
    public static final float PLAYER_WIDTH = 56f;
    public static final float PLAYER_HEIGHT = 96f;
    public static final float PLAYER_SPEED = 300f;

    // === Map ===
    public static final float MAP_WIDTH = 3000f;         // Lebar total map
    public static final float WALL_WIDTH = 32f;          // Wall kiri
    public static final float TERRAIN_HEIGHT = 84f;      // Tinggi tanah di bawah

    // === Tree Randomization ===
    public static final int TREE_MIN_COUNT = 12;
    public static final int TREE_MAX_COUNT = 24;
    public static final float TREE_MIN_WIDTH = 50f;
    public static final float TREE_MAX_WIDTH = 100f;
    public static final float TREE_MIN_HEIGHT = 250f;
    public static final float TREE_MAX_HEIGHT = 550f;

    // === Fixed Tree Heights ===
    public static final float TREE1_HEIGHT = 420f; // Oak Tree
    public static final float TREE2_HEIGHT = 500f; // Pine Tree
    public static final float TREE3_HEIGHT = 380f; // Apple Tree
    public static final float TREE4_HEIGHT = 260f; // Small Tree

    // === Bullet ===
    public static final float BULLET_WIDTH = 24f;
    public static final float BULLET_HEIGHT = 10f;
    public static final float BULLET_SPEED = 400f;

    // === Spawn Position ===
    public static final float PLAYER_START_X = 100f;
    public static final float PLAYER_START_Y = 100f;

    // === UI ===
    public static final float BUTTON_WIDTH = 200f;
    public static final float BUTTON_HEIGHT = 50f;
    public static final float UI_PADDING = 20f;

    // === Asset Path ===
    public static final String PLAYER_TEXTURE_PATH = "sprites/player.png";
    public static final String BULLET_TEXTURE_PATH = "sprites/bullet.png";
    public static final String SHOOT_SOUND_PATH = "sounds/shoot.wav";
    public static final String FONT_PATH = "fonts/default.fnt";
    public static final String BG_TREE_PATH = "backgrounds/background_tree.png";
    public static final String TERRAIN_TEXTURE_PATH = "backgrounds/terrain.png";

    // === Tree Textures ===
    public static final String[] TREE_TEXTURE_PATHS = {
        "trees/tree1.png",
        "trees/tree2.png",
        "trees/tree3.png",
        "trees/tree4.png"
    };
}
