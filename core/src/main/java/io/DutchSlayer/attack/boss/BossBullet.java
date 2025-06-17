package io.DutchSlayer.attack.boss;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion; // Import TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.DutchSlayer.utils.Constant;

public class BossBullet {

    private float x, y;
    private final float speed = 400f;
    private float width = 40f;
    private float height = 40f;
    private boolean isAlive = true;
    private float vx;
    private Texture bulletFullTexture; // Texture asli yang dimuat
    private TextureRegion bulletRegion; // TextureRegion yang akan digambar dan diflip
    private boolean facingRight; // Menandakan apakah peluru harus menghadap kanan

    public BossBullet(float startX, float startY, float directionX, boolean bossFacingRight) { // Tambahkan bossFacingRight
        this.x = startX;
        this.y = startY;
        this.vx = 400f * directionX;
        this.facingRight = bossFacingRight; // Simpan arah hadap boss saat peluru ditembakkan

        this.bulletFullTexture = new Texture("boss/boss_bullet.png");
        this.bulletRegion = new TextureRegion(bulletFullTexture); // Buat TextureRegion dari Texture

        // Sesuaikan flipping awal berdasarkan arah hadap boss
        // Asumsi: Gambar aset default peluru menghadap KIRI (jika ada arah) atau netral.
        // Jika peluru harus menghadap kanan DAN saat ini belum ter-flip, flip.
        if (facingRight && !bulletRegion.isFlipX()) {
            bulletRegion.flip(true, false);
        }
        // Jika peluru harus menghadap kiri DAN saat ini ter-flip, flip kembali.
        else if (!facingRight && bulletRegion.isFlipX()) {
            bulletRegion.flip(true, false);
        }

        // Ukuran peluru tetap 40f
    }

    public void update(float delta) {
        x += vx * delta;
        if (x + width < 0 || x > Constant.MAP_WIDTH) {
            isAlive = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (!isAlive) return;
        // Gambar TextureRegion yang sudah diflip
        batch.draw(bulletRegion, x, y, width, height);
    }

    public void render(ShapeRenderer renderer) {
        if (!isAlive) return;
        renderer.rect(x, y, width, height);
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void kill() {
        isAlive = false;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void dispose() {
        if (bulletFullTexture != null) {
            bulletFullTexture.dispose(); // Dispose Texture asli
        }
    }
}
