package io.DutchSlayer.attack.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.attack.boss.TankBoss;
import io.DutchSlayer.attack.player.weapon.Bullet;
import io.DutchSlayer.attack.player.weapon.Weapon;
import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.utils.Constant;

public class Player {

    private final PlayerState playerState;
    private final PlayerVisuals visualsHandler;
    private final PlayerMovement movementHandler;
    private PlayerCombat combatHandler;

    private final OrthographicCamera camera;
    private TankBoss boss;

    private final Array<Bullet> bullets;

    private final Rectangle bounds;

    public Player(OrthographicCamera camera) {
        this.camera = camera;

        this.playerState = new PlayerState();
        this.playerState.x = Constant.PLAYER_START_X;
        this.playerState.y = Constant.TERRAIN_HEIGHT;
        this.playerState.lives = 5;
        this.playerState.grenadeAmmo = 5;

        this.playerState.playerWidth = Constant.PLAYER_WIDTH;
        this.playerState.normalPlayerHeight = Constant.PLAYER_HEIGHT;
        this.playerState.duckPlayerHeight = Constant.PLAYER_HEIGHT / 2.5f;
        this.playerState.playerHeight = this.playerState.normalPlayerHeight;

        this.visualsHandler = new PlayerVisuals();
        this.movementHandler = new PlayerMovement();

        this.bullets = new Array<>();

        this.bounds = new Rectangle(
            this.playerState.x,
            this.playerState.y,
            this.playerState.playerWidth,
            this.playerState.playerHeight
        );
    }

    public void setBoss(TankBoss boss) {
        this.boss = boss;
    }

    public void setGameScreen(GameScreen gameScreen) {
        if (this.combatHandler == null && gameScreen != null) {
            this.combatHandler = new PlayerCombat(this, gameScreen);
        }
    }

    public void update(float delta) {
        if (combatHandler == null) {
            if (movementHandler != null) {
                movementHandler.update(delta, playerState);
            }
            if (this.bounds != null) {
                this.bounds.set(playerState.x, playerState.y, playerState.playerWidth, playerState.playerHeight);
            }
            return;
        }

        boolean fireInput = Gdx.input.isKeyJustPressed(Input.Keys.J);
        boolean grenadeInput = Gdx.input.isKeyJustPressed(Input.Keys.K);

        movementHandler.update(delta, playerState);

        combatHandler.update(delta, playerState, fireInput, grenadeInput);

        if (!playerState.isDead &&
            playerState.invincibilityTimer == combatHandler.getInvincibilityDurationPlayer() &&
            playerState.lives > 0) {

            float newRespawnX = camera.position.x - (camera.viewportWidth / 2f);

            newRespawnX = Math.max(newRespawnX, Constant.WALL_WIDTH);
            newRespawnX = Math.min(newRespawnX, Constant.MAP_WIDTH - playerState.playerWidth);

            respawn(newRespawnX, Constant.TERRAIN_HEIGHT);
        }

        if (boss != null) {
            checkBossCollision(boss);
        }

        float camLeft = camera.position.x - Constant.SCREEN_WIDTH / 2f;
        float camRight = camera.position.x + Constant.SCREEN_WIDTH / 2f;

        for (Bullet bullet : bullets) {
            bullet.update(delta, camLeft, camRight);
        }
        for (int i = bullets.size - 1; i >= 0; i--) {
            if (!bullets.get(i).isAlive()) {
                bullets.removeIndex(i);
            }
        }

        this.bounds.set(
            playerState.x,
            playerState.y,
            playerState.playerWidth,
            playerState.playerHeight
        );

        if (playerState.isDead && !playerState.isWaitingToRespawn) {
            playerState.y = -500f;
            playerState.vx = 0;
            playerState.vy = 0;
            this.bounds.set(playerState.x, playerState.y, playerState.playerWidth, playerState.playerHeight);
            combatHandler.update(delta, playerState, false, false);
        }
    }

    private void checkBossCollision(TankBoss boss) {
        if (isInvincible() || isDead() || playerState.isWaitingToRespawn) {
            return;
        }

        Rectangle playerRect = getBounds();
        Rectangle bossRect = new Rectangle(boss.getPosition().x, boss.getPosition().y, boss.getWidth(), boss.getHeight());

        if (playerRect.overlaps(bossRect)) {
            if (boss.isCharging()) {
                System.out.println("[Player] Collided with charging boss! Taking death.");
                takeDeath();
            }
        }
    }

    public void render(SpriteBatch spriteBatch, float delta) {
        if (visualsHandler == null) return;

        boolean isRunning = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D);
        if (playerState.isDashing) isRunning = true;

        String currentWeaponName = getCurrentWeaponName();
        TextureRegion region = visualsHandler.getFrameToRender(playerState, isRunning, delta, currentWeaponName);

        if (region == null) {
            for (Bullet bullet : bullets) {
                bullet.render(spriteBatch);
            }
            return;
        }

        float renderY = playerState.y;

        if (playerState.isDead && playerState.isWaitingToRespawn) {
            renderY -= 25f;
        }

        float renderHeight = playerState.playerHeight;
        if (playerState.isDucking) {
            renderHeight = playerState.normalPlayerHeight - 40f;
        }

