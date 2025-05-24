package io.DutchSlayer.defend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

/**
 * ImageLoader bertanggung-jawab memuat semua Texture.
 * Jika loading gagal, Texture akan null dan saat render
 * akan digambar sebagai Shape dengan warna berbeda.
 */
public class ImageLoader {
    public static Texture towerTex;
    public static Texture enemyTex;
    public static Texture projTex;
    public static Texture trapTex;
    public static Texture grassTex;
    public static Texture terratex;
    public static Texture skytex;
    public static Texture dutchtex;
    public static Texture maintowertex;
    public static Texture tower1Tex, tower2Tex, tower3Tex;
    public static Texture aoeProjTex, fastProjTex, slowProjTex;

    /** Panggil sekali di create() sebelum digunakan */
    public static void load() {
        towerTex = loadOrNull("Defend/Tower2.png");
        enemyTex = loadOrNull("Defend/Enemy.png");
        projTex  = loadOrNull("Defend/Projectile.png");
        trapTex = loadOrNull("Defend/Trap.png");
        grassTex = loadOrNull("Defend/Grass1.png");
        terratex = loadOrNull("backgrounds/terrain.png");
        skytex = loadOrNull("backgrounds/Sky.png");
        dutchtex = loadOrNull("Defend/Dutch.png");
        maintowertex = loadOrNull("Defend/MainTower.png");

        tower1Tex = loadOrNull("Defend/TowerAOE.png");
        tower2Tex = loadOrNull("Defend/TowerSpeed.png");
        tower3Tex = loadOrNull("Defend/TowerDefensif.png");

        aoeProjTex = loadOrNull("Defend/ProjectileAOE.png");
        fastProjTex= loadOrNull("Defend/ProjectileSpeed.png");
        slowProjTex= loadOrNull("Defend/ProjectileDefensif.png");

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
    }
}
