package io.DutchSlayer.defend;

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

public class GameScreen implements Screen {
    private final Main game;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    // game entities
    private final Array<Tower> towers = new Array<>();
    private final Array<Enemy> enemies = new Array<>();
    private final Array<Projectile> projectiles = new Array<>();
    private final Array<Trap>  trapZones   = new Array<>();
    private final Array<float[]> trapVerts = new Array<>();  // simpan verts polygon tiap zona
    private static final int TRAP_COST = 10;

    // deploy zones
    private final Array<Zone> zones = new Array<>();

    // navbar selection
    private NavItem selectedType;
    private final float[] navTowerX = new float[3];
    private final float[] navTowerW = new float[3];
    private static final String[] NAV_TOWERS = {"Tower1","Tower2","Tower3"};

    private final float[] navTrapX = new float[4];
    private final float[] navTrapW = new float[4];
    private static final String[] NAV_TRAPS = {"Trap1","Trap2","Trap3","Trap4"};


    private float spawnTimer = 0f;
    private boolean isGameOver = false;

    private static final float NAVBAR_HEIGHT = 80f;
    private static final float GROUND_Y = 150f;
    private static final float ZONE_OFFSET_Y = 20f;    // jarak zona di bawah tower kecil

    private int gold = 80;               // 1) mulai dengan 80 gold
    private static final int TOWER_COST = 40;
    private static final float INCOME_INTERVAL = 2f;
    private static final int INCOME_AMOUNT   = 5;
    private float goldTimer = 0f;         // untuk pendapatan pasif

    public GameScreen(final Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);
        gold = 80;

        ImageLoader.load();
        shapes = new ShapeRenderer();
        font = new BitmapFont();

        // initialize only main tower
//        float tyMain = GROUND_Y + (ImageLoader.towerTex.getHeight() * 0.5f) / 2f;
        float towerMainH = ImageLoader.towerTex.getHeight() * 0.5f;  // scale 0.5f
        float tyMain     = GROUND_Y + towerMainH/5f;
        towers.add(new Tower(
            ImageLoader.towerTex,
            ImageLoader.projTex,
            100, tyMain,
            0.4f,
            true,
            true,
            10
        ));

        // define deploy zones at fixed positions under where small towers were
        float spacing     = 150f;
        float firstCenter = 100f + spacing;
        float[] zoneCenters = {
            firstCenter,
            firstCenter + spacing,
            firstCenter + spacing*2
        };
        for (float cx : zoneCenters) {
            float w = ImageLoader.towerTex.getWidth() * 0.2f;
            float h = ImageLoader.towerTex.getHeight() * 0.1f;
            float x0 = cx - w/2f;
            float y0 = GROUND_Y;
            float skew = 50f;
            float[] verts = new float[]{
                x0,   y0,
                x0 + w, y0,
                x0 + w + skew, y0 - h,
                x0 + skew,    y0 - h
            };
            zones.add(new Zone(verts));
        }
        // setelah define deploy-zones tower
        // setelah define deploy‐zones tower
        float trapSpacing = 150f;              // jarak antar trap zone
        float lastTowerCx = zoneCenters[zoneCenters.length - 1];  // center tower ke-3

        int numTrapZones = 3;                  // kalau cuma mau 3 zone trap
        for (int i = 0; i < numTrapZones; i++) {
            float tx = lastTowerCx + (i + 1) * trapSpacing;
            float w  = ImageLoader.towerTex.getWidth() * 0.2f;
            float h  = ImageLoader.towerTex.getHeight() * 0.1f;
            float x0 = tx - w/2f;
            float y0 = GROUND_Y + ZONE_OFFSET_Y;
            float skew = 20f;
            float[] v = {
                x0,        y0,
                x0 + w,    y0,
                x0 + w + skew, y0 - h,
                x0 + skew,    y0 - h
            };
            trapVerts.add(v);
            trapZones.add(new Trap(v, 0.2f));
        }

        // calculate navbar hit areas
        recalcNavPositions();

//        // input handling for selection and deployment
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 v = new Vector3(screenX, screenY, 0);
                camera.unproject(v);
                float x = v.x, y = v.y;

