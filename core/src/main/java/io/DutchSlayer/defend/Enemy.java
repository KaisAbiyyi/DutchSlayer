package io.DutchSlayer.defend;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Enemy class dengan sistem AI yang comprehensive
 * Support 5 enemy types: BASIC, SHOOTER, BOMBER, SHIELD, BOSS
 * Setiap type memiliki stats, behavior, dan special abilities yang berbeda
 */
public class Enemy {
    /* ===== Size Enemy ===== */
    public static final float BASIC_SCALE = 0.2f;
    public static final float SHOOTER_SCALE = 0.18f;
    public static final float BOMBER_SCALE = 0.15f;
    public static final float SHIELD_SCALE = 0.25f;
    public static final float BOSS_SCALE = 0.4f;

    /* ===== CONSTANTS ===== */
    public static float scale;                     // Scale factor untuk semua enemy sprites
    private static final float SHOOTER_INTERVAL = 1.5f;         // Shooter menembak setiap 1.5 detik
    private static final float BOSS_INTERVAL = 8f;            // Boss menembak setiap 2.5 detik
    private static final float KNOCKBACK_DURATION = 0.5f;       // Durasi knockback effect
    private static final float KNOCKBACK_SPEED = 200f;          // Kecepatan mundur saat knockback
    private static final float ATTACK_COOLDOWN_DURATION = 1f;   // Cooldown antar serangan ke tower

    /* ===== VISUAL COMPONENTS ===== */
    private final Texture tex;          // Texture sprite enemy
    private final float scaledWidth;    // Lebar sprite setelah scaling
    private final float scaledHeight;   // Tinggi sprite setelah scaling
    private final Rectangle bounds;     // Collision bounds

    /* ===== POSITION & MOVEMENT ===== */
    private final Vector2 pos;              // Posisi saat ini (center point)
    private final float baseSpeed;          // Kecepatan dasar (tidak berubah)
    private float currentSpeed;             // Kecepatan saat ini (affected by effects)

    /* ===== ENEMY STATS ===== */
    private final EnemyType type;           // Jenis enemy (BASIC/SHOOTER/etc.)
    private int health;                     // HP saat ini
    private final int maxHealth;            // HP maksimal (untuk health bar)

    /* ===== AI STATE MACHINE ===== */
    private EnemyState state = EnemyState.MOVING;    // State saat ini
    private float stateTimer = 0f;                   // Timer untuk state transitions
    private float shootCooldown = 0f;                // Cooldown untuk shooting abilities
    private float shootInterval;                     // Interval tembakan (per instance)

    /* ===== SPECIAL BEHAVIORS ===== */
    private boolean hasDroppedBomb = false;          // Bomber sudah drop bomb? (unused tapi kept for future)
    private float targetX = 0f;                      // Position target (untuk boss positioning)
    private boolean hasReachedTarget = false;        // Boss sudah sampai target position?

    /* ===== STATUS EFFECTS ===== */
    private boolean isSlowed = false;                // Sedang kena slow effect?
    private float slowDuration = 0f;                 // Sisa durasi slow effect
    private float slowStrength = 0.5f;               // Kekuatan slow (0.5 = 50% speed reduction)
    private boolean isKnockedBack = false;           // Sedang kena knockback effect?
    private float knockbackTimer = 0f;               // Sisa waktu knockback
    private float attackCooldown = 0f;               // Cooldown setelah attack tower

    // ===== EFFECTS =====
    private final float knockbackDuration = 0.5f;  // Durasi knockback 0.5 detik
    private final float knockbackSpeed = 200f;     // Kecepatan mundur

    /* ===== REFERENCES FOR INTERACTIONS ===== */
    private Array<Tower> towersRef;                  // Reference ke towers (untuk shooting)
    private Array<EnemyProjectile> enemyProjectilesRef;  // Reference ke enemy projectiles
    private Array<BombAsset> bombsRef;               // Reference ke bombs (untuk bomber)

