package io.DutchSlayer.attack.screens.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class VNScene {
    private Texture backgroundTexture;
    private Array<String> dialogues;

    public VNScene(Texture backgroundTexture, Array<String> dialogues) {
        this.backgroundTexture = backgroundTexture;
        this.dialogues = dialogues;
    }

    public Texture getBackgroundTexture() {
        return backgroundTexture;
    }

    public Array<String> getDialogues() {
        return dialogues;
    }
}
