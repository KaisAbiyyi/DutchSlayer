package io.DutchSlayer.defend.entities.towers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.enemies.Enemy;
import io.DutchSlayer.defend.entities.projectiles.AoeProjectile;
import io.DutchSlayer.defend.entities.projectiles.Projectile;
import io.DutchSlayer.defend.entities.projectiles.SlowProjectile;
import io.DutchSlayer.defend.ui.ImageLoader;
import io.DutchSlayer.defend.utils.AudioManager;

/**
 * Optimized Tower class dengan sistem upgrade dan berbagai type tower
 * Support 4 type: BASIC, AOE, FAST, SLOW dengan stats dan projectile berbeda
 *
 * OPTIMIZATIONS:
 * - Pre-calculated upgrade costs
 * - Cached textures and targeting
 * - Reusable objects (Vector2, Rectangle)
 * - Reduced string operations
 * - Efficient animation system
 */
public class Tower {

    /* ===== STATIC CONSTANTS & PRE-CALCULATED VALUES ===== */
    private static final boolean DEBUG_MODE = false;
    private static final int MAX_TOTAL_UPGRADES = 10;
    private static final float ANIMATION_DURATION = 0.2f;
    private static final float IDLE_ANIMATION_SPEED = 0.6f;
    private static final float TARGET_CHECK_INTERVAL = 0.1f;
    private static final float MIN_FIRE_RATE = 0.05f;

    // Pre-calculated upgrade costs (menghindari Math.pow berulang)
    private static final int[] ATTACK_UPGRADE_COSTS = new int[MAX_TOTAL_UPGRADES + 1];
    private static final int[] DEFENSE_UPGRADE_COSTS = new int[MAX_TOTAL_UPGRADES + 1];
    private static final int[] SPEED_UPGRADE_COSTS = new int[MAX_TOTAL_UPGRADES + 1];

    // Projectile origin offsets per tower type
    private static final float[][] PROJECTILE_OFFSETS = {
        {0.4f, 0.1f},   // BASIC
        {0.5f, 0.7f},   // AOE
        {0.39f, 0.35f}, // FAST
        {0.35f, 0.15f}  // SLOW
    };

    // Main tower offsets
    private static final float MAIN_TOWER_OFFSET_X = 0.3f;
    private static final float MAIN_TOWER_OFFSET_Y = 0.4f;

    static {
        // Pre-calculate semua upgrade costs
        for (int i = 0; i <= MAX_TOTAL_UPGRADES; i++) {
            ATTACK_UPGRADE_COSTS[i] = (int)(20 * Math.pow(1.5f, i));
            DEFENSE_UPGRADE_COSTS[i] = (int)(15 * Math.pow(1.5f, i));
            SPEED_UPGRADE_COSTS[i] = (int)(25 * Math.pow(1.5f, i));
        }
    }

    /* ===== TOWER PROPERTIES ===== */
    public final boolean canShoot;
    public final boolean isMain;
    public final TowerType type;

    /* ===== POSITION & VISUAL ===== */
    public final float x, y;
    public final float scaledW, scaledH;
    private final float scale;
    private final Texture towerTex;
    private final Texture projTex;
    private final float projScale;

    /* ===== COMBAT STATS ===== */
    private int health;
    private int baseHealth;
    private int damage;
    private int baseDamage;
    private float fireRate;
    private float baseFireRate;
    private float slowDuration;

    /* ===== SHOOTING MECHANICS ===== */
    private float cooldown = 0f;

    /* ===== TARGETING OPTIMIZATION ===== */
    private Enemy currentTarget;
    private float targetCheckCooldown = 0f;

    /* ===== UPGRADE SYSTEM ===== */
    private int totalUpgradeCount = 0;
    private int attackLevel = 0;
    private int defenseLevel = 0;
    private int speedLevel = 0;

    /* ===== ANIMATION SYSTEM OPTIMIZATION ===== */
    private boolean isAnimating = false;
    private boolean hasIdleAnimation = false;
    private int currentFrame = 0;
    private float animationTimer = 0f;

    // Cached texture untuk menghindari getCurrentTexture() setiap frame
    private Texture cachedCurrentTexture;
    private boolean textureNeedsUpdate = true;

    /* ===== REUSABLE OBJECTS (menghindari garbage collection) ===== */
    private final Vector2 tempOrigin = new Vector2();
    private final Rectangle tempBounds = new Rectangle();

    private Array<Enemy> enemiesRef = null;

    /* ===== TOWER TYPE CONFIGS ===== */
    private static final TowerConfig[] TOWER_CONFIGS = {
        new TowerConfig(5f, 1, false),      // BASIC
        new TowerConfig(4f, 2, false),      // AOE
        new TowerConfig(0.5f, 1, false),    // FAST
        new TowerConfig(1.5f, 0, true)      // SLOW
    };

