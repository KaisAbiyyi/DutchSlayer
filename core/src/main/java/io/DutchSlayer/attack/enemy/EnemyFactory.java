package io.DutchSlayer.attack.enemy;

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.utils.Constant;

/**
 * Factory untuk menciptakan berbagai jenis musuh BasicEnemy dengan fleksibel.
 */
public class EnemyFactory {

    /**
     * Membuat satu enemy dengan tipe dan posisi yang ditentukan.
     */

    /**
     * Membuat satu enemy dengan tipe acak pada posisi tertentu.
     */
    public static BasicEnemy createRandomEnemy(float x, float y, GameScreen gameScreen) {
        AttackType[] types = AttackType.values();
        AttackType randomType = types[(int) (Math.random() * types.length)];
        return new BasicEnemy(randomType, x, y, gameScreen);
    }

    /**
     * Membuat sekumpulan enemy yang terletak sejajar secara horizontal.
     */
    public static Array<BasicEnemy> spawnWave(int count, float startX, float y, float spacing, GameScreen gameScreen) {
        Array<BasicEnemy> enemies = new Array<>();
        for (int i = 0; i < count; i++) {
            float spawnX = startX + i * spacing;
            enemies.add(createRandomEnemy(spawnX, y, gameScreen));
        }
        return enemies;
    }

    /**
     * Membuat enemy dengan posisi acak dan tipe dari daftar yang diizinkan, deterministik berdasarkan stage.
     * Setiap stage akan menghasilkan susunan musuh yang sama selama stageNumber dan allowedTypes-nya sama.
     */
    public static Array<BasicEnemy> spawnDeterministicEnemies(int stageNumber, int count, float y, AttackType[] allowedTypes, GameScreen gameScreen) {
        RandomXS128 rng = new RandomXS128(stageNumber); // seed berdasarkan stage
        Array<BasicEnemy> enemies = new Array<>();
        float mapWidth = Constant.MAP_WIDTH + (float) Math.pow(stageNumber, 1.2);

        float minX = Constant.WALL_WIDTH + 200f;
        float maxX = mapWidth - 200f;
        float screenOffset = Constant.SCREEN_WIDTH * 1.2f;

        float minGap = Constant.PLAYER_WIDTH * 2; // Jarak minimal antar musuh
        int maxAttempts = count * 10; // Hindari infinite loop

        int placed = 0;
        int attempts = 0;

        while (placed < count && attempts < maxAttempts) {
            float rawX = rng.nextFloat() * (maxX - minX) + minX;
            float x = Math.min(rawX + screenOffset, mapWidth - 200f);
            boolean overlaps = false;

            // Cek apakah terlalu dekat dengan musuh lain
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