    /**
     * Constructor Enemy dengan type dan starting position
     * @param type Jenis enemy (BASIC/SHOOTER/BOMBER/SHIELD/BOSS)
     * @param xCenter Posisi awal X (center point)
     * @param yCenter Posisi awal Y (center point)
     */
    public Enemy(EnemyType type, float xCenter, float yCenter) {
        this.type = type;
        this.pos = new Vector2(xCenter, yCenter);

        // ===== SET PROPERTIES BERDASARKAN ENEMY TYPE =====
        switch(type) {
            case BASIC:
                this.tex = ImageLoader.enemyBasicTex != null ? ImageLoader.enemyBasicTex : ImageLoader.dutchtex;
                this.health = 3;
                this.baseSpeed = 100f;
                this.scale = BASIC_SCALE;
                break;

            case SHOOTER:
                this.tex = ImageLoader.enemyShooterTex != null ? ImageLoader.enemyShooterTex : ImageLoader.dutchtex;
                this.shootInterval = SHOOTER_INTERVAL;
                this.health = 2;            // Lower HP tapi bisa shoot
                this.baseSpeed = 80f;       // Slower karena ranged
                this.scale = SHOOTER_SCALE;
                break;

            case BOMBER:
                this.tex = ImageLoader.enemyBomberTex != null ? ImageLoader.enemyBomberTex : ImageLoader.dutchtex;
                this.health = 2;            // Low HP, suicide unit
                this.baseSpeed = 120f;      // Fast untuk kamikaze rush
                this.scale = BOMBER_SCALE;
                break;

            case SHIELD:
                this.tex = ImageLoader.enemyShieldTex != null ? ImageLoader.enemyShieldTex : ImageLoader.dutchtex;
                this.health = 8;            // High HP, tank unit
                this.baseSpeed = 60f;       // Slow karena heavy armor
                this.scale = SHIELD_SCALE;
                break;

            case BOSS:
                this.tex = ImageLoader.enemyBossTex != null ? ImageLoader.enemyBossTex : ImageLoader.dutchtex;
                this.shootInterval = BOSS_INTERVAL;
                this.health = 100;           // Very high HP
                this.baseSpeed = 50f;       // Slow tapi powerful
                this.targetX = 1100f;       // Stop position di area tertentu
                this.scale = BOSS_SCALE;
                break;

            default:
                // Fallback case
                this.tex = ImageLoader.dutchtex;
                this.shootInterval = 2f;
                this.health = 3;
                this.baseSpeed = 100f;
                this.scale = BASIC_SCALE;
                break;
        }

        // Initialize derived properties
        this.maxHealth = this.health;
        this.currentSpeed = this.baseSpeed;
        this.scaledWidth = tex.getWidth() * this.scale;
        this.scaledHeight = tex.getHeight() * this.scale;

        // Setup collision bounds (centered pada posisi)
        this.bounds = new Rectangle(
            xCenter - scaledWidth/2,
            yCenter - scaledHeight/2,
            scaledWidth,
            scaledHeight
        );
    }

    /**
     * Main update method - dipanggil setiap frame
     * @param delta Time since last frame (seconds)
     */

    public void update(float delta) {
        // Update status effects (slow, knockback, cooldowns)
        updateEffects(delta);

        // Update AI behavior berdasarkan enemy type
        switch(type) {
            case BASIC:
                updateBasic(delta);
                break;
            case SHOOTER:
                updateShooter(delta);
            case BOMBER:
                updateBomber(delta);
                break;
            case SHIELD:
                updateShield(delta);
                break;
            case BOSS:
                updateBoss(delta);
                break;
        }

        // Update posisi berdasarkan calculated speed
        updatePosition(delta);
    }

