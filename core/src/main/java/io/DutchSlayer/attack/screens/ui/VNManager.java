package io.DutchSlayer.attack.screens.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;// Pastikan import Constant
import io.DutchSlayer.utils.Constant;

public class VNManager implements Disposable {

    private final Array<VNScene> scenes;
    private int currentSceneIndex;

    private int currentTextIndex;
    private String currentDialogue;
    private final StringBuilder displayedText;

    private float textDisplayTimer;

    private boolean isDialogueComplete;
    private boolean isActive;

    private Texture currentBackground;
    private final BitmapFont font;
    private final GlyphLayout layout;

    public VNManager(BitmapFont font) {
        this.font = font;
        this.scenes = new Array<>();
        this.displayedText = new StringBuilder();
        this.layout = new GlyphLayout();
        reset();
    }

    public void addScene(VNScene scene) {
        scenes.add(scene);
    }

    public void start() {
        if (scenes.size == 0) {
            Gdx.app.log("VNManager", "No scenes added to VNManager.");
            return;
        }
        isActive = true;
        currentSceneIndex = 0;
        loadCurrentScene();
    }

    public void update(float delta) {
        if (!isActive) {
            return;
        }

        if (!isDialogueComplete) {
            textDisplayTimer += delta;
            float CHAR_PER_SECOND = 40.0f;
            int charsToDisplay = (int) (textDisplayTimer * CHAR_PER_SECOND);
            if (charsToDisplay > currentDialogue.length()) {
                charsToDisplay = currentDialogue.length();
                isDialogueComplete = true;
            }
            displayedText.setLength(0);
            displayedText.append(currentDialogue, 0, charsToDisplay);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (!isDialogueComplete) {
                displayedText.setLength(0);
                displayedText.append(currentDialogue);
                isDialogueComplete = true;
            } else {
                nextDialogue();
            }
        }
    }

    public int getSceneCount() {
        return scenes.size;
    }

    public void clearScenes() {
        scenes.clear();
        currentSceneIndex = 0;
    }

    public void render(SpriteBatch batch) {
        if (!isActive) {
            return;
        }

        if (currentBackground != null) {
            batch.draw(currentBackground, 0, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
            Gdx.app.log("VNManager", "Drawing background texture.");
        } else {
            Gdx.app.log("VNManager", "currentBackground is NULL. Check texture loading!");
        }

        float textX = 50;
        float textY = 200;
        float maxWidth = Constant.SCREEN_WIDTH - 100;

        font.setColor(1, 1, 1, 1);
        font.getData().setScale(1.2f);

        layout.setText(font, displayedText, font.getColor(), maxWidth, com.badlogic.gdx.utils.Align.left, true);
        font.draw(batch, layout, textX, textY + layout.height);

        if (isDialogueComplete) {
            String prompt = "Press SPACE";
            layout.setText(font, prompt);
            font.draw(batch, prompt, Constant.SCREEN_WIDTH - layout.width - 50, 50);
        }
    }

    private void loadCurrentScene() {
        VNScene scene = scenes.get(currentSceneIndex);
        currentBackground = scene.getBackgroundTexture();
        currentTextIndex = 0;
        loadCurrentDialogue();
    }

    private void loadCurrentDialogue() {
        VNScene scene = scenes.get(currentSceneIndex);
        currentDialogue = scene.getDialogues().get(currentTextIndex);
        displayedText.setLength(0);
        textDisplayTimer = 0f;
        isDialogueComplete = false;
    }

    private void nextDialogue() {
        currentTextIndex++;
        VNScene scene = scenes.get(currentSceneIndex);
        if (currentTextIndex < scene.getDialogues().size) {
            loadCurrentDialogue();
        } else {
            currentSceneIndex++;
            if (currentSceneIndex < scenes.size) {
                loadCurrentScene();
            } else {
                end();
            }
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void end() {
        isActive = false;
        reset();
        Gdx.app.log("VNManager", "Visual Novel finished.");
    }

    private void reset() {
        currentSceneIndex = 0;
        currentTextIndex = 0;
        currentDialogue = "";
        displayedText.setLength(0);
        textDisplayTimer = 0f;
        isDialogueComplete = false;
        isActive = false;
        currentBackground = null;
    }

    @Override
    public void dispose() {
    }
}
