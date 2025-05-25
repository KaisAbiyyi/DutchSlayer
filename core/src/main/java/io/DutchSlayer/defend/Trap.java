package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Trap {
    public final Rectangle bounds;
    public boolean occupied = false;
    private final float centerX, centerY;
    private final Texture tex = ImageLoader.trapTex;
    private final float scale;
    private final float w, h;

    // Trap effect properties
    private static final int TRAP_DAMAGE = 1;
    private static final float SLOW_DURATION = 3f;  // 3 detik slow effect
    private float cooldown = 0f;  // Cooldown antar aktivasi trap
    private static final float TRAP_COOLDOWN = 0.5f;  // 0.5 detik cooldown

    private boolean isUsed = false;  // Tambahan: trap sudah digunakan
    private static final boolean SINGLE_USE = true;  // Set true jika mau single-use

    // TAMBAHAN: Simpan vertices untuk perhitungan Y yang sama dengan tower
    private float[] verts;

    // terima verts only untuk hit‐zone, plus scale untuk gambar
    public Trap(float[] verts, float scale) {
        this.verts = verts.clone();
        // hit‐zone calculation
        float minX = Float.MAX_VALUE;
        float minY = Math.min(Math.min(verts[1], verts[3]), Math.min(verts[5], verts[7]));
        float maxX = Float.MIN_VALUE;
        float maxY = Math.max(Math.max(verts[1], verts[3]), Math.max(verts[5], verts[7]));
        for (int i = 0; i < verts.length; i += 2) {
            minX = Math.min(minX, verts[i]);
            maxX = Math.max(maxX, verts[i]);
            minY = Math.min(minY, verts[i+1]);
            maxY = Math.max(maxY, verts[i+1]);
        }
        bounds = new Rectangle(minX, minY, maxX-minX, maxY-minY);

        // simpan center untuk draw, dan scale sprite
        this.centerX = minX + (maxX-minX)/2f;
        this.centerY = minY + (maxY-minY)/2f;
        this.scale   = scale;
        this.w       = tex.getWidth()  * scale;
        this.h       = tex.getHeight() * scale;
    }

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }

    // Method baru: Update trap (handle cooldown)
    public void update(float delta) {
        if (cooldown > 0) {
            cooldown -= delta;
        }
    }

    // Method baru: Aktivasi trap ketika enemy masuk
    public boolean triggerTrap(Array<Enemy> enemies) {
        if (!occupied || cooldown > 0 || (SINGLE_USE && isUsed)) {
            return false;
        }

        boolean trapActivated = false;

        // Cek semua enemy yang masuk area trap
        for (Enemy enemy : enemies) {
            if (bounds.overlaps(enemy.getBounds())) {
                // Beri damage dan slow effect
                enemy.takeDamage(TRAP_DAMAGE);
                enemy.slow(SLOW_DURATION);
                trapActivated = true;

                System.out.println("Trap activated! Enemy damaged and slowed.");
                break;
            }
        }

        if (trapActivated) {
            cooldown = TRAP_COOLDOWN;  // Set cooldown setelah aktivasi

            if (SINGLE_USE) {
                isUsed = true;  // Mark sebagai sudah digunakan
                occupied = false;  // ← PENTING: Set occupied = false agar trap "hilang"
                System.out.println("Trap consumed (single-use)!");
            }
        }

        return trapActivated;
    }

    // Getter untuk cooldown (untuk visual effects)
    public boolean isOnCooldown() {
        return cooldown > 0;
    }

    public boolean isUsed() {
        return SINGLE_USE && isUsed;
    }

    public void drawBatch(SpriteBatch batch) {
        if (occupied && !(SINGLE_USE && isUsed)) {
            // BONUS: Visual effect berdasarkan cooldown
            if (isOnCooldown()) {
                // Trap on cooldown - warna agak redup
                batch.setColor(0.5f, 0.5f, 0.5f, 0.8f);
            } else {
                // Trap ready - warna normal
                batch.setColor(1f, 1f, 1f, 1f);
            }

            // PERBAIKAN: Posisi X dan Y menggunakan formula tower
            float spriteX = centerX - w/2f;
            float spriteY = getTowerAlignedY();  // ← Method khusus untuk alignment

            batch.draw(tex, spriteX, spriteY, w, h);

            // Reset color
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private float getTowerAlignedY() {
        // Formula yang PERSIS SAMA dengan tower deployment di GameScreen:
        // float cy = (z.verts[1] + z.verts[3] + z.verts[5] + z.verts[7]) / 2.3f;

        // verts format: [x0,y0, x1,y1, x2,y2, x3,y3]
        // Indices:       [0,1,  2,3,   4,5,   6,7]
        // Kita ambil semua Y coordinates: verts[1], verts[3], verts[5], verts[7]

        float y0 = verts[1];  // Y coordinate vertex 0
        float y1 = verts[3];  // Y coordinate vertex 1
        float y2 = verts[5];  // Y coordinate vertex 2
        float y3 = verts[7];  // Y coordinate vertex 3

        // Formula PERSIS SAMA dengan tower deployment
        float alignedY = (y0 + y1 + y2 + y3) / 4.4f;

        return alignedY;
    }
}