    private static class TowerConfig {
        final float baseFireRate;
        final int baseDamage;
        final boolean hasIdleAnim;

        TowerConfig(float fireRate, int damage, boolean idleAnim) {
            this.baseFireRate = fireRate;
            this.baseDamage = damage;
            this.hasIdleAnim = idleAnim;
        }
    }

    /**
     * Constructor dengan optimasi initialization
     */
    public Tower(Texture towerTex, Texture projTex, float xCenter, float yCenter,
                 float scale, boolean canShoot, boolean isMain, TowerType type,
                 int initialHealth, float projScale) {

        this.towerTex = towerTex;
        this.projTex = projTex;
        this.scale = scale;
        this.canShoot = canShoot;
        this.isMain = isMain;
        this.type = type;
        this.health = initialHealth;
        this.baseHealth = initialHealth;
        this.projScale = projScale;

        // Calculate sprite dimensions
        this.scaledW = towerTex.getWidth() * scale;
        this.scaledH = towerTex.getHeight() * scale;
        this.x = xCenter;
        this.y = yCenter;

        // Initialize bounds
        tempBounds.set(x - scaledW/2, y - scaledH/2, scaledW, scaledH);

        // Set stats berdasarkan tower type menggunakan config array
        TowerConfig config = TOWER_CONFIGS[type.ordinal()];
        this.baseFireRate = config.baseFireRate;
        this.baseDamage = config.baseDamage;
        this.hasIdleAnimation = config.hasIdleAnim;

        // Special case untuk SLOW tower
        if (type == TowerType.SLOW) {
            this.slowDuration = 2f;
        }

        // Initialize current stats
        this.fireRate = this.baseFireRate;
        this.damage = this.baseDamage;

        // Cache initial texture
        updateCachedTexture();
    }

    /**
     * Optimized projectile origin calculation dengan reusable Vector2
     */
    private Vector2 getProjectileOrigin() {
        tempOrigin.set(0, 0);

        if (isMain) {
            tempOrigin.x = x + scaledW * MAIN_TOWER_OFFSET_X;
            tempOrigin.y = y + scaledH * MAIN_TOWER_OFFSET_Y;
        } else {
            int typeIndex = type.ordinal();
            if (typeIndex < PROJECTILE_OFFSETS.length) {
                tempOrigin.x = x + scaledW * PROJECTILE_OFFSETS[typeIndex][0];
                tempOrigin.y = y + scaledH * PROJECTILE_OFFSETS[typeIndex][1];
            } else {
                // Fallback
                tempOrigin.x = x + scaledW * 0.3f;
                tempOrigin.y = y;
            }
        }

        return tempOrigin;
    }

    /**
     * Optimized targeting system dengan caching
     */
    private Enemy findBestTarget(Array<Enemy> enemies) {
        if (enemies.isEmpty()) return null;

        // Simple targeting: return first alive enemy
        for (Enemy enemy : enemies) {
            if (!enemy.isDestroyed()) {
                return enemy;
            }
        }
        return null;
    }

    /**
     * Optimized update method
     */
    public void update(float delta, Array<Enemy> enemies, Array<Projectile> projs) {
        this.enemiesRef = enemies;
        updateAnimation(delta);

        // Early return untuk kondisi yang tidak bisa shoot
        if (!canShoot || isDestroyed() || enemies.isEmpty()) {
            return;
        }

        // Update cooldown
        cooldown -= delta;
        if (cooldown > 0) return;

        // Update target secara berkala, bukan setiap frame
        targetCheckCooldown -= delta;
        if (targetCheckCooldown <= 0f || currentTarget == null || currentTarget.isDestroyed()) {
            currentTarget = findBestTarget(enemies);
            targetCheckCooldown = TARGET_CHECK_INTERVAL;
        }

        if (currentTarget == null) return;

        // Get target position
        Rectangle targetBounds = currentTarget.getBounds();
        float targetX = targetBounds.x + targetBounds.width * 0.5f;
        float targetY = targetBounds.y + targetBounds.height * 0.5f;

        Vector2 origin = getProjectileOrigin();

        // Create projectile berdasarkan tower type
        createProjectile(projs, origin.x, origin.y, targetX, targetY);

        // Reset cooldown
        cooldown = fireRate;
    }

