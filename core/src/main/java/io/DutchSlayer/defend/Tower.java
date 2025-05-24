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
    public final float scaledW;
    public final float scaledH;
    public final float x, y;

    private float cooldown = 0f;
//    private float fireRate = 5f;
    private final float range    = 1280f;
    private int upgradeLevel = 0;
    private static final int MAX_UPGRADE_LEVEL = 10;
    private int attackPower = 1;
    private int defensePower = 0;

    private TowerType type = null;
    private float fireRate;
    private int   damage;
    private float slowDuration;       // hanya untuk SLOW

    private float projScale;



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
                 TowerType type,
                 int initialHealth,
                 float projScale)
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
        this.projScale = projScale;

        this.type = type;
        switch(type) {
            case BASIC:
                this.fireRate = 5f;
                this.damage   = 1;
                break;
            case AOE:
                this.fireRate    = 4f;
                this.damage      = 2;
                break;
            case FAST:
                this.fireRate    = 0.5f;
                this.damage      = 1;
                break;
            case SLOW:
                this.fireRate    = 1.5f;
                this.damage      = 0;
                this.slowDuration= 2f;
                break;
        }
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
        if (cooldown > 0) return;
        if (enemies.isEmpty()) return;

        Enemy target = enemies.first();
        float ty = target.getBounds().y + target.getBounds().height/2f;

        switch(type) {
            case BASIC:
                projs.add(new Projectile(
                    projTex, x, y, target.getX(), ty, projScale
                ));
                break;
            case AOE:
                projs.add(new AoeProjectile(
                    projTex, x, y, target.getX(), ty,
                    damage, 100f, projScale         // radius 100
                ));
                break;
            case FAST:
                projs.add(new Projectile(
                    projTex, x, y, target.getX(), ty, projScale
                ));
                break;
            case SLOW:
                projs.add(new SlowProjectile(
                    projTex, x, y, target.getX(), ty,
                    slowDuration, projScale
                ));
                break;
        }

        cooldown = fireRate;
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

    public void upgradeAttack() {
        if (upgradeLevel < MAX_UPGRADE_LEVEL) {
            attackPower += 1;
            upgradeLevel++;
        }
    }

    public void upgradeDefense() {
        if (upgradeLevel < MAX_UPGRADE_LEVEL) {
            defensePower += 1;
            upgradeLevel++;
        }
    }

    public void upgradeSpeed() {
        if (upgradeLevel < MAX_UPGRADE_LEVEL) {
            fireRate = Math.max(0.1f, fireRate - 0.5f);
            upgradeLevel++;
        }
    }

    public String getUpgradeLevel() {
        return String.valueOf(upgradeLevel);
    }

    public int getAttackLevel() {
        return attackPower;
    }

    public int getDefenseLevel() {
        return health;
    }

    public int getSpeedLevel() {
        return (int) fireRate;
    }

    public String getUpgradeRemaining() {
        return String.valueOf(upgradeLevel);
    }
}
