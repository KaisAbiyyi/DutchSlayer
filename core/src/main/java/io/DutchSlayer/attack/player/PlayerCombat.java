package io.DutchSlayer.attack.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import io.DutchSlayer.attack.player.weapon.AssaultRifle;
import io.DutchSlayer.attack.player.weapon.Grenade;
import io.DutchSlayer.attack.player.weapon.Pistol;
import io.DutchSlayer.attack.player.weapon.Weapon;
import io.DutchSlayer.attack.screens.GameScreen;

public class PlayerCombat {

    private Weapon currentWeapon;
    private final Player playerRef;
    private final GameScreen gameScreenRef;
    private final Sound throwSound;
    private final Sound playerDiesSound;
    private final float INVINCIBILITY_DURATION_PLAYER = 3.0f;

    public PlayerCombat(Player playerReference, GameScreen gameScreenReference) {
        this.playerRef = playerReference;
        this.gameScreenRef = gameScreenReference;
        this.currentWeapon = new Pistol();
        this.throwSound = Gdx.audio.newSound(Gdx.files.internal("player/grenade_throw.mp3"));
        this.playerDiesSound = Gdx.audio.newSound(Gdx.files.internal("player/player_dies.mp3"));
    }

    public void update(float delta, PlayerState playerState, boolean fireInput, boolean grenadeInput) {
        if (playerState.invincibilityTimer > 0f)
            playerState.invincibilityTimer -= delta;
        if (playerState.grenadeTimer > 0f) playerState.grenadeTimer -= delta;

        if (playerState.isWaitingToRespawn) {
            playerState.respawnTimer -= delta;
            if (playerState.respawnTimer <= 0f) {
                playerState.isWaitingToRespawn = false;
                performCombatRespawn(playerState);
            }
            return;
        }

        if (playerState.isDead) return;

        if (playerState.fireTimer > 0f) playerState.fireTimer -= delta;

        if (fireInput && playerState.fireTimer <= 0f) {
            fireWeapon();
            float PLAYER_FIRE_COOLDOWN = 0.25f;
            playerState.fireTimer = PLAYER_FIRE_COOLDOWN;
        }

        if (currentWeapon != null) {
            currentWeapon.updateBurst(this.playerRef, delta);
        }

        if (grenadeInput && playerState.grenadeTimer <= 0f) {
            throwGrenade(playerState);
            float GRENADE_COOLDOWN_PLAYER = 1.0f;
            playerState.grenadeTimer = GRENADE_COOLDOWN_PLAYER;
        }
    }

    private void fireWeapon() {
        if (currentWeapon == null || currentWeapon.isOutOfAmmo()) {
            setWeapon(new Pistol());
        }
        currentWeapon.fire(this.playerRef);
    }

    private void throwGrenade(PlayerState playerState) {
        if (playerState.grenadeAmmo <= 0) return;

        Texture grenadeTex = gameScreenRef.getGrenadeTexture();
        Texture explosionTex = gameScreenRef.getExplosionTexture();
        throwSound.play(0.8f);
        float centerX = playerState.x + playerState.playerWidth / 2;
        float centerY = playerState.y + playerState.playerHeight * 0.5f;
        float angle = playerState.facingRight ? 0.5f : (float) Math.PI - 0.5f;
        float power = 800f;

        if (gameScreenRef != null) {
            gameScreenRef.getGrenades().add(new Grenade(centerX, centerY, angle, power, false, grenadeTex, explosionTex));
        } else {
            System.err.println("PlayerCombat: GameScreen reference is null, cannot throw grenade.");
        }
        playerState.grenadeAmmo--;
    }

    public void takeDeath(PlayerState playerState) {
        if ((playerState.isDead && playerState.isWaitingToRespawn) || (playerState.isDead && playerState.lives < 0)) {
            return;
        }
        if (playerState.invincibilityTimer > 0f) return;

        if (playerDiesSound != null) {
            playerDiesSound.play(0.8f);
        }

        playerState.lives--;
        if (playerState.lives < 0) {
            playerState.isDead = true;
            playerState.isWaitingToRespawn = false;
            playerState.x = -10000f;
            playerState.y = -10000f;
            setWeapon(new Pistol());
            System.out.println("PlayerCombat: Player GAME OVER. Final death.");
            return;
        }

        playerState.isDead = true;
        float RESPAWN_DELAY_PLAYER = 2.0f;
        playerState.respawnTimer = RESPAWN_DELAY_PLAYER;
        playerState.isWaitingToRespawn = true;
        setWeapon(new Pistol());
        System.out.println("PlayerCombat: Player died! Remaining lives: " + playerState.lives);
    }

    public void performCombatRespawn(PlayerState playerState) {
        playerState.isDead = false;
        playerState.invincibilityTimer = INVINCIBILITY_DURATION_PLAYER;

        setWeapon(new Pistol());
        playerState.grenadeAmmo = 5;
        System.out.println("PlayerCombat: Player combat state respawned. Lives: " + playerState.lives);
    }

    public void setWeapon(Weapon weapon) {
        this.currentWeapon = weapon;
    }

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    public String getCurrentWeaponName() {
        return currentWeapon != null ? currentWeapon.getName() : "None";
    }

    public int getCurrentAmmo() {
        if (currentWeapon instanceof AssaultRifle rifle) {
            return rifle.getAmmo();
        }
        return -1;
    }

    public float getInvincibilityDurationPlayer() {
        return INVINCIBILITY_DURATION_PLAYER;
    }

    public void pickupGrenade(PlayerState playerState, int amount) {
        playerState.grenadeAmmo += amount;
    }

    public boolean isInvincible(PlayerState playerState) {
        return playerState.invincibilityTimer > 0f;
    }

    public void dispose() {
        if (throwSound != null) {
            throwSound.dispose();
        }
        if (playerDiesSound != null) {
            playerDiesSound.dispose();
        }
    }

}
