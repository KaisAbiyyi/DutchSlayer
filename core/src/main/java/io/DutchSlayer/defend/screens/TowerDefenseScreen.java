package io.DutchSlayer.defend.screens;

import io.DutchSlayer.defend.objects.Tower;
import io.DutchSlayer.defend.enemy.Enemy;
import io.DutchSlayer.defend.objects.Projectile;
import io.DutchSlayer.defend.ui.ImageLoader;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.DutchSlayer.Main;

public class TowerDefenseScreen implements Screen {
    private final Main game;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    // game entities
    private final Array<Tower> towers = new Array<>();
    private final Array<Enemy> enemies = new Array<>();
    private final Array<Projectile> projectiles = new Array<>();
    // deploy zones
    private final Array<Zone> zones = new Array<>();

    // navbar selection
    private TowerType selectedType;
    private final float[] navTowerX = new float[3];
    private final float[] navTowerW = new float[3];
    private static final String[] NAV_TOWERS = {"Tower1","Tower2","Tower3"};

    private float spawnTimer = 0f;
    private boolean isGameOver = false;
    private int gold = 100;

    private static final float NAVBAR_HEIGHT = 80f;
    private static final float GROUND_Y = 100f;

    public TowerDefenseScreen(final Main game,int stage) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        ImageLoader.load();
        shapes = new ShapeRenderer();
        font = new BitmapFont();

        // initialize towers: main tower and small towers
        float tyMain = GROUND_Y + (ImageLoader.towerTex.getHeight() * 0.5f) / 2f;
        towers.add(new Tower(ImageLoader.towerTex, ImageLoader.projTex, 100, tyMain, 0.5f, true));
        for (int i = 1; i < 4; i++) {
            float tx = 190 + i * 190;
            float ty = GROUND_Y + (ImageLoader.towerTex.getHeight() * 0.2f) / 2f;
            towers.add(new Tower(ImageLoader.towerTex, ImageLoader.projTex, tx, ty, 0.2f, false));
        }

        // define deploy zones under small towers (indexes 1..3)
        for (int i = 1; i < towers.size; i++) {
            Tower small = towers.get(i);
            float w = small.getBounds().width;
            float h = small.getBounds().height / 2f;
            float skew = 20f;
            float baseX = small.x - w / 2f;
            float baseY = small.y - small.getBounds().height / 2f;
            float[] verts = {
                baseX, baseY,
                baseX + w, baseY,
                baseX + w + skew, baseY - h,
                baseX + skew, baseY - h
            };
            zones.add(new Zone(verts));
        }

        // calculate navbar hit areas
        recalcNavPositions();

        // input handling for selection and deployment
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 v = new Vector3(screenX, screenY, 0);
                camera.unproject(v);
                float x = v.x, y = v.y;

