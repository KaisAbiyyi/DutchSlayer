package io.DutchSlayer.attack.enemy.fsm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import io.DutchSlayer.attack.enemy.BasicEnemy;

public enum EnemyState implements State<BasicEnemy> {

    PATROL {
        @Override
        public void enter(BasicEnemy enemy) {
        }

        @Override
        public void update(BasicEnemy enemy) {
            enemy.updatePatrol();
        }

        @Override
        public void exit(BasicEnemy enemy) {
        }
    },

    CHASE {
        @Override
        public void enter(BasicEnemy enemy) {
            enemy.setChasePrepared(false);
            enemy.setChaseDelayTimer(2f);
        }

        @Override
        public void update(BasicEnemy enemy) {
            enemy.updateChase();
        }

        @Override
        public void exit(BasicEnemy enemy) {
        }
    },

    SHOOT {
        @Override
        public void enter(BasicEnemy enemy) {
        }

        @Override
        public void update(BasicEnemy enemy) {
            enemy.updateShoot();
        }

        @Override
        public void exit(BasicEnemy enemy) {
        }
    },

    DYING {
        @Override
        public void enter(BasicEnemy enemy) {
            enemy.setDeathTimer(1.0f);
        }

        @Override
        public void update(BasicEnemy enemy) {
        }

        @Override
        public void exit(BasicEnemy enemy) {
        }
    };

    @Override
    public boolean onMessage(BasicEnemy entity, Telegram telegram) {
        return false;
    }
}
