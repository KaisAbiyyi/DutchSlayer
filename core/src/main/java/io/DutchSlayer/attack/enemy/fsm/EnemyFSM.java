package io.DutchSlayer.attack.enemy.fsm;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import io.DutchSlayer.attack.enemy.BasicEnemy;

/**
 * Kelas pembungkus untuk FSM (Finite State Machine) musuh tipe BasicEnemy.
 * Mengelola transisi dan eksekusi state melalui Gdx-AI.
 */
public class EnemyFSM {

    private final StateMachine<BasicEnemy, EnemyState> stateMachine;

    public EnemyFSM(BasicEnemy enemy) {
        // Inisialisasi FSM dengan state awal: PATROL
        this.stateMachine = new DefaultStateMachine<>(enemy, EnemyState.PATROL);
    }

    /**
     * Menjalankan logika update FSM. Harus dipanggil dari Enemy.update().
     */
    public void update() {
        stateMachine.update();
    }

    /**
     * Ganti state musuh.
     *
     * @param newState state tujuan
     */
    public void changeState(EnemyState newState) {
        stateMachine.changeState(newState);
    }

    /**
     * Mendapatkan state aktif saat ini.
     */
    public EnemyState getCurrentState() {
        return stateMachine.getCurrentState();
    }
}
