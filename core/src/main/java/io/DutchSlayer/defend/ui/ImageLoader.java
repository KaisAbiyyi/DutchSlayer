package io.DutchSlayer.defend.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Texture;

/**
 * ImageLoader bertanggung-jawab memuat semua Texture.
 * Jika loading gagal, Texture akan null dan saat render
 * akan digambar sebagai Shape dengan warna berbeda.
 */
public class ImageLoader {
    public static Texture grassTex;
    public static Texture terratex;
    public static Texture skytex;

    public static Texture dutchtex;
    public static Texture enemyTex;

    public static Texture towerTex;
    public static Texture maintowertex;
    public static Texture tower1Tex, tower2Tex, tower3Tex;

    public static Texture projTex;
    public static Texture aoeProjTex, fastProjTex, slowProjTex;

    public static Texture trapTex;
    public static Texture trapAttackTex;
    public static Texture trapSlowTex;
    public static Texture trapBombTex;

    public static Texture enemyBasicTex;     // Tentara dengan kapak
    public static Texture enemyShooterTex;   // Enemy dengan senjata
    public static Texture enemyBomberTex;    // Enemy bomb
    public static Texture enemyShieldTex;    // Enemy shield
    public static Texture enemyBossTex;      // Enemy boss
    public static Texture[] enemyBasicFrames = new Texture[4]; // Array untuk 4 frame animasi
    public static Texture[] enemyShieldFrames = new Texture[4]; // Array untuk 4 frame animasi Shield
    public static Texture[] enemyShooterFrames = new Texture[4]; // Array untuk 4 frame animasi Shooter
    public static Texture[] enemyBomberFrames = new Texture[4];  // Array untuk 4 frame animasi Bomber

    public static Texture bombAssetTex;      // Asset bomb yang ditaruh
    public static Texture enemyProjectileTex; // Projectile enemy
    public static Texture removeBtnTex;
    public static Texture hammerCursorTex;
    public static Texture PauseBtntex;

    public static Texture UITowerAOE;
    public static Texture UITowerSpeed;     // ← ADD THIS
    public static Texture UITowerDefensif;  // ← ADD THIS
    public static Texture UITrapAttack;     // ← ADD THIS
    public static Texture UITrapSlow;       // ← ADD THIS
    public static Texture UITrapBomb;       // ← ADD THIS

    public static Texture goldIconTex;

    public static Texture WinUI;
    public static Texture LoseUI;
    public static Texture BtnNext;
    public static Texture BtnMenu;
    public static Texture BtnRetry;

    /** Panggil sekali di create() sebelum digunakan */
    public static void load() {
        grassTex = loadOrNull("Defend/Grass1.png");
        terratex = loadOrNull("backgrounds/terrain.png");
        skytex = loadOrNull("backgrounds/Background1.png");

        enemyTex = loadOrNull("Defend/Enemy/Enemy.png");
        dutchtex = loadOrNull("Defend/Enemy/DutchBasic.png");
        enemyBasicTex = loadOrNull("Defend/Enemy/DutchBasic.png");
        enemyShooterTex = loadOrNull("Defend/Enemy/DutchShooter.png");
        enemyBomberTex = loadOrNull("Defend/Enemy/DutchBomber.png");
        enemyShieldTex = loadOrNull("Defend/Enemy/DutchShield.png");
        enemyBossTex = loadOrNull("Defend/Enemy/DutchBoss.png");
        bombAssetTex = loadOrNull("Defend/Projectile/Bomb.png");
        enemyProjectileTex = loadOrNull("Defend/Projectile/EnemyProjectileSpeed.png");

        enemyBasicFrames[0] = loadOrNull("Defend/Enemy/enemyBasic/EnemyB1.png");
        enemyBasicFrames[1] = loadOrNull("Defend/Enemy/enemyBasic/EnemyB2.png");
        enemyBasicFrames[2] = loadOrNull("Defend/Enemy/enemyBasic/EnemyB3.png");
        enemyBasicFrames[3] = loadOrNull("Defend/Enemy/enemyBasic/EnemyB4.png");

        enemyShieldFrames[0] = loadOrNull("Defend/Enemy/enemyShield/enemyS1.png");
        enemyShieldFrames[1] = loadOrNull("Defend/Enemy/enemyShield/enemyS2.png");
        enemyShieldFrames[2] = loadOrNull("Defend/Enemy/enemyShield/enemyS3.png");
        enemyShieldFrames[3] = loadOrNull("Defend/Enemy/enemyShield/enemyS4.png");

        enemyShooterFrames[0] = loadOrNull("Defend/Enemy/enemyShooter/EnemyST1.png");
        enemyShooterFrames[1] = loadOrNull("Defend/Enemy/enemyShooter/EnemyST2.png");
        enemyShooterFrames[2] = loadOrNull("Defend/Enemy/enemyShooter/EnemyST3.png");
        enemyShooterFrames[3] = loadOrNull("Defend/Enemy/enemyShooter/EnemyST4.png");

        enemyBomberFrames[0] = loadOrNull("Defend/Enemy/enemyBomber/EnemyBM1.png");
        enemyBomberFrames[1] = loadOrNull("Defend/Enemy/enemyBomber/EnemyBM2.png");
        enemyBomberFrames[2] = loadOrNull("Defend/Enemy/enemyBomber/EnemyBM3.png");
        enemyBomberFrames[3] = loadOrNull("Defend/Enemy/enemyBomber/EnemyBM4.png");

        towerTex = loadOrNull("Defend/Tower/Tower2.png");
        maintowertex = loadOrNull("Defend/Tower/MainTower.png");
        tower1Tex = loadOrNull("Defend/Tower/TowerAOE.png");
        tower2Tex = loadOrNull("Defend/Tower/TowerSpeed.png");
        tower3Tex = loadOrNull("Defend/Tower/TowerDefensif.png");

        projTex  = loadOrNull("Defend/Projectile/Projectile.png");
        aoeProjTex = loadOrNull("Defend/Projectile/ProjectileAOE.png");
        fastProjTex= loadOrNull("Defend/Projectile/ProjectileSpeed.png");
        slowProjTex= loadOrNull("Defend/Projectile/ProjectileDefensif.png");

        trapTex = loadOrNull("Defend/Trap/Trap.png");
        trapAttackTex = loadOrNull("Defend/Trap/TrapAttack.png");
        trapSlowTex   = loadOrNull("Defend/Trap/TrapSlow.png");
        trapBombTex   = loadOrNull("Defend/Trap/TrapBomb.png");

        // ===== LOAD CURSOR & UI TEXTURES =====
        hammerCursorTex = loadOrNull("Defend/Kursor/Hammer.png");
        removeBtnTex = loadOrNull("Defend/Button/BtnRemove.png");
        PauseBtntex = loadOrNull("Defend/Button/BtnPause.png");

        UITowerAOE = loadOrNull("Defend/UI/UITowerAOE.png");
        UITowerSpeed = loadOrNull("Defend/UI/UITowerSpeed.png");       // GANTI DENGAN PATH YANG BENAR NANTI
        UITowerDefensif = loadOrNull("Defend/UI/UITowerDefend.png"); // GANTI DENGAN PATH YANG BENAR NANTI
        UITrapAttack = loadOrNull("Defend/UI/UITrapAttack.png");       // GANTI DENGAN PATH YANG BENAR NANTI
        UITrapSlow = loadOrNull("Defend/UI/UITrapSlow.png");           // GANTI DENGAN PATH YANG BENAR NANTI
        UITrapBomb = loadOrNull("Defend/UI/UITrapBomb.png");

        goldIconTex = loadOrNull("Defend/UI/GoldIcon.png");// GANTI DENGAN PATH YANG BENAR NANTI

        WinUI = loadOrNull("Defend/UI/WinUI.png");
        LoseUI = loadOrNull("Defend/UI/LoseUI.png");
        BtnNext = loadOrNull("Defend/UI/BtnNext.png");
        BtnMenu = loadOrNull("Defend/UI/BtnMenu.png");
        BtnRetry = loadOrNull("Defend/UI/BtnRetry.png");
    }