    /**
     * Optimized projectile creation
     */
    private void createProjectile(Array<Projectile> projs, float originX, float originY,
                                  float targetX, float targetY) {
        switch(type) {
            case BASIC:
                projs.add(new Projectile(projTex, originX, originY, targetX, targetY,
                    projScale, 900f, damage));
                break;

            case AOE:
                AudioManager.playAOEShootWithVolume(0.7f);
                triggerShootAnimation();
                projs.add(new AoeProjectile(projTex, originX, originY, targetX, targetY,
                    100f, projScale, damage, 800f));
                break;

            case FAST:
                AudioManager.playTowerShootWithVolume(0.5f);
                triggerShootAnimation();
                projs.add(new Projectile(projTex, originX, originY, targetX, targetY,
                    projScale, 1500f, damage));
                break;

            case SLOW:
                AudioManager.playSlowProjectileWithVolume(1f);
                projs.add(new SlowProjectile(projTex, originX, originY, targetX, targetY,
                    slowDuration, projScale, 500f));
                break;
        }
    }

    /**
     * Trigger shooting animation dengan flag update
     */
    private void triggerShootAnimation() {
        if (type == TowerType.FAST || type == TowerType.AOE || type == TowerType.SLOW) {
            isAnimating = true;
            animationTimer = 0f;
            textureNeedsUpdate = true;
            debugPrint("🎬 " + type + " tower shooting animation!");
        }
    }

    /**
     * Optimized animation update
     */
    private void updateAnimation(float delta) {
        boolean wasAnimating = isAnimating;
        int oldFrame = currentFrame;

        // Update shooting animation
        if (isAnimating) {
            animationTimer += delta;
            if (animationTimer >= ANIMATION_DURATION) {
                isAnimating = false;
                animationTimer = 0f;
            }
        }

        // Update idle animation
        if (hasIdleAnimation) {
            animationTimer += delta;
            if (animationTimer >= IDLE_ANIMATION_SPEED) {
                animationTimer = 0f;
                currentFrame = (currentFrame + 1) % 3;
            }
        }

        // Update texture cache hanya jika ada perubahan
        if (wasAnimating != isAnimating || oldFrame != currentFrame) {
            textureNeedsUpdate = true;
        }
    }

    /**
     * Update cached texture hanya ketika diperlukan
     */
    private void updateCachedTexture() {
        if (!textureNeedsUpdate) return;

        Texture[] frames = ImageLoader.getTowerAnimationFrames(type);

        // Shooting animation (priority)
        if (isAnimating && frames != null && frames.length > 1 && frames[1] != null) {
            cachedCurrentTexture = frames[1];
        }
        // Idle animation
        else if (hasIdleAnimation && frames != null && frames.length > 0) {
            if (currentFrame == 0 && frames[0] != null) {
                cachedCurrentTexture = frames[0];
            } else if (currentFrame == 1 && frames.length > 1 && frames[1] != null) {
                cachedCurrentTexture = frames[1];
            } else if (currentFrame == 2 && frames.length > 2 && frames[2] != null) {
                cachedCurrentTexture = frames[2];
            } else {
                cachedCurrentTexture = frames[0];
            }
        }
        // Fallback
        else if (frames != null && frames.length > 0 && frames[0] != null) {
            cachedCurrentTexture = frames[0];
        } else {
            cachedCurrentTexture = towerTex;
        }

        textureNeedsUpdate = false;
    }

    /**
     * Optimized damage handling
     */
    public void takeDamage(int dmg) {
        health -= dmg;
    }

    /**
     * Check destroyed status
     */
    public boolean isDestroyed() {
        return health <= 0;
    }

    /* ===== OPTIMIZED UPGRADE METHODS ===== */

    /**
     * Upgrade attack dengan pre-calculated costs
     */
    public boolean upgradeAttack() {
        if (totalUpgradeCount >= MAX_TOTAL_UPGRADES) return false;

        attackLevel++;
        totalUpgradeCount++;

        int damageBonus = (type == TowerType.AOE) ? 2 : 1;
        damage = baseDamage + (attackLevel * damageBonus);

        debugPrint("⚔️ Attack upgraded! Level: " + attackLevel + ", Damage: " + damage);
        return true;
    }

    /**
     * Upgrade defense dengan pre-calculated costs
     */
    public boolean upgradeDefense() {
        if (totalUpgradeCount >= MAX_TOTAL_UPGRADES) return false;

        defenseLevel++;
        totalUpgradeCount++;

        int healthIncrease = defenseLevel * 2;
        int newMaxHealth = baseHealth + healthIncrease;
        health = Math.min(health + 2, newMaxHealth);

        debugPrint("🛡️ Defense upgraded! Level: " + defenseLevel + ", Health: " + health + "/" + newMaxHealth);
        return true;
    }

    /**
     * Upgrade speed dengan pre-calculated costs
     */
    public boolean upgradeSpeed() {
        if (totalUpgradeCount >= MAX_TOTAL_UPGRADES) return false;

        speedLevel++;
        totalUpgradeCount++;

        float speedBonus;
        switch(type) {
            case FAST: speedBonus = 0.05f; break;
            case SLOW: speedBonus = 0.25f; break;
            default: speedBonus = 0.15f; break;
        }

        fireRate = Math.max(MIN_FIRE_RATE, baseFireRate - (speedLevel * speedBonus));

        debugPrint("⚡ Speed upgraded! Level: " + speedLevel + ", Fire rate: " + fireRate + "s");
        return true;
    }

