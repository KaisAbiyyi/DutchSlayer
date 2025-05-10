package io.DutchSlayer.defend;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.Main;

public class GameScreen implements Screen {
    private final Main game;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapes;
    private final BitmapFont font = new BitmapFont();

    private final Array<Tower> towers = new Array<>();
    private final Array<Enemy> enemies = new Array<>();
    private final Array<Projectile> projectiles = new Array<>();

    private float spawnTimer = 0f;
    private boolean isGameOver = false;

    private static final float GROUND_Y = 100f;

    public GameScreen(final Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        ImageLoader.load();
        shapes = new ShapeRenderer();

        // Tower utama (besar, bisa menembak, HP=10)
        float tyMain = GROUND_Y + (ImageLoader.towerTex.getHeight() * 0.3f)/2f;
        towers.add(new Tower(
            ImageLoader.towerTex,
            ImageLoader.projTex,
            100,    // x
            tyMain, // y
            0.5f,   // scale
            true    // canShoot
        ));

        // 3 tower lainnya (kecil, no shoot, HP=5)
        for (int i = 1; i < 4; i++) {
            float tx = 190 + i * 200;
            float ty = GROUND_Y + (ImageLoader.towerTex.getHeight() * 0.1f)/2f;
            towers.add(new Tower(
                ImageLoader.towerTex,
                ImageLoader.projTex,
                tx,
                ty,
                0.2f,
                false
            ));
        }
    }

    private void update(float delta) {
        if (isGameOver) return;

        // Spawn musuh
        spawnTimer += delta;
        if (spawnTimer > 2f) {
            spawnTimer = 0f;
            float ey = GROUND_Y + (ImageLoader.towerTex.getHeight() * 0.1f)/2f;
            enemies.add(new Enemy(ImageLoader.enemyTex, 1280, ey));
        }

        // Gerakkan & cek serangan enemy ke tower
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update(delta);

            boolean attacked = false;
            for (int j = towers.size - 1; j >= 0; j--) {
                Tower t = towers.get(j);
                if (e.getBounds().overlaps(t.getBounds())) {
                    // serang tower
                    t.takeDamage(1);
                    attacked = true;
                    // jika tower hancur
                    if (t.isDestroyed()) {
                        towers.removeIndex(j);
                        // jika itu tower utama → game over
                        if (t.canShoot) {
                            isGameOver = true;
                        }
                    }
                    break;
                }
            }

            if (attacked) {
                enemies.removeIndex(i);
                continue;
            }
            // hapus jika keluar layar kiri
            if (e.getX() < -e.getWidth()/2) {
                enemies.removeIndex(i);
            }
        }

        // Tower menembak
        for (Tower t : towers) {
            t.update(delta, enemies, projectiles);
        }

        // Update & cek tabrakan projectile→enemy
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);

            boolean hit = false;
            for (int j = enemies.size - 1; j >= 0; j--) {
                if (p.getBounds().overlaps(enemies.get(j).getBounds())) {
                    enemies.removeIndex(j);
                    hit = true;
                    break;
                }
            }
            if (hit || p.getX() > 1280 + p.getBounds().width/2) {
                projectiles.removeIndex(i);
            }
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1) Gambar ground
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(0, 0, 1280, GROUND_Y);
        shapes.end();

        // Jika game over → tampil teks dan stop di sini
        if (isGameOver) {
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            font.draw(game.batch, "GAME OVER", 1280/2f - 50, 720/2f);
            game.batch.end();
            return;
        }

        // 2) Gambar sprites
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        for (Tower t : towers)         t.drawBatch(game.batch);
        for (Enemy e : enemies)        e.drawBatch(game.batch);
        for (Projectile p : projectiles) p.drawBatch(game.batch);
        game.batch.end();

        // 3) Fallback shapes bila texture null
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Tower t : towers)         t.drawShape(shapes);
        for (Enemy e : enemies)        e.drawShape(shapes);
        for (Projectile p : projectiles) p.drawShape(shapes);
        shapes.end();
    }

    @Override
    public void resize(int width, int height) {

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

    // … metode kosong lainnya …

    @Override
    public void dispose() {
        shapes.dispose();
        ImageLoader.dispose();
    }
}
