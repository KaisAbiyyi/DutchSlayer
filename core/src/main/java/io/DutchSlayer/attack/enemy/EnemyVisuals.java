package io.DutchSlayer.attack.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.attack.enemy.fsm.EnemyState; // Import EnemyState

public class EnemyVisuals {

    private final AttackType attackType;

    private Texture idleTexture;
    private TextureRegion idleFrame;
    private Texture deadTexture;

    private final Array<Texture> runTextures;
    private Animation<TextureRegion> runAnimation;

    private float stateTime = 0f;

    public EnemyVisuals(AttackType type) {
        this.attackType = type;
        this.runTextures = new Array<>();
        loadAssets();
    }

    private void loadAssets() {
        deadTexture = new Texture(Gdx.files.internal("enemy/enemy_dead.png"));

        Array<TextureRegion> runFrames = new Array<>();

        if (attackType == AttackType.BURST_FIRE) {

            idleTexture = new Texture(Gdx.files.internal("enemy/enemy_run_ar1.png"));
            idleFrame = new TextureRegion(idleTexture);
            runTextures.add(idleTexture);
            runFrames.add(idleFrame);

            for (int i = 2; i <= 4; i++) {
                Texture runTex = new Texture(Gdx.files.internal("enemy/enemy_run_ar" + i + ".png"));
                runTextures.add(runTex);
                runFrames.add(new TextureRegion(runTex));
            }

            runAnimation = new Animation<>(0.1f, runFrames, Animation.PlayMode.LOOP);

        } else {
            idleTexture = new Texture(Gdx.files.internal("enemy/enemy_run1.png"));
            idleFrame = new TextureRegion(idleTexture);

            for (int i = 1; i <= 5; i++) {
                Texture runTex = new Texture(Gdx.files.internal("enemy/enemy_run" + i + ".png"));
                if (i > 1 || !runTex.equals(idleTexture)) {
                    runTextures.add(runTex);
                }
                runFrames.add(new TextureRegion(runTex));
            }
            if (!runTextures.contains(idleTexture, true)) {
                runTextures.add(idleTexture);
            }

            runAnimation = new Animation<>(0.1f, runFrames, Animation.PlayMode.LOOP);
        }
    }

    public TextureRegion getFrameToRender(EnemyState currentState, boolean movingRight, float deltaTime) {
        if (currentState == EnemyState.DYING) {
            TextureRegion deadRegion = new TextureRegion(deadTexture);
            if (movingRight && !deadRegion.isFlipX()) {
                deadRegion.flip(true, false);
            } else if (!movingRight && deadRegion.isFlipX()) {
                deadRegion.flip(true, false);
            }
            return deadRegion;
        }

        this.stateTime += deltaTime;
        TextureRegion region;

        if (currentState == EnemyState.PATROL || currentState == EnemyState.CHASE) {
            region = runAnimation.getKeyFrame(this.stateTime, true);
        } else {
            region = idleFrame;
        }

        if (movingRight && !region.isFlipX()) {
            region.flip(true, false);
        } else if (!movingRight && region.isFlipX()) {
            region.flip(true, false);
        }

        return region;
    }

    public void dispose() {
        if (deadTexture != null) {
            deadTexture.dispose();
        }
        if (idleTexture != null) {
            idleTexture.dispose();
        }
        for (Texture tex : runTextures) {
            if (tex != null) {
                tex.dispose();
            }
        }
        runTextures.clear();
    }
}
