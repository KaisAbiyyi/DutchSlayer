package io.DutchSlayer.defend;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
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
    private final Array<Zone> deployedTowerZones = new Array<>();  // Zone yang berisi tower

    // navbar selection
    private NavItem selectedType;
    private final float[] navTowerX = new float[3];
    private final float[] navTowerW = new float[3];
    private static final String[] NAV_TOWERS = {"Tower1","Tower2","Tower3"};

    private final float[] navTrapX = new float[3];
    private final float[] navTrapW = new float[3];
    private static final String[] NAV_TRAPS = {"TrapAtk","TrapSlow","TrapBomb"}; // ← Changed to 3 traps


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

    // wave system
    private int currentWave   = 1;
    private int spawnCount    = 0;              // berapa musuh sudah di‐spawn di wave ini
    private int enemiesThisWave = 5;            // wave 1 spawn 5, nanti dinaikkan
    private static final int MAX_WAVE = 3;

    // tower yang sedang dipilih untuk tampilkan panel
    private Tower selectedTowerUI;

    // konfigurasi panel
    private static final float PANEL_W      = 120f;
    private static final float PANEL_H      = 100f;
    private static final float PANEL_MARGIN = 10f;

    // bounds tombol (akan di‐recalculated tiap render berdasar posisi panel)
    private Rectangle btnAttack, btnDefense , btnSpeed;
    private boolean upgradeButtonsValid = false;

    // in GameScreen.java, fields section
    private boolean isPaused = false;

    // button bounds for the navbar “Pause” label
    private Rectangle btnPause;
    private Rectangle btnRemove;

    // pause‐menu panel + buttons
    private final float PAUSE_W = 200f, PAUSE_H = 120f;
    private Rectangle pausePanel;
    private Rectangle btnContinue, btnQuit;

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
        float towerMainH = ImageLoader.maintowertex.getHeight() * 0.6f;  // scale 0.5f
        float tyMain     = GROUND_Y + towerMainH/5f;
        towers.add(new Tower(
            ImageLoader.maintowertex,
            ImageLoader.projTex,
            100, tyMain,
            0.3f,
            true,
            true,
            TowerType.BASIC,
            10, 0.1f
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
            float x0 = cx - w / 2f;
            float y0 = GROUND_Y;
            float skew = 50f;
            float[] verts = new float[]{
                x0, y0,
                x0 + w, y0,
                x0 + w + skew, y0 - h,
                x0 + skew, y0 - h
            };
            zones.add(new Zone(verts));
        }

        // setelah define deploy‐zones tower
        int numTrapZones = 3;                           // kalau cuma mau 3 zone trap
        float trapFirstCx = firstCenter + spacing*3;    // ← Start setelah 3 tower zones
        float trapY0      = GROUND_Y;                   // SAMA dengan tower
        float trapWidth   = ImageLoader.towerTex.getWidth() * 0.2f;
        float trapHeight  = ImageLoader.towerTex.getHeight()* 0.1f;
        float skew        = 50f;         // sama dengan tower‐zone

        for (int i = 0; i < numTrapZones; i++) {
            float cx = trapFirstCx + i * spacing; // Spacing sama dengan tower
            float x0 = cx - trapWidth/2;
            float y0 = trapY0; // ← Y position sama dengan tower zones
            float[] v = {
                x0,                 y0,
                x0 + trapWidth,     y0,
                x0 + trapWidth + skew,  y0 - trapHeight,
                x0 + skew,              y0 - trapHeight
            };
            trapVerts.add(v);
            trapZones.add(new Trap(v, 0.2f, TrapType.ATTACK));
        }

        // calculate navbar hit areas
        recalcNavPositions();

        // after recalcNavPositions() in your constructor:
        float vw = camera.viewportWidth;
        float yNav = camera.viewportHeight - NAVBAR_HEIGHT/2 + 10f;

        // figure out where “Pause” was drawn
        layout.setText(font, "Pause");
        float pauseW = layout.width;
        float pauseX = vw - 20f - pauseW;   // matches your render()
        float pauseH = layout.height;

        // store the button rectangle
        btnPause = new Rectangle(pauseX, yNav - pauseH, pauseW, pauseH);

        // layout.setText(font, "Remove");  // hitung width Remove
        layout.setText(font, "Remove");
        float removeW = layout.width;
// spasi 20px ke kiri dari Pause
        float removeX = (vw - 20f - pauseW) - 20f - removeW;
        float removeY = yNav - layout.height;
        btnRemove = new Rectangle(removeX, removeY, removeW, layout.height);

//        // input handling for selection and deployment
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 v = new Vector3(screenX, screenY, 0);
                camera.unproject(v);
                float x = v.x, y = v.y;

                if (!isPaused) {
                    // if we clicked the navbar Pause label, enter pause
                    if (btnPause.contains(x, y)) {
                        isPaused = true;

                        // compute the center‐screen pause panel
                        float px = (camera.viewportWidth - PAUSE_W)/2;
                        float py = (camera.viewportHeight - PAUSE_H)/2;
                        pausePanel = new Rectangle(px, py, PAUSE_W, PAUSE_H);

                        // continue & quit buttons inside that panel:
                        float bw = PAUSE_W - 40, bh = 30;
                        btnContinue = new Rectangle(px + 20, py + PAUSE_H - 50, bw, bh);
                        btnQuit     = new Rectangle(px + 20, py + PAUSE_H - 90, bw, bh);

                        return true;
                    }
                } else {
                    // ——————— INSIDE PAUSE MENU ———————
                    // any click gets eaten while paused
                    if (btnContinue.contains(x,y)) {
                        isPaused = false;
                        return true;
                    }
                    if (btnQuit.contains(x,y)) {
                        Gdx.app.exit();
                        return true;
                    }
                    // click anywhere else in pause menu area also swallowed
                    return true;
                }

                // if paused → do *not* fall through to game / deploy logic
                if (isPaused) return true;

                // **1) Kalau ada tower ter-select untuk UI, cek tombol dulu**
                if (selectedTowerUI != null) {
                    // hitung posisi panel persis seperti di render():
                    float panelW = 160, panelH = 100;
                    float px = selectedTowerUI.x + selectedTowerUI.getBounds().width/2 + 10;
                    float py = selectedTowerUI.y - panelH/2;
                    Rectangle panelRect = new Rectangle(px, py, panelW, panelH);

                    // SUPER LARGE CLICK AREAS - Bagi panel jadi 3 bagian vertikal
                    float sectionH = panelH / 3f;

                    // Attack button - 1/3 bagian atas panel
                    btnAttack = new Rectangle(px, py + sectionH*2, panelW, sectionH);

                    // Defense button - 1/3 bagian tengah panel
                    btnDefense = new Rectangle(px, py + sectionH, panelW, sectionH);

                    // Speed button - 1/3 bagian bawah panel
                    btnSpeed = new Rectangle(px, py, panelW, sectionH);
                    upgradeButtonsValid = true;

                    if (panelRect.contains(x, y)) {
                        if (btnAttack.contains(x,y)) {
                            if (selectedTowerUI.canUpgrade()) {
                                selectedTowerUI.upgradeAttack();
                                System.out.println("Attack upgraded!");
                            }
                            return true;
                        }
                        if (btnDefense.contains(x,y)) {
                            if (selectedTowerUI.canUpgrade()) {
                                selectedTowerUI.upgradeDefense();
                                System.out.println("Defense upgraded!");
                            }
                            return true;
                        }
                        if (btnSpeed.contains(x,y)) {
                            if (selectedTowerUI.canUpgrade()) {
                                selectedTowerUI.upgradeSpeed();
                                System.out.println("Speed upgraded!");
                            }
                            return true;
                        }
                        return true;
                    }
                    selectedTowerUI = null;
                    upgradeButtonsValid = false;
                }

                // kalau klik di area game world…
                if (selectedType == NavItem.REMOVE) {
                    // coba hapus tower dulu
                    for (int i = towers.size - 1; i >= 0; i--) {
                        Tower t = towers.get(i);
                        if (t.getBounds().contains(x, y)) {
                            try {
                                // Coba ambil zone dengan index
                                if (i < deployedTowerZones.size) {
                                    Zone occupiedZone = deployedTowerZones.get(i);
                                    towers.removeIndex(i);
                                    deployedTowerZones.removeIndex(i);
                                    occupiedZone.occupied = false;
                                } else {
                                    // Fallback: hapus tower saja, reset semua zone
                                    towers.removeIndex(i);
                                    resetAllEmptyZones();  // Method helper
                                }
                            } catch (IndexOutOfBoundsException e) {
                                // Emergency fallback
                                System.out.println("IndexOutOfBounds caught, using fallback");
                                towers.removeIndex(i);
                                resetAllEmptyZones();
                            }

                            selectedType = null;
                            return true;
                        }
                    }
                    // coba hapus trap
                    for (int i = trapZones.size - 1; i >= 0; i--) {
                        Trap tz = trapZones.get(i);
                        // pakai trapVerts untuk cek hit
                        if (Intersector.isPointInPolygon(trapVerts.get(i), 0, trapVerts.get(i).length, x, y)
                            && tz.occupied) {
                            tz.occupied = false;
                            selectedType = null;
                            return true;
                        }
                    }
                    // kalau klik di mana-mana (tapi bukan objek) → batalkan Remove
                    selectedType = null;
                    return true;
                }

                // 0) klik tower untuk select
                for (Tower t : towers) {
                    if (t.getBounds().contains(x, y)) {
                        selectedTowerUI = t;
                        return true;
                    }
                }
                selectedTowerUI = null;

                // select tower type from navbar
                if (y > camera.viewportHeight - NAVBAR_HEIGHT) {
                    // tombol Remove?
                    if (btnRemove.contains(x,y)) {
                        selectedType = NavItem.REMOVE;
                        return true;
                    }
                    //Tower
                    for (int i = 0; i < NAV_TOWERS.length; i++) {
                        if (x >= navTowerX[i] && x <= navTowerX[i] + navTowerW[i]) {
                            selectedType = NavItem.values()[i];
                            return true;
                        }
                    }
                    //Trap
                    for (int i = 0; i < NAV_TRAPS.length; i++) { // ← Use NAV_TRAPS.length instead of hardcoded 4
                        if (x >= navTrapX[i] && x <= navTrapX[i] + navTrapW[i]) {
                            selectedType = NavItem.values()[NAV_TOWERS.length + i];
                            return true;
                        }
                    }
                    return false;
                }

                // 2) Deploy berdasarkan selectedItem
                if (selectedType != null) {
                    switch(selectedType) {
                        case T1: {
                            // Hitung kembali center zona seperti sebelumnya
                            for (int zoneIdx = 0; zoneIdx < zones.size; zoneIdx++) {
                                Zone z = zones.get(zoneIdx);
                                if (!z.occupied && z.contains(x,y) && gold >= TOWER_COST) {
                                    gold -= TOWER_COST;
                                    float cx = (z.verts[0] + z.verts[2] + z.verts[4] + z.verts[6]) / 4f;
                                    float cy = (z.verts[1] + z.verts[3] + z.verts[5] + z.verts[7]) / 2.3f;
                                    // Tambahkan AOE‐tower
                                    towers.add(new Tower(
                                        ImageLoader.tower1Tex,    // texture AOE
                                        ImageLoader.aoeProjTex,   // projectile AOE
                                        cx, cy,
                                        0.3f,                     // skala
                                        true,
                                        false,
                                        TowerType.AOE,
                                        5, // initial HP
                                        0.1f
                                    ));
                                    deployedTowerZones.add(z);
                                    z.occupied = true;
                                    selectedType = null;
                                    return true;
                                }
                            }
                            break;
                        }
                        case T2: {
                            for (int zoneIdx = 0; zoneIdx < zones.size; zoneIdx++) {
                                Zone z = zones.get(zoneIdx);
                                if (!z.occupied && z.contains(x,y) && gold >= TOWER_COST) {
                                    gold -= TOWER_COST;
                                    float cx = (z.verts[0] + z.verts[2] + z.verts[4] + z.verts[6]) / 4f;
                                    float cy = (z.verts[1] + z.verts[3] + z.verts[5] + z.verts[7]) / 2.5f;
                                    // Tambahkan fast‐attack tower
                                    towers.add(new Tower(
                                        ImageLoader.tower2Tex,
                                        ImageLoader.fastProjTex,
                                        cx, cy,
                                        0.3f,
                                        true,
                                        false,
                                        TowerType.FAST,
                                        3,
                                        0.015f
                                    ));
                                    deployedTowerZones.add(z);
                                    z.occupied = true;
                                    selectedType = null;
                                    return true;
                                }
                            }
                            break;
                        }
                        case T3: {
                            for (int zoneIdx = 0; zoneIdx < zones.size; zoneIdx++) {
                                Zone z = zones.get(zoneIdx);
                                if (!z.occupied && z.contains(x,y) && gold >= TOWER_COST) {
                                    gold -= TOWER_COST;
                                    float cx = (z.verts[0] + z.verts[2] + z.verts[4] + z.verts[6]) / 4f;
                                    float cy = (z.verts[1] + z.verts[3] + z.verts[5] + z.verts[7]) / 2.3f;
                                    // Tambahkan slow‐effect tower
                                    towers.add(new Tower(
                                        ImageLoader.tower3Tex,
                                        ImageLoader.slowProjTex,
                                        cx, cy,
                                        0.2f,
                                        true,
                                        false,
                                        TowerType.SLOW,
                                        10, 0.5f
                                    ));
                                    deployedTowerZones.add(z);
                                    z.occupied = true;
                                    selectedType = null;
                                    return true;
                                }
                            }
                            break;
                        }
                        case TRAP1: case TRAP2: case TRAP3:
                            // deploy trap…
                            TrapType trapType;
                            switch(selectedType) {
                                case TRAP1: trapType = TrapType.ATTACK; break;
                                case TRAP2: trapType = TrapType.SLOW; break;
                                case TRAP3: trapType = TrapType.EXPLOSION; break;
                                default: trapType = TrapType.ATTACK; break;
                            }

                            for (int trapIdx = 0; trapIdx < trapZones.size; trapIdx++) {
                                Trap tz = trapZones.get(trapIdx);
                                float[] trapVert = trapVerts.get(trapIdx);

                                if (!tz.occupied &&
                                    Intersector.isPointInPolygon(trapVert, 0, trapVert.length, x, y) &&
                                    gold >= TRAP_COST) {

                                    gold -= TRAP_COST;

                                    // ===== REPLACE OLD TRAP WITH NEW TYPED TRAP =====
                                    trapZones.set(trapIdx, new Trap(trapVert, 0.2f, trapType));
                                    trapZones.get(trapIdx).occupied = true;

                                    selectedType = null;
                                    System.out.println(trapType + " trap deployed!");
                                    return true;
                                }
                            }
                            break;
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
            NAV_TRAPS[0], NAV_TRAPS[1], NAV_TRAPS[2]
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
                if (ti < NAV_TRAPS.length) { // ← Safety check
                    navTrapX[ti] = x;
                    navTrapW[ti] = widths[i];
                }
            }
            x += widths[i] + spacing;
        }
    }

    private void update(float delta) {
        if (isPaused || isGameOver) return;

        // 1) Update traps (handle cooldown)
        for (Trap trap : trapZones) {
            trap.update(delta);
        }

        // 2) Gerakkan & cek serangan musuh ke tower
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update(delta);    // ← DI SINI MUSUH MAJU

            // Cek collision dengan tower
            for (int j = towers.size - 1; j >= 0; j--) {
                Tower t = towers.get(j);
                if (e.getBounds().overlaps(t.getBounds())) {
                    if (e.canAttack()) {
                        t.takeDamage(1);
                        e.knockback();  // ← Knockback enemy

                        if (t.isDestroyed()) {
                            towers.removeIndex(j);
                            if (t.isMain) isGameOver = true;
                        }
                    }
                    break;
                }
            }
            if (e.getX() < -e.getWidth()/2) {
                enemies.removeIndex(i);
            }
        }

        // ===== 3) SIMPLIFIED: Let trap handle its own collision detection =====
        for (Trap trap : trapZones) {
            if (trap.occupied && !trap.isUsed()) {
                // Trap akan handle collision sendiri dengan method triggerTrap()
                if (trap.triggerTrap(enemies)) {
                    System.out.println("🎯 TRAP ACTIVATED! Type: " + trap.getType());
                }
            }
        }

        // 4) Tower menembak (sama seperti sebelumnya)
        for (Tower t : towers) {
            t.update(delta, enemies, projectiles);
        }

        // 5) Update projectiles
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);

            boolean hit = false;
            // cek tiap enemy
            for (int j = enemies.size - 1; j >= 0; j--) {
                Enemy e = enemies.get(j);
                if (p.getBounds().overlaps(e.getBounds())) {
                    // Panggil onHit() di sini, bukan langsung e.takeDamage()
                    p.onHit(enemies);
                    hit = true;
                    break;
                }
            }
            // Hapus projectile kalau sudah hit atau keluar layar
            if (hit || p.getX() > camera.viewportWidth + p.getBounds().width/2) {
                projectiles.removeIndex(i);
            }
        }

        // 6) Cleanup dead enemies
        for (int j = enemies.size - 1; j >= 0; j--) {
            Enemy e = enemies.get(j);
            if (e.isDestroyed()) {
                enemies.removeIndex(j);
                gold += 10;
            }
        }

        // 7) Wave spawning (sama seperti sebelumnya)
        if (spawnCount < enemiesThisWave) {
            spawnTimer += delta;
            if (spawnTimer > 2f) {
                spawnTimer = 0f;
                spawnEnemy();
                spawnCount++;
            }
        }

        // kalau sudah spawn semua dan semua musuh mati → next wave
        if (spawnCount >= enemiesThisWave && enemies.size == 0) {
            if (currentWave < MAX_WAVE) {
                currentWave++;
                // misal tiap wave nambah 5 musuh lagi
                enemiesThisWave += 5;
                spawnCount = 0;
            } else {
                // semua wave selesai → you win, atau loop kembali?
                isGameOver = true;  // misal game selesai
            }
        }

        // 8) Gold income
        goldTimer += delta;
        if (goldTimer >= INCOME_INTERVAL) {
            gold += INCOME_AMOUNT;
            goldTimer -= INCOME_INTERVAL;
        }
    }

    // refactor spawn code jadi method supaya reusable
    private void spawnEnemy() {
        float enemyH = ImageLoader.dutchtex.getHeight() * Enemy.SCALE;
        float ey     = GROUND_Y + enemyH/3f;
        Enemy newEnemy = new Enemy(ImageLoader.dutchtex, 1280, ey);
        enemies.add(newEnemy);

        // ===== DEBUG: Log enemy spawn position =====
        System.out.println("🆕 ENEMY SPAWNED:");
        System.out.println("  Position: (" + newEnemy.getX() + ", " + ey + ")");
        System.out.println("  Bounds: " + newEnemy.getBounds());
        System.out.println("  Total enemies: " + enemies.size);

        // ===== DEBUG: Log all trap positions for comparison =====
        System.out.println("📍 TRAP POSITIONS:");
        for (int i = 0; i < trapZones.size; i++) {
            Trap trap = trapZones.get(i);
            if (trap.occupied) {
                System.out.println("  Trap " + i + " (" + trap.getType() + "): " + trap.bounds);
            } else {
                System.out.println("  Trap " + i + ": NOT OCCUPIED");
            }
        }
        System.out.println("==========================================");
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float vw = camera.viewportWidth;
        float vy = camera.viewportHeight;         // 720
        float yNav = vy - NAVBAR_HEIGHT/2 + 10f;       // y pos teks navbar

        // 2) gambar background sky
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        if (ImageLoader.skytex != null) {
            game.batch.draw(
                ImageLoader.skytex,
                0, 0,            // origin di sudut kiri bawah
                vw, vy           // tarik sampai memenuhi viewport
            );
        }
        game.batch.end();


        // 1) Ground
        // Draw 3 rows of grass tiles instead of a single rect
        Texture grass = ImageLoader.terratex;
        // ukuran “logika” yang kamu inginkan (misal 64×64 world‐unit)
        float tileSize = 150f;
        // mau overlap 20px → step antara tile cuma 64−20 = 44
        float overlap = 20f;
        float step = tileSize - overlap;
        // columns dan rows hard‐coded jadi 3×3
        int cols = 100, rows = 1;

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float x = c * step;
                float y = r * step;             // misal di pangkal layar
                // kalau mau naik per baris, bisa ganti y = r*(tileSize+gap)
                game.batch.draw(
                    grass,
                    x, y,
                    tileSize, tileSize
                );
            }
        }
        game.batch.end();

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
            } else if (idx < NAV_TOWERS.length + NAV_TRAPS.length) {
                // highlight hanya Trap yang dipilih
                int trapIdx = idx - NAV_TOWERS.length;
                shapes.rect(
                    navTrapX[trapIdx],
                    vy - NAVBAR_HEIGHT,
                    navTrapW[trapIdx],
                    NAVBAR_HEIGHT
                );
            } else if (selectedType == NavItem.REMOVE) {
                shapes.setColor(1f, 1f, 0f, 0.3f);  // misal kuning semi
                shapes.rect(
                    btnRemove.x,
                    btnRemove.y,
                    btnRemove.width,
                    btnRemove.height
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
            "TrapAtk","TrapSlow","TrapBomb"
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

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

// …— gold, pause/remove, navbar, dsb. …

// gambar Label Wave di paling bawah kiri
        layout.setText(font, "Wave: " + currentWave);
        float waveX = 20f;
        float waveY = layout.height + 10f;  // 10px dari bawah
        font.draw(game.batch, "Wave: " + currentWave, waveX, waveY);

        game.batch.end();

        // 5) Gambar world sprites
        game.batch.begin();
        for (Tower t : towers)         t.drawBatch(game.batch);
        for (Enemy e : enemies) {
            // 1) gambar sprite musuh
            e.drawBatch(game.batch);

            // 2) hitung teks HP dan posisinya
            String hpText = String.valueOf(e.getHealth());
            layout.setText(font, hpText);
            float textX = e.getBounds().x + e.getBounds().width/2f - layout.width/2f;
            float textY = e.getBounds().y + e.getBounds().height + 15f; // 15px di atas kepala

            // 3) gambar label HP
            font.draw(game.batch, hpText, textX, textY);
        }
        for (Projectile p : projectiles) p.drawBatch(game.batch);
        game.batch.end();

        // inside render(), after your normal world drawing:
        if (isPaused) {
            // 1) darken the game behind
            shapes.setProjectionMatrix(camera.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 0f, 0f, 0.6f);
            shapes.rect(0, 0, camera.viewportWidth, camera.viewportHeight);
            shapes.end();

            // 2) draw panel background (centered)
            float panelW = 300f, panelH = 150f;
            float px = (camera.viewportWidth - panelW) / 2f;
            float py = (camera.viewportHeight - panelH) / 2f;
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0.8f, 0.8f, 0.8f, 1f);  // light grey
            shapes.rect(px, py, panelW, panelH);
            shapes.end();

            // 3) panel border
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.DARK_GRAY);
            shapes.rect(px, py, panelW, panelH);
            shapes.end();

            // 4) header text “Game Pause”
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            String header = "Game Pause";
            layout.setText(font, header);
            font.draw(
                game.batch,
                header,
                px + (panelW - layout.width) / 2f,
                py + panelH - 20f
            );
            game.batch.end();

            // 5) draw buttons
            float btnW = 100f, btnH = 40f;
            float btnY = py + 20f;
            float gap  = 40f;
            float totalWp = btnW * 2 + gap;
            float startXp = px + (panelW - totalWp) / 2f;

            Rectangle btnLanjut = new Rectangle(startXp,        btnY, btnW, btnH);
            Rectangle btnKeluar = new Rectangle(startXp + btnW + gap, btnY, btnW, btnH);

            // filled button backgrounds
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(Color.WHITE);
            shapes.rect(btnLanjut.x, btnLanjut.y, btnLanjut.width, btnLanjut.height);
            shapes.rect(btnKeluar.x, btnKeluar.y, btnKeluar.width, btnKeluar.height);
            shapes.end();

            // button borders
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.BLACK);
            shapes.rect(btnLanjut.x, btnLanjut.y, btnLanjut.width, btnLanjut.height);
            shapes.rect(btnKeluar.x, btnKeluar.y, btnKeluar.width, btnKeluar.height);
            shapes.end();

            // button labels
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            layout.setText(font, "Lanjut");
            font.draw(
                game.batch,
                "Lanjut",
                btnLanjut.x + (btnW - layout.width) / 2f,
                btnLanjut.y + (btnH + layout.height) / 2f
            );
            layout.setText(font, "Keluar");
            font.draw(
                game.batch,
                "Keluar",
                btnKeluar.x + (btnW - layout.width) / 2f,
                btnKeluar.y + (btnH + layout.height) / 2f
            );
            game.batch.end();

            // 6) consume the rest of render (skip drawing navbar, tower UI, etc.)
            return;
        }

        // 6) Fallback shapes untuk missing textures
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Tower t : towers)         t.drawShape(shapes);
        for (Enemy e : enemies)        e.drawShape(shapes);
        for (Projectile p : projectiles) p.drawShape(shapes);
        shapes.end();

        // 7) Gambar panel UI di kanan tower yang dipilih
        if (selectedTowerUI != null) {
            // ukuran panel
            float panelW = 160, panelH = 160;
            // letak: kanan tower + 10px
            float px = selectedTowerUI.x + (selectedTowerUI.getBounds().width/2) + 10;
            // sedikit ke atas agar tengah‐tengah tower
            float py = selectedTowerUI.y - panelH/2;

            // draw panel background
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0, 0, 0, 0.7f);
            shapes.rect(px, py, panelW, panelH);
            shapes.end();

