package io.DutchSlayer.attack.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class PlayerVisuals {
    private Texture idleTexture;
    private TextureRegion idleFrame;

    private final Array<Texture> runTextures;
    private Animation<TextureRegion> walkAnimation;

    private Texture deadTexture;
    private TextureRegion deadFrame;

    private Texture arIdleTexture;
    private TextureRegion arIdleFrame;
    private final Array<Texture> arRunTextures;
    private Animation<TextureRegion> arWalkAnimation;

    private Texture duckTexture;
    private TextureRegion duckFrame;

    private Texture arDuckTexture;
    private TextureRegion arDuckFrame;

    private float stateTime = 0f;

    private Texture dashTexture;
    private TextureRegion dashFrame;

    private Texture jumpTexture;
    private TextureRegion jumpFrame;

    private float blinkTimer = 0f;
    private boolean isVisible = true;

    public PlayerVisuals() {
        runTextures = new Array<>();
        arRunTextures = new Array<>();
        loadAssets();
    }

    private void loadAssets() {
        idleTexture = new Texture(Gdx.files.internal("player/player_idle.png"));
        idleFrame = new TextureRegion(idleTexture);

        Array<TextureRegion> runFrames = new Array<>();
        for (int i = 1; i <= 5; i++) {
            Texture runTex = new Texture(Gdx.files.internal("player/player_run" + i + ".png"));
            runTextures.add(runTex);
            TextureRegion runRegion = new TextureRegion(runTex);
            runFrames.add(runRegion);
        }
        walkAnimation = new Animation<>(0.1f, runFrames, Animation.PlayMode.LOOP);

        deadTexture = new Texture(Gdx.files.internal("player/player_dead.png"));
        deadFrame = new TextureRegion(deadTexture);

        arIdleTexture = new Texture(Gdx.files.internal("player/player_run_ar1.png"));
        arIdleFrame = new TextureRegion(arIdleTexture);

        Array<TextureRegion> arRunFrames = new Array<>();
        for (int i = 1; i <= 8; i++) {
            Texture arRunTex = new Texture(Gdx.files.internal("player/player_run_ar" + i + ".png"));
            arRunTextures.add(arRunTex);
            TextureRegion arRunRegion = new TextureRegion(arRunTex);
            arRunFrames.add(arRunRegion);
        }
        arWalkAnimation = new Animation<>(0.1f, arRunFrames, Animation.PlayMode.LOOP);

        duckTexture = new Texture(Gdx.files.internal("player/player_duck.png"));
        duckFrame = new TextureRegion(duckTexture);

        arDuckTexture = new Texture(Gdx.files.internal("player/player_duck_ar.png"));
        arDuckFrame = new TextureRegion(arDuckTexture);

        dashTexture = new Texture(Gdx.files.internal("player/player_dash.png"));
        dashFrame = new TextureRegion(dashTexture);

        jumpTexture = new Texture(Gdx.files.internal("player/player_jump.png"));
        jumpFrame = new TextureRegion(jumpTexture);
    }

    public TextureRegion getFrameToRender(PlayerState playerState, boolean isRunning, float deltaTime, String currentWeaponName) {
        this.stateTime += deltaTime;

        if (playerState.isInvincible()) {
            blinkTimer += deltaTime;
            float BLINK_INTERVAL = 0.1f;
            if (blinkTimer >= BLINK_INTERVAL) {
                isVisible = !isVisible;
                blinkTimer = 0f;
            }
            if (!isVisible) {
                return null;
            }
        } else {
            isVisible = true;
            blinkTimer = 0f;
        }

        TextureRegion region;

        if (playerState.isDead && playerState.isWaitingToRespawn) {
            region = deadFrame;
        } else if (playerState.isJumping) {
            region = jumpFrame;
        } else if (playerState.isDashing) {
            region = dashFrame;
        } else if (playerState.isDucking) {
            if ("Assault Rifle".equals(currentWeaponName)) {
                region = arDuckFrame;
            } else {
                region = duckFrame;
            }
        } else {
            if ("Assault Rifle".equals(currentWeaponName)) {
                if (isRunning) {
                    region = arWalkAnimation.getKeyFrame(this.stateTime, true);
                } else {
                    region = arIdleFrame;
                }
            } else {
                if (isRunning) {
                    region = walkAnimation.getKeyFrame(this.stateTime, true);
                } else {
                    region = idleFrame;
                }
            }
        }

        if (region != deadFrame) {
            if (playerState.facingRight && region.isFlipX()) {
                region.flip(true, false);
            } else if (!playerState.facingRight && !region.isFlipX()) {
                region.flip(true, false);
            }
        }
        return region;
    }

    public void dispose() {
        if (idleTexture != null) {
            idleTexture.dispose();
        }
        for (Texture tex : runTextures) {
            if (tex != null) {
                tex.dispose();
            }
        }
        runTextures.clear();
        if (deadTexture != null) {
            deadTexture.dispose();
        }

        if (arIdleTexture != null) {
            arIdleTexture.dispose();
        }
        for (Texture tex : arRunTextures) {
            if (tex != null) {
                tex.dispose();
            }
        }
        arRunTextures.clear();

        if (duckTexture != null) {
            duckTexture.dispose();
        }
        if (arDuckTexture != null) {
            arDuckTexture.dispose();
        }

        if (dashTexture != null) {
            dashTexture.dispose();
        }
        if (jumpTexture != null) {
            jumpTexture.dispose();
        }

    }
}
