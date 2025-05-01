package io.DutchSlayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.DutchSlayer.Main;
import io.DutchSlayer.objects.Tree;
import io.DutchSlayer.enemy.Enemy;
import io.DutchSlayer.player.Player;
import io.DutchSlayer.player.Bullet;
import io.DutchSlayer.utils.Constant;
import io.DutchSlayer.assets.AssetLoader;

public class GameScreen implements Screen {

    private final Main game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final Player player;
    private final Array<Tree> trees;
    private final Array<Enemy> enemies;
    private final Texture bgTree;
    private final Texture terrain;

    public GameScreen(Main game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, camera);
        this.viewport.apply();

        AssetLoader.load();

        this.player = new Player();
        this.trees = new Array<>();
        this.enemies = new Array<>();

        this.bgTree = AssetLoader.bgTree;
        this.terrain = AssetLoader.terrain;

        generateTrees();
        spawnEnemies();
    }

    private void generateTrees() {
        int targetCount = MathUtils.random(Constant.TREE_MIN_COUNT * 3, Constant.TREE_MAX_COUNT * 4);
        int maxAttempts = targetCount * 5;
        int placed = 0;
        int attempts = 0;

        while (placed < targetCount && attempts < maxAttempts) {
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

    private void spawnEnemies() {
        int count = 15;
        for (int i = 0; i < count; i++) {
            float x = MathUtils.random(Constant.SCREEN_WIDTH, Constant.MAP_WIDTH - Constant.PLAYER_WIDTH);
            enemies.add(new Enemy(x, Constant.TERRAIN_HEIGHT));
        }
    }

    private void checkBulletEnemyCollision() {
        Array<Bullet> bullets = player.getBullets();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            float ex = enemy.getX();
            float ey = enemy.getY();
            float ew = enemy.getWidth();
            float eh = enemy.getHeight();

            for (Bullet bullet : bullets) {
                if (!bullet.isAlive()) continue;

                float bx = bullet.getX();
                float by = bullet.getY();
                float bw = bullet.getWidth();
                float bh = bullet.getHeight();

                boolean overlap =
                    bx < ex + ew && bx + bw > ex &&
                        by < ey + eh && by + bh > ey;

                if (overlap) {
                    bullet.kill();
                    enemy.takeHit();
                    break;
                }
            }
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.53f, 0.81f, 0.92f, 1);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        player.update(delta);

        for (Enemy enemy : enemies) {
            float leftBound = camera.position.x - Constant.SCREEN_WIDTH / 2f;
            float rightBound = camera.position.x + Constant.SCREEN_WIDTH / 2f;
            enemy.update(delta, new Vector2(player.getX(), player.getY()), leftBound, rightBound);
        }

        checkBulletEnemyCollision();

        float camX = MathUtils.clamp(
            player.getX(),
            Constant.SCREEN_WIDTH / 2f,
            Constant.MAP_WIDTH - Constant.SCREEN_WIDTH / 2f
        );
        camera.position.set(camX, Constant.SCREEN_HEIGHT / 2f, 0);
        camera.update();
        viewport.apply();

        shapeRenderer.setProjectionMatrix(camera.combined);
        spriteBatch.setProjectionMatrix(camera.combined);

        spriteBatch.begin();
        for (float x = 0; x < Constant.MAP_WIDTH; x += bgTree.getWidth()) {
            spriteBatch.draw(bgTree, x, Constant.TERRAIN_HEIGHT -12f);
        }

        float terrainHeightScale = Constant.TERRAIN_HEIGHT + 26f;
        float scale = terrainHeightScale / terrain.getHeight();
        float scaledWidth = terrain.getWidth() * scale;

        for (float x = 0; x < Constant.MAP_WIDTH; x += scaledWidth) {
            spriteBatch.draw(terrain, x, 0, scaledWidth, terrainHeightScale);
        }

        for (int i = 0; i < trees.size; i++) {
            Tree current = trees.get(i);
            boolean overlap = false;
            for (int j = 0; j < trees.size; j++) {
                if (i == j) continue;
                if (current.overlaps(trees.get(j))) {
                    overlap = true;
                    break;
                }
            }
            current.render(spriteBatch, overlap);
        }
        spriteBatch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1f);
        shapeRenderer.rect(0, 0, Constant.WALL_WIDTH, Constant.SCREEN_HEIGHT);
        shapeRenderer.rect(Constant.MAP_WIDTH - Constant.WALL_WIDTH, 0, Constant.WALL_WIDTH, Constant.SCREEN_HEIGHT);

        for (Enemy enemy : enemies) {
            enemy.render(shapeRenderer);
        }
        player.render(shapeRenderer);
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        AssetLoader.dispose();
    }
}
