package io.DutchSlayer.attack.enemy;

/**
 * Enum ini mendefinisikan semua jenis serangan yang dimiliki oleh BasicEnemy.
 * Setiap BasicEnemy hanya boleh memiliki satu AttackType.
 */
public enum AttackType {

    /**
     * Menembakkan satu peluru lurus ke arah horizontal.
     */
    STRAIGHT_SHOOT,

    /**
     * Menembak burst: 3 peluru dengan delay singkat antar peluru.
     */
    BURST_FIRE,

    /**
     * Melempar granat dalam lintasan parabola (arc).
     */
    ARC_GRENADE,

}
