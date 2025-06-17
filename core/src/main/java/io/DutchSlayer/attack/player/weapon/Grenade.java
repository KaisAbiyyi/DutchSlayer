package io.DutchSlayer.attack.player.weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.DutchSlayer.utils.Constant;

public class Grenade {

    private transient Texture grenadeTexture;
    private transient Texture explosionTexture;
    private final Sound explosionSound;
    private final Sound groundHitSound;

    private float width = 24f;  // Lebar sprite granat
    private float height = 24f;

    private float x;
    private float y;
    private float vx;
    private float vy;

    // Perbesar radius ledakan untuk memastikan mengenai musuh
    private final float radius = Constant.GRENADE_RADIUS * 1.5f;
    private final float gravity = -1500f;
    private final float explosionDelay = Constant.GRENADE_TIMER;
    private final float explosionDuration = 0.4f;

    private float timer;
    private float postExplodeTimer = 0f;
    private long groundHitSoundId = -1;

    // Tingkatkan damage untuk memastikan musuh menerima damage yang cukup
    private final int damage = 25;
    private boolean exploded = false;
    private boolean isAlive = true;
    private boolean hasDealtDamage = false;
    private boolean hasTouchedGround = false;
    private boolean isEnemyGrenade;

    private Float impactY = null;

    public Grenade(float startX, float startY, float angleRad, float power, boolean isEnemyGrenade, Texture grenadeTexture, Texture explosionTexture) { // Parameter Sound dihapus
        this.x = startX;
        this.y = startY;
        this.vx = MathUtils.cos(angleRad) * power;
        this.vy = MathUtils.sin(angleRad) * power;
        this.timer = explosionDelay;
        this.isEnemyGrenade = isEnemyGrenade;
        this.grenadeTexture = grenadeTexture;
        this.explosionTexture = explosionTexture;

        // --- BARU: Memuat suara di dalam konstruktor ---
        this.explosionSound = Gdx.audio.newSound(Gdx.files.internal("player/grenade.mp3"));
        this.groundHitSound = Gdx.audio.newSound(Gdx.files.internal("player/grenade_ground.mp3"));
    }

    public boolean isEnemyGrenade() {
        return isEnemyGrenade;
    }

    public void update(float delta) {
        if (!exploded) {
            // Efek gravitasi
            vy += gravity * delta;

            // Update posisi
            x += vx * delta;
            y += vy * delta;

            // Jika menyentuh tanah
            if (y <= Constant.TERRAIN_HEIGHT) {
                y = Constant.TERRAIN_HEIGHT;

                if (!hasTouchedGround) {
                    hasTouchedGround = true;
                    vy = 0f;
                    // Mainkan suara benturan tanah HANYA saat pertama kali menyentuh
                    if (groundHitSound != null) {
                        groundHitSound.play(0.5f);
                    }
                }

                vx *= 0.9f; // Sliding/friksi
                if (Math.abs(vx) < 5f) vx = 0f;
            }

            // Hitung mundur timer ledakan
            timer -= delta;
            if (timer <= 0f) {
                explode();
            }
        } else {
            // Setelah meledak
            postExplodeTimer -= delta;
            if (postExplodeTimer <= 0f) {
                isAlive = false;
            }
        }
    }

    public void updatePhysics() {
        if (!exploded) {
            // Efek gravitasi
            vy += gravity * Gdx.graphics.getDeltaTime();

            // Update posisi
            x += vx * Gdx.graphics.getDeltaTime();
            y += vy * Gdx.graphics.getDeltaTime();

            // Jika menyentuh tanah
            if (y <= Constant.TERRAIN_HEIGHT) {
                y = Constant.TERRAIN_HEIGHT;

                if (!hasTouchedGround) {
                    hasTouchedGround = true;
                    vy = 0f;
                }

                vx *= 0.9f; // Sliding/friksi
                if (Math.abs(vx) < 5f) vx = 0f;
            }

            // Hitung mundur timer ledakan
            timer -= Gdx.graphics.getDeltaTime();
            if (timer <= 0f) {
                explode();
            }

        } else {
            // Setelah meledak, hitung waktu habisnya efek
            postExplodeTimer -= Gdx.graphics.getDeltaTime();
            if (postExplodeTimer <= 0f) {
                isAlive = false;
            }
        }
    }


    public void render(SpriteBatch spriteBatch) {
        if (!isAlive) return;

        if (!exploded) {
            // Gambar sprite granat sebelum meledak (tidak ada perubahan di sini)
            if (grenadeTexture != null) {
                spriteBatch.draw(grenadeTexture, x - width / 2f, y, width, height);
            }
        } else {
            // Gambar sprite ledakan setelah meledak
            if (explosionTexture != null) {
                float explosionDiameter = radius * 2;
                float renderX = x - radius; // Posisi X tetap di tengah
                float renderY;

                // --- PERUBAHAN LOGIKA DIMULAI DI SINI ---

                // Jika granat sudah menyentuh tanah, paksa posisi Y ledakan ke atas permukaan tanah.
                if (impactY != null) {
                    // KASUS 1: Ledakan karena benturan langsung.
                    // Gunakan posisi impact, tapi tidak boleh lebih rendah dari tanah.
                    renderY = Math.max(impactY, Constant.TERRAIN_HEIGHT) - 20f;
                    renderX += 20f;
                } else if (hasTouchedGround) {
                    // KASUS 2: Ledakan karena timer di tanah.
                    renderY = Constant.TERRAIN_HEIGHT;
                } else {
                    // KASUS 3: Ledakan karena timer di udara.
                    renderY = y - radius;
                }

                // --- AKHIR LOGIKA BARU ---

                // Gambar ledakan di posisi yang sudah disesuaikan
                spriteBatch.draw(explosionTexture, renderX, renderY, explosionDiameter, explosionDiameter);
            }
        }
    }

    public void forceExplode(float impactY) { // <-- MENJADI SEPERTI INI
        if (!exploded) {
            // System.out.println("Forcing grenade to explode!"); // Debug
            this.impactY = impactY; // <-- BARU: Simpan posisi Y saat impact
            explode();
        }
    }

    private void explode() {
        exploded = true;
        timer = 0f;
        postExplodeTimer = explosionDuration;

        if (groundHitSound != null && groundHitSoundId != -1) {
            groundHitSound.stop(groundHitSoundId);
            groundHitSoundId = -1; // Reset ID agar tidak dihentikan lagi
        }

        // Logika play sound tidak berubah
        if (explosionSound != null) {
            explosionSound.play(0.7f);
        }
    }

    public boolean isFinished() {
        return exploded && postExplodeTimer <= 0f;
    }

    public boolean isExploded() {
        return exploded;
    }

    public boolean shouldDealDamage() {
        return exploded && !hasDealtDamage;
    }

    public void markDamageDealt() {
        hasDealtDamage = true;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getDamage() {
        return damage;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void dispose() {
        if (explosionSound != null) {
            explosionSound.dispose();
        }
    }

    public float getRadius() {
        return radius;
    }
}