    /**
     * Update status effects dan cooldowns
     */
    private void updateEffects(float delta) {
        // Handle knockback timer
        if (isKnockedBack) {
            knockbackTimer -= delta;
            if (knockbackTimer <= 0) {
                isKnockedBack = false;
            }
        }

        // Handle attack cooldown (setelah menyerang tower)
        if (attackCooldown > 0) {
            attackCooldown -= delta;
        }

        // Handle slow effect duration
        if (isSlowed && slowDuration > 0) {
            slowDuration -= delta;
            if (slowDuration <= 0) {
                isSlowed = false;
            }
        }
    }

    /**
     * Dapatkan konfigurasi projectile berdasarkan enemy type
     */
    private ProjectileConfig getProjectileConfig() {
        switch(type) {
            case SHOOTER:
                return new ProjectileConfig(250f, 0.12f, 1);  // Medium speed, small size, normal damage
            case BOSS:
                return new ProjectileConfig(180f, 0.20f, 3);  // Slower, bigger, high damage
            default:
                return new ProjectileConfig(200f, 0.15f, 1);  // Default config
        }
    }

    /**
     * Inner class untuk projectile configuration
     */
    private static class ProjectileConfig {
        final float speed;
        final float scale;
        final int damage;

        ProjectileConfig(float speed, float scale, int damage) {
            this.speed = speed;
            this.scale = scale;
            this.damage = damage;
        }
    }


    /**
     * AI behavior untuk BASIC enemy - simple movement
     */
    private void updateBasic(float delta) {
        determineSpeed();
    }

    /**
     * AI behavior untuk SHOOTER enemy - stop and shoot tactics
     */
    private void updateShooter(float delta) {
        stateTimer += delta;
        shootCooldown -= delta;

        switch(state) {
            case MOVING:
                determineSpeed();
                // Transition ke ATTACKING state ketika sampai shooting range
                if (pos.x <= 1000f) {
                    state = EnemyState.ATTACKING;
                    currentSpeed = 0f;
                    stateTimer = 0f;
                }
                break;

            case ATTACKING:
                currentSpeed = 0f;  // Stay stationary while shooting
                // Shoot jika cooldown sudah habis dan ada target
                if (shootCooldown <= 0f && towersRef != null && !towersRef.isEmpty()) {
                    shoot();
                    shootCooldown = SHOOTER_INTERVAL;
                }
                break;
        }
    }

    /**
     * AI behavior untuk BOMBER enemy - simplified ke basic movement
     * Bombing behavior dihandle di collision detection di GameScreen
     */
    private void updateBomber(float delta) {
        determineSpeed();   // Just rush forward untuk kamikaze
    }

    /**
     * AI behavior untuk SHIELD enemy - simple movement, no special actions
     */
    private void updateShield(float delta) {
        determineSpeed();   // Tank unit, just move forward
    }

    /**
     * AI behavior untuk BOSS enemy - move to position then stationary shooting
     */
    private void updateBoss(float delta) {
        stateTimer += delta;
        shootCooldown -= delta;

        switch(state) {
            case MOVING:
                determineSpeed();
                // Transition ke STATIONARY ketika sampai target position
                if (pos.x <= targetX) {
                    state = EnemyState.STATIONARY;
                    currentSpeed = 0f;
                    hasReachedTarget = true;
                }
                break;

            case STATIONARY:
                currentSpeed = 0f;  // Stay at position
                // Shoot powerful projectiles
                if (shootCooldown <= 0f && towersRef != null && !towersRef.isEmpty()) {
                    shootBoss();
                    shootCooldown = BOSS_INTERVAL;
                }
                break;
        }
    }

    /**
     * Calculate current speed berdasarkan status effects
     */
    private void determineSpeed() {
        if (isKnockedBack) {
            currentSpeed = -knockbackSpeed;     // Negative = move backward
        } else if (isSlowed) {
            currentSpeed = baseSpeed * slowStrength;    // Reduced speed
        } else {
            currentSpeed = baseSpeed;           // Normal speed
        }
    }

