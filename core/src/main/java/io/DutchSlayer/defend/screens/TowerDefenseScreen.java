package io.DutchSlayer.defend.screens;

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
import io.DutchSlayer.defend.objects.*;
import io.DutchSlayer.defend.enemy.Enemy;
import io.DutchSlayer.defend.ui.ImageLoader;
import io.DutchSlayer.defend.untils.AudioManager;

import static io.DutchSlayer.defend.objects.EnemyType.*;

/**
 * GameScreen adalah main controller untuk tower defense game
 * Menghandle rendering, input, game logic, UI, dan wave management
 */
public class TowerDefenseScreen implements Screen {
    /* ===== CORE COMPONENTS ===== */
    private final Main game;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapes;
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    // game entities
    // game entities - ADD THESE:
    private final Array<EnemyProjectile> enemyProjectiles = new Array<>();
    private final Array<BombAsset> bombs = new Array<>();
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

    // Tower costs - berbeda untuk setiap jenis
    private static final int TOWER1_COST = 50;  // AOE Tower - mahal karena AOE damage
    private static final int TOWER2_COST = 30;  // Fast Tower - murah tapi damage kecil
    private static final int TOWER3_COST = 40;  // Slow Tower - sedang, utility based

    // Trap costs - berbeda untuk setiap jenis
    private static final int TRAP_ATTACK_COST = 15;    // Attack trap - damage + slow
    private static final int TRAP_SLOW_COST = 20;      // Slow trap - utility tinggi
    private static final int TRAP_EXPLOSION_COST = 25; // Explosion trap - paling mahal, AOE

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

    // Tower deployment cooldown system
    private final float[] towerCooldowns = new float[3];        // Cooldown timer untuk setiap jenis tower
    private final float[] towerMaxCooldowns = {3f, 2f, 4f};    // Max cooldown: AOE=3s, Fast=2s, Slow=4s
    private final boolean[] towerCooldownActive = new boolean[3]; // Apakah sedang cooldown

    // Trap deployment cooldown system
    private final float[] trapCooldowns = new float[3];         // Cooldown timer untuk setiap jenis trap
    private final float[] trapMaxCooldowns = {2f, 3f, 5f};     // Max cooldown: Attack=2s, Slow=3s, Explosion=5s
    private final boolean[] trapCooldownActive = new boolean[3]; // Apakah sedang cooldown

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

    // Base upgrade costs
    private static final int BASE_ATTACK_UPGRADE_COST = 20;
    private static final int BASE_DEFENSE_UPGRADE_COST = 15;
    private static final int BASE_SPEED_UPGRADE_COST = 25;

    // Cost multiplier setiap upgrade (exponential growth)
    private static final float UPGRADE_COST_MULTIPLIER = 1.5f;

    // Wave spawn probabilities
    private static final float BASIC_SPAWN_CHANCE = 0.4f;    // 40%
    private static final float SHOOTER_SPAWN_CHANCE = 0.2f;  // 20%
    private static final float BOMBER_SPAWN_CHANCE = 0.15f;  // 15%
    private static final float SHIELD_SPAWN_CHANCE = 0.2f;   // 20%
    private static final float BOSS_SPAWN_CHANCE = 0.05f;    // 5%

    private boolean bossSpawned = false; // Prevent multiple boss spawns per wave

    private boolean isRemoveButtonHovered = false;
    private float mouseX = 0f, mouseY = 0f;
    private boolean isPauseButtonHovered = false;

    private boolean isGameWon = false;          // status kemenangan
    private Rectangle btnNext, btnMenuWin;      // buttons untuk win screen
    private Rectangle btnRetryLose, btnMenuLose; // buttons untuk lose screen
    // Ukuran UI dan button
    private static final float UI_WIDTH = 800f;
    private static final float UI_HEIGHT = 600f;
    private static final float BUTTON_WIDTH = 300f;
    private static final float BUTTON_HEIGHT = 100f;

    private boolean isWaveTransition = false;
    private float waveTransitionTimer = 0f;
    private static final float WAVE_TRANSITION_DELAY = 3f; // 3 detik delay antar wave
    private boolean waveCompleteBonusGiven = false;

    private final int currentStage;
    private static final int FINAL_STAGE = 4;


