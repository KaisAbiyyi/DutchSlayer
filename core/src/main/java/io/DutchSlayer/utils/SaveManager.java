// io.DutchSlayer.utils.SaveManager.java
package io.DutchSlayer.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class SaveManager {

    private static final String PREFS_NAME = "game_save";
    private static final String MAX_STAGE_KEY = "max_stage";

    public static int getUnlockedStage() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        return prefs.getInteger(MAX_STAGE_KEY, 1); // default hanya stage 1 terbuka
    }

    public static void unlockStage(int stageNumber) {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        int current = getUnlockedStage();
        if (stageNumber > current) {
            prefs.putInteger(MAX_STAGE_KEY, stageNumber);
            prefs.flush();
        }
    }
}
