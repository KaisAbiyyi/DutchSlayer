package io.DutchSlayer.attack.boss;

public enum BossType {

    TANK(
        "Ironback",
        "Heavy tank with cannon, machine gun, and charging attack"
    ),
    HELICOPTER(
        "Vulture 9",
        "Aerial assault helicopter with missiles and mine drops"
    ),
    BARRACK(
        "Outpost Nest",
        "Enemy base that spawns soldiers and uses mounted weapons"
    ),
    WALKER(
        "VX-Beta",
        "Experimental walker with EMP, plasma beam, and energy orbs"
    ),
    COMMANDER(
        "The Dominion Hand",
        "Final boss with ranged attacks, summons, and tactical movement"
    );

    private final String displayName;
    private final String description;

    BossType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAttackPattern() {
        switch (this) {
            case TANK:
                return new String[]{
                    "Main Cannon Shot (AoE)",
                    "Machine Gun Burst",
                    "Charge Forward",
                    "Detached Turret (Phase 2)"
                };
            case HELICOPTER:
                return new String[]{
                    "Diagonal Bullet Spray",
                    "Missile Lock-On",
                    "Mine Drop",
                    "Zigzag Sweep (Phase 2)"
                };
            case BARRACK:
                return new String[]{
                    "Soldier Spawn",
                    "Mounted Gun Fire",
                    "Shockwave Burst",
                    "Elite Reinforcement (Phase 2)"
                };
            case WALKER:
                return new String[]{
                    "Plasma Beam",
                    "EMP Burst",
                    "Homing Orb",
                    "Stomp Tremor (Phase 2)"
                };
            case COMMANDER:
                return new String[]{
                    "Pistol Shot",
                    "Grenade Throw",
                    "Call Elite Unit",
                    "Teleport Blink",
                    "Flamethrower Charge (Phase 2)"
                };
            default:
                return new String[]{};
        }
    }
}