    public TowerDefenseScreen(final Main game, int stage) {
        this.game = game;
        this.currentStage = stage;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        gold = 80;

        ImageLoader.load();
        AudioManager.initialize();
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
//        recalcNavPositions();

        calculateNavButtonPositions();

        // after recalcNavPositions() in your constructor:
        float vw = camera.viewportWidth;
        float yNav = camera.viewportHeight - NAVBAR_HEIGHT/2 + 10f;

        // figure out where “Pause” was drawn
        float pauseSize = 60f; // ukuran button pause (sama dengan remove)
        float pauseX = vw - 20f - pauseSize;
        float pauseY = yNav - pauseSize/2 - 8f; // sama seperti remove untuk alignment
        btnPause = new Rectangle(pauseX, pauseY, pauseSize, pauseSize);

        // layout.setText(font, "Remove");  // hitung width Remove
        float removeSize = 60f;
        float removeX = pauseX - 20f - removeSize; // 20px spacing dari Pause
        float removeY = yNav - removeSize/2 - 8f;
        btnRemove = new Rectangle(removeX, removeY, removeSize, removeSize);

//        // input handling for selection and deployment
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                // Convert screen coordinates ke world coordinates
                Vector3 v = new Vector3(screenX, screenY, 0);
                camera.unproject(v);
                mouseX = v.x;
                mouseY = v.y;

                // Cek apakah mouse hover di atas remove button
                isRemoveButtonHovered = btnRemove.contains(mouseX, mouseY);
                isPauseButtonHovered = btnPause.contains(mouseX, mouseY);

                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 v = new Vector3(screenX, screenY, 0);
                camera.unproject(v);
                float x = v.x, y = v.y;

                // ===== WIN SCREEN HANDLING =====
                if (isGameWon && !isPaused) {
                    if (btnMenuWin != null && btnMenuWin.contains(x, y)) {
                        // Kembali ke main menu
                        System.out.println("Going to Main Menu...");
                        game.setScreen(new StageSelectionScreen(game, true));
                        // TODO: implement main menu transition
                        return true;
                    }
                    if (btnNext != null && btnNext.contains(x, y)) {
                        // Lanjut ke stage berikutnya (if not final stage)
                        if (currentStage < FINAL_STAGE) {
                            System.out.println("Going to Stage " + (currentStage + 1) + "...");
                            game.setScreen(new TowerDefenseScreen(game, currentStage + 1));
                        } else {
                            // If final stage completed, go back to stage selection
                            System.out.println("All stages completed! Returning to Stage Selection...");
                            game.setScreen(new StageSelectionScreen(game, true));
                        }
                        return true;
                    }
                    return true; // consume all clicks in win screen
                }

                // ===== LOSE SCREEN HANDLING =====
                if (isGameOver && !isGameWon && !isPaused) {
                    if (btnMenuLose != null && btnMenuLose.contains(x, y)) {
                        // Kembali ke main menu
                        System.out.println("Going to Main Menu...");
                        // TODO: implement main menu transition
                        return true;
                    }
                    if (btnRetryLose != null && btnRetryLose.contains(x, y)) {
                        // Restart game
                        System.out.println("Restarting game...");
                        restartGame();
                        return true;
                    }
                    return true; // consume all clicks in lose screen
                }

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
                            int cost = getAttackUpgradeCost(selectedTowerUI);
                            if (selectedTowerUI.canUpgrade() && gold >= cost) {
                                gold -= cost; // ← Deduct gold
                                selectedTowerUI.upgradeAttack();
                                System.out.println("Attack upgraded! Cost: " + cost + ", Gold left: " + gold);
                            } else if (gold < cost) {
                                System.out.println("❌ Not enough gold for Attack upgrade! Need: " + cost + ", Have: " + gold);
                            }
                            return true;
                        }
                        if (btnDefense.contains(x,y)) {
                            int cost = getDefenseUpgradeCost(selectedTowerUI);
                            if (selectedTowerUI.canUpgrade() && gold >= cost) {
                                gold -= cost; // ← Deduct gold
                                selectedTowerUI.upgradeDefense();
                                System.out.println("Defense upgraded! Cost: " + cost + ", Gold left: " + gold);
                            } else if (gold < cost) {
                                System.out.println("❌ Not enough gold for Defense upgrade! Need: " + cost + ", Have: " + gold);
                            }
                            return true;
                        }
                        if (btnSpeed.contains(x,y)) {
                            int cost = getSpeedUpgradeCost(selectedTowerUI);
                            if (selectedTowerUI.canUpgrade() && gold >= cost) {
                                gold -= cost; // ← Deduct gold
                                selectedTowerUI.upgradeSpeed();
                                System.out.println("Speed upgraded! Cost: " + cost + ", Gold left: " + gold);
                            } else if (gold < cost) {
                                System.out.println("❌ Not enough gold for Speed upgrade! Need: " + cost + ", Have: " + gold);
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
                            // Tentukan refund berdasarkan tower type
                            int refund = 0;
                            // Cek tower type dari texture atau buat field type di Tower class
                            switch(t.type) { // Assuming tower has type field
                                case AOE:  refund = TOWER1_COST / 2; break;
                                case FAST: refund = TOWER2_COST / 2; break;
                                case SLOW: refund = TOWER3_COST / 2; break;
                            }

                            gold += refund;
                            towers.removeIndex(i);
                            // ... zone management logic ...

                            System.out.println("Tower removed! Refund: " + refund + ", Gold: " + gold);
                            selectedType = null;
                            return true;
                        }
                    }
                    // Saat remove trap:
                    for (int i = trapZones.size - 1; i >= 0; i--) {
                        Trap tz = trapZones.get(i);
                        if (Intersector.isPointInPolygon(trapVerts.get(i), 0, trapVerts.get(i).length, x, y)
                            && tz.occupied) {

                            // Tentukan refund berdasarkan trap type
                            int refund = 0;
                            switch(tz.getType()) {
                                case ATTACK:    refund = TRAP_ATTACK_COST / 2; break;
                                case SLOW:      refund = TRAP_SLOW_COST / 2; break;
                                case EXPLOSION: refund = TRAP_EXPLOSION_COST / 2; break;
                            }

                            gold += refund;
                            tz.occupied = false;
                            System.out.println("Trap removed! Refund: " + refund + ", Gold: " + gold);
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
                            int towerIndex = 0; // Tower AOE
                            // Hitung kembali center zona seperti sebelumnya
                            if (!canDeployTower(towerIndex)) {
                                System.out.println("🔴 Tower AOE masih cooldown! Sisa: " + String.format("%.1f", towerCooldowns[towerIndex]) + "s");
                                return true;
                            }

                            int cost = getTowerCost(selectedType);
                            for (int zoneIdx = 0; zoneIdx < zones.size; zoneIdx++) {
                                Zone z = zones.get(zoneIdx);
                                if (!z.occupied && z.contains(x,y)) {
                                    if (gold >= cost) {
                                        gold -= cost;
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
                                        startTowerCooldown(towerIndex);
                                        AudioManager.playTowerDeploy();
                                        return true;
                                    } else {
                                        System.out.println("❌ Not enough gold! Need " + cost + ", have " + gold);
                                    }
                                    return true;
                                }
                            }
                            break;
                        }
                        case T2: {
                            int towerIndex = 1; // Tower Fast
                            int cost = getTowerCost(selectedType);

                            if (!canDeployTower(towerIndex)) {
                                System.out.println("🔴 Tower Fast masih cooldown! Sisa: " + String.format("%.1f", towerCooldowns[towerIndex]) + "s");
                                return true;
                            }
                            for (int zoneIdx = 0; zoneIdx < zones.size; zoneIdx++) {
                                Zone z = zones.get(zoneIdx);
                                if (!z.occupied && z.contains(x,y)) {
                                    if (gold >= cost) {
                                        gold -= cost;
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
                                        startTowerCooldown(towerIndex);
                                        AudioManager.playTowerDeploy();
                                        return true;
                                    } else {
                                        System.out.println("❌ Not enough gold! Need " + cost + ", have " + gold);
                                    }
                                    return true; // Consume click event
                                }
                            }
                            break;
                        }
                        case T3: {
                            int towerIndex = 2; // Tower Slow
                            int cost = getTowerCost(selectedType);

                            if (!canDeployTower(towerIndex)) {
                                System.out.println("🔴 Tower Slow masih cooldown! Sisa: " + String.format("%.1f", towerCooldowns[towerIndex]) + "s");
                                return true;
                            }

                            for (int zoneIdx = 0; zoneIdx < zones.size; zoneIdx++) {
                                Zone z = zones.get(zoneIdx);
                                if (!z.occupied && z.contains(x,y)) {
                                    if (gold >= cost) {
                                        gold -= cost;
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
                                        startTowerCooldown(towerIndex);
                                        AudioManager.playTowerDeploy();
                                        return true;
                                    } else {
                                        System.out.println("❌ Not enough gold! Need " + cost + ", have " + gold);
                                    }
                                    return true; // Consume click event
                                }
                            }
                            break;
                        }
                        case TRAP1: case TRAP2: case TRAP3:
                            // deploy trap…
                            int cost = getTrapCost(selectedType);
                            TrapType trapType;
                            String trapName;

                            switch(selectedType) {
                                case TRAP1:
                                    trapType = TrapType.ATTACK;
                                    trapName = "Attack";
                                    break;
                                case TRAP2:
                                    trapType = TrapType.SLOW;
                                    trapName = "Slow";
                                    break;
                                case TRAP3:
                                    trapType = TrapType.EXPLOSION;
                                    trapName = "Explosion";
                                    break;
                                default:
                                    trapType = TrapType.ATTACK;
                                    trapName = "Attack";
                                    break;
                            }

                            for (int trapIdx = 0; trapIdx < trapZones.size; trapIdx++) {
                                Trap tz = trapZones.get(trapIdx);
                                float[] trapVert = trapVerts.get(trapIdx);

                                if (!tz.occupied &&
                                    Intersector.isPointInPolygon(trapVert, 0, trapVert.length, x, y) &&
                                    gold >= cost) {

                                    gold -= cost;

                                    // ===== REPLACE OLD TRAP WITH NEW TYPED TRAP =====
                                    trapZones.set(trapIdx, new Trap(trapVert, 0.2f, trapType));
                                    trapZones.get(trapIdx).occupied = true;

                                    selectedType = null;
                                    AudioManager.playTrapDeploy();
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

    private void initializeStageSettings(int stage) {
        System.out.println("🎮 Initializing Stage " + stage + " settings...");

        switch(stage) {
            case 1:
                // Stage 1: Easy - Basic enemies only
                gold = 100; // More starting gold for beginners
                enemiesThisWave = 4; // Fewer enemies
                System.out.println("📊 Stage 1: Beginner mode - Basic enemies only");
                break;

            case 2:
                // Stage 2: Medium - Basic + some advanced
                gold = 80;
                enemiesThisWave = 5;
                System.out.println("📊 Stage 2: Medium difficulty");
                break;

            case 3:
                // Stage 3: Hard - All enemy types except boss
                gold = 60; // Less starting gold for challenge
                enemiesThisWave = 6;
                System.out.println("📊 Stage 3: Hard difficulty - No boss");
                break;

            case 4:
                // Stage 4: Final Boss Stage
                gold = 50; // Minimal starting gold for maximum challenge
                enemiesThisWave = 8; // Most enemies
                System.out.println("👑 Stage 4: FINAL BOSS STAGE!");
                break;

            default:
                // Fallback
                gold = 80;
                enemiesThisWave = 5;
                break;
        }
    }

    // Tambahkan method baru di GameScreen.java:
    private void calculateNavButtonPositions() {
        float vw = camera.viewportWidth;
        float yNav = camera.viewportHeight - NAVBAR_HEIGHT/2 + 10f;

        // Size untuk setiap button UI
        float buttonSize = 80f;  // ukuran square button
        float spacing = 18f;     // jarak antar button

        // Total width untuk 6 buttons + spacing
        float totalWidth = (buttonSize * 6) + (spacing * 5);

        // Starting X untuk center semua buttons
        float startX = (vw - totalWidth) / 2f;
        float buttonY = yNav - buttonSize/2 - 5f; // center vertikal

        // Hitung posisi setiap button (3 towers + 3 traps)
        for (int i = 0; i < 3; i++) {
            navTowerX[i] = startX + i * (buttonSize + spacing);
            navTowerW[i] = buttonSize; // width jadi size button
        }

        for (int i = 0; i < 3; i++) {
            navTrapX[i] = startX + (3 + i) * (buttonSize + spacing);
            navTrapW[i] = buttonSize; // width jadi size button
        }
    }

    private void update(float delta) {
        if (isPaused || isGameOver) return;

        updateCooldowns(delta);

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
                    // ===== SPECIAL HANDLING UNTUK BOMBER =====
                    if (e.getType() == BOMBER) {
                        System.out.println("💣 BOMBER COLLISION WITH TOWER!");
                        System.out.println("  Bomber position: (" + e.getX() + ", " + e.getBounds().y + ")");
                        System.out.println("  Tower position: (" + t.x + ", " + t.y + ")");

                        // Bomb akan "di-drop" di lokasi tower dan akan terlempar ke atas dulu seperti PickupItem
                        float dropX = e.getX(); // Use bomber's X position
                        float dropY = e.getBounds().y + e.getBounds().height/2;

                        BombAsset bomb = new BombAsset(
                            ImageLoader.bombAssetTex != null ? ImageLoader.bombAssetTex : ImageLoader.trapTex,
                            dropX,      // Drop location X (near tower)
                            dropY       // Drop location Y (ground level)
                        );

                        if (bomb != null) {
                            bombs.add(bomb);
                            System.out.println("💣 Bomb dropped with pickup-style animation!");
                            System.out.println("   Drop location: (" + dropX + ", " + dropY + ")");
                            System.out.println("   Bomb will fly up first, then fall down!");
                        }

                        // Remove bomber immediately (bomber menghilang setelah drop)
                        enemies.removeIndex(i);
                        System.out.println("💣 Bomber disappeared after dropping bomb!");

                        // Don't check other towers for this enemy
                        break;
                    }
                    // ===== NORMAL ENEMY COLLISION =====
                    else if (e.canAttack()) {
                        int damage = (e.getType() == EnemyType.SHIELD) ? 0 : 1; // Shield doesn't attack

                        if (damage > 0) {
                            t.takeDamage(damage);
                            System.out.println("⚔️ Enemy attacked tower! Tower HP: " + t.getHealth());
                        }
                        e.knockback();

                        if (t.isDestroyed()) {
                            towers.removeIndex(j);
                            if (t.isMain) {
                                isGameOver = true;
                                isGameWon = false;  // pastikan bukan kemenangan
                                setupLoseUI();      // setup button positions
                                System.out.println("💀 MAIN TOWER DESTROYED! GAME OVER!");
                            }
                        }
                    }
                    break;
                }
            }
            // Remove enemies that went off-screen (except bombers, already handled above)
            if (i < enemies.size && e.getX() < -e.getWidth()/2) {
                enemies.removeIndex(i);
            }
        }

        // 3) Shield protection mechanism
        for (int i = 0; i < enemies.size; i++) {
            Enemy e = enemies.get(i);
            if (e.getType() == EnemyType.BASIC) {
                e.seekProtection(enemies);
            }
        }

        // 4) Update enemy projectiles
        for (int i = enemyProjectiles.size - 1; i >= 0; i--) {
            EnemyProjectile ep = enemyProjectiles.get(i);
            ep.update(delta);

            // Check collision with towers
            boolean hit = false;
            for (int j = towers.size - 1; j >= 0; j--) {
                Tower t = towers.get(j);
                if (ep.getBounds().overlaps(t.getBounds())) {
                    t.takeDamage(ep.getDamage());
                    hit = true;

                    if (t.isDestroyed()) {
                        towers.removeIndex(j);
                        if (t.isMain) isGameOver = true;
                    }
                    break;
                }
            }

            // Remove if hit or off-screen
            if (hit || ep.getX() < -50f) {
                enemyProjectiles.removeIndex(i);
            }
        }

        // 5) Update bombs
        for (int i = bombs.size - 1; i >= 0; i--) {
            BombAsset bomb = bombs.get(i);
            bomb.update(delta);

            if (bomb.shouldExplode()) {
                bomb.explode(towers);
            }

            // Remove exploded bombs after delay
            if (bomb.hasExploded()) {
                bombs.removeIndex(i);
            }
        }

        // 6) Trap collision (same as before)
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);