    /* ===== PRE-CALCULATED UPGRADE COSTS ===== */

    public int getAttackUpgradeCost() {
        return ATTACK_UPGRADE_COSTS[Math.min(attackLevel, MAX_TOTAL_UPGRADES)];
    }

    public int getDefenseUpgradeCost() {
        return DEFENSE_UPGRADE_COSTS[Math.min(defenseLevel, MAX_TOTAL_UPGRADES)];
    }

    public int getSpeedUpgradeCost() {
        return SPEED_UPGRADE_COSTS[Math.min(speedLevel, MAX_TOTAL_UPGRADES)];
    }

    /* ===== UPGRADE VALIDATION ===== */

    public boolean canUpgradeAttack(int currentGold) {
        return totalUpgradeCount < MAX_TOTAL_UPGRADES &&
            currentGold >= getAttackUpgradeCost();
    }

    public boolean canUpgradeDefense(int currentGold) {
        return totalUpgradeCount < MAX_TOTAL_UPGRADES &&
            currentGold >= getDefenseUpgradeCost();
    }

    public boolean canUpgradeSpeed(int currentGold) {
        return totalUpgradeCount < MAX_TOTAL_UPGRADES &&
            currentGold >= getSpeedUpgradeCost();
    }

    /**
     * Get upgrade info dengan optimized string building
     */
    public String getUpgradeInfo(String upgradeType) {
        switch(upgradeType.toLowerCase()) {
            case "attack":
                int nextDamage = damage + (type == TowerType.AOE ? 2 : 1);
                return "DMG: " + damage + " → " + nextDamage;

            case "defense":
                int maxHealth = getMaxHealth();
                return "HP: " + health + "/" + maxHealth + " → " + (health + 2) + "/" + (maxHealth + 2);

            case "speed":
                float speedBonus = (type == TowerType.FAST) ? 0.05f : 0.15f;
                float nextFireRate = Math.max(MIN_FIRE_RATE, fireRate - speedBonus);
                return String.format("Rate: %.2fs → %.2fs", fireRate, nextFireRate);

            default:
                return "";
        }
    }

    /* ===== OPTIMIZED RENDERING ===== */

    /**
     * Optimized batch drawing dengan cached texture
     */
    public void drawBatch(SpriteBatch batch) {
        // Update cached texture hanya jika diperlukan
        updateCachedTexture();

        // Visual flash effect saat shooting
        if (isAnimating && type != TowerType.BASIC) {
            batch.setColor(1.2f, 1.2f, 1.2f, 1f);
        }

        batch.draw(cachedCurrentTexture, x - scaledW/2, y - scaledH/2, scaledW, scaledH);

        // Reset color
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * Shape renderer fallback
     */
    public void drawShape(ShapeRenderer shapes) {
        if (towerTex == null) {
            shapes.setColor(canShoot ? Color.CYAN : Color.BLUE);
            shapes.rect(x - scaledW/2, y - scaledH/2, scaledW, scaledH);
        }
    }

    /* ===== OPTIMIZED GETTERS ===== */

    public Rectangle getBounds() {
        // Update reusable bounds object
        tempBounds.set(x - scaledW/2, y - scaledH/2, scaledW, scaledH);
        return tempBounds;
    }

    // Basic getters
    public int getHealth() { return health; }
    public int getRemainingUpgrades() { return MAX_TOTAL_UPGRADES - totalUpgradeCount; }
    public boolean canUpgrade() { return totalUpgradeCount < MAX_TOTAL_UPGRADES; }

    // Upgrade levels
    public int getAttackLevel() { return attackLevel; }
    public int getDefenseLevel() { return defenseLevel; }
    public int getSpeedLevel() { return speedLevel; }
    public int getTotalUpgradeCount() { return totalUpgradeCount; }

    // Current stats
    public int getCurrentDamage() { return damage; }
    public float getCurrentFireRate() { return fireRate; }
    public int getMaxHealth() { return baseHealth + (defenseLevel * 2); }

    // UI helpers
    public String getUpgradeRemaining() { return String.valueOf(getRemainingUpgrades()); }
    public String getUpgradeLevel() { return String.valueOf(totalUpgradeCount); }

    /* ===== DEBUG UTILITY ===== */

    private void debugPrint(String message) {
        if (DEBUG_MODE) {
            System.out.println(message);
        }
    }

    /**
     * Set enemies reference untuk AOE projectile tracking
     * Panggil di update() method
     */
    public void setEnemiesReference(Array<Enemy> enemies) {
        this.enemiesRef = enemies;
    }
}