    /**
     * Update posisi enemy berdasarkan current speed dan state
     */
    private void updatePosition(float delta) {
        // Skip movement jika speed = 0 (stationary/attacking states)
        if (currentSpeed != 0f) {
            if (type == EnemyType.BOMBER && state == EnemyState.RETREATING) {
                // BOMBER retreating - move right (away from towers)
                pos.x += currentSpeed * delta;
            } else {
                // Normal movement - move left (towards towers)
                pos.x -= currentSpeed * delta;
            }

            // Update collision bounds sesuai posisi baru
            bounds.setPosition(pos.x - bounds.width/2, pos.y - bounds.height/2);
        }
    }

    /**
     * SHOOTER enemy shoot projectile
     */
    private void shoot() {
        if (towersRef == null || towersRef.isEmpty() || enemyProjectilesRef == null) return;

        EnemyProjectile projectile = EnemyProjectile.createShooterProjectile(
            ImageLoader.enemyProjectileTex != null ? ImageLoader.enemyProjectileTex : ImageLoader.projTex,
            pos.x - 20f,  // Start di depan enemy (offset ke kiri)
            pos.y,        // Same height sebagai enemy
            1             // Normal damage
        );
        enemyProjectilesRef.add(projectile);
        System.out.println("Enemy shot projectile!");
    }

    /**
     * BOSS enemy shoot powerful projectile
     */
    private void shootBoss() {
        if (towersRef == null || towersRef.isEmpty() || enemyProjectilesRef == null) return;

        EnemyProjectile projectile = EnemyProjectile.createBossProjectile(
            ImageLoader.enemyProjectileTex != null ? ImageLoader.enemyProjectileTex : ImageLoader.projTex,
            pos.x - 30f,  // Start lebih jauh dari boss (bigger offset)
            pos.y,        // Same height sebagai boss
            3             // Higher damage untuk boss projectile
        );
        enemyProjectilesRef.add(projectile);
        System.out.println("Boss shot powerful projectile!");
    }

    /**
     * Check apakah enemy ini adalah bomber type
     * @return true jika enemy type adalah BOMBER
     */
    public boolean isBomber() {
        return type == EnemyType.BOMBER;
    }

    /**
     * Create bomb asset di posisi enemy saat ini (untuk bomber collision)
     * @return BombAsset baru atau null jika bukan bomber
     */
    public BombAsset createBombAtPosition() {
        if (type != EnemyType.BOMBER) return null;

        return new BombAsset(
            ImageLoader.bombAssetTex != null ? ImageLoader.bombAssetTex : ImageLoader.trapTex,
            pos.x, pos.y
        );
    }

    /**
     * Shield protection mechanism - BASIC enemy seek protection behind SHIELD enemy
     * @param allEnemies Array semua enemy untuk find shield
     */
    public void seekProtection(Array<Enemy> allEnemies) {
        if (type != EnemyType.BASIC) return;    // Only basic enemies seek protection

        // Find nearest shield enemy dalam range
        Enemy nearestShield = null;
        float nearestDistance = Float.MAX_VALUE;

        for (Enemy e : allEnemies) {
            if (e.type == EnemyType.SHIELD && !e.isDestroyed()) {
                float distance = Math.abs(pos.x - e.pos.x);
                if (distance < nearestDistance && distance < 100f) { // Within 100px
                    nearestDistance = distance;
                    nearestShield = e;
                }
            }
        }

        // Move behind shield (position adjustment)
        if (nearestShield != null && pos.x < nearestShield.pos.x) {
            pos.x = nearestShield.pos.x + 30f; // Position behind shield
            System.out.println("Basic enemy seeking protection behind shield!");
        }
    }


    /**
     * Apply knockback effect (enemy terdorong mundur)
     */
    public void knockback() {
        if (!isKnockedBack) {  // Prevent multiple knockbacks
            isKnockedBack = true;
            knockbackTimer = knockbackDuration;
            attackCooldown = ATTACK_COOLDOWN_DURATION;
            System.out.println("Enemy knockback started!"); // Debug
        }
    }

