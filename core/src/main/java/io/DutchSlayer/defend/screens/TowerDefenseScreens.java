package io.DutchSlayer.defend.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.defend.enemy.Enemy;
import io.DutchSlayer.utils.Constant;
import io.DutchSlayer.assets.AssetLoader;
import io.DutchSlayer.defend.objects.Tree;

import java.util.ArrayList;
import java.util.List;

public class TowerDefenseScreen implements Screen {
    private final Main game;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final SpriteBatch batch;

    private TurretManager turretManager;
    private WaveManager waveManager;
    private BaseHealthUI baseHealthUI;
    private int baseHealth;

    private Texture terrain;
    private Texture bgTree;
    private final List<Tree> trees;

    public TowerDefenseScreen(Main game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, camera);
        batch = new SpriteBatch();

        baseHealth = 100;

        AssetLoader.load();
        this.terrain = AssetLoader.terrain;
        this.bgTree = AssetLoader.bgTree;
        this.trees = new ArrayList<>();

        initDefenseSystem();k
        generateTrees();
    }

    private void initDefenseSystem() {
        turretManager = new TurretManager();
        waveManager = new WaveManager();
        baseHealthUI = new BaseHealthUI();
    }

    private void generateTrees() {
        int targetCount = 25;
        int placed = 0;
        int attempts = 0;

        while (placed < targetCount && attempts < 100) {
            Tree candidate = Tree.generateRandom(Constant.MAP_WIDTH);
            boolean tooClose = false;
            for (Tree existing : trees) {
                float dx = candidate.getX() - existing.getX();
                float dw = (candidate.getWidth() + existing.getWidth()) / 2f;

                if (candidate.getWidth() < existing.getWidth() * 0.75f) continue;

                if (Math.abs(dx) < dw + 10f) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) {
                trees.add(candidate);
                placed++;
            }
            attempts++;
        }
    }

    private void updateDefenseLogic(float delta) {
        turretManager.update(delta);
        waveManager.update(delta);

        List<io.DutchSlayer.defend.enemy.Enemy> enemies = waveManager.getEnemies();
        for (Enemy enemy : enemies) {
            if (enemy.reachedBase()) {
                baseHealth -= 10;
                enemy.setDead(true);
            }
        }

        enemies.removeIf(enemy -> !enemy.isAlive());

        if (baseHealth <= 0) {
            System.out.println("Game Over");
        }
    }

    private void renderDefenseWorld() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (float x = 0; x < Constant.MAP_WIDTH; x += bgTree.getWidth()) {
            batch.draw(bgTree, x, Constant.TERRAIN_HEIGHT - 12f);
        }

        float terrainHeightScale = Constant.TERRAIN_HEIGHT + 26f;
        float scale = terrainHeightScale / terrain.getHeight();
        float scaledWidth = terrain.getWidth() * scale;

        for (float x = 0; x < Constant.MAP_WIDTH; x += scaledWidth) {
            batch.draw(terrain, x, 0, scaledWidth, terrainHeightScale);
        }

        for (Tree tree : trees) {
            tree.render(batch, false);
        }

        turretManager.render(batch);
        waveManager.render(batch);
        baseHealthUI.render(batch, baseHealth);
        batch.end();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.12f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        updateDefenseLogic(delta);
        renderDefenseWorld();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        AssetLoader.dispose();
    }
}
