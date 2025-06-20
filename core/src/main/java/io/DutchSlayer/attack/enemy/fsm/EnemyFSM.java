package io.DutchSlayer.attack.enemy.fsm;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import io.DutchSlayer.attack.enemy.BasicEnemy;

public class EnemyFSM {

    private final StateMachine<BasicEnemy, EnemyState> stateMachine;

    public EnemyFSM(BasicEnemy enemy) {
        this.stateMachine = new DefaultStateMachine<>(enemy, EnemyState.PATROL);
    }
    public void update() {
        stateMachine.update();
    }
    public void changeState(EnemyState newState) {
        stateMachine.changeState(newState);
    }
    public EnemyState getCurrentState() {
        return stateMachine.getCurrentState();
    }
}
