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
    // PERBAIKAN: Sistem upgrade yang fleksibel
    private int totalUpgradeCount = 0;              // Total upgrade yang sudah dilakukan
    private static final int MAX_TOTAL_UPGRADES = 10;  // Maksimal 10 upgrade total

    // Level individual untuk setiap stat
    private int attackLevel = 0;     // Level attack (0 = base level)
    private int defenseLevel = 0;    // Level defense (0 = base level)
    private int speedLevel = 0;      // Level speed (0 = base level)


    private TowerType type = null;
    private float baseFireRate;      // Fire rate awal (backup)
    private float fireRate;          // Fire rate aktual
    private int baseDamage;          // Damage awal (backup)
    private int damage;              // Damage aktual
    private int baseHealth;          // Health awal (backup)
    private float slowDuration;

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

        // Set base stats berdasarkan tower type
        this.type = type;
        switch(type) {
            case BASIC:
                this.baseFireRate  = 5f;
                this.baseDamage      = 1;
                break;
            case AOE:
                this.baseFireRate    = 4f;
                this.baseDamage         = 2;
                break;
            case FAST:
                this.baseFireRate    = 0.5f;
                this.baseDamage         = 1;
                break;
            case SLOW:
                this.baseFireRate    = 1.5f;
                this.baseDamage         = 0;
                this.slowDuration= 2f;
                break;
        }

        // Initialize current stats = base stats
        this.fireRate = this.baseFireRate;
        this.damage = this.baseDamage;
        this.baseHealth = initialHealth;
        this.health = initialHealth;
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
                    projTex, x, y, target.getX(), ty, projScale, damage
                ));
                break;
            case AOE:
                projs.add(new AoeProjectile(
                    projTex, x, y, target.getX(), ty,
                     400f, projScale, damage         // radius 100
                ));
                break;
            case FAST:
                projs.add(new Projectile(
                    projTex, x, y, target.getX(), ty, projScale, 1000f, damage
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



    public void upgradeAttack() {
        if (totalUpgradeCount < MAX_TOTAL_UPGRADES) {
            attackLevel++;
            totalUpgradeCount++;

            // Increase damage berdasarkan level
            damage = baseDamage + attackLevel;
            System.out.println("Tower Type: " + type);
            System.out.println("Attack Level: " + attackLevel);

//            System.out.println("Attack upgraded! Level: " + attackLevel +
//                ", Damage: " + damage +
//                ", Upgrades left: " + getRemainingUpgrades());
        }
    }

    public void upgradeDefense() {
        if (totalUpgradeCount < MAX_TOTAL_UPGRADES) {
            defenseLevel++;
            totalUpgradeCount++;

            // Increase max health berdasarkan level
            int healthIncrease = defenseLevel * 2;  // +2 HP per level
            int newMaxHealth = baseHealth + healthIncrease;

            // Restore beberapa HP juga (bonus healing)
            health = Math.min(health + 1, newMaxHealth);  // +1 HP heal

            System.out.println("Tower Type: " + type);
            System.out.println("Defense Level: " + defenseLevel);

//            System.out.println("Defense upgraded! Level: " + defenseLevel +
//                ", Health: " + health + "/" + newMaxHealth +
//                ", Upgrades left: " + getRemainingUpgrades());
        }
    }

    public void upgradeSpeed() {
        if (totalUpgradeCount < MAX_TOTAL_UPGRADES) {
            speedLevel++;
            totalUpgradeCount++;

            // Decrease fire rate (faster shooting) berdasarkan level
            float speedBonus = speedLevel * 0.1f;  // -0.1s per level
            fireRate = Math.max(0.05f, baseFireRate - speedBonus);  // Minimum 0.05s

            System.out.println("Tower Type: " + type);
            System.out.println("Speed Level: " + speedLevel);
//            System.out.println("Speed upgraded! Level: " + speedLevel +
//                ", Fire rate: " + fireRate +
//                ", Upgrades left: " + getRemainingUpgrades());
        }
    }

    public int getRemainingUpgrades() {
        return MAX_TOTAL_UPGRADES - totalUpgradeCount;
    }

    public int getAttackLevel() {
        return attackLevel;
    }

    public int getDefenseLevel() {
        return defenseLevel;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public int getTotalUpgradeCount() {
        return totalUpgradeCount;
    }

    public boolean canUpgrade() {
        return totalUpgradeCount < MAX_TOTAL_UPGRADES;
    }

    // Method untuk UI - tampilkan sisa upgrade
    public String getUpgradeRemaining() {
        return String.valueOf(getRemainingUpgrades());
    }

    // BONUS: Method untuk mendapatkan stats aktual
    public int getCurrentDamage() {
        return damage;
    }

    public float getCurrentFireRate() {
        return fireRate;
    }

    public int getMaxHealth() {
        return baseHealth + (defenseLevel * 2);
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

    public String getUpgradeLevel() {
        return String.valueOf(totalUpgradeCount);
    }

}