            // Check collision with each trap (SAME LOGIC AS TOWER)
            for (int trapIdx = 0; trapIdx < trapZones.size; trapIdx++) {
                Trap trap = trapZones.get(trapIdx);

                // Only check occupied traps that aren't used
                if (!trap.occupied || trap.isUsed()) continue;

                // ===== DIRECT BOUNDS COLLISION (SAME AS TOWER) =====
                if (e.getBounds().overlaps(trap.bounds)) {
                    System.out.println("🎯 DIRECT TRAP COLLISION DETECTED!");
                    System.out.println("  Enemy bounds: " + e.getBounds());
                    System.out.println("  Trap bounds: " + trap.bounds);

                    // Apply trap effect directly (no method delegation)
                    switch (trap.getType()) {
                        case ATTACK:
                            System.out.println("⚔️ ATTACK TRAP TRIGGERED ⚔️");
                            e.takeDamage(1);
                            e.slow(2f);  // 2 second slow
                            System.out.println("Enemy took 1 damage and slowed for 2s");
                            break;

                        case SLOW:
                            System.out.println("🐌 SLOW TRAP TRIGGERED 🐌");
                            e.slowHeavy(5f, 0.1f);  // Heavy slow
                            System.out.println("Enemy heavily slowed for 5s (90% speed reduction)");
                            break;

                        case EXPLOSION:
                            System.out.println("💣 EXPLOSION TRAP TRIGGERED 💣");

                            // ===== FIXED: Process AOE BEFORE trap consumption =====
                            float trapX = trap.getCenterX();
                            float trapY = trap.getCenterY();
                            float explosionRadius = 250f;
                            int explosionDamage = 2;

                            System.out.println("💥 EXPLOSION at (" + trapX + ", " + trapY + ") radius: " + explosionRadius);
                            System.out.println("💥 Total enemies to check: " + enemies.size);

                            int hitCount = 0;
                            // AOE damage to all enemies within radius
                            for (int enemyIdx = enemies.size - 1; enemyIdx >= 0; enemyIdx--) {
                                Enemy target = enemies.get(enemyIdx);
                                if (target.isDestroyed()) continue;

                                // PERBAIKAN: Gunakan center point enemy yang benar
                                float targetX = target.getX(); // Sudah center dari Enemy class
                                float targetY = target.getBounds().y + target.getBounds().height / 2;

                                float distance = (float) Math.sqrt(
                                    Math.pow(trapX - targetX, 2) + Math.pow(trapY - targetY, 2)
                                );

                                System.out.println("🎯 Checking enemy " + enemyIdx + " at (" + targetX + ", " + targetY + ") - Distance: " + distance);

                                if (distance <= explosionRadius) {
                                    int oldHp = target.getHealth();
                                    target.takeDamage(explosionDamage);
                                    int newHp = target.getHealth();
                                    hitCount++;
                                    System.out.println("💥 EXPLOSION HIT! Enemy " + enemyIdx + " HP: " + oldHp + " → " + newHp + " (damage: " + explosionDamage + ")");
                                } else {
                                    System.out.println("❌ Enemy " + enemyIdx + " too far (distance: " + distance + " > radius: " + explosionRadius + ")");
                                }
                            }

                            System.out.println("💥 EXPLOSION COMPLETE! Hit " + hitCount + " enemies total!");
                            break;
                    }

                    // Mark trap as used (single-use)
                    trap.occupied = false;
                    System.out.println("💥 Trap consumed!");

                    break; // Only trigger one trap per enemy per frame
                }
            }
        }

        // 7) Tower shooting (same as before)
        for (Tower t : towers) {
            t.update(delta, enemies, projectiles);
        }

        // 8) Update projectiles (same as before)
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

        // 9) Cleanup dead enemies
        for (int j = enemies.size - 1; j >= 0; j--) {
            Enemy e = enemies.get(j);
            if (e.isDestroyed()) {
                enemies.removeIndex(j);
                gold += 10;
            }
        }

        // 10) Wave spawning (updated for boss handling)
        if (spawnCount < enemiesThisWave) {
            spawnTimer += delta;
            if (spawnTimer > 2f) {
                spawnTimer = 0f;
                spawnEnemy();
                spawnCount++;
            }
        }

        // Wave completion check
        if (spawnCount >= enemiesThisWave && enemies.size == 0) {
            if (!isWaveTransition && !waveCompleteBonusGiven) {
                // Berikan bonus gold untuk menyelesaikan wave
                int waveBonus = 50 + (currentWave * 10); // Bonus increases per wave
                gold += waveBonus;
                waveCompleteBonusGiven = true;

                System.out.println("🎉 Wave " + currentWave + " Complete! Bonus: +" + waveBonus + " gold");

                if (currentWave < MAX_WAVE) {
                    isWaveTransition = true;
                    waveTransitionTimer = 0f;
                } else {
                    isGameWon = true;
                    setupWinUI();
                    if (currentStage == FINAL_STAGE) {
                        System.out.println("🏆 CONGRATULATIONS! YOU COMPLETED THE FINAL STAGE! 🏆");
                    } else {
                        System.out.println("🎉 Stage " + currentStage + " completed! Ready for Stage " + (currentStage + 1));
                    }
                }
            }

            // Handle wave transition timer
            if (isWaveTransition) {
                waveTransitionTimer += delta;

                if (waveTransitionTimer >= WAVE_TRANSITION_DELAY) {
                    // Start next wave
                    currentWave++;
                    enemiesThisWave += 5; // Increase difficulty
                    spawnCount = 0;
                    bossSpawned = false;
                    isWaveTransition = false;
                    waveTransitionTimer = 0f;
                    waveCompleteBonusGiven = false;

                    System.out.println("🚀 Starting Wave " + currentWave + " with " + enemiesThisWave + " enemies!");
                }
            }
        }

        // 11) Gold income (same as before)
        goldTimer += delta;
        if (goldTimer >= INCOME_INTERVAL) {
            gold += INCOME_AMOUNT;
            goldTimer -= INCOME_INTERVAL;
        }
    }

    // ===== TAMBAHKAN METHOD INI DI update() =====
    private void updateCooldowns(float delta) {
        // Update tower cooldowns
        for (int i = 0; i < 3; i++) {
            if (towerCooldownActive[i]) {
                towerCooldowns[i] -= delta;
                if (towerCooldowns[i] <= 0f) {
                    towerCooldownActive[i] = false;
                    towerCooldowns[i] = 0f;
                    System.out.println("🟢 Tower " + (i+1) + " cooldown finished!");
                }
            }
        }

        // Update trap cooldowns
        for (int i = 0; i < 3; i++) {
            if (trapCooldownActive[i]) {
                trapCooldowns[i] -= delta;
                if (trapCooldowns[i] <= 0f) {
                    trapCooldownActive[i] = false;
                    trapCooldowns[i] = 0f;
                    System.out.println("🟢 Trap " + (i+1) + " cooldown finished!");
                }
            }
        }
    }

    // ===== METHOD UNTUK START COOLDOWN =====
    private void startTowerCooldown(int towerIndex) {
        towerCooldowns[towerIndex] = towerMaxCooldowns[towerIndex];
        towerCooldownActive[towerIndex] = true;
        System.out.println("🔴 Tower " + (towerIndex+1) + " cooldown started: " + towerMaxCooldowns[towerIndex] + "s");
    }

    private void startTrapCooldown(int trapIndex) {
        trapCooldowns[trapIndex] = trapMaxCooldowns[trapIndex];
        trapCooldownActive[trapIndex] = true;
        System.out.println("🔴 Trap " + (trapIndex+1) + " cooldown started: " + trapMaxCooldowns[trapIndex] + "s");
    }

    // ===== METHOD UNTUK CEK APAKAH BISA DEPLOY =====
    private boolean canDeployTower(int towerIndex) {
        return !towerCooldownActive[towerIndex];
    }

    private boolean canDeployTrap(int trapIndex) {
        return !trapCooldownActive[trapIndex];
    }


    // Tambahkan method untuk setup Win UI
    private void setupWinUI() {
        float centerX = camera.viewportWidth / 2f;
        float centerY = camera.viewportHeight / 2f;

        // Position buttons di bawah UI background
        float buttonY = centerY - UI_HEIGHT/2f + 50f; // 150px dari bawah UI
        float buttonSpacing = 50f;

        // Left button (MAIN MENU)
        float leftButtonX = centerX - BUTTON_WIDTH - buttonSpacing/2f;
        btnMenuWin = new Rectangle(leftButtonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);

        // Right button (NEXT)
        float rightButtonX = centerX + buttonSpacing/2f;
        btnNext = new Rectangle(rightButtonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    // Tambahkan method untuk setup Lose UI
    private void setupLoseUI() {
        float centerX = camera.viewportWidth / 2f;
        float centerY = camera.viewportHeight / 2f;

        // Position buttons di bawah UI background
        float buttonY = centerY - UI_HEIGHT/2f + 50f;
        float buttonSpacing = 50f;

        // Left button (MAIN MENU)
        float leftButtonX = centerX - BUTTON_WIDTH - buttonSpacing/2f;
        btnMenuLose = new Rectangle(leftButtonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);

        // Right button (RETRY)
        float rightButtonX = centerX + buttonSpacing/2f;
        btnRetryLose = new Rectangle(rightButtonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    // Tambahkan method untuk restart game
    private void restartGame() {
        // Reset semua game state
        isGameOver = false;
        isGameWon = false;
        isPaused = false;

        // Reset wave system
        currentWave = 1;
        spawnCount = 0;
        enemiesThisWave = 5;
        bossSpawned = false;

        // Reset gold
        gold = 80;
        goldTimer = 0f;
        spawnTimer = 0f;

        // Clear all entities
        enemies.clear();
        projectiles.clear();
        enemyProjectiles.clear();
        bombs.clear();

        // Reset towers (keep main tower, remove deployed towers)
        towers.clear();
        float towerMainH = ImageLoader.maintowertex.getHeight() * 0.6f;
        float tyMain = GROUND_Y + towerMainH/5f;
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

        // Reset zones
        for (Zone z : zones) {
            z.occupied = false;
        }
        deployedTowerZones.clear();

        // Reset traps
        for (int i = 0; i < trapZones.size; i++) {
            Trap trap = trapZones.get(i);
            trap.occupied = false;
        }

        // Reset UI selection
        selectedType = null;
        selectedTowerUI = null;

        System.out.println("🔄 Game restarted!");
    }

    // Tambahkan method baru untuk menggambar wave progress bar
    private void drawWaveProgressBar() {
        float vw = camera.viewportWidth;
        float vh = camera.viewportHeight;

        // === UBAH POSISI KE KIRI BAWAH ===
        // Dimensi progress bar
        float barWidth = 200f;  // Sedikit lebih kecil agar muat di pojok
        float barHeight = 15f;  // Sedikit lebih tipis

        // POSISI BARU: Kiri bawah (di atas wave label yang sudah ada)
        float barX = vw - barWidth - 20f;       // 20px dari kiri (sama dengan wave label)
        float barY = 20f;       // 60px dari bawah (di atas wave label dan enemy stats)

        // Hitung progress (berapa enemy yang sudah di-spawn vs total wave)
        float spawnProgress = (float) spawnCount / (float) enemiesThisWave;

        // Hitung progress enemy yang sudah mati/selesai
        int enemiesKilled = spawnCount - enemies.size;
        float killProgress = (float) enemiesKilled / (float) enemiesThisWave;

        // === BACKGROUND BAR ===
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.3f, 0.3f, 0.3f, 0.8f); // Dark gray background
        shapes.rect(barX, barY, barWidth, barHeight);
        shapes.end();

        // === SPAWN PROGRESS (Blue) ===
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.2f, 0.6f, 1f, 0.8f); // Blue - enemies spawned
        shapes.rect(barX, barY, barWidth * spawnProgress, barHeight);
        shapes.end();

        // === KILL PROGRESS (Green) ===
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.2f, 1f, 0.2f, 0.8f); // Green - enemies killed
        shapes.rect(barX, barY, barWidth * killProgress, barHeight);
        shapes.end();

        // === BORDER ===
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(Color.WHITE);
        shapes.rect(barX, barY, barWidth, barHeight);
        shapes.end();

        // === TEXT LABELS ===
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Wave title DI ATAS progress bar
        String waveTitle = "Wave " + currentWave + " / " + MAX_WAVE;
        layout.setText(font, waveTitle);
        float titleX = barX; // Align kiri dengan progress bar
        float titleY = barY + barHeight + 20f; // 20px di atas progress bar
        font.setColor(Color.WHITE);
        font.draw(game.batch, waveTitle, titleX, titleY);

        // Progress text DI DALAM progress bar (overlay)
        String progressText = enemiesKilled + "/" + enemiesThisWave;
        layout.setText(font, progressText);
        float progressX = barX + (barWidth - layout.width) / 2f; // Center di progress bar
        float progressY = barY + (barHeight + layout.height) / 2f; // Center vertikal
        font.setColor(Color.WHITE);
        font.draw(game.batch, progressText, progressX, progressY);

//        // Remaining enemies text DI KANAN progress bar
//        if (enemies.size > 0) {
//            String remainingText = "Active: " + enemies.size;
//            layout.setText(font, remainingText);
//            float remainingX = barX + barWidth + 10f; // 10px ke kanan dari progress bar
//            float remainingY = barY + (barHeight + layout.height) / 2f; // Center vertikal
//            font.setColor(Color.YELLOW);
//            font.draw(game.batch, remainingText, remainingX, remainingY);
//        }

        // Next wave indicator DI BAWAH progress bar
        if (spawnCount >= enemiesThisWave && enemies.size == 0 && currentWave < MAX_WAVE) {
            String nextWaveText = "Preparing Wave " + (currentWave + 1) + "...";
            layout.setText(font, nextWaveText);
            float nextX = barX; // Align kiri dengan progress bar
            float nextY = barY - 8f; // 8px di bawah progress bar
            font.setColor(Color.CYAN);
            font.draw(game.batch, nextWaveText, nextX, nextY);
        }

        font.setColor(Color.WHITE); // Reset color
        game.batch.end();
    }

    // Method untuk menggambar wave countdown (opsional)
    private void drawWaveCountdown() {
        if (isWaveTransition) {
            // PINDAHKAN COUNTDOWN KE TENGAH KANAN (tidak di tengah layar)
            float centerX = camera.viewportWidth - 200f;  // Kanan layar
            float centerY = camera.viewportHeight / 2f;   // Tengah vertikal

            // Calculate remaining time
            float remainingTime = WAVE_TRANSITION_DELAY - waveTransitionTimer;
            int seconds = (int) Math.ceil(remainingTime);

            String countdownText = "Wave " + (currentWave + 1) + " in " + seconds + "s";
            layout.setText(font, countdownText);

            // Background dengan ukuran yang lebih kecil
            float bgWidth = 150f;
            float bgHeight = 80f;
            float bgX = centerX - bgWidth/2f;
            float bgY = centerY - bgHeight/2f;

            shapes.setProjectionMatrix(camera.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 0f, 0f, 0.8f); // Dark background
            shapes.rect(bgX, bgY, bgWidth, bgHeight);
            shapes.end();

            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(Color.CYAN);
            shapes.rect(bgX, bgY, bgWidth, bgHeight);
            shapes.end();

            // Countdown text
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();
            font.setColor(Color.CYAN);

            // Main countdown
            font.draw(game.batch, countdownText,
                centerX - layout.width/2f,
                centerY + layout.height/2f);

            // Bonus info
            String bonusText = "Bonus: +" + (50 + (currentWave * 10));
            layout.setText(font, bonusText);
            font.setColor(Color.YELLOW);
            font.draw(game.batch, bonusText,
                centerX - layout.width/2f,
                centerY - 15f);

            font.setColor(Color.WHITE);
            game.batch.end();
        }
    }

    // Method untuk menggambar mini progress indicators per enemy type
    private void drawEnemyTypeProgress() {
        // PINDAHKAN POSISI ENEMY TYPE PROGRESS KE KANAN BAWAH
        float startX = camera.viewportWidth - 150f; // Kanan layar
        float startY = 120f; // Dari bawah, di atas navbar
        float iconSize = 20f; // Lebih kecil
        float spacing = 25f;  // Lebih rapat

        // Count enemies by type that are spawned vs killed
        int[] spawned = new int[5]; // BASIC, SHOOTER, BOMBER, SHIELD, BOSS
        int[] active = new int[5];

        // Count active enemies
        for (Enemy e : enemies) {
            switch(e.getType()) {
                case BASIC: active[0]++; break;
                case SHOOTER: active[1]++; break;
                case BOMBER: active[2]++; break;
                case SHIELD: active[3]++; break;
                case BOSS: active[4]++; break;
            }
        }

        // Estimate total spawned
        spawned[0] = (int)(enemiesThisWave * 0.4f); // 40% basic
        spawned[1] = (int)(enemiesThisWave * 0.2f); // 20% shooter
        spawned[2] = (int)(enemiesThisWave * 0.15f); // 15% bomber
        spawned[3] = (int)(enemiesThisWave * 0.2f); // 20% shield
        spawned[4] = bossSpawned ? 1 : 0; // Boss

        String[] typeIcons = {"⚔️", "🏹", "💣", "🛡️", "👑"};
        Color[] typeColors = {Color.WHITE, Color.GREEN, Color.ORANGE, Color.CYAN, Color.PURPLE};

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        for (int i = 0; i < 5; i++) {
            if (spawned[i] > 0) {
                float x = startX;
                float y = startY - i * spacing;

                // Icon
                font.setColor(typeColors[i]);
                font.draw(game.batch, typeIcons[i], x, y);

                // Progress text
                String progressText = active[i] + "/" + spawned[i];
                font.setColor(Color.LIGHT_GRAY);
                font.draw(game.batch, progressText, x + iconSize, y);
            }
        }

        font.setColor(Color.WHITE);
        game.batch.end();
    }

    // Tambahkan method untuk wave transition effects
    private void drawWaveTransition() {
        if (spawnCount >= enemiesThisWave && enemies.size == 0) {
            float centerX = camera.viewportWidth / 2f;
            float centerY = camera.viewportHeight / 2f;

            if (currentWave < MAX_WAVE) {
                // "Wave Complete" effect
                shapes.setProjectionMatrix(camera.combined);
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(0f, 1f, 0f, 0.3f); // Green overlay
                shapes.rect(0, 0, camera.viewportWidth, camera.viewportHeight);
                shapes.end();

                game.batch.setProjectionMatrix(camera.combined);
                game.batch.begin();

                String completeText = "Wave " + currentWave + " Complete!";
                layout.setText(font, completeText);
                font.setColor(Color.GREEN);
                font.draw(game.batch, completeText,
                    centerX - layout.width/2f,
                    centerY + 30f);

                String rewardText = "+50 Gold Bonus!";
                layout.setText(font, rewardText);
                font.setColor(Color.YELLOW);
                font.draw(game.batch, rewardText,
                    centerX - layout.width/2f,
                    centerY);

                game.batch.end();
            }
        }
    }

    // ===== HELPER METHOD FOR GOLD REWARDS =====
    private int getGoldReward(EnemyType enemyType) {
        switch(enemyType) {
            case BASIC: return 10;
            case SHOOTER: return 15;
            case BOMBER: return 12;
            case SHIELD: return 20; // High HP = higher reward
            case BOSS: return 50;   // Boss = big reward
            default: return 10;
        }
    }

    // refactor spawn code jadi method supaya reusable
    private void spawnEnemy() {
        float enemyH = ImageLoader.dutchtex.getHeight() * Enemy.scale;
        float ey     = GROUND_Y + 40f;

        // Determine enemy type based on wave and probability
        EnemyType enemyType = determineEnemyType();

        Enemy newEnemy = new Enemy(enemyType, 1280, ey);
        // ===== CRITICAL FIX: Set references SEBELUM add ke array =====
        newEnemy.setReferences(towers, enemyProjectiles, bombs);
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

    private EnemyType determineEnemyType() {
        switch(currentStage) {
            case 1:
                // Stage 1: Only BASIC and SHIELD enemies (beginner friendly)
                return Math.random() < 0.7f ? EnemyType.BASIC : EnemyType.SHIELD;

            case 2:
                // Stage 2: BASIC, SHIELD, and SHOOTER (no bombers or boss)
                float rand2 = (float) Math.random();
                if (rand2 < 0.5f) return EnemyType.BASIC;
                else if (rand2 < 0.8f) return EnemyType.SHIELD;
                else return EnemyType.SHOOTER;

            case 3:
                // Stage 3: All enemies EXCEPT boss
                float rand3 = (float) Math.random();
                if (rand3 < 0.3f) return EnemyType.BASIC;
                else if (rand3 < 0.5f) return EnemyType.SHOOTER;
                else if (rand3 < 0.7f) return EnemyType.BOMBER;
                else return EnemyType.SHIELD;

            case 4:
                // ===== STAGE 4: FINAL BOSS STAGE =====
                // Boss only spawns in Stage 4, Wave 3
                if (currentWave == 3 && !bossSpawned) {
                    // Option 1: Spawn boss as the LAST enemy (most dramatic)
                    if (spawnCount >= enemiesThisWave - 1) {
                        bossSpawned = true;
                        System.out.println("👑 FINAL BOSS SPAWNED AS LAST ENEMY! 👑");
                        return EnemyType.BOSS;
                    }

                    // Option 2: High probability early in the wave (80% chance)
                    if (Math.random() < 0.8f) {
                        bossSpawned = true;
                        System.out.println("👑 FINAL BOSS SPAWNED IN STAGE 4, WAVE 3! 👑");
                        return EnemyType.BOSS;
                    }
                }

                // Other waves in Stage 4: all enemy types except boss
                float rand4 = (float) Math.random();
                if (rand4 < BASIC_SPAWN_CHANCE) {
                    return EnemyType.BASIC;
                } else if (rand4 < BASIC_SPAWN_CHANCE + SHOOTER_SPAWN_CHANCE) {
                    return EnemyType.SHOOTER;
                } else if (rand4 < BASIC_SPAWN_CHANCE + SHOOTER_SPAWN_CHANCE + BOMBER_SPAWN_CHANCE) {
                    return EnemyType.BOMBER;
                } else {
                    return EnemyType.SHIELD;
                }

            default:
                // Fallback to normal spawning
                return EnemyType.BASIC;
        }
    }

    // Tambahkan method baru di GameScreen.java:
    private void drawNavbarButtons() {
        float vw = camera.viewportWidth;
        float yNav = camera.viewportHeight - NAVBAR_HEIGHT/2 + 10f;
        float buttonSize = 80f;
        float buttonY = yNav - buttonSize/2 - 5f;

        // Array textures dan costs
        Texture[] towerTextures = {
            ImageLoader.UITowerAOE,      // Akan menggunakan UITowerAOE untuk sementara
            ImageLoader.UITowerSpeed,     // ← GANTI dengan UITowerSpeed nanti
            ImageLoader.UITowerDefensif       // ← GANTI dengan UITowerDefensif nanti
        };

        Texture[] trapTextures = {
            ImageLoader.UITrapAttack,      // ← GANTI dengan UITrapAttack nanti
            ImageLoader.UITrapSlow,      // ← GANTI dengan UITrapSlow nanti
            ImageLoader.UITrapBomb       // ← GANTI dengan UITrapBomb nanti
        };

        int[] towerCosts = {TOWER1_COST, TOWER2_COST, TOWER3_COST};
        int[] trapCosts = {TRAP_ATTACK_COST, TRAP_SLOW_COST, TRAP_EXPLOSION_COST};

        // Draw tower buttons
        for (int i = 0; i < 3; i++) {
            float x = navTowerX[i];
            boolean isOnCooldown = towerCooldownActive[i];
            boolean canAfford = gold >= towerCosts[i];
            boolean isSelected = selectedType != null && selectedType.ordinal() == i;

            // ===== BUTTON COLOR LOGIC =====
            if (isOnCooldown) {
                game.batch.setColor(0.7f, 0.7f, 0.7f, 0.9f); // Lighter gray saat cooldown
            } else if (isSelected) {
                game.batch.setColor(1f, 1f, 0.8f, 1f); // Kuning saat selected
            } else if (!canAfford) {
                game.batch.setColor(1f, 0.7f, 0.7f, 1f); // Merah muda saat tidak mampu
            } else {
                game.batch.setColor(Color.WHITE); // Normal
            }

            // Draw button image
            if (towerTextures[i] != null) {
                game.batch.draw(towerTextures[i], x, buttonY, buttonSize, buttonSize);
            } else {
                // Fallback rectangle
                game.batch.end();
                shapes.setProjectionMatrix(camera.combined);
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(Color.GRAY);
                shapes.rect(x, buttonY, buttonSize, buttonSize);
                shapes.end();
                game.batch.begin();
            }

            if (isOnCooldown) {
                game.batch.end();
                // Calculate cooldown progress (0.0 = full cooldown, 1.0 = ready)
                float cooldownProgress = 1f - (towerCooldowns[i] / towerMaxCooldowns[i]);

                // Dark overlay dari atas ke bawah (seperti PvZ)
                float overlayHeight = buttonSize * (1f - cooldownProgress);

                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                shapes.setProjectionMatrix(camera.combined);
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(0f, 0f, 0f, 0.5f); // Dark overlay
                shapes.rect(x, buttonY + buttonSize - overlayHeight, buttonSize, overlayHeight);
                shapes.end();
                game.batch.begin();
            }
        }

        // Draw trap buttons dengan cooldown overlay (sama seperti tower)
        for (int i = 0; i < 3; i++) {
            float x = navTrapX[i];
            boolean isOnCooldown = trapCooldownActive[i];
            boolean canAfford = gold >= trapCosts[i];
            boolean isSelected = selectedType != null && selectedType.ordinal() == (3 + i);

            // Button color logic
            if (isOnCooldown) {
                game.batch.setColor(0.5f, 0.5f, 0.5f, 0.7f);
            } else if (isSelected) {
                game.batch.setColor(1f, 1f, 0.8f, 1f);
            } else if (!canAfford) {
                game.batch.setColor(1f, 0.7f, 0.7f, 1f);
            } else {
                game.batch.setColor(Color.WHITE);
            }

            // Draw button image
            if (trapTextures[i] != null) {
                game.batch.draw(trapTextures[i], x, buttonY, buttonSize, buttonSize);
            } else {
                game.batch.end();
                shapes.setProjectionMatrix(camera.combined);
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(Color.ORANGE);
                shapes.rect(x, buttonY, buttonSize, buttonSize);
                shapes.end();
                game.batch.begin();
            }

            // Cooldown overlay untuk trap
            if (isOnCooldown) {
                float cooldownProgress = 1f - (trapCooldowns[i] / trapMaxCooldowns[i]);
                float overlayHeight = buttonSize * (1f - cooldownProgress);

                game.batch.end();
                shapes.setProjectionMatrix(camera.combined);
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(0f, 0f, 0f, 0.5f);
                shapes.rect(x, buttonY + buttonSize - overlayHeight, buttonSize, overlayHeight);
                shapes.end();
                game.batch.begin();
            }
        }

        game.batch.setColor(Color.WHITE); // Reset color
        font.setColor(Color.WHITE); // Reset font color
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

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
        if (isGameOver || isGameWon) {
            // Draw background darkening
            shapes.setProjectionMatrix(camera.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0f, 0f, 0f, 0.7f); // dark overlay
            shapes.rect(0, 0, camera.viewportWidth, camera.viewportHeight);
            shapes.end();

            // Draw Win/Lose UI
            game.batch.setProjectionMatrix(camera.combined);
            game.batch.begin();

            float centerX = camera.viewportWidth / 2f;
            float centerY = camera.viewportHeight / 2f;
            float uiX = centerX - UI_WIDTH / 2f;
            float uiY = centerY - UI_HEIGHT / 2f;

            if (isGameWon) {
                // Draw Win UI
                if (ImageLoader.WinUI != null) {
                    game.batch.draw(ImageLoader.WinUI, uiX, uiY, UI_WIDTH, UI_HEIGHT);
                } else {
                    // Fallback text jika image tidak ada
                    font.draw(game.batch, "YOU WIN!", centerX - 50, centerY);
                }

                // Draw Win buttons
                if (ImageLoader.BtnMenu != null && btnMenuWin != null) {
                    game.batch.draw(ImageLoader.BtnMenu, btnMenuWin.x, btnMenuWin.y,
                        btnMenuWin.width, btnMenuWin.height);
                }
                if (ImageLoader.BtnNext != null && btnNext != null) {
                    game.batch.draw(ImageLoader.BtnNext, btnNext.x, btnNext.y,
                        btnNext.width, btnNext.height);
                }

            } else {
                // Draw Lose UI
                if (ImageLoader.LoseUI != null) {
                    game.batch.draw(ImageLoader.LoseUI, uiX, uiY, UI_WIDTH, UI_HEIGHT);
                } else {
                    // Fallback text jika image tidak ada
                    font.draw(game.batch, "GAME OVER", centerX - 50, centerY);
                }

                // Draw Lose buttons
                if (ImageLoader.BtnMenu != null && btnMenuLose != null) {
                    game.batch.draw(ImageLoader.BtnMenu, btnMenuLose.x, btnMenuLose.y,
                        btnMenuLose.width, btnMenuLose.height);
                }
                if (ImageLoader.BtnRetry != null && btnRetryLose != null) {
                    game.batch.draw(ImageLoader.BtnRetry, btnRetryLose.x, btnRetryLose.y,
                        btnRetryLose.width, btnRetryLose.height);
                }

                if (btnMenuLose == null || btnRetryLose == null) {
                    System.out.println("⚠️ Buttons are null, forcing setup...");
                    setupLoseUI();
                }
            }

            game.batch.end();
            return; // Stop rendering game elements
        }

        // 4) Gambar elemen navbar: Gold | [Tower,Trap] tengah | Remove & Pause kanan
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        drawGoldUI();

        // — Remove & Pause di kanan
        float rightMargin = 20f;
        float pauseSize = 80f;
        float removeSize = 60f;

        // Hitung posisi kedua button dulu
        float pauseX = vw - rightMargin - pauseSize;
        float pauseY = yNav - pauseSize/2 - 8f;
        float removeX = pauseX - 20f - removeSize; // Sekarang pauseX sudah tersedia
        float removeY = yNav - removeSize/2 - 8f;

        // ===== GAMBAR PAUSE BUTTON SEBAGAI IMAGE =====
        if (ImageLoader.PauseBtntex != null) {
            // Color tinting untuk Pause button
            if (isPauseButtonHovered) {
                game.batch.setColor(0.8f, 0.8f, 1f, 1f); // Biru muda saat hover
            } else {
                game.batch.setColor(Color.WHITE); // Normal
            }

            game.batch.draw(ImageLoader.PauseBtntex, pauseX, pauseY, pauseSize, pauseSize);
            game.batch.setColor(Color.WHITE); // Reset color
        } else {
            // Fallback jika image tidak ada
            game.batch.end();
            shapes.setProjectionMatrix(camera.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(Color.BLUE);
            shapes.rect(btnPause.x, btnPause.y, btnPause.width, btnPause.height);
            shapes.end();
            game.batch.begin();
        }

        // ===== GAMBAR REMOVE BUTTON (update position) =====
        if (ImageLoader.removeBtnTex != null) {
            if (selectedType == NavItem.REMOVE) {
                game.batch.setColor(1f, 0.2f, 0.2f, 1f);
            } else if (isRemoveButtonHovered) {
                game.batch.setColor(1f, 0.7f, 0.7f, 1f);
            } else {
                game.batch.setColor(Color.WHITE);
            }

            game.batch.draw(ImageLoader.removeBtnTex, removeX, removeY, removeSize, removeSize);
            game.batch.setColor(Color.WHITE);
        } else {
            // Fallback untuk Remove
            game.batch.end();
            shapes.setProjectionMatrix(camera.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(Color.BROWN);
            shapes.rect(btnRemove.x, btnRemove.y, btnRemove.width, btnRemove.height);
            shapes.end();
            game.batch.begin();
        }

        drawNavbarButtons();

        game.batch.end();

        game.batch.begin();
        for (Trap t : trapZones) t.drawBatch(game.batch);
        game.batch.end();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

// …— gold, pause/remove, navbar, dsb. …

// gambar Label Wave di paling bawah kiri
        game.batch.end();

// === TAMBAHKAN WAVE PROGRESS BAR DI SINI ===
        if (!isPaused && !isGameOver && !isGameWon) {
            drawWaveProgressBar();
            drawStageInfo();
//            drawEnemyTypeProgress(); // Opsional: mini progress per enemy type
//            drawWaveTransition();    // Opsional: wave transition effects
        }

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

            // Color-code HP text based on health percentage
            float healthPercent = (float)e.getHealth() / e.getMaxHealth();
            if (healthPercent > 0.6f) {
                font.setColor(Color.GREEN);
            } else if (healthPercent > 0.3f) {
                font.setColor(Color.YELLOW);
            } else {
                font.setColor(Color.RED);
            }

            // 3) gambar label HP
            font.draw(game.batch, hpText, textX, textY);

            // 3) Draw enemy type indicator
            String typeText = getEnemyTypeDisplay(e.getType());
            layout.setText(font, typeText);
            float typeX = e.getBounds().x + e.getBounds().width/2f - layout.width/2f;
            float typeY = textY + 15f;

            // Color-code type text
            font.setColor(getEnemyTypeColor(e.getType()));
            font.draw(game.batch, typeText, typeX, typeY);

            // 4) Draw state indicator for special enemies
            if (e.getType() == SHOOTER || e.getType() == BOMBER || e.getType() == BOSS) {
                String stateText = getStateDisplay(e);
                if (!stateText.isEmpty()) {
                    layout.setText(font, stateText);
                    float stateX = e.getBounds().x + e.getBounds().width/2f - layout.width/2f;
                    float stateY = typeY + 15f;
                    font.setColor(Color.CYAN);
                    font.draw(game.batch, stateText, stateX, stateY);
                }
            }
        }
        for (Projectile p : projectiles) p.drawBatch(game.batch);

        // Draw enemy projectiles
        for (EnemyProjectile ep : enemyProjectiles) {
            ep.drawBatch(game.batch);
        }

        // Draw bombs dengan pickup-style effects
        for (BombAsset bomb : bombs) {
            // 1) Main bomb rendering
            bomb.drawBatch(game.batch);

            // 2) Status indicator
            if (bomb.isFalling()) {
                // Altitude indicator saat terbang (seperti PickupItem offset)
                String statusText = "↑Flying";
                layout.setText(font, statusText);
                float textX = bomb.getX() - layout.width/2f;
                float textY = bomb.getY() + 35f;

                font.setColor(1f, 1f, 0f, 0.8f); // Yellow
                font.draw(game.batch, statusText, textX, textY);

                // Show altitude
                String altText = (int)bomb.getAltitude() + "px";
                layout.setText(font, altText);
                textX = bomb.getX() - layout.width/2f;
                textY = bomb.getY() + 20f;

                font.setColor(1f, 1f, 1f, 0.6f); // White transparent
                font.draw(game.batch, altText, textX, textY);
            }
            else if (bomb.isLanded() && !bomb.hasExploded()) {
                // Countdown timer saat landed
                float timeLeft = bomb.getTimeLeft();
                String countdownText = String.format("%.1f", timeLeft);

                layout.setText(font, countdownText);
                float textX = bomb.getX() - layout.width/2f;
                float textY = bomb.getY() + 35f;

                // Color berdasarkan urgency (sama seperti sebelumnya)
                if (timeLeft < 1f) {
                    font.setColor(1f, 0.2f, 0.2f, 1f); // Red - urgent!
                } else if (timeLeft < 2f) {
                    font.setColor(1f, 0.8f, 0.2f, 1f); // Orange - warning
                } else {
                    font.setColor(1f, 1f, 1f, 1f); // White - normal
                }

                font.draw(game.batch, countdownText, textX, textY);

                // Status label
                String statusText = "ARMED";
                layout.setText(font, statusText);
                textX = bomb.getX() - layout.width/2f;
                textY = bomb.getY() + 20f;

                font.setColor(1f, 0.4f, 0.4f, 0.8f); // Red transparent
                font.draw(game.batch, statusText, textX, textY);
            }
        }

        font.setColor(1f, 1f, 1f, 1f); // Reset color

        game.batch.end();



        // inside render(), after your normal world drawing:
        if (isPaused) {
            Gdx.gl.glEnable(GL20.GL_BLEND); // Tes: Aktifkan lagi di sini
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
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
        // Draw enemy projectiles
        for (EnemyProjectile ep : enemyProjectiles) {
            ep.drawShape(shapes);
        }
        shapes.end();

//        // Display enemy statistics
//        game.batch.begin();
//
//// Count enemies by type
//        int basicCount = 0, shooterCount = 0, bomberCount = 0, shieldCount = 0, bossCount = 0;
//        for (Enemy e : enemies) {
//            switch(e.getType()) {
//                case BASIC: basicCount++; break;
//                case SHOOTER: shooterCount++; break;
//                case BOMBER: bomberCount++; break;
//                case SHIELD: shieldCount++; break;
//                case BOSS: bossCount++; break;
//            }
//        }
//
//// Display enemy counts
//        String enemyStats = String.format("Enemies: ⚔️%d 🏹%d 💣%d 🛡️%d 👑%d",
//            basicCount, shooterCount, bomberCount, shieldCount, bossCount);
//        layout.setText(font, enemyStats);
//        float statsX = 20f;
//        float statsY = layout.height + 35f; // Below wave display
//        font.draw(game.batch, enemyStats, statsX, statsY);
//
//        game.batch.end();

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

            // Get upgrade costs
            int attackCost = getAttackUpgradeCost(selectedTowerUI);
            int defenseCost = getDefenseUpgradeCost(selectedTowerUI);
            int speedCost = getSpeedUpgradeCost(selectedTowerUI);

            // stat labels and values
            String[] labels = {
                "Attack ($" + attackCost + ")",
                "Defense ($" + defenseCost + ")",
                "Speed ($" + speedCost + ")"
            };

            int[] values = {
                selectedTowerUI.getAttackLevel(),
                selectedTowerUI.getDefenseLevel(),
                selectedTowerUI.getSpeedLevel()
            };

            // Check affordability untuk color coding
            boolean[] canAfford = {
                gold >= attackCost && selectedTowerUI.canUpgrade(),
                gold >= defenseCost && selectedTowerUI.canUpgrade(),
                gold >= speedCost && selectedTowerUI.canUpgrade()
            };

            // vertical starting point just below the header
            float startY = headerY - 8;

            for (int i = 0; i < 3; i++) {
                float y = startY - (i+1)*(btnH + margin);

                // ===== COLOR-CODED UPGRADE BARS =====
                shapes.begin(ShapeRenderer.ShapeType.Filled);

                // Background color berdasarkan affordability
                if (canAfford[i]) {
                    shapes.setColor(0.2f, 0.4f, 0.2f, 1f); // Dark green - affordable
                } else {
                    shapes.setColor(0.4f, 0.2f, 0.2f, 1f); // Dark red - not affordable
                };
                // ✅ DRAW THE RECTANGLE
                shapes.rect(px + margin, y - btnH, btnW, btnH);
                shapes.end();

                // Bar outline
                shapes.begin(ShapeRenderer.ShapeType.Line);
                if (canAfford[i]) {
                    shapes.setColor(Color.GREEN);  // Green border - affordable
                } else {
                    shapes.setColor(Color.RED);    // Red border - not affordable
                }
                shapes.rect(px + margin, y - btnH, btnW, btnH);
                shapes.end();

                // Text dengan color coding
                game.batch.setProjectionMatrix(camera.combined);
                game.batch.begin();

                // Left label dengan cost
                if (canAfford[i]) {
                    font.setColor(Color.WHITE);
                } else {
                    font.setColor(Color.LIGHT_GRAY);
                }

                font.draw(game.batch, labels[i], px + margin + 4, y - btnH/2 + 6);

                // Right level value
                String valTxt = String.valueOf(values[i]);
                layout.setText(font, valTxt);
                font.draw(game.batch,
                    valTxt,
                    px + margin + btnW - layout.width - 4,
                    y - btnH/2 + 6);

                // Reset font color
                font.setColor(Color.WHITE);
                game.batch.end();
            }
        }
    }

    private void drawGoldUI() {
        float vw = camera.viewportWidth;
        float vy = camera.viewportHeight;
        float yNav = vy - NAVBAR_HEIGHT/2 + 10f;

        // Posisi dan ukuran gold icon
        float goldIconSize = 80f;
        float goldX = 20f;
        float goldY = yNav - 50f; // sedikit lebih tinggi

        // Draw gold icon
        if (ImageLoader.goldIconTex != null) {
            game.batch.draw(ImageLoader.goldIconTex, goldX, goldY, goldIconSize, goldIconSize);
        } else {
            // Fallback circle
            game.batch.end();
            shapes.setProjectionMatrix(camera.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(Color.GOLD);
            shapes.circle(goldX + goldIconSize/2, goldY + goldIconSize/2, goldIconSize/2);
            shapes.end();
            game.batch.begin();
        }

        // Draw gold text di bawah icon
        String goldText = String.valueOf(gold);
        layout.setText(font, goldText);
        float textX = goldX + (goldIconSize - layout.width) / 2f;
        float textY = goldY + 20f; // 8px di bawah icon

        font.setColor(Color.YELLOW);
        font.draw(game.batch, goldText, textX, textY);
        font.setColor(Color.WHITE);
    }

    // ===== HELPER METHODS FOR DISPLAY =====
    private String getEnemyTypeDisplay(EnemyType type) {
        switch(type) {
            case BASIC: return "⚔️";    // Sword icon
            case SHOOTER: return "🏹";  // Bow icon
            case BOMBER: return "💣";   // Bomb icon
            case SHIELD: return "🛡️";   // Shield icon
            case BOSS: return "👑";     // Crown icon
            default: return "❓";
        }
    }

    private Color getEnemyTypeColor(EnemyType type) {
        switch(type) {
            case BASIC: return Color.WHITE;
            case SHOOTER: return Color.GREEN;
            case BOMBER: return Color.ORANGE;
            case SHIELD: return Color.CYAN;
            case BOSS: return Color.PURPLE;
            default: return Color.WHITE;
        }
    }

    private String getStateDisplay(Enemy enemy) {
        switch(enemy.getType()) {
            case SHOOTER:
                // ===== FIXED: Gunakan name() bukan toString() =====
                return enemy.getState().name().equals("ATTACKING") ? "🎯 Shooting" : "";
            case BOMBER:
                String stateName = enemy.getState().name();
                if (stateName.equals("BOMBING")) return "💣 Planting";
                if (stateName.equals("RETREATING")) return "🏃 Fleeing";
                return "";
            case BOSS:
                if (enemy.hasReachedTarget()) return "🔥 Attacking";
                return "📍 Moving";
            default:
                return "";
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


    // ===== 2) BUAT METHOD HELPER UNTUK GET COST =====
    private int getTowerCost(NavItem towerType) {
        switch(towerType) {
            case T1: return TOWER1_COST;  // AOE Tower
            case T2: return TOWER2_COST;  // Fast Tower
            case T3: return TOWER3_COST;  // Slow Tower
            default: return 40; // fallback
        }
    }

    private int getTrapCost(NavItem trapType) {
        switch(trapType) {
            case TRAP1: return TRAP_ATTACK_COST;    // Attack Trap
            case TRAP2: return TRAP_SLOW_COST;      // Slow Trap
            case TRAP3: return TRAP_EXPLOSION_COST; // Explosion Trap
            default: return 10; // fallback
        }
    }

    // ===== 2) METHOD UNTUK MENGHITUNG UPGRADE COST =====
    private int getAttackUpgradeCost(Tower tower) {
        // Formula: baseCost * (multiplier ^ currentLevel)
        return (int)(BASE_ATTACK_UPGRADE_COST * Math.pow(UPGRADE_COST_MULTIPLIER, tower.getAttackLevel()));
    }

    private int getDefenseUpgradeCost(Tower tower) {
        return (int)(BASE_DEFENSE_UPGRADE_COST * Math.pow(UPGRADE_COST_MULTIPLIER, tower.getDefenseLevel()));
    }

    private int getSpeedUpgradeCost(Tower tower) {
        return (int)(BASE_SPEED_UPGRADE_COST * Math.pow(UPGRADE_COST_MULTIPLIER, tower.getSpeedLevel()));
    }

    private void showUpgradeCostPreview(Tower tower) {
        int totalCost = 0;

        System.out.println("=== UPGRADE COST PREVIEW ===");
        System.out.println("Tower Type: " + tower.type);
        System.out.println("Current Gold: " + gold);
        System.out.println();

        System.out.println("Attack (Level " + tower.getAttackLevel() + "):");
        System.out.println("  Next upgrade: $" + getAttackUpgradeCost(tower));

        System.out.println("Defense (Level " + tower.getDefenseLevel() + "):");
        System.out.println("  Next upgrade: $" + getDefenseUpgradeCost(tower));

        System.out.println("Speed (Level " + tower.getSpeedLevel() + "):");
        System.out.println("  Next upgrade: $" + getSpeedUpgradeCost(tower));

        totalCost = getAttackUpgradeCost(tower) +
            getDefenseUpgradeCost(tower) +
            getSpeedUpgradeCost(tower);

        System.out.println();
        System.out.println("Total for all 3 upgrades: $" + totalCost);
        System.out.println("Upgrades remaining: " + tower.getRemainingUpgrades());
        System.out.println("==============================");
    }

    private void drawStageInfo() {
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        // Display stage info at top center
        String stageText = "STAGE " + currentStage;
        if (currentStage == FINAL_STAGE) {
            stageText += " - FINAL BOSS STAGE";
        }

        layout.setText(font, stageText);
        float stageX = (camera.viewportWidth - layout.width) / 2f;
        float stageY = camera.viewportHeight - 20f;

        // Special color for final stage
        if (currentStage == FINAL_STAGE) {
            font.setColor(Color.GOLD);
        } else {
            font.setColor(Color.WHITE);
        }

        font.draw(game.batch, stageText, stageX, stageY);
        font.setColor(Color.WHITE); // Reset

        game.batch.end();
    }


    // implementasi Screen kosong
    @Override public void resize(int w, int h) {}
    @Override public void show()       {
// Mengaktifkan blending secara global untuk OpenGL

        System.out.println("🎵 TowerDefenseScreen: Starting tower defense music...");
        AudioManager.playTowerDefenseMusic();
    }
    @Override public void hide()       {
        // ===== STOP MUSIC WHEN LEAVING TOWER DEFENSE =====
        System.out.println("🛑 TowerDefenseScreen: Stopping tower defense music...");
        AudioManager.stopMusic();
    }
    @Override public void pause()      {}
    @Override public void resume()     {}

    @Override
    public void dispose() {
        AudioManager.stopMusic();
        shapes.dispose();
        font.dispose();
        ImageLoader.dispose();
    }
}