    /**
     * Set references untuk enemy interactions
     * CRITICAL: Method ini HARUS dipanggil setelah enemy creation dan sebelum add ke game arrays
     * @param towers Reference ke towers array (untuk shooting target)
     * @param enemyProjectiles Reference ke enemy projectiles array
     * @param bombs Reference ke bombs array (untuk bomber)
     */
    public void setReferences(Array<Tower> towers, Array<EnemyProjectile> enemyProjectiles, Array<BombAsset> bombs) {
        this.towersRef = towers;
        this.enemyProjectilesRef = enemyProjectiles;
        this.bombsRef = bombs;
    }

    /**
     * Check apakah enemy bisa menyerang tower
     * @return true jika tidak ada cooldown dan tidak sedang knockback
     */
    public boolean canAttack() {
        return attackCooldown <= 0 && !isKnockedBack;
    }

    /**
     * Enemy menerima damage
     * @param dmg Amount damage yang diterima
     */
    public void takeDamage(int dmg) {
        health = Math.max(0, health - dmg);
    }

    /**
     * Check apakah enemy sudah mati
     * @return true jika health <= 0
     */
    public boolean isDestroyed() {
        return health <= 0;
    }

    /**
     * Apply normal slow effect (50% speed reduction)
     * @param duration Durasi slow dalam detik
     */
    public void slow(float duration) {
        this.slowDuration = duration;
        this.slowStrength = 0.5f; // Normal slow = 50% speed reduction
        this.isSlowed = true;
        System.out.println("Enemy slowed! Speed reduced by 50% for " + duration + "s");
    }

    /**
     * Apply heavy slow effect dengan custom strength
     * @param duration Durasi slow dalam detik
     * @param strength Strength slow (0.1f = 90% speed reduction)
     */
    public void slowHeavy(float duration, float strength) {
        this.slowDuration = duration;
        this.slowStrength = strength; // 0.1f = 90% speed reduction
        this.isSlowed = true;
        System.out.println("Enemy heavily slowed! Speed reduced by " + ((1-strength)*100) + "% for " + duration + "s");
    }

    /**
     * Render enemy dengan visual effects berdasarkan state
     * @param batch SpriteBatch untuk drawing
     */
    public void drawBatch(SpriteBatch batch) {
        if (tex != null) {
            // ===== VISUAL EFFECTS BERDASARKAN STATUS =====
            if (isKnockedBack) {
                batch.setColor(1f, 0.5f, 0.5f, 1f); // Warna kemerahan saat knockback
            } else if (isSlowed) {
                batch.setColor(0.5f, 0.5f, 1f, 1f); // Warna biru saat slowed
            } else {
                batch.setColor(1f, 1f, 1f, 1f); // Normal color
            }

            // Draw sprite centered pada posisi
            batch.draw(
                tex,
                pos.x - scaledWidth/2,
                pos.y - scaledHeight/2,
                scaledWidth,
                scaledHeight
            );

            // Reset color untuk sprites berikutnya
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    /**
     * Render enemy menggunakan ShapeRenderer (fallback jika texture null)
     * @param shapes ShapeRenderer untuk drawing shapes
     */

    public void drawShape(ShapeRenderer shapes) {
        if (tex == null) {
            // Color-coded berdasarkan status
            if (isKnockedBack) {
                shapes.setColor(Color.ORANGE);
            } else if (isSlowed) {
                shapes.setColor(Color.BLUE);
            } else {
                shapes.setColor(Color.RED);
            }

            // Draw circle centered pada posisi
            float radius = scaledWidth/2f;
            shapes.circle(pos.x, pos.y, radius);;
        }
    }

    /* ===== GETTERS ===== */
    public float getX() { return pos.x; }
    public float getWidth() { return scaledWidth; }
    public Rectangle getBounds() { return bounds; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public EnemyType getType() { return type; }
    public boolean isKnockedBack() { return isKnockedBack; }
    public EnemyState getState() { return state; }
    public boolean hasReachedTarget() { return hasReachedTarget; }
}