                // click on navbar to select tower type
                if (y > camera.viewportHeight - NAVBAR_HEIGHT) {
                    for (int i = 0; i < NAV_TOWERS.length; i++) {
                        if (x >= navTowerX[i] && x <= navTowerX[i] + navTowerW[i]) {
                            selectedType = TowerType.values()[i];
                            break;
                        }
                    }
                } else {
                    // click on zones to deploy
                    if (selectedType != null) {
                        for (Zone z : zones) {
                            if (!z.occupied && z.contains(x, y)) {
                                float cx = (z.verts[0] + z.verts[2] + z.verts[4] + z.verts[6]) / 4f;
                                float cy = (z.verts[1] + z.verts[3] + z.verts[5] + z.verts[7]) / 4f;
                                towers.add(new Tower(ImageLoader.towerTex, ImageLoader.projTex, cx, cy, 0.2f, true));
                                z.occupied = true;
                                selectedType = null;
                                break;
                            }
                        }
                    }
                }
                return true;
            }
        });
    }

    private void recalcNavPositions() {
        float vw = camera.viewportWidth;
        float spacing = 40f;
        String[] center = {NAV_TOWERS[0], NAV_TOWERS[1], NAV_TOWERS[2], "Trap1","Trap2","Trap3","Trap4"};
        int n = center.length;
        float[] widths = new float[n];
        float total = 0;
        for (int i = 0; i < n; i++) {
            layout.setText(font, center[i]);
            widths[i] = layout.width;
            total += widths[i];
        }
        total += spacing * (n - 1);
        float startX = (vw - total) / 2f;
        float x = startX;
        for (int i = 0; i < n; i++) {
            if (i < NAV_TOWERS.length) {
                navTowerX[i] = x;
                navTowerW[i] = widths[i];
            }
            x += widths[i] + spacing;
        }
    }

    private void update(float delta) {
        if (isGameOver) return;

        spawnTimer += delta;
        if (spawnTimer > 2f) {
            spawnTimer = 0f;
            float ey = GROUND_Y + (ImageLoader.enemyTex.getHeight() * Enemy.SCALE) / 2f;
            enemies.add(new Enemy(ImageLoader.enemyTex, 1280, ey));
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update(delta);

            boolean attacked = false;
            for (int j = towers.size - 1; j >= 0; j--) {
                Tower t = towers.get(j);
                if (e.getBounds().overlaps(t.getBounds())) {
                    t.takeDamage(1);
                    attacked = true;
                    if (t.isDestroyed()) {
                        towers.removeIndex(j);
                        if (t.canShoot) {
                            isGameOver = true;
                        }
                    }
                    break;
                }
            }

            if (attacked || e.getX() < -e.getWidth()/2) {
                enemies.removeIndex(i);
            }
        }

        for (Tower t : towers) {
            t.update(delta, enemies, projectiles);
        }

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
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float vw   = camera.viewportWidth;
        float vy   = camera.viewportHeight;
        float yNav = vy - NAVBAR_HEIGHT/2 + 10f;

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(0, vy - NAVBAR_HEIGHT, vw, NAVBAR_HEIGHT);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.LIGHT_GRAY);
        for (Zone z : zones) {
            if (!z.occupied) shapes.polygon(z.verts);
        }
        shapes.end();

        if (isGameOver) {
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            font.draw(game.batch, "GAME OVER", vw/2f - 50, vy/2f);
            game.batch.end();
            return;
        }

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        font.draw(game.batch, "Gold: " + gold, 20f, yNav);

        String removeTxt = "Remove";
        String pauseTxt  = "Pause";
        layout.setText(font, pauseTxt);
        float pauseW = layout.width;
        layout.setText(font, removeTxt);
        float removeW = layout.width;

        float rightMargin = 20f;
        font.draw(game.batch, pauseTxt, vw - rightMargin - pauseW, yNav);
        font.draw(game.batch, removeTxt, vw - rightMargin - pauseW - 20f - removeW, yNav);

        String[] centerItems = {
            "Tower1","Tower2","Tower3",
            "Trap1","Trap2","Trap3","Trap4"
        };
        int n = centerItems.length;
        float spacing = 40f;

        float totalW = 0f;
        float[] widths = new float[n];
        for (int i = 0; i < n; i++) {
            layout.setText(font, centerItems[i]);
            widths[i] = layout.width;
            totalW += widths[i];
        }
        totalW += spacing * (n - 1);

        float startX = (vw - totalW) / 2f;
        float x = startX;
        for (int i = 0; i < n; i++) {
            font.draw(game.batch, centerItems[i], x, yNav);
            x += widths[i] + spacing;
        }

        game.batch.end();

        game.batch.begin();
        for (Tower t : towers)         t.drawBatch(game.batch);
        for (Enemy e : enemies)        e.drawBatch(game.batch);
        for (Projectile p : projectiles) p.drawBatch(game.batch);
        game.batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Tower t : towers)         t.drawShape(shapes);
        for (Enemy e : enemies)        e.drawShape(shapes);
        for (Projectile p : projectiles) p.drawShape(shapes);
        shapes.end();
    }

    private enum TowerType { T1, T2, T3 }

    private static class Zone {
        final float[] verts;
        boolean occupied;
        Zone(float[] verts) { this.verts = verts; }
        boolean contains(float x, float y) {
            return Intersector.isPointInPolygon(verts, 0, verts.length, x, y);
        }
    }

    @Override public void resize(int w, int h) {}
    @Override public void show()       {}
    @Override public void hide()       {}
    @Override public void pause()      {}
    @Override public void resume()     {}

    @Override
    public void dispose() {
        shapes.dispose();
        font.dispose();
        ImageLoader.dispose();
    }
}