// draw panel border
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.WHITE);
            shapes.rect(px, py, panelW, panelH);
            shapes.end();

            // header text
            String header = "Upgrade Left " + selectedTowerUI.getUpgradeRemaining();
            layout.setText(font, header);
            float headerX = px + (panelW - layout.width)/2;
            float headerY = py + panelH - 12;    // 12px down from top
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            font.draw(game.batch, header, headerX, headerY);
            game.batch.end();

            // common button dimensions
            float margin = 8f;
            float btnH    = 24f;
            float btnW    = panelW - margin*2;

            // stat labels and values
            String[] labels = {"Attack", "Defend", "Atk Speed"};
            int[] values = {
                selectedTowerUI.getAttackLevel(),
                selectedTowerUI.getDefenseLevel(),
                selectedTowerUI.getSpeedLevel()
            };

            // vertical starting point just below the header
            float startY = headerY - 8;

            for (int i = 0; i < 3; i++) {
                float y = startY - (i+1)*(btnH + margin);

                // draw filled bar
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(Color.DARK_GRAY);
                shapes.rect(px + margin, y - btnH, btnW, btnH);
                shapes.end();

                // draw bar outline
                shapes.begin(ShapeRenderer.ShapeType.Line);
                shapes.setColor(Color.WHITE);
                shapes.rect(px + margin, y - btnH, btnW, btnH);
                shapes.end();

                // left label
                game.batch.setProjectionMatrix(camera.combined);
                game.batch.begin();
                font.draw(game.batch, labels[i], px + margin + 4, y - btnH/2 + 6);

                // right “X/10”
                String valTxt = String.valueOf(values[i]);  // ← Hapus "/10"
                layout.setText(font, valTxt);
                font.draw(game.batch,
                    valTxt,
                    px + margin + btnW - layout.width - 4,
                    y - btnH/2 + 6);
                game.batch.end();
            }
        }


    }

    // selection enum
    private enum NavItem {
        T1, T2, T3,
        TRAP1, TRAP2, TRAP3,
        REMOVE
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

    private void resetAllEmptyZones() {
        for (Zone z : zones) {
            boolean hasAnytower = false;
            // Cek apakah masih ada tower di zone ini
            for (Tower tower : towers) {
                if (z.contains(tower.x, tower.y)) {
                    hasAnytower = true;
                    break;
                }
            }
            // Jika tidak ada tower, bebaskan zone
            if (!hasAnytower) {
                z.occupied = false;
            }
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
