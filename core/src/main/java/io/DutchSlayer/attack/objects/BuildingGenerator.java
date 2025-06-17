package io.DutchSlayer.attack.objects;

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.utils.Constant;

import java.util.Arrays;
import java.util.List;

public class BuildingGenerator {

    static class BlockDefinition {
        public final BuildingType type;
        public final int minCount;
        public final int maxCount;
        public final boolean prefersCluster;
        public final boolean prefersSpacing;

        public BlockDefinition(BuildingType type, int minCount, int maxCount, boolean prefersCluster, boolean prefersSpacing) {
            this.type = type;
            this.minCount = minCount;
            this.maxCount = maxCount;
            this.prefersCluster = prefersCluster;
            this.prefersSpacing = prefersSpacing;
        }
    }

    public static Array<Building> generate(float mapWidth, int stageNumber) {
        Array<Building> buildings = new Array<>();
        RandomXS128 rng = new RandomXS128(stageNumber);

        // Layout dasar tetap (tapi jumlah repeat lebih rasional)
        List<BlockDefinition> layout = Arrays.asList(
            new BlockDefinition(BuildingType.ADMIN_OFFICE, 1, 1, false, false),
            new BlockDefinition(BuildingType.FORT, 1, 1, false, false),
            new BlockDefinition(BuildingType.BARRACKS, 1, 2, true, false),
            new BlockDefinition(BuildingType.WAREHOUSE, 1, 1, false, false),
            new BlockDefinition(BuildingType.PLANTATION, 2, 3, true, false),
            new BlockDefinition(BuildingType.WAREHOUSE, 1, 1, false, false),
            new BlockDefinition(BuildingType.BARRACKS, 1, 1, true, false)
        );

        int zoneCount = Math.min(5, 2 + stageNumber / 2); // disederhanakan jadi maksimal 5 zona
        float zoneWidth = mapWidth / zoneCount;

        for (int z = 0; z < zoneCount; z++) {
            float zoneStart = z * zoneWidth + 80f;
            float zoneEnd = (z + 1) * zoneWidth - 80f;
            float currentX = zoneStart;

            for (BlockDefinition block : layout) {
                int repeat = rng.nextInt(block.maxCount - block.minCount + 1) + block.minCount;

                for (int i = 0; i < repeat; i++) {
                    if (currentX + block.type.width > zoneEnd) break;

                    float xOffset = rng.nextFloat() * 15f - 7.5f;
                    buildings.add(new Building(currentX + xOffset, block.type));

                    float spacing = block.prefersCluster
                        ? 50f + rng.nextFloat() * 20f    // antar barracks, plantation
                        : 100f + rng.nextFloat() * 40f;  // antar warehouse, fort, office

                    currentX += block.type.width + spacing;
                }

                // Spacing antar tipe bangunan berbeda
                currentX += 120f + rng.nextFloat() * 40f;
            }
        }

        return buildings;
    }

}
