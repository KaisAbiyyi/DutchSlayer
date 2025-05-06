package io.DutchSlayer.attack.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.attack.objects.Tree;
import io.DutchSlayer.attack.enemy.Enemy;
import io.DutchSlayer.attack.player.Player;
import io.DutchSlayer.attack.player.Bullet;
import io.DutchSlayer.utils.Constant;
import io.DutchSlayer.assets.AssetLoader;
import io.DutchSlayer.attack.player.Grenade;

public class GameScreen implements Screen {

    private final Main game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;
    private final Player player;
    private final Array<Tree> trees;
    private final Array<Enemy> enemies;
    private final Array<Grenade> grenades = new Array<>();

    private final Array<Vector2> respawnPoints = new Array<>();
    private boolean isGameOver = false;

    public GameScreen(Main game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, camera);
        this.viewport.apply();

        AssetLoader.load();

        this.player = new Player(camera);
        this.player.setGameScreen(this); // ✅ Penting! Set referensi agar bisa akses grenades

        this.trees = new Array<>();
        this.enemies = new Array<>();

        generateTrees();
        spawnEnemies();
        setupRespawnPoints();
        player.setGameScreen(this);
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
        for (int i = 0; i < 15; i++) {
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

                boolean overlap = bx < ex + ew && bx + bw > ex && by < ey + eh && by + bh > ey;
                if (overlap) {
                    bullet.kill();
                    enemy.takeHit();
                    break;
                }
            }
        }
    }

    private void checkEnemyBulletHitsPlayer() {
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            for (Bullet bullet : enemy.getBullets()) {
                if (!bullet.isAlive()) continue;

                float bx = bullet.getX();
                float by = bullet.getY();
                float bw = bullet.getWidth();
                float bh = bullet.getHeight();

                float px = player.getX();
                float py = player.getY();
                float pw = player.getWidth();
                float ph = player.getHeight();

                boolean hit = bx < px + pw && bx + bw > px && by < py + ph && by + bh > py;
                if (hit) {
                    bullet.kill();
                    player.takeDeath();
                    break;
                }
            }
        }
    }


    private void setupRespawnPoints() {
        // Misalnya 5 titik di sepanjang level (bisa kamu sesuaikan)
        respawnPoints.add(new Vector2(100, Constant.TERRAIN_HEIGHT));
        respawnPoints.add(new Vector2(800, Constant.TERRAIN_HEIGHT));
        respawnPoints.add(new Vector2(1600, Constant.TERRAIN_HEIGHT));
        respawnPoints.add(new Vector2(2400, Constant.TERRAIN_HEIGHT));
        respawnPoints.add(new Vector2(3200, Constant.TERRAIN_HEIGHT));
    }

    private void updateGrenades(float delta) {
        for (Grenade grenade : grenades) {
            grenade.update(delta);


            if (!grenade.isExploded()) {
                Rectangle grenadeRect = new Rectangle(grenade.getX() - 6f, grenade.getY() - 6f, 12f, 12f);
                for (Enemy enemy : enemies) {
                    if (!enemy.isAlive()) continue;

                    Rectangle enemyRect = new Rectangle(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());

                    if (grenadeRect.overlaps(enemyRect)) {
                        System.out.println("Collision detected - force exploding grenade");
                        grenade.forceExplode();
                        break;
                    }
                }
            }


            // Apply damage when grenade should deal damage
            if (grenade.shouldDealDamage()) {
                float explosionX = grenade.getX();
                float explosionY = grenade.getY() + 10f; // Tambahkan offset agar lebih sejajar ke tengah musuh
                for (Enemy enemy : enemies) {
                    if (!enemy.isAlive()) continue;
                    enemy.checkHitByExplosion(explosionX, explosionY, grenade.getRadius(), grenade.getDamage());
                }
                grenade.markDamageDealt();
            }

        }

        // Remove finished grenades
        for (int i = grenades.size - 1; i >= 0; i--) {
            if (grenades.get(i).isFinished()) {
                grenades.removeIndex(i);
            }
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.53f, 0.81f, 0.92f, 1); // Sky blue

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        if (!isGameOver) {
            checkEnemyBulletHitsPlayer();

            if (player.isDead()) {
                if (player.getLives() > 0) {
                    float newX = Math.max(player.getX() - 30f, 0f);
                    float newY = Constant.TERRAIN_HEIGHT;
                    player.respawn(newX, newY);
                } else {
                    isGameOver = true;
                    game.setScreen(new GameOverScreen(game));
                    return;
                }

            }
        }

        player.update(delta);

        for (Enemy enemy : enemies) {
            float leftBound = camera.position.x - Constant.SCREEN_WIDTH / 2f;
            float rightBound = camera.position.x + Constant.SCREEN_WIDTH / 2f;
            enemy.update(delta, new Vector2(player.getX(), player.getY()), leftBound, rightBound);
        }

        updateGrenades(delta);
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
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.5f, 0.35f, 0.2f, 1f);
        shapeRenderer.rect(0, 0, Constant.MAP_WIDTH, Constant.TERRAIN_HEIGHT + 26f);

        shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1f);
        shapeRenderer.rect(0, 0, Constant.WALL_WIDTH, Constant.SCREEN_HEIGHT);
        shapeRenderer.rect(Constant.MAP_WIDTH - Constant.WALL_WIDTH, 0, Constant.WALL_WIDTH, Constant.SCREEN_HEIGHT);

        for (Tree tree : trees) tree.render(shapeRenderer);
        for (Enemy enemy : enemies) enemy.render(shapeRenderer);
        player.render(shapeRenderer);
        for (Grenade grenade : grenades) grenade.render(shapeRenderer);

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
    public void show() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        AssetLoader.dispose();
    }

    public Array<Grenade> getGrenades() {
        return grenades;
    }
}
