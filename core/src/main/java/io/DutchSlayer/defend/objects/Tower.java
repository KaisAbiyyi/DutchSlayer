package io.DutchSlayer.defend.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.enemy.Enemy;

/**
 * Tower class dengan sistem upgrade dan berbagai type tower
 * Support 4 type: BASIC, AOE, FAST, SLOW dengan stats dan projectile berbeda
 */
public class Tower {
    /* ===== TOWER PROPERTIES ===== */
    public final boolean canShoot;          // Apakah tower bisa menembak
    public final boolean isMain;            // Apakah ini main tower (game over jika hancur)
    public final TowerType type;            // Jenis tower (BASIC/AOE/FAST/SLOW)

    /* ===== POSITION & VISUAL ===== */
    public final float x, y;                // Posisi center tower
    public final float scaledW, scaledH;    // Ukuran sprite setelah scaling
    private final float scale;              // Scale factor
    private final Texture towerTex;         // Texture tower
    private final Texture projTex;          // Texture projectile
    private float projScale;                // Scale projectile

    /* ===== COMBAT STATS ===== */
    private int health;                     // HP saat ini
    private int baseHealth;                 // Health awal (backup)
    private int damage;                     // Damage aktual
    private int baseDamage;                 // Damage awal (backup)
    private float fireRate;                 // Fire rate aktual
    private float baseFireRate;             // Fire rate awal (backup)
    private float slowDuration;             // Durasi slow (untuk SLOW tower)

    /* ===== SHOOTING MECHANICS ===== */
    private float cooldown = 0f;            // Cooldown sampai bisa shoot lagi

    /* ===== UPGRADE SYSTEM ===== */
    private int totalUpgradeCount = 0;                  // Total upgrade yang sudah dilakukan
    private static final int MAX_TOTAL_UPGRADES = 10;   // Maksimal 10 upgrade total

