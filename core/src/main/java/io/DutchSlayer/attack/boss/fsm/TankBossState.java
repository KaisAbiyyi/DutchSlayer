package io.DutchSlayer.attack.boss.fsm;

public enum TankBossState {
    IDLE,
    ENTERING_ARENA,
    PERFORMING_BURST,
    BURST_COOLDOWN,
    PRE_GRENADE_DELAY,
    PERFORMING_GRENADE_TOSS,
    GRENADE_TOSS_COOLDOWN,
    PREPARE_CHARGE,
    CHARGE,
    DEAD
}
