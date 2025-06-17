package io.DutchSlayer.attack.enemy.fsm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import io.DutchSlayer.attack.enemy.BasicEnemy;

/**
 * Enum ini mewakili semua state dalam FSM musuh biasa.
 * Setiap state bertanggung jawab memanggil logika update yang sesuai di BasicEnemy.
 */
public enum EnemyState implements State<BasicEnemy> {

    PATROL {
        @Override
        public void enter(BasicEnemy enemy) {
            // Bisa tambahkan animasi atau reset variabel di sini
        }

        @Override
        public void update(BasicEnemy enemy) {
            enemy.updatePatrol();
        }

        @Override
        public void exit(BasicEnemy enemy) {
            // Kosongkan jika tidak perlu
        }
    },

    CHASE {
        @Override
        public void enter(BasicEnemy enemy) {
            enemy.setChasePrepared(false);
            enemy.setChaseDelayTimer(2f); // 2 detik
        }

        @Override
        public void update(BasicEnemy enemy) {
            enemy.updateChase(); // tidak pakai Gdx.graphics.getDeltaTime()
        }

        @Override
        public void exit(BasicEnemy enemy) {
        }
    },

    SHOOT {
        @Override
        public void enter(BasicEnemy enemy) {
            // Bisa reset timer tembakan
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
            // Saat memasuki state ini, set timer kematian di musuh
            enemy.setDeathTimer(1.0f); // Durasi 1 detik
        }

        @Override
        public void update(BasicEnemy enemy) {
            // Logika update timer akan ada di BasicEnemy.update()
            // agar bisa terus berjalan bahkan saat FSM di state ini.
        }

        @Override
        public void exit(BasicEnemy enemy) {
            // Tidak ada aksi saat keluar
        }
    };

    @Override
    public boolean onMessage(BasicEnemy entity, Telegram telegram) {
        return false;
    }
}