    // Level individual untuk setiap stat
    private int attackLevel = 0;     // Level attack (0 = base level)
    private int defenseLevel = 0;    // Level defense (0 = base level)
    private int speedLevel = 0;      // Level speed (0 = base level)

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
                 boolean isMain,
                 TowerType type,
                 int initialHealth,
                 float projScale)
    {
        this.towerTex = towerTex;
        this.projTex = projTex;
        this.scale = scale;
        this.canShoot = canShoot;
        this.isMain = isMain;
        this.type = type;
        this.health = initialHealth;
        this.projScale = projScale;

        // Calculate sprite dimensions
        this.scaledW = towerTex.getWidth()  * scale;
        this.scaledH = towerTex.getHeight() * scale;
        this.x = xCenter;
        this.y = yCenter;

        // ===== SET BASE STATS BERDASARKAN TOWER TYPE =====
        switch(type) {
            case BASIC:
                this.baseFireRate = 5f;     // Lambat
                this.baseDamage = 1;        // Damage rendah
                break;
            case AOE:
                this.baseFireRate = 4f;     // Agak lambat
                this.baseDamage = 2;        // Damage tinggi (AOE)
                break;
            case FAST:
                this.baseFireRate = 0.5f;   // Sangat cepat
                this.baseDamage = 1;        // Damage rendah
                break;
            case SLOW:
                this.baseFireRate = 1.5f;   // Sedang
                this.baseDamage = 0;        // Tidak ada damage (pure utility)
                this.slowDuration= 2f;      // 2 detik slow
                break;
        }

        // Initialize current stats = base stats
        this.fireRate = this.baseFireRate;
        this.damage = this.baseDamage;
        this.baseHealth = initialHealth;
        this.health = initialHealth;
    }

    /**
     * Tower menerima damage
     */
    public void takeDamage(int dmg) {
        health -= dmg;
    }

    /**
     * Check apakah tower sudah hancur
     */
    public boolean isDestroyed() {
        return health <= 0;
    }

    /**
     * Update tower logic setiap frame (handle shooting)
     */
    public void update(float delta, Array<Enemy> enemies, Array<Projectile> projs) {
        if (!canShoot || isDestroyed()) return;

        // Update cooldown
        cooldown -= delta;
        if (cooldown > 0) return;
        if (enemies.isEmpty()) return;

        // Target enemy pertama (simple targeting)
        Enemy target = enemies.first();
        float ty = target.getBounds().y + target.getBounds().height/2f;

        // ===== CREATE PROJECTILE BERDASARKAN TOWER TYPE =====
        switch(type) {
            case BASIC:
                projs.add(new Projectile(
                    projTex, x, y, target.getX(), ty, projScale,500f, damage
                ));
                break;
            case AOE:
                projs.add(new AoeProjectile(
                    projTex, x, y, target.getX(), ty,
                     400f, projScale, damage, 350f
                ));
                break;
            case FAST:
                projs.add(new Projectile(
                    projTex, x, y, target.getX(), ty, projScale, 1200f, damage
                ));
                break;
            case SLOW:
                projs.add(new SlowProjectile(
                    projTex, x, y, target.getX(), ty,
                    slowDuration, projScale, 400f
                ));
                break;
        }

        // Reset cooldown
        cooldown = fireRate;
    }

    /* ===== UPGRADE METHODS ===== */

    /**
     * Upgrade attack (increase damage)
     */
    public boolean upgradeAttack() {
        if (totalUpgradeCount < MAX_TOTAL_UPGRADES) {
            attackLevel++;
            totalUpgradeCount++;

            // Increase damage berdasarkan tower type
            int damageBonus = 1;
            if (type == TowerType.AOE) damageBonus = 2; // AOE gets more damage

            damage = baseDamage + (attackLevel * damageBonus);
            System.out.println("⚔️ Attack upgraded! Level: " + attackLevel + ", Damage: " + damage);
            return true;
        }
        return false;
    }

    /**
     * Upgrade defense (increase max health + heal)
     */
    public boolean upgradeDefense() {
        if (totalUpgradeCount < MAX_TOTAL_UPGRADES) {
            defenseLevel++;
            totalUpgradeCount++;

            // Increase max health dan heal
            int healthIncrease = defenseLevel * 2;
            int newMaxHealth = baseHealth + healthIncrease;

            // Bonus healing - restore 2 HP
            health = Math.min(health + 2, newMaxHealth);
            System.out.println("🛡️ Defense upgraded! Level: " + defenseLevel + ", Health: " + health + "/" + newMaxHealth);
            return true;
        }
        return false;
    }

    /**
     * Upgrade speed (decrease fire rate = faster shooting)
     */
    public boolean upgradeSpeed() {
        if (totalUpgradeCount < MAX_TOTAL_UPGRADES) {
            speedLevel++;
            totalUpgradeCount++;

            // Decrease fire rate berdasarkan tower type
            float speedBonus = 0.15f; // Base speed bonus
            if (type == TowerType.FAST) speedBonus = 0.05f; // Fast tower gets smaller bonus
            if (type == TowerType.SLOW) speedBonus = 0.25f; // Slow tower gets bigger bonus

            fireRate = Math.max(0.05f, baseFireRate - (speedLevel * speedBonus));
            System.out.println("⚡ Speed upgraded! Level: " + speedLevel + ", Fire rate: " + fireRate + "s");
            return true;
        }
        return false;
    }

    // Tambahkan method untuk mendapatkan upgrade info
    public String getUpgradeInfo(String upgradeType) {
        switch(upgradeType.toLowerCase()) {
            case "attack":
                return "DMG: " + damage + " → " + (damage + (type == TowerType.AOE ? 2 : 1));
            case "defense":
                return "HP: " + health + "/" + getMaxHealth() + " → " + (health + 2) + "/" + (getMaxHealth() + 2);
            case "speed":
                float nextFireRate = Math.max(0.05f, fireRate - (type == TowerType.FAST ? 0.05f : 0.15f));
                return "Rate: " + String.format("%.2f", fireRate) + "s → " + String.format("%.2f", nextFireRate) + "s";
            default:
                return "";
        }
    }

    /* ===== UPGRADE COST METHODS ===== */

    /**
     * Calculate attack upgrade cost (exponential scaling)
     */
    public int getAttackUpgradeCost() {
        return (int)(20 * Math.pow(1.5f, attackLevel));
    }

    public int getDefenseUpgradeCost() {
        return (int)(15 * Math.pow(1.5f, defenseLevel));
    }

    public int getSpeedUpgradeCost() {
        return (int) (25 * Math.pow(1.5f, speedLevel));
    }

    /* ===== UPGRADE VALIDATION METHODS ===== */

    // Method untuk cek apakah bisa upgrade (dengan cost)
    public boolean canUpgradeAttack(int currentGold) {
        return totalUpgradeCount < MAX_TOTAL_UPGRADES && currentGold >= getAttackUpgradeCost();
    }

    public boolean canUpgradeDefense(int currentGold) {
        return totalUpgradeCount < MAX_TOTAL_UPGRADES && currentGold >= getDefenseUpgradeCost();
    }

    public boolean canUpgradeSpeed(int currentGold) {
        return totalUpgradeCount < MAX_TOTAL_UPGRADES && currentGold >= getSpeedUpgradeCost();
    }

    /**
     * Render tower menggunakan SpriteBatch
     */
    public void drawBatch(SpriteBatch batch) {
        batch.draw(towerTex, x - scaledW/2, y - scaledH/2, scaledW, scaledH);
    }

    /**
     * Render tower menggunakan ShapeRenderer (fallback)
     */
    public void drawShape(ShapeRenderer shapes) {
        if (towerTex == null) {
            shapes.setColor(canShoot ? Color.CYAN : Color.BLUE);
            shapes.rect(x - scaledW/2, y - scaledH/2, scaledW, scaledH);
        }
    }

    /* ===== GETTERS ===== */
    public Rectangle getBounds() {
        return new Rectangle(x - scaledW/2, y - scaledH/2, scaledW, scaledH);
    }

    public int getHealth() { return health; }
    public int getRemainingUpgrades() { return MAX_TOTAL_UPGRADES - totalUpgradeCount; }
    public boolean canUpgrade() { return totalUpgradeCount < MAX_TOTAL_UPGRADES; }

    // Individual upgrade levels
    public int getAttackLevel() { return attackLevel; }
    public int getDefenseLevel() { return defenseLevel; }
    public int getSpeedLevel() { return speedLevel; }
    public int getTotalUpgradeCount() { return totalUpgradeCount; }

    // Current stats (after upgrades)
    public int getCurrentDamage() { return damage; }
    public float getCurrentFireRate() { return fireRate; }
    public int getMaxHealth() { return baseHealth + (defenseLevel * 2); }

    // UI helpers
    public String getUpgradeRemaining() { return String.valueOf(getRemainingUpgrades()); }
    public String getUpgradeLevel() { return String.valueOf(totalUpgradeCount); }
}