                // select tower type from navbar
                if (y > camera.viewportHeight - NAVBAR_HEIGHT) {
                    //Tower
                    for (int i = 0; i < NAV_TOWERS.length; i++) {
                        if (x >= navTowerX[i] && x <= navTowerX[i] + navTowerW[i]) {
                            selectedType = NavItem.values()[i];
                            return true;
                        }
                    }
                    //Trap
                    for (int i = 0; i < 4; i++) {
                        if (x >= navTrapX[i] && x <= navTrapX[i] + navTrapW[i]) {
                            selectedType = NavItem.values()[NAV_TOWERS.length + i];  // atau TRAP1/2/3 …
                            return true;
                        }
                    }
                    return false;
                }
//                if (selectedType == TowerType.TRAP) {
//                    for (int i=0; i<trapZones.size; i++) {
//                        Trap tz = trapZones.get(i);
//                        if (!tz.occupied && tz.contains(x,y) && gold>=TRAP_COST) {
//                            gold -= TRAP_COST;
//                            tz.occupied = true;
//                            selectedType = null;
//                            return true;
//                        }
//                    }
//                } else if (selectedType != null) {
//                    // deploy tower in a free zone
//                    for (Zone z : zones) {
//                        if (!z.occupied && z.contains(x, y)) {
//                            if (gold >= TOWER_COST) {
//                                gold -= TOWER_COST;
//                                float cx = (z.verts[0] + z.verts[2] + z.verts[4] + z.verts[6]) / 4f;
//                                float cy = (z.verts[1] + z.verts[3] + z.verts[5] + z.verts[7]) / 2f;
//                                towers.add(new Tower(
//                                    ImageLoader.towerTex,
//                                    ImageLoader.projTex,
//                                    cx, cy,
//                                    0.2f, true, false, 3
//                                ));
//                                z.occupied = true;
//                            }
//                            selectedType = null;
//                            break;
//                        }
//                    }
//                }
                // 2) Deploy berdasarkan selectedItem
                if (selectedType != null) {
                    switch(selectedType) {
                        case T1: case T2: case T3:
                            // deploy tower…
                            for (Zone z : zones) {
                                if (!z.occupied && z.contains(x,y) && gold >= TOWER_COST) {
                                    gold -= TOWER_COST;
                                    float cx = (z.verts[0] + z.verts[2] + z.verts[4] + z.verts[6]) / 4f;
                                    float cy = (z.verts[1] + z.verts[3] + z.verts[5] + z.verts[7]) / 2f;
                                    towers.add(new Tower(
                                        ImageLoader.towerTex,
                                        ImageLoader.projTex,
                                        cx, cy,
                                        0.2f, true, false, 3
                                    ));
                                    z.occupied = true;
                                    selectedType = null;
                                    return true;
                                }
                            }
                            break;
                        case TRAP1: case TRAP2: case TRAP3: case TRAP4:
                            // deploy trap…
                            for (Trap tz : trapZones) {
                                if (!tz.occupied && tz.contains(x, y) && gold >= TRAP_COST) {
                                    gold -= TRAP_COST;
                                    tz.occupied = true;
                                    selectedType = null;   // reset pilihan
                                    return true;           // sukses deploy
                                }
                            };
                    }
                    selectedType = null;
                }
                return false;
            }
        });
    }


    // calculate center navbar label positions for hit detection
    private void recalcNavPositions() {
        float vw = camera.viewportWidth;
        float spacing = 40f;

        // Gabungkan nama Tower + Trap
        String[] all = new String[]{
            NAV_TOWERS[0], NAV_TOWERS[1], NAV_TOWERS[2],
            NAV_TRAPS[0], NAV_TRAPS[1], NAV_TRAPS[2], NAV_TRAPS[3]
        };

        // Hitung total width
        float total = 0f;
        float[] widths = new float[all.length];
        for (int i = 0; i < all.length; i++) {
            layout.setText(font, all[i]);
            widths[i] = layout.width;
            total    += widths[i];
        }
        total += spacing * (all.length - 1);

        // Starting X agar teks ter‐center
        float x = (vw - total) / 2f;

        // Simpan posisi hit‐area berdasarkan index
        for (int i = 0; i < all.length; i++) {
            if (i < NAV_TOWERS.length) {
                navTowerX[i] = x;
                navTowerW[i] = widths[i];
            } else {
                int ti = i - NAV_TOWERS.length;
                navTrapX[ti] = x;
                navTrapW[ti] = widths[i];
            }
            x += widths[i] + spacing;
        }
    }

    private void update(float delta) {
        if (isGameOver) return;

        // — Pendapatan pasif tiap 2 detik
        goldTimer += delta;
        if (goldTimer >= INCOME_INTERVAL) {
            gold += INCOME_AMOUNT;
            goldTimer -= INCOME_INTERVAL;
        }

        // Spawn musuh
        spawnTimer += delta;
        if (spawnTimer > 2f) {
            spawnTimer = 0f;
//            float ey = GROUND_Y + (ImageLoader.enemyTex.getHeight() * Enemy.SCALE) / 2f;
            float enemyH = ImageLoader.enemyTex.getHeight() * Enemy.SCALE;
            float ey     = GROUND_Y + enemyH/3f;
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
                    t.takeDamage(1);
                    attacked = true;
                    if (t.isDestroyed()) {
                        towers.removeIndex(j);
                        if (t.isMain) {
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
                Enemy e = enemies.get(j);
                if (p.getBounds().overlaps(e.getBounds())) {
                    e.takeDamage(1);                  // kurangi HP
                    hit = true;
                    if (e.isDestroyed()) {
                        enemies.removeIndex(j);       // baru di‐remove kalau HP ≤ 0
                        gold += 10;           // 2) reward kill +10 gold
                    }
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

        float vw = camera.viewportWidth;
        float vy = camera.viewportHeight;         // 720
        float yNav = vy - NAVBAR_HEIGHT/2 + 10f;       // y pos teks navbar

        // 1) Ground
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(0, 0, camera.viewportWidth, GROUND_Y);
        shapes.end();

        // navbar background
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.DARK_GRAY);
        shapes.rect(0, vy - NAVBAR_HEIGHT, vw, NAVBAR_HEIGHT);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0, 0, 0, 0.5f));  // semi-transparan

        if (selectedType != null) {
            int idx = selectedType.ordinal();
            if (idx < NAV_TOWERS.length) {
                // highlight Tower1..Tower3
                shapes.rect(
                    navTowerX[idx],
                    vy - NAVBAR_HEIGHT,
                    navTowerW[idx],
                    NAVBAR_HEIGHT
                );
            } else {
                // highlight hanya Trap yang dipilih
                int trapIdx = idx - NAV_TOWERS.length;  // TRAP1→0, TRAP2→1, dst.
                shapes.rect(
                    navTrapX[trapIdx],
                    vy - NAVBAR_HEIGHT,
                    navTrapW[trapIdx],
                    NAVBAR_HEIGHT
                );
            }
        }

        shapes.end();

        // draw deploy zones
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.LIGHT_GRAY);
        for (Zone z : zones) {
            if (!z.occupied) shapes.polygon(z.verts);
        }
        shapes.end();

        // draw deploy trap
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.ORANGE);
        for (int i = 0; i < trapZones.size; i++) {
            if (!trapZones.get(i).occupied) {
                shapes.polygon(trapVerts.get(i));
            }
        }
        shapes.end();

        // 3) Jika game over, tampilkan pesan dan stop
        if (isGameOver) {
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            font.draw(game.batch, "GAME OVER", vw/2f - 50, vy/2f);
            game.batch.end();
            return;
        }

