package io.DutchSlayer.defend.entities.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.defend.entities.projectiles.EnemyProjectile;
import io.DutchSlayer.defend.entities.towers.Tower;
import io.DutchSlayer.defend.ui.ImageLoader;
import io.DutchSlayer.defend.utils.AudioManager;

/**
 * Optimized Enemy class dengan efficient memory management
 */
public class Enemy {
    /* ===== Size Enemy ===== */
    public static final float BASIC_SCALE = 0.2f;
    public static final float SHOOTER_SCALE = 0.18f;
    public static final float BOMBER_SCALE = 0.2f;
    public static final float SHIELD_SCALE = 0.25f;
    public static final float BOSS_SCALE = 0.4f;

    /* ===== CONSTANTS ===== */
    public static float scale;
    private static final float SHOOTER_INTERVAL = 1.5f;
    private static final float BOSS_INTERVAL = 8f;
    private static final float KNOCKBACK_DURATION = 0.5f;
    private static final float KNOCKBACK_SPEED = 200f;
    private static final float ATTACK_COOLDOWN_DURATION = 1f;

    /* ===== OPTIMIZATIONS: CACHED VALUES ===== */
    private final Vector2 reusableVector = new Vector2(); // ✅ Reusable untuk calculations
    private final float halfWidth;
    private final float halfHeight; // ✅ Cache half dimensions
    private Texture currentTexture; // ✅ Cache current texture
    private boolean textureDirty = true; // ✅ Flag untuk texture updates

    /* ===== VISUAL COMPONENTS ===== */
    private final Texture tex;
    private final float scaledWidth;
    private final float scaledHeight;
    private final Rectangle bounds;

    /* ===== POSITION & MOVEMENT ===== */
    private final Vector2 pos;
    private final float baseSpeed;
    private float currentSpeed;

    /* ===== ENEMY STATS ===== */
    private final EnemyType type;
    private int health;
    private final int maxHealth;

    /* ===== AI STATE MACHINE ===== */
    private EnemyState state = EnemyState.MOVING;
    private float shootCooldown = 0f;

    /* ===== SPECIAL BEHAVIORS ===== */
    private float targetX = 0f;

    /* ===== STATUS EFFECTS ===== */
    private boolean isSlowed = false;
    private float slowDuration = 0f;
    private float slowStrength = 0.5f;
    private boolean isKnockedBack = false;
    private float knockbackTimer = 0f;
    private float attackCooldown = 0f;

    /* ===== REFERENCES FOR INTERACTIONS ===== */
    private Array<Tower> towersRef;
    private Array<EnemyProjectile> enemyProjectilesRef;

    // ===== ANIMASI FIELDS =====
    private float animationTimer;
    private int currentFrame;
    private static final float ANIMATION_SPEED = 0.2f;

    private boolean hasReachedTargetPosition = false;

    /**
     * Constructor Enemy dengan optimized initialization
     */
    public Enemy(EnemyType type, float xCenter, float yCenter) {
        this.type = type;
        this.pos = new Vector2(xCenter, yCenter);

        // Initialize animasi
        this.animationTimer = 0f;
        this.currentFrame = 0;

        // ===== OPTIMIZED INITIALIZATION =====
        EnemyStats stats = getEnemyStats(type);
        this.tex = stats.texture;
        this.health = stats.health;
        this.baseSpeed = stats.speed;
        scale = stats.scale;
        this.targetX = stats.targetX;

        // Initialize derived properties
        this.maxHealth = this.health;
        this.currentSpeed = this.baseSpeed;
        this.scaledWidth = tex.getWidth() * scale;
        this.scaledHeight = tex.getHeight() * scale;

        // ✅ Cache half dimensions
        this.halfWidth = scaledWidth / 2f;
        this.halfHeight = scaledHeight / 2f;

        // Setup collision bounds
        this.bounds = new Rectangle(
            xCenter - halfWidth,
            yCenter - halfHeight,
            scaledWidth,
            scaledHeight
        );
    }

    /**
     * ✅ OPTIMIZATION: Single method untuk enemy stats initialization
     */
    private static class EnemyStats {
        final Texture texture;
        final int health;
        final float speed;
        final float scale;
        final float shootInterval;
        final float targetX;

        EnemyStats(Texture texture, int health, float speed, float scale, float shootInterval, float targetX) {
            this.texture = texture;
            this.health = health;
            this.speed = speed;
            this.scale = scale;
            this.shootInterval = shootInterval;
            this.targetX = targetX;
        }
    }

