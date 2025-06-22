package io.DutchSlayer.defend.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import io.DutchSlayer.defend.entities.towers.TowerType;

public class ImageLoader {
    public static Texture grassTex;
    public static Texture terratex;
    public static Texture skytex;

    public static Texture dutchtex;
    public static Texture enemyTex;

    public static Texture towerTex;
    public static Texture maintowertex;
    public static Texture tower1Tex, tower2Tex, tower3Tex;

    public static Texture[] towerAOEFrames = new Texture[2];
    public static Texture[] towerSpeedFrames = new Texture[2];
    public static Texture[] towerDefensifFrames = new Texture[3];

    public static Texture projTex, projtowtex;
    public static Texture aoeProjTex, fastProjTex, slowProjTex;
    public static Texture bossProjectileTex;

    public static Texture trapTex;
    public static Texture trapAttackTex;
    public static Texture trapSlowTex;
    public static Texture trapBombTex;

    public static Texture enemyBasicTex;
    public static Texture enemyShooterTex;
    public static Texture enemyBomberTex;
    public static Texture enemyShieldTex;
    public static Texture enemyBossTex;
    public static Texture[] enemyBasicFrames = new Texture[4];
    public static Texture[] enemyShieldFrames = new Texture[4];
    public static Texture[] enemyShooterFrames = new Texture[4];
    public static Texture[] enemyBomberFrames = new Texture[4];

    public static Texture bombAssetTex;
    public static Texture enemyProjectileTex;
    public static Texture removeBtnTex;
    public static Texture hammerCursorTex;
    public static Texture PauseBtntex;
    public static Texture explosionTex;

    public static Texture UITowerAOE;
    public static Texture UITowerSpeed;
    public static Texture UITowerDefensif;
    public static Texture UITrapAttack;
    public static Texture UITrapSlow;
    public static Texture UITrapBomb;

    public static Texture goldIconTex;

    public static Texture WinUI;
    public static Texture LoseUI;
    public static Texture BtnNext;
    public static Texture BtnMenu;
    public static Texture BtnRetry;
    public static Texture BtnMode;

    public static Texture PauseUI;
    public static Texture MenuBtn;
    public static Texture ResumeBtn;
    public static Texture SettingBtn;

    public static void load() {
        grassTex = loadOrNull("Defend/Grass1.png");
        terratex = loadOrNull("backgrounds/terrain3.png");
        skytex = loadOrNull("backgrounds/Background1.png");

        enemyTex = loadOrNull("Defend/Enemy/Enemy.png");
        dutchtex = loadOrNull("Defend/Enemy/DutchBasic.png");
        enemyBasicTex = loadOrNull("Defend/Enemy/DutchBasic.png");
        enemyShooterTex = loadOrNull("Defend/Enemy/DutchShooter.png");
        enemyBomberTex = loadOrNull("Defend/Enemy/DutchBomber.png");
        enemyShieldTex = loadOrNull("Defend/Enemy/DutchShield.png");
        enemyBossTex = loadOrNull("Defend/Enemy/DutchBoss.png");
        bombAssetTex = loadOrNull("Defend/Projectile/Bomb.png");
        enemyProjectileTex = loadOrNull("Defend/Projectile/Projectile2.png");
        bossProjectileTex = loadOrNull("Defend/Projectile/BossProjectile.png");

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
        towerAOEFrames[0] = loadOrNull("Defend/Tower/TowerAOE/TowerAOE1.png");
        towerAOEFrames[1] = loadOrNull("Defend/Tower/TowerAOE/TowerAOE2.png");

        towerSpeedFrames[0] = loadOrNull("Defend/Tower/TowerSpeed/TowerSpeed1.png");
        towerSpeedFrames[1] = loadOrNull("Defend/Tower/TowerSpeed/TowerSpeed2.png");

        towerDefensifFrames[0] = loadOrNull("Defend/Tower/TowerDefensif/TowerDefensif1.png");
        towerDefensifFrames[1] = loadOrNull("Defend/Tower/TowerDefensif/TowerDefensif2.png");
        towerDefensifFrames[2] = loadOrNull("Defend/Tower/TowerDefensif/TowerDefensif3.png");

        projTex  = loadOrNull("Defend/Projectile/Projectile2.png");
        projtowtex = loadOrNull("Defend/Projectile/Projectile1.png");
        aoeProjTex = loadOrNull("Defend/Projectile/ProjectileAOE.png");
        fastProjTex= loadOrNull("Defend/Projectile/ProjectileSpeed.png");
        slowProjTex= loadOrNull("Defend/Projectile/ProjectileDefensif.png");
        explosionTex = loadOrNull("Defend/Explosion.png");

        trapTex = loadOrNull("Defend/Trap/Trap.png");
        trapAttackTex = loadOrNull("Defend/Trap/TrapAttack.png");
        trapSlowTex   = loadOrNull("Defend/Trap/TrapSlow.png");
        trapBombTex   = loadOrNull("Defend/Trap/TrapBomb.png");

        removeBtnTex = loadOrNull("Defend/Button/BtnRemove.png");
        PauseBtntex = loadOrNull("Defend/Button/BtnPause.png");

        UITowerAOE = loadOrNull("Defend/UI/UITowerAOE.png");
        UITowerSpeed = loadOrNull("Defend/UI/UITowerSpeed.png");
        UITowerDefensif = loadOrNull("Defend/UI/UITowerDefend.png");
        UITrapAttack = loadOrNull("Defend/UI/UITrapAttack.png");
        UITrapSlow = loadOrNull("Defend/UI/UITrapSlow.png");
        UITrapBomb = loadOrNull("Defend/UI/UITrapBomb.png");

        goldIconTex = loadOrNull("Defend/UI/GoldIcon.png");

        WinUI = loadOrNull("Defend/UI/LoseAndWin/WinUI.png");
        LoseUI = loadOrNull("Defend/UI/LoseAndWin/LoseUI.png");
        BtnNext = loadOrNull("Defend/UI/LoseAndWin/BtnNext.png");
        BtnMenu = loadOrNull("Defend/UI/LoseAndWin/BtnMenu.png");
        BtnRetry = loadOrNull("Defend/UI/LoseAndWin/BtnRetry.png");
        BtnMode = loadOrNull("Defend/UI/LoseAndWin/ModeSelection.png");

        PauseUI = loadOrNull("Defend/UI/PauseUI/PauseUI.png");
        MenuBtn = loadOrNull("Defend/UI/PauseUI/MenuBtn.png");
        ResumeBtn = loadOrNull("Defend/UI/PauseUI/ResumeBtn.png");
        SettingBtn = loadOrNull("Defend/UI/PauseUI/SettingBtn.png");
    }

