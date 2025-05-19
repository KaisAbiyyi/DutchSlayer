package io.DutchSlayer.defend.ui;

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

    /** Panggil sekali di create() sebelum digunakan */
    public static void load() {
        towerTex = loadOrNull("Defend/Tower.png");
        enemyTex = loadOrNull("Defend/Enemy.png");
        projTex  = loadOrNull("Defend/Projectile.png");
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
    }
}