    private EnemyStats getEnemyStats(EnemyType type) {
        return switch (type) {
            case BASIC -> new EnemyStats(
                ImageLoader.enemyBasicTex != null ? ImageLoader.enemyBasicTex : ImageLoader.dutchtex,
                3, 100f, BASIC_SCALE, 0f, 0f
            );
            case SHOOTER -> new EnemyStats(
                ImageLoader.enemyShooterTex != null ? ImageLoader.enemyShooterTex : ImageLoader.dutchtex,
                2, 80f, SHOOTER_SCALE, SHOOTER_INTERVAL, 0f
            );
            case BOMBER -> new EnemyStats(
                ImageLoader.enemyBomberTex != null ? ImageLoader.enemyBomberTex : ImageLoader.dutchtex,
                2, 120f, BOMBER_SCALE, 0f, 0f
            );
            case SHIELD -> new EnemyStats(
                ImageLoader.enemyShieldTex != null ? ImageLoader.enemyShieldTex : ImageLoader.dutchtex,
                8, 60f, SHIELD_SCALE, 0f, 0f
            );
            case BOSS -> new EnemyStats(
                ImageLoader.enemyBossTex != null ? ImageLoader.enemyBossTex : ImageLoader.dutchtex,
                100, 50f, BOSS_SCALE, BOSS_INTERVAL, 1100f
            );
        };
    }

    /**
     * ✅ OPTIMIZED: Animation update dengan early exit
     */
    private void updateAnimation(float delta) {
        // Early exit untuk non-animated types
        if (type == EnemyType.BOSS) return;

        boolean shouldAnimate = Math.abs(currentSpeed) > 0 && !isKnockedBack;

        if (shouldAnimate) {
            animationTimer += delta;

            float animSpeed = (type == EnemyType.BOMBER) ? 0.15f :
                (type == EnemyType.SHIELD) ? 0.25f : ANIMATION_SPEED;

            if (animationTimer >= animSpeed) {
                animationTimer = 0f;
                int oldFrame = currentFrame;
                currentFrame = (currentFrame + 1) % 4;
                textureDirty = (oldFrame != currentFrame); // ✅ Mark texture as dirty only if changed
            }
        } else {
            if (currentFrame != 0) {
                currentFrame = 0;
                animationTimer = 0f;
                textureDirty = true;
            }
        }
    }

    /**
     * Main update method - optimized flow
     */
    public void update(float delta) {
        updateEffects(delta);
        updateAI(delta); // ✅ Simplified AI dispatch
        updatePosition(delta);

        if (type != EnemyType.BOSS) {
            updateAnimation(delta);
        }
    }

    /**
     * ✅ OPTIMIZATION: Simplified AI dispatch
     */
    private void updateAI(float delta) {
        switch(type) {
            case BASIC:
            case BOMBER:
            case SHIELD:
                determineSpeed();
                break;
            case SHOOTER:
                updateShooter(delta);
                break;
            case BOSS:
                updateBoss(delta);
                break;
        }
    }

    private void updateEffects(float delta) {
        if (isKnockedBack) {
            knockbackTimer -= delta;
            if (knockbackTimer <= 0) {
                isKnockedBack = false;
            }
        }

        if (attackCooldown > 0) {
            attackCooldown -= delta;
        }

        if (isSlowed && slowDuration > 0) {
            slowDuration -= delta;
            if (slowDuration <= 0) {
                isSlowed = false;
            }
        }
    }

    private void updateShooter(float delta) {
        shootCooldown -= delta;

        switch(state) {
            case MOVING:
                determineSpeed();
                if (pos.x <= 1000f) {
                    state = EnemyState.ATTACKING;
                    currentSpeed = 0f;
                }
                break;

            case ATTACKING:
                currentSpeed = 0f;
                if (shootCooldown <= 0f && towersRef != null && !towersRef.isEmpty()) {
                    shoot();
                    shootCooldown = SHOOTER_INTERVAL;
                }
                break;
        }
    }

    private void updateBoss(float delta) {
        shootCooldown -= delta;

        switch(state) {
            case MOVING:
                determineSpeed();
                if (pos.x <= targetX) {
                    state = EnemyState.STATIONARY;
                    currentSpeed = 0f;
                    hasReachedTargetPosition = true;
                }
                break;

            case STATIONARY:
                currentSpeed = 0f;
                if (shootCooldown <= 0f && towersRef != null && !towersRef.isEmpty()) {
                    shootBoss();
                    shootCooldown = BOSS_INTERVAL;
                }
                break;
        }
    }

    private void determineSpeed() {
        if (isKnockedBack) {
            currentSpeed = -KNOCKBACK_SPEED;
        } else if (isSlowed) {
            currentSpeed = baseSpeed * slowStrength;
        } else {
            currentSpeed = baseSpeed;
        }
    }

    /**
     * ✅ OPTIMIZED: Position update dengan cached values
     */
    private void updatePosition(float delta) {
        if (currentSpeed != 0f) {
            if (type == EnemyType.BOMBER && state == EnemyState.RETREATING) {
                pos.x += currentSpeed * delta;
            } else {
                pos.x -= currentSpeed * delta;
            }

            // ✅ Use cached half dimensions
            bounds.setPosition(pos.x - halfWidth, pos.y - halfHeight);
        }
    }

    /**
     * Shooting dengan reusable vector
     */
    private void shoot() {
        if (towersRef == null || towersRef.isEmpty() || enemyProjectilesRef == null) return;

        AudioManager.playEnemyShoot();
        getProjectileOrigin(reusableVector);

        EnemyProjectile projectile = EnemyProjectile.createShooterProjectile(
            ImageLoader.enemyProjectileTex,
            reusableVector.x,
            reusableVector.y,
            1
        );
        enemyProjectilesRef.add(projectile);
    }

