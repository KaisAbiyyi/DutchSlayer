package io.DutchSlayer.attack.enemy;

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.utils.Constant;

public class EnemyFactory {
    public static Array<BasicEnemy> spawnDeterministicEnemies(int stageNumber, int count, float y, AttackType[] allowedTypes, GameScreen gameScreen) {
        RandomXS128 rng = new RandomXS128(stageNumber);
        Array<BasicEnemy> enemies = new Array<>();
        float mapWidth = Constant.MAP_WIDTH + (float) Math.pow(stageNumber, 1.2);

        float minX = Constant.WALL_WIDTH + 200f;
        float maxX = mapWidth - 200f;
        float screenOffset = Constant.SCREEN_WIDTH * 1.2f;

        float minGap = Constant.PLAYER_WIDTH * 2;
        int maxAttempts = count * 10;

        int placed = 0;
        int attempts = 0;

        while (placed < count && attempts < maxAttempts) {
            float rawX = rng.nextFloat() * (maxX - minX) + minX;
            float x = Math.min(rawX + screenOffset, mapWidth - 200f);
            boolean overlaps = false;
            for (BasicEnemy e : enemies) {
                if (Math.abs(e.getX() - x) < minGap) {
                    overlaps = true;
                    break;
                }
            }

            if (!overlaps) {
                AttackType type = allowedTypes[rng.nextInt(allowedTypes.length)];
                enemies.add(new BasicEnemy(type, x, y, gameScreen));
                placed++;
            }

            attempts++;
        }

        return enemies;
    }


}
