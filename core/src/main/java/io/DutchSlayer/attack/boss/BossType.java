package io.DutchSlayer.attack.boss;

public enum BossType {

    TANK(
        "Ironback"
    );

    private final String displayName;

    BossType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