    private static Texture loadOrNull(String path) {
        try {
            return new Texture(Gdx.files.internal(path));
        } catch (Exception e) {
            Gdx.app.error("ImageLoader", "Gagal load "+path+", akan pakai shape", e);
            return null;
        }
    }

    /** Pastikan dipanggil di dispose() */
    public static void dispose() {
        if (towerTex != null) towerTex.dispose();
        if (enemyTex  != null) enemyTex.dispose();
        if (projTex   != null) projTex.dispose();
        if (trapTex != null) trapTex.dispose();
        if (grassTex  != null) grassTex.dispose();
        if (terratex  != null) terratex.dispose();
        if (skytex  != null) skytex.dispose();
        if (dutchtex  != null) dutchtex.dispose();
        if (maintowertex != null) maintowertex.dispose();

        if (tower1Tex != null) tower1Tex.dispose();
        if (tower2Tex != null) tower2Tex.dispose();
        if (tower3Tex != null) tower3Tex.dispose();

        if (aoeProjTex != null) aoeProjTex.dispose();
        if (fastProjTex != null) fastProjTex.dispose();
        if (slowProjTex != null) slowProjTex.dispose();

        if (trapAttackTex != null) trapAttackTex.dispose();
        if (trapSlowTex != null) trapSlowTex.dispose();
        if (trapBombTex != null) trapBombTex.dispose();

        if (hammerCursorTex != null) hammerCursorTex.dispose();
        if (removeBtnTex != null) removeBtnTex.dispose();
        if (PauseBtntex != null) PauseBtntex.dispose();

        if (UITowerAOE != null) UITowerAOE.dispose();
        if (UITowerSpeed != null) UITowerSpeed.dispose();
        if (UITowerDefensif != null) UITowerDefensif.dispose();
        if (UITrapAttack != null) UITrapAttack.dispose();
        if (UITrapSlow != null) UITrapSlow.dispose();
        if (UITrapBomb != null) UITrapBomb.dispose();

        if (WinUI != null) WinUI.dispose();
        if (LoseUI != null) LoseUI.dispose();
        if (BtnNext != null) BtnNext.dispose();
        if (BtnMenu != null) BtnMenu.dispose();
        if (BtnRetry != null) BtnRetry.dispose();

        for (int i = 0; i < enemyBasicFrames.length; i++) {
            if (enemyBasicFrames[i] != null) {
                enemyBasicFrames[i].dispose();
            }
        }

        for (int i = 0; i < enemyShieldFrames.length; i++) {
            if (enemyShieldFrames[i] != null) {
                enemyShieldFrames[i].dispose();
            }
        }

        for (int i = 0; i < enemyShooterFrames.length; i++) {
            if (enemyShooterFrames[i] != null) {
                enemyShooterFrames[i].dispose();
            }
        }

        for (int i = 0; i < enemyBomberFrames.length; i++) {
            if (enemyBomberFrames[i] != null) {
                enemyBomberFrames[i].dispose();
            }
        }
    }
}
