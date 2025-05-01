package io.DutchSlayer.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.utils.Constant;

public class AssetLoader {

    public static Array<Texture> treeTextures;
    public static Texture bgTree;
    public static Texture terrain;

    public static void load() {
        treeTextures = new Array<>();
        for (String path : Constant.TREE_TEXTURE_PATHS) {
            treeTextures.add(new Texture(Gdx.files.internal(path)));
        }
        bgTree = new Texture(Gdx.files.internal(Constant.BG_TREE_PATH));
        terrain = new Texture(Gdx.files.internal(Constant.TERRAIN_TEXTURE_PATH));
    }

    public static Texture getRandomTreeTexture() {
        if (treeTextures == null || treeTextures.size == 0) return null;
        return treeTextures.random();
    }

    public static void dispose() {
        if (treeTextures != null) {
            for (Texture texture : treeTextures) {
                texture.dispose();
            }
            treeTextures.clear();
        }
        if (bgTree != null) bgTree.dispose();
    }
}