//         input handling for selection and deployment
//        Gdx.input.setInputProcessor(new InputAdapter() {
//            @Override
//            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
//                Vector3 v = new Vector3(screenX, screenY, 0);
//                camera.unproject(v);
//                float x = v.x, y = v.y;
//
//                // select tower type from navbar
//                if (y > camera.viewportHeight - NAVBAR_HEIGHT) {
//                    for (int i = 0; i < NAV_TOWERS.length; i++) {
//                        if (x >= navTowerX[i] && x <= navTowerX[i] + navTowerW[i]) {
//                            selectedType = TowerType.values()[i];
//                            break;
//                        }
//                    }
//                    for (int i = 0; i < 4; i++) {
//                        if (x >= navTrapX[i] && x <= navTrapX[i] + navTrapW[i]) {
//                            selectedType = TowerType.TRAP;  // atau TRAP1/2/3 …
//                            return true;
//                        }
//                    }
//                }
//                if (selectedType == TowerType.TRAP) {
//                    for (int i=0; i<trapZones.size; i++) {
//                        Trap tz = trapZones.get(i);
//                        if (!tz.occupied && tz.contains(x,y) && gold>=TRAP_COST) {
//                            gold -= TRAP_COST;
//                            tz.occupied = true;
//                            selectedType = null;
//                            return true;
//                        }
//                    }
//                } else if (selectedType != null) {
//                    // deploy tower in a free zone
//                    for (Zone z : zones) {
//                        if (!z.occupied && z.contains(x, y)) {
//                            if (gold >= TOWER_COST) {
//                                gold -= TOWER_COST;
//                                float cx = (z.verts[0] + z.verts[2] + z.verts[4] + z.verts[6]) / 4f;
//                                float cy = (z.verts[1] + z.verts[3] + z.verts[5] + z.verts[7]) / 2f;
//                                towers.add(new Tower(
//                                    ImageLoader.towerTex,
//                                    ImageLoader.projTex,
//                                    cx, cy,
//                                    0.2f, true, false, 3
//                                ));
//                                z.occupied = true;
//                            }
//                            selectedType = null;
//                            break;
//                        }
//                    }
//                }
//                return false;
//            }
//        });

        // 4) Gambar elemen navbar: Gold | [Tower,Trap] tengah | Remove & Pause kanan
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // — Gold di kiri
        font.draw(game.batch, "Gold: " + gold, 20f, yNav);

        // — Remove & Pause di kanan
        String removeTxt = "Remove";
        String pauseTxt  = "Pause";
        layout.setText(font, pauseTxt);
        float pauseW = layout.width;
        layout.setText(font, removeTxt);
        float removeW = layout.width;

        float rightMargin = 20f;
        // gambar Pause paling kanan
        font.draw(game.batch,
            pauseTxt,
            vw - rightMargin - pauseW, yNav);
        // gambar Remove di kiri Pause, spasi 20px
        font.draw(game.batch,
            removeTxt,
            vw - rightMargin - pauseW - 20f - removeW, yNav);

        // — Tower1–3 & Trap1–4 di tengah
        String[] centerItems = {
            "Tower1","Tower2","Tower3",
            "Trap1","Trap2","Trap3","Trap4"
        };
        int n = centerItems.length;
        float spacing = 40f;

        // hitung total width teks + spacing
        float totalW = 0f;
        float[] widths = new float[n];
        for (int i = 0; i < n; i++) {
            layout.setText(font, centerItems[i]);
            widths[i] = layout.width;
            totalW += widths[i];
        }
        totalW += spacing * (n - 1);

        // mulai di (viewport - totalW)/2
        float startX = (vw - totalW) / 2f;
        float x = startX;
        for (int i = 0; i < n; i++) {
            font.draw(game.batch, centerItems[i], x, yNav);
            x += widths[i] + spacing;
        }

        game.batch.end();

        game.batch.begin();
        for (Trap t : trapZones) t.drawBatch(game.batch);
        game.batch.end();

        // 5) Gambar world sprites
        game.batch.begin();
        for (Tower t : towers)         t.drawBatch(game.batch);
        for (Enemy e : enemies)        e.drawBatch(game.batch);
        for (Projectile p : projectiles) p.drawBatch(game.batch);
        game.batch.end();

        // 6) Fallback shapes untuk missing textures
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Tower t : towers)         t.drawShape(shapes);
        for (Enemy e : enemies)        e.drawShape(shapes);
        for (Projectile p : projectiles) p.drawShape(shapes);
        shapes.end();
    }

    // selection enum
    private enum NavItem {
        T1, T2, T3,
        TRAP1, TRAP2, TRAP3, TRAP4
    }

    // inner Zone class
    private static class Zone {
        final float[] verts;
        boolean occupied;
        Zone(float[] verts) { this.verts = verts; }
        boolean contains(float x, float y) {
            return Intersector.isPointInPolygon(verts, 0, verts.length, x, y);
        }
    }


    // implementasi Screen kosong
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
