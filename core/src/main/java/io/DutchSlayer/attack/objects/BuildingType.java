package io.DutchSlayer.attack.objects;

import com.badlogic.gdx.graphics.Texture;

import static io.DutchSlayer.utils.Constant.PLAYER_HEIGHT;
import static io.DutchSlayer.utils.Constant.PLAYER_WIDTH;

public enum BuildingType {

    BARRACKS("buildings/barracks.png", 6f * PLAYER_WIDTH, 4.2f * PLAYER_HEIGHT),
    WAREHOUSE("buildings/warehouse.png", 7.0f * PLAYER_WIDTH, 5.0f * PLAYER_HEIGHT),
    FORT("buildings/fort.png", 9.0f * PLAYER_WIDTH, 5.5f * PLAYER_HEIGHT),
    PLANTATION("buildings/plantation.png", 8.0f * PLAYER_WIDTH, 4.8f * PLAYER_HEIGHT),
    ADMIN_OFFICE("buildings/admin_office.png", 6.5f * PLAYER_WIDTH, 5.2f * PLAYER_HEIGHT);

    public final float width;
    public final float height;
    public final Texture texture;

    BuildingType(String path, float width, float height) {
        this.width = width;
        this.height = height;
        this.texture = new Texture(path); // otomatis load saat enum diakses
    }

    public static void disposeAll() {
        for (BuildingType type : BuildingType.values()) {
            type.texture.dispose();
        }
    }

}
