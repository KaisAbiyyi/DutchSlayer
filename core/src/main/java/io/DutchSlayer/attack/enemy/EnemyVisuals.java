// File: EnemyVisuals.java
package io.DutchSlayer.attack.enemy; // Sesuaikan dengan struktur paket Anda

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.attack.enemy.fsm.EnemyState; // Import EnemyState

public class EnemyVisuals {

    private final AttackType attackType;

    private Texture idleTexture; // Menggunakan enemy_run1 untuk idle
    private TextureRegion idleFrame;
    private Texture deadTexture;

    private Array<Texture> runTextures; // Untuk menyimpan referensi Texture agar bisa di-dispose
    private Animation<TextureRegion> runAnimation;

    private float stateTime = 0f;

    public EnemyVisuals(AttackType type) {
        this.attackType = type; // Simpan tipe serangannya
        this.runTextures = new Array<>();
        loadAssets();
    }

    private void loadAssets() {
        deadTexture = new Texture(Gdx.files.internal("enemy/enemy_dead.png"));

        Array<TextureRegion> runFrames = new Array<>();

        if (attackType == AttackType.BURST_FIRE) {
            // Aset khusus untuk BURST_FIRE
            // Idle menggunakan frame pertama dari animasi lari
            idleTexture = new Texture(Gdx.files.internal("enemy/enemy_run_ar1.png"));
            idleFrame = new TextureRegion(idleTexture);
            runTextures.add(idleTexture); // Tambahkan ke daftar dispose
            runFrames.add(idleFrame);

            // Memuat sisa frame animasi lari (2 sampai 4)
            for (int i = 2; i <= 4; i++) {
                Texture runTex = new Texture(Gdx.files.internal("enemy/enemy_run_ar" + i + ".png"));
                runTextures.add(runTex);
                runFrames.add(new TextureRegion(runTex));
            }

            // Kecepatan animasi bisa disesuaikan jika perlu
            runAnimation = new Animation<>(0.1f, runFrames, Animation.PlayMode.LOOP);

        } else {
            // Aset default untuk tipe musuh lainnya
            idleTexture = new Texture(Gdx.files.internal("enemy/enemy_run1.png"));
            idleFrame = new TextureRegion(idleTexture);

            // Memuat Texture RUN (enemy_run1.png sampai enemy_run5.png)
            for (int i = 1; i <= 5; i++) {
                Texture runTex = new Texture(Gdx.files.internal("enemy/enemy_run" + i + ".png"));
                // Hindari duplikasi jika idle texture sama dengan frame pertama
                if (i > 1 || !runTex.equals(idleTexture)) {
                    runTextures.add(runTex);
                }
                runFrames.add(new TextureRegion(runTex));
            }
            // Jika idleTexture adalah bagian dari animasi, pastikan sudah ditambahkan
            if (!runTextures.contains(idleTexture, true)) {
                runTextures.add(idleTexture);
            }

            runAnimation = new Animation<>(0.1f, runFrames, Animation.PlayMode.LOOP);
        }
    }

    /**
     * Mendapatkan TextureRegion yang sesuai untuk dirender berdasarkan state musuh.
     *
     * @param currentState State FSM musuh saat ini.
     * @param movingRight  Apakah musuh sedang bergerak ke kanan (untuk flipping).
     * @param deltaTime    Waktu delta dari frame terakhir.
     * @return TextureRegion yang siap dirender.
     */
    public TextureRegion getFrameToRender(EnemyState currentState, boolean movingRight, float deltaTime) {
        // --- MODIFIKASI: Prioritaskan state DYING ---
        if (currentState == EnemyState.DYING) {
            TextureRegion deadRegion = new TextureRegion(deadTexture);
            // Pastikan arah hadapnya benar saat mati
            if (movingRight && !deadRegion.isFlipX()) {
                deadRegion.flip(true, false);
            } else if (!movingRight && deadRegion.isFlipX()) {
                deadRegion.flip(true, false);
            }
            return deadRegion;
        }

        this.stateTime += deltaTime;
        TextureRegion region;

        if (currentState == EnemyState.PATROL || currentState == EnemyState.CHASE) {
            region = runAnimation.getKeyFrame(this.stateTime, true);
        } else {
            region = idleFrame;
        }

        if (movingRight && !region.isFlipX()) {
            region.flip(true, false);
        } else if (!movingRight && region.isFlipX()) {
            region.flip(true, false);
        }

        return region;
    }

    public void dispose() {
        // --- BARU: Jangan lupa dispose tekstur baru ---
        if (deadTexture != null) {
            deadTexture.dispose();
        }
        if (idleTexture != null) {
            idleTexture.dispose();
        }
        for (Texture tex : runTextures) {
            if (tex != null) {
                tex.dispose();
            }
        }
        runTextures.clear();
    }
}