        float renderWidth = playerState.playerWidth;
        float renderX = playerState.x;

        if (playerState.isDashing) {
            renderWidth *= 1.15f;
            renderX -= (renderWidth - playerState.playerWidth) / 2f;
        }

        float scaleHorizontal = 1.25f + 0.3f;
        float scaleVertical = 1.25f;

        spriteBatch.draw(
            region,
            renderX,
            renderY,
            0, 0,
            renderWidth,
            renderHeight,
            scaleHorizontal, scaleVertical,
            0
        );

        for (Bullet bullet : bullets) {
            bullet.render(spriteBatch);
        }
    }

    public void respawn(float newXBase, float newYRespawnPlatform) {
        playerState.x = newXBase;
        playerState.y = newYRespawnPlatform + 400f;
        playerState.vx = 0;
        playerState.vy = -300f;
        playerState.isJumping = true;
        playerState.isDucking = false;
        playerState.isDashing = false;
        playerState.dashActiveTimer = 0f;
        System.out.println("Player physically respawned at: " + playerState.x + ", " + playerState.y + " and falling.");
    }


    public void dispose() {
        if (visualsHandler != null) {
            visualsHandler.dispose();
        }
        if (combatHandler != null) {
            combatHandler.dispose();
        }

        if (movementHandler != null) {
            movementHandler.dispose();
        }
    }

    public PlayerState getPlayerState() { // Untuk akses internal jika dibutuhkan, atau oleh GameLogicHandler
        return playerState;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setX(float newX) {
        playerState.x = newX;
        if (bounds != null) bounds.setX(newX);
    }

    public float getX() {
        return playerState.x;
    }

    public float getY() {
        return playerState.y;
    }

    public float getWidth() {
        return playerState.playerWidth;
    }

    public float getHeight() {
        return playerState.playerHeight;
    }

    public boolean isDead() {
        return playerState.isDead;
    }

    public int getLives() {
        return playerState.lives;
    }

    public boolean isInvincible() {
        return combatHandler != null ? combatHandler.isInvincible(playerState) : playerState.invincibilityTimer > 0f;
    }

    public boolean isHitByExplosion(float explosionX, float explosionY, float explosionRadius) {
        if (isInvincible()) {
            return false;
        }

        if (playerState.isDead || playerState.isWaitingToRespawn) {
            return false;
        }

        Rectangle playerBounds = getBounds();

        float closestX = Math.max(playerBounds.x, Math.min(explosionX, playerBounds.x + playerBounds.width));
        float closestY = Math.max(playerBounds.y, Math.min(explosionY, playerBounds.y + playerBounds.height));

        float distanceX = explosionX - closestX;
        float distanceY = explosionY - closestY;
        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);

        return distanceSquared < (explosionRadius * explosionRadius);
    }

    public void takeDeath() {
        if (combatHandler != null) {
            combatHandler.takeDeath(playerState);
        } else {
            if (playerState.isDead || playerState.lives <= 0) return;
            playerState.isDead = true;
            playerState.lives--;
            playerState.isWaitingToRespawn = true;
        }
    }

    public void setWeapon(Weapon weapon) {
        if (combatHandler != null) {
            combatHandler.setWeapon(weapon);
        }
    }

    public Weapon getCurrentWeapon() {
        return combatHandler != null ? combatHandler.getCurrentWeapon() : null;
    }

    public String getCurrentWeaponName() {
        return combatHandler != null ? combatHandler.getCurrentWeaponName() : "None";
    }

    public int getCurrentAmmo() {
        return combatHandler != null ? combatHandler.getCurrentAmmo() : -1;
    }

    public void pickupGrenade(int amount) {
        if (combatHandler != null) {
            combatHandler.pickupGrenade(playerState, amount);
        }
    }

    public int getGrenadeAmmo() {
        return playerState.grenadeAmmo;
    }

    public boolean isFacingRight() {
        return playerState.facingRight;
    }

    public float getFireAngle() {
        boolean keyLeft = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean keyRight = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean keyUp = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean keyDown = Gdx.input.isKeyPressed(Input.Keys.S);

        if (keyUp) return (float) Math.PI / 2;
        if (keyDown && playerState.isJumping) return (float) -Math.PI / 2;
        if (keyLeft) return (float) Math.PI;
        if (keyRight) return 0f;

        return playerState.facingRight ? 0f : (float) Math.PI;
    }

    public float getFireY() {
        boolean keyDown = Gdx.input.isKeyPressed(Input.Keys.S);
        return keyDown ? playerState.y + playerState.playerHeight * 0.725f : playerState.y + playerState.playerHeight * 0.80f;
    }

    public Array<Bullet> getBullets() {
        return bullets;
    }

    public int getDashCount() {
        return playerState.dashCooldown <= 0f ? 1 : 0;
    }

    public float getDashCooldownRemaining() {
        return playerState.dashCooldown;
    }

    public float getDuckCooldownRemaining() {
        return playerState.duckCooldownTimer;
    }

    public float getDuckTimeRemaining() {
        return playerState.duckTimer;
    }

    public boolean isDucking() {
        return playerState.isDucking;
    }

    public Vector2 getPosition() {
        return new Vector2(playerState.x, playerState.y);
    }
}
