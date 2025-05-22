package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Tower {
    public final boolean canShoot;
    public final boolean isMain;    // ← baru
    private int health;
    private final float scale;
    private final Texture towerTex, projTex;
    private final float scaledW, scaledH;
    public final float x, y;

    private float cooldown = 0f;
    private final float fireRate = 5f;
    private final float range    = 1280f;

    /**
     * @param towerTex    texture tower
     * @param projTex     texture projectile
     * @param xCenter     posisi X center
     * @param yCenter     posisi Y center
     * @param scale       skala sprite
     * @param canShoot    apakah tower ini bisa menembak
     * @param initialHealth  nilai HP awal
     */
    public Tower(Texture towerTex,
                 Texture projTex,
                 float xCenter,
                 float yCenter,
                 float scale,
                 boolean canShoot,
                 boolean isMain,        // ← tambahkan
                 int initialHealth)
    {
        this.towerTex = towerTex;
        this.projTex  = projTex;
        this.scale    = scale;
        this.canShoot = canShoot;
        this.health   = initialHealth;  // now explicit
        this.isMain   = isMain;          // ← set
        this.scaledW = towerTex.getWidth()  * scale;
        this.scaledH = towerTex.getHeight() * scale;
        this.x = xCenter;
        this.y = yCenter;
    }

    /** Dikurangi HP-nya */
    public void takeDamage(int dmg) {
        health -= dmg;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public void update(float delta, Array<Enemy> enemies, Array<Projectile> projs) {
        if (!canShoot || isDestroyed()) return;
        cooldown -= delta;
        if (cooldown <= 0f && !enemies.isEmpty()) {
            Enemy e = enemies.first();
            float ey = e.getBounds().y + e.getBounds().height/2f;
            projs.add(new Projectile(projTex, x, y, e.getX(), ey));
            cooldown = fireRate;
        }
    }

    public void drawBatch(SpriteBatch batch) {
        batch.draw(towerTex, x - scaledW/2, y - scaledH/2, scaledW, scaledH);
    }

    public void drawShape(ShapeRenderer shapes) {
        if (towerTex == null) {
            shapes.setColor(canShoot ? Color.CYAN : Color.BLUE);
            shapes.rect(x - scaledW/2, y - scaledH/2, scaledW, scaledH);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x - scaledW/2, y - scaledH/2, scaledW, scaledH);
    }

    public int getHealth() {
        return health;
    }
}