    private static Texture loadOrNull(String path) {
        try {
            return new Texture(Gdx.files.internal(path));
        } catch (Exception e) {
            Gdx.app.error("ImageLoader", "Gagal load "+path+", akan pakai shape", e);
            return null;
        }
    }

    public static Texture[] getTowerAnimationFrames(TowerType type) {
        return switch (type) {
            case AOE -> towerAOEFrames;
            case FAST -> towerSpeedFrames;
            case SLOW -> towerDefensifFrames;
            default -> null;
        };
    }

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
        if (projtowtex != null)  projtowtex.dispose();

        if (trapAttackTex != null) trapAttackTex.dispose();
        if (trapSlowTex != null) trapSlowTex.dispose();
        if (trapBombTex != null) trapBombTex.dispose();
        if (explosionTex != null) explosionTex.dispose();

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

        if (PauseUI != null) PauseUI.dispose();
        if (MenuBtn != null) MenuBtn.dispose();
        if (ResumeBtn != null) ResumeBtn.dispose();
        if (SettingBtn != null) SettingBtn.dispose();
        if (BtnMode != null) BtnMode.dispose();

        for (Texture enemyBasicFrame : enemyBasicFrames) {
            if (enemyBasicFrame != null) {
                enemyBasicFrame.dispose();
            }
        }

        for (Texture enemyShieldFrame : enemyShieldFrames) {
            if (enemyShieldFrame != null) {
                enemyShieldFrame.dispose();
            }
        }

        for (Texture enemyShooterFrame : enemyShooterFrames) {
            if (enemyShooterFrame != null) {
                enemyShooterFrame.dispose();
            }
        }

        for (Texture enemyBomberFrame : enemyBomberFrames) {
            if (enemyBomberFrame != null) {
                enemyBomberFrame.dispose();
            }
        }

        for (Texture towerAOEFrame : towerAOEFrames) {
            if (towerAOEFrame != null) {
                towerAOEFrame.dispose();
            }
        }

        for (Texture towerSpeedFrame : towerSpeedFrames) {
            if (towerSpeedFrame != null) {
                towerSpeedFrame.dispose();
            }
        }

        for (Texture towerDefensifFrame : towerDefensifFrames) {
            if (towerDefensifFrame != null) {
                towerDefensifFrame.dispose();
            }
        }
    }
}
