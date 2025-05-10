package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Tower {
    public final boolean canShoot;
    private int health;
    private final float scale;
    private final Texture towerTex, projTex;
    private final float scaledW, scaledH;
    public final float x, y;

    private float cooldown = 0f;
    private final float fireRate = 6f;   // 3x per detik
    private final float range    = 1280f;   // melihat seluruh layar

    public Tower(Texture towerTex,
                 Texture projTex,
                 float xCenter,
                 float yCenter,
                 float scale,
                 boolean canShoot)
    {
        this.towerTex = towerTex;
        this.projTex  = projTex;
        this.scale    = scale;
        this.canShoot = canShoot;

        // HP: 10 untuk tower utama, 5 untuk lainnya
        this.health = canShoot ? 10 : 5;

        this.scaledW = towerTex.getWidth()  * scale;
        this.scaledH = towerTex.getHeight() * scale;
        this.x = xCenter;
        this.y = yCenter;
    }

    /** Dikurang 1 setiap kali diserang. */
    public void takeDamage(int dmg) {
        health -= dmg;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    /** Hanya menembak jika canShoot==true dan cooldown sudah 0. */
    public void update(float delta, Array<Enemy> enemies, Array<Projectile> projs) {
        if (!canShoot || isDestroyed()) return;
        cooldown -= delta;
        if (cooldown <= 0f) {
            for (Enemy e : enemies) {
                float ey = e.getBounds().y + e.getBounds().height/2f;
                // hilangkan cek range kalau mau selalu nembak
                projs.add(new Projectile(projTex, x, y, e.getX(), ey));
                cooldown = fireRate;
                break;
            }
        }
    }

    public void drawBatch(SpriteBatch batch) {
        batch.draw(
            towerTex,
            x - scaledW/2,
            y - scaledH/2,
            scaledW,
            scaledH
        );
    }

    public void drawShape(ShapeRenderer shapes) {
        // hanya fallback bila texture null
        if (towerTex == null) {
            shapes.setColor(canShoot ? Color.CYAN : Color.BLUE);
            shapes.rect(
                x - scaledW/2,
                y - scaledH/2,
                scaledW,
                scaledH
            );
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(
            x - scaledW/2,
            y - scaledH/2,
            scaledW,
            scaledH
        );
    }

    /** Untuk menampilkan HP (opsional) */
    public int getHealth() {
        return health;
    }
}