    private void shootBoss() {
        if (towersRef == null || towersRef.isEmpty() || enemyProjectilesRef == null) return;

        AudioManager.playBossShoot();

        EnemyProjectile projectile = EnemyProjectile.createBossProjectile(
            ImageLoader.enemyProjectileTex != null ? ImageLoader.enemyProjectileTex : ImageLoader.projTex,
            reusableVector.x,
            reusableVector.y,
            3
        );
        enemyProjectilesRef.add(projectile);
    }

    /**
     *  No object creation, use passed vector
     */
    private void getProjectileOrigin(Vector2 result) {
        switch(type) {
            case SHOOTER:
                result.set(pos.x - scaledWidth * 0.40f, pos.y + scaledHeight * 0.01f);
                break;
            case BOSS:
                result.set(pos.x - scaledWidth * 0.45f, pos.y + scaledHeight * 0.09f);
                break;
            default:
                result.set(pos.x - scaledWidth * 0.3f, pos.y);
                break;
        }
    }

    /**
     *  Texture caching sistem
     */
    private Texture getCurrentTexture() {
        if (!textureDirty && currentTexture != null) {
            return currentTexture;
        }

        // Update texture only when dirty
        currentTexture = getTextureForCurrentFrame();
        textureDirty = false;
        return currentTexture;
    }

    private Texture getTextureForCurrentFrame() {
        Texture[] frames = getFramesArray();

        if (frames != null && currentFrame >= 0 && currentFrame < frames.length) {
            Texture frameTexture = frames[currentFrame];
            if (frameTexture != null) {
                return frameTexture;
            }
        }

        return tex; // Fallback to original texture
    }

    /**
     *  Single method untuk frame arrays
     */
    private Texture[] getFramesArray() {
        return switch (type) {
            case BASIC -> ImageLoader.enemyBasicFrames;
            case SHIELD -> ImageLoader.enemyShieldFrames;
            case SHOOTER -> ImageLoader.enemyShooterFrames;
            case BOMBER -> ImageLoader.enemyBomberFrames;
            default -> null;
        };
    }

    /**
     *  Rendering dengan cached values
     */
    public void drawBatch(SpriteBatch batch) {
        if (tex != null) {
            // Set color berdasarkan status
            if (isKnockedBack) {
                batch.setColor(1f, 0.5f, 0.5f, 1f);
            } else if (isSlowed) {
                batch.setColor(0.5f, 0.5f, 1f, 1f);
            } else {
                batch.setColor(1f, 1f, 1f, 1f);
            }

            batch.draw(
                getCurrentTexture(),
                pos.x - halfWidth,
                pos.y - halfHeight,
                scaledWidth,
                scaledHeight
            );

            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    public void drawShape(ShapeRenderer shapes) {
        if (tex == null) {
            if (isKnockedBack) {
                shapes.setColor(Color.ORANGE);
            } else if (isSlowed) {
                shapes.setColor(Color.BLUE);
            } else {
                shapes.setColor(Color.RED);
            }

            shapes.circle(pos.x, pos.y, halfWidth);
        }
    }

    public void seekProtection(Array<Enemy> allEnemies) {
        if (type != EnemyType.BASIC) return;

        Enemy nearestShield = null;
        float nearestDistance = Float.MAX_VALUE;

        for (Enemy e : allEnemies) {
            if (e.type == EnemyType.SHIELD && !e.isDestroyed()) {
                float distance = Math.abs(pos.x - e.pos.x);
                if (distance < nearestDistance && distance < 100f) {
                    nearestDistance = distance;
                    nearestShield = e;
                }
            }
        }

        if (nearestShield != null && pos.x < nearestShield.pos.x) {
            pos.x = nearestShield.pos.x + 30f;
        }
    }

    public void knockback() {
        if (!isKnockedBack) {
            isKnockedBack = true;
            knockbackTimer = KNOCKBACK_DURATION;
            attackCooldown = ATTACK_COOLDOWN_DURATION;
        }
    }

    public void setReferences(Array<Tower> towers, Array<EnemyProjectile> enemyProjectiles) {
        this.towersRef = towers;
        this.enemyProjectilesRef = enemyProjectiles;
    }

    public boolean canAttack() { return attackCooldown <= 0 && !isKnockedBack; }
    public void takeDamage(int dmg) { health = Math.max(0, health - dmg); }
    public boolean isDestroyed() { return health <= 0; }

    public void slow(float duration) {
        this.slowDuration = duration;
        this.slowStrength = 0.5f;
        this.isSlowed = true;
    }

    public void slowHeavy(float duration, float strength) {
        this.slowDuration = duration;
        this.slowStrength = strength;
        this.isSlowed = true;
    }

    public boolean hasReachedTarget() {
        return type == EnemyType.BOSS && (hasReachedTargetPosition || state == EnemyState.STATIONARY);
    }

    // ===== GETTERS =====
    public float getX() { return pos.x; }
    public float getWidth() { return scaledWidth; }
    public Rectangle getBounds() { return bounds; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public EnemyType getType() { return type; }
    public EnemyState getState() { return state; }
}
