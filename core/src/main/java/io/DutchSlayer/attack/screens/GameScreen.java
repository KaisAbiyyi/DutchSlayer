package io.DutchSlayer.attack.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.DutchSlayer.Main;
import io.DutchSlayer.attack.boss.TankBoss;
import io.DutchSlayer.attack.enemy.AttackType;
import io.DutchSlayer.attack.enemy.BasicEnemy;
import io.DutchSlayer.attack.enemy.EnemyFactory;
import io.DutchSlayer.attack.objects.Building;
import io.DutchSlayer.attack.objects.BuildingGenerator;
import io.DutchSlayer.attack.objects.PickupItem;
import io.DutchSlayer.attack.objects.Tree;
import io.DutchSlayer.attack.player.Player;
import io.DutchSlayer.attack.player.weapon.Grenade;
import io.DutchSlayer.attack.objects.PickupType;
import io.DutchSlayer.attack.screens.logic.GameLogicHandler;
import io.DutchSlayer.attack.screens.render.GameRenderer;
import io.DutchSlayer.attack.screens.ui.PauseMenu;
import io.DutchSlayer.attack.screens.ui.VNManager;
import io.DutchSlayer.attack.screens.ui.VNScene;
import io.DutchSlayer.utils.Constant;
import io.DutchSlayer.assets.AssetLoader;

public class GameScreen implements Screen {
    private final Main game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final OrthographicCamera uiCamera; // <--- TAMBAHKAN INI
    private final Viewport uiViewport;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final BitmapFont font;

    private final Player player;
    private final Array<BasicEnemy> enemies = new Array<>();
    private final Array<PickupItem> pickupItems = new Array<>();
    private final Array<Grenade> grenades = new Array<>();
    private final Array<Tree> trees = new Array<>();
    private final Array<Building> buildings = new Array<>();
    private final Array<Vector2> respawnPoints = new Array<>();

    private final GameRenderer renderer;
    private final GameLogicHandler logicHandler;
    private final PauseMenu pauseMenu;
    private VNManager vnManager; // // NEW: Visual Novel Manager
    private Texture vnScene1Bg, vnScene2Bg, vnScene3Bg;
    private Texture vnScene4Bg, vnScene5Bg, vnScene6Bg, vnScene7Bg;

    private final int stageNumber;
    private final RandomXS128 rng;
    private final float mapWidth;

    private Texture backgroundTexture, bgTreeTexture, bgMountainTexture, terrainTexture, terrain2Texture, wallTexture;
    private Rectangle leftWall, rightWall;

    private boolean isPaused = false;
    private boolean isGameOver = false;
    private boolean isPlayerRespawning = false;
    private boolean triggerWallTrap = false;
    private boolean wallsAreRising = false;
    private boolean bossIntroPlayed = false;

    private final float wallRiseSpeed = 350f;
    private final float wallTargetY = Constant.TERRAIN_HEIGHT;
    private TankBoss tankBoss;
    private boolean isDefeatSequenceActive = false;

    private boolean isVictorySequenceActive = false;
    private float victoryDelayTimer = 0f;
    private float cameraTriggerX;

    private Music backgroundMusic; // <--- TAMBAHKAN INI
    private Music bossMusic;

    private Texture grenadeTexture, explosionTexture; // <-- TAMBAHKAN INI

    public GameScreen(Main game, int stageNumber) {
        this.game = game;
        this.stageNumber = stageNumber;
        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();
        this.font = new BitmapFont();
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, camera);
        this.viewport.apply();
        this.uiCamera = new OrthographicCamera();
        this.uiViewport = new FitViewport(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, uiCamera);
        this.mapWidth = Constant.MAP_WIDTH * (1f + 0.15f * (stageNumber - 1));
        this.rng = new RandomXS128(stageNumber);

        AssetLoader.load();

        this.player = new Player(camera);
        this.player.setGameScreen(this);


        loadTextures();

        this.backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("backgrounds/background.mp3"));
        this.bossMusic = Gdx.audio.newMusic(Gdx.files.internal("boss/boss_music.mp3"));

        this.backgroundMusic.setLooping(true);
        this.bossMusic.setLooping(true);

// --- UBAH ANGKA DI BAWAH INI ---

// Contoh: Mengubah volume musik latar menjadi 20% (sangat pelan)
        this.backgroundMusic.setVolume(0.2f);

// Contoh: Mengubah volume musik bos menjadi 75% (sedikit lebih pelan dari maksimal)
        this.bossMusic.setVolume(0.5f);


        generateTrees();
        generateBuildings();
        spawnEnemies();

        this.renderer = new GameRenderer();
        this.logicHandler = new GameLogicHandler();
        this.pauseMenu = new PauseMenu(game, game.uiSkin, uiViewport);

        this.vnManager = new VNManager(font);
        setupVNScripts();

        logicHandler.setupRespawnPoints(this);
    }

    private void loadTextures() {
        terrainTexture = new Texture(Gdx.files.internal("backgrounds/terrain.png"));
        terrain2Texture = new Texture(Gdx.files.internal("backgrounds/terrain2.png"));
        backgroundTexture = new Texture(Gdx.files.internal("backgrounds/sky.png"));
        bgTreeTexture = new Texture(Gdx.files.internal("backgrounds/bgTree.png"));
        bgMountainTexture = new Texture(Gdx.files.internal("backgrounds/bgMountain.png"));
        wallTexture = new Texture(Gdx.files.internal("boss/wall.png"));
        grenadeTexture = new Texture(Gdx.files.internal("player/grenade.png"));
        explosionTexture = new Texture(Gdx.files.internal("player/explosion.png"));
        vnScene1Bg = new Texture(Gdx.files.internal("story/scene1.png"));
        vnScene2Bg = new Texture(Gdx.files.internal("story/scene2.png"));
        vnScene3Bg = new Texture(Gdx.files.internal("story/scene3.png"));
        vnScene4Bg = new Texture(Gdx.files.internal("story/scene4.png"));
        vnScene5Bg = new Texture(Gdx.files.internal("story/scene5.png"));
        vnScene6Bg = new Texture(Gdx.files.internal("story/scene6.png"));
        vnScene7Bg = new Texture(Gdx.files.internal("story/scene7.png"));

        float wallWidth = 100f;
        float wallHeight = 300f;
        float startY = -wallHeight - 50f;

        leftWall = new Rectangle(0, startY, wallWidth, wallHeight);
        rightWall = new Rectangle(0, startY, wallWidth, wallHeight);
    }

    public void setupAndTriggerBossVictoryVN() {
        vnManager.clearScenes(); // Bersihkan adegan lain yang mungkin ada

        Array<String> dialogues = new Array<>();
        dialogues.add("COMMANDER: Hah... and they called you a 'hero'. Pathetic.");
        dialogues.add("COMMANDER: Look at you. Broken. Defeated. Just another casualty of a war you failed to understand.");
        dialogues.add("COMMANDER: Everything I've done... was to forge an era of strength and order. You were just a fool who stood in the way.");
        dialogues.add("COMMANDER: Now, be forgotten.");

        // Gunakan background scene 2 yang sudah ada (vnScene2Bg)
        VNScene defeatScene = new VNScene(vnScene2Bg, dialogues);
        vnManager.addScene(defeatScene);

        vnManager.start();
    }


    private void setupVNScripts() {
        // === SCENE 1: THE STARE DOWN ===
        Array<String> scene1Dialogues = new Array<>();
        scene1Dialogues.add("NARRATOR: After a long and punishing battle, only one obstacle remains.");
        scene1Dialogues.add("NARRATOR: The command post. The source of the relentless assault.");
        scene1Dialogues.add("YOU: (This is it... It all ends here.)");
        VNScene scene1 = new VNScene(vnScene1Bg, scene1Dialogues);
        vnManager.addScene(scene1);

        // === SCENE 2: THE TAUNT ===
        Array<String> scene2Dialogues = new Array<>();
        scene2Dialogues.add("COMMANDER: Well, well, well! Look what the cat dragged in!");
        scene2Dialogues.add("COMMANDER: A single, mud-covered grunt thinking he can play the hero. How utterly pathetic.");
        scene2Dialogues.add("COMMANDER: You're nothing! A tiny insect about to be crushed under my boot. Turn back now, and I might let you live.");
        VNScene scene2 = new VNScene(vnScene2Bg, scene2Dialogues);
        vnManager.addScene(scene2);

        // === SCENE 3: THE ULTIMATUM ===
        Array<String> scene3Dialogues = new Array<>();
        scene3Dialogues.add("YOU: I'm done running.");
        scene3Dialogues.add("YOU: I'm not here to play hero. I'm here to finish this.");
        scene3Dialogues.add("YOU: Come out of that tin can, or I'll drag you out myself!");
        VNScene scene3 = new VNScene(vnScene3Bg, scene3Dialogues);
        vnManager.addScene(scene3);
    }

    public void setupAndTriggerBossDefeatedVN() {
        // Kosongkan adegan sebelumnya untuk memastikan tidak ada sisa dari VN intro
        vnManager.clearScenes(); // Anda mungkin perlu menambahkan metode clearScenes() ke VNManager

        if (stageNumber < 3) {
            // --- Skenario A: Bos Kabur (Stage 1-2) ---

            // Scene 1: Disbelief
            Array<String> scene4Dialogues = new Array<>();
            scene4Dialogues.add("COMMANDER: No... NO! This can't be happening!");
            scene4Dialogues.add("COMMANDER: My beautiful war machine... destroyed by a single soldier?! Impossible!");
            vnManager.addScene(new VNScene(vnScene4Bg, scene4Dialogues));

            // Scene 2: The Escape
            Array<String> scene5Dialogues = new Array<>();
            scene5Dialogues.add("COMMANDER: This isn't the end! You haven't won!");
            scene5Dialogues.add("COMMANDER: I'll be back... and you will pay for this! YOU WILL PAY!");
            vnManager.addScene(new VNScene(vnScene5Bg, scene5Dialogues));

        } else {
            // --- Skenario B: Kekalahan Mutlak (Stage 3+) ---

            // Scene 1: Acceptance
            Array<String> scene4Dialogues = new Array<>();
            scene4Dialogues.add("COMMANDER: Heh... so this is it. The end of the line.");
            scene4Dialogues.add("COMMANDER: You really are something else, kid... You actually did it.");
            vnManager.addScene(new VNScene(vnScene4Bg, scene4Dialogues));

            // Scene 2: The End
            Array<String> scene6Dialogues = new Array<>();
            scene6Dialogues.add("NARRATOR: And so, the tyrant's reign of terror finally came to an end.");
            vnManager.addScene(new VNScene(vnScene6Bg, scene6Dialogues));

            // Scene 3: Reflection
            Array<String> scene1Dialogues = new Array<>();
            scene1Dialogues.add("YOU: It's over... It's finally... over.");
            vnManager.addScene(new VNScene(vnScene1Bg, scene1Dialogues));

            // Scene 4: Relief
            Array<String> scene7Dialogues = new Array<>();
            scene7Dialogues.add("YOU: (Now... I can finally rest.)");
            vnManager.addScene(new VNScene(vnScene7Bg, scene7Dialogues));
        }

        // Mulai Visual Novel
        if (vnManager.getSceneCount() > 0) {
            vnManager.start();
        }
    }

    private void generateTrees() {
        float avgTreeWidth = (Constant.TREE_MIN_WIDTH + Constant.TREE_MAX_WIDTH) / 2f;
        float spacePerTree = avgTreeWidth + 60f;
        float densityFactor = 0.8f;

        int targetCount = (int) (mapWidth / spacePerTree * densityFactor);
        int maxAttempts = targetCount * 5;

        int placed = 0;
        int attempts = 0;

        // Saran: Muat tekstur sekali saja di luar loop untuk performa yang lebih baik (lihat di bawah)
        Texture treeTexture = new Texture(Gdx.files.internal("trees/tree.png"));

        while (placed < targetCount && attempts < maxAttempts) {
            // Gunakan tekstur yang sudah dimuat
            Tree candidate = Tree.generateFixed(mapWidth, rng, treeTexture);

            // --- AWAL PERUBAHAN UNTUK VARIASI UKURAN ---

            // 1. Dapatkan ukuran asli pohon
            float originalWidth = candidate.getWidth();
            float originalHeight = candidate.getHeight();

            // 2. Buat faktor skala acak yang sedikit bervariasi (misal: antara 0.9f hingga 1.15f)
            // Ini berarti ukuran pohon akan bervariasi dari 90% hingga 115% dari ukuran asli.
            float scale = 0.9f + rng.nextFloat() * 0.25f;

            // 3. Hitung dan atur ukuran baru dengan menjaga rasio aspek
            float newWidth = originalWidth * scale;
            float newHeight = originalHeight * scale;
            candidate.setSize(newWidth, newHeight); // Asumsi ada metode setSize(w, h) di kelas Tree

            // --- AKHIR PERUBAHAN ---

            boolean tooClose = false;
            for (Tree existing : trees) {
                float dx = candidate.getX() - existing.getX();
                // Pengecekan jarak sekarang menggunakan ukuran pohon yang sudah bervariasi
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
        // Sebaiknya dispose tekstur ini di metode dispose() GameScreen
        // treeTexture.dispose(); // Jangan dispose di sini jika dipakai di tempat lain
    }


    private void generateBuildings() {
        buildings.clear();
        buildings.addAll(BuildingGenerator.generate(mapWidth, stageNumber));
    }


    private void spawnEnemies() {
        AttackType[] allowedTypes;
        int base = (int) (mapWidth / 180f);
        int bonus = stageNumber * 2;
        int numberOfEnemy = base + bonus;

        switch (stageNumber) {
            case 1:
                allowedTypes = new AttackType[]{AttackType.STRAIGHT_SHOOT};
                break;
            case 2:
                allowedTypes = new AttackType[]{AttackType.STRAIGHT_SHOOT, AttackType.ARC_GRENADE};
                break;
            case 3:
                allowedTypes = new AttackType[]{AttackType.BURST_FIRE, AttackType.ARC_GRENADE};
                break;
            default: // Untuk stage 4 dan seterusnya, atau jika stageNumber tidak ada dalam case di atas
                allowedTypes = new AttackType[]{AttackType.STRAIGHT_SHOOT, AttackType.BURST_FIRE, AttackType.ARC_GRENADE};
                break;
        }

        enemies.addAll(EnemyFactory.spawnDeterministicEnemies(stageNumber, numberOfEnemy, Constant.TERRAIN_HEIGHT, allowedTypes, this));
    }


    public void spawnTankBoss() {
        if (this.tankBoss == null || !this.tankBoss.isAlive()) {
            // Tentukan posisi spawn boss, di luar layar kanan
            float spawnX = camera.position.x + Constant.SCREEN_WIDTH / 2f + 100f; // 100f adalah offset agar benar-benar di luar
            float spawnY = Constant.TERRAIN_HEIGHT;

            this.tankBoss = new TankBoss(spawnX, spawnY, player, camera, this); // <--- Teruskan objek kamera
            this.player.setBoss(this.tankBoss); // Pastikan player tahu tentang boss yang baru
            System.out.println("GameScreen: TankBoss spawned!");
        }
    }


    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.53f, 0.81f, 0.92f, 1);

        // Proses input global seperti pause dan fullscreen terlebih dahulu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPaused = !isPaused;
            pauseMenu.setPaused(isPaused);

            if (isPaused) {
                if (backgroundMusic.isPlaying()) backgroundMusic.pause();
                if (bossMusic.isPlaying()) bossMusic.pause();
            } else {
                if (tankBoss != null && tankBoss.isAlive()) {
                    if (!bossMusic.isPlaying()) bossMusic.play();
                } else {
                    if (!backgroundMusic.isPlaying()) backgroundMusic.play();
                }
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        // --- LOGIKA UTAMA PERENDERAN (BAGIAN YANG DIMODIFIKASI) ---

        // 1. Jalankan logika game (pergerakan, kolisi, AI, dll)
        // Jangan jalankan logika jika sekuens kekalahan atau kemenangan sudah aktif.
        if (!isDefeatSequenceActive && !isVictorySequenceActive) {
            logicHandler.update(this, delta);
        }

        // --- BARU: Logika untuk memicu VN kekalahan ---
        // Cek jika game over, belum ada sekuens kekalahan, dan bos masih hidup
        if (isGameOver() && !isDefeatSequenceActive) {
            if (tankBoss != null && tankBoss.isAlive()) {
                isDefeatSequenceActive = true; // Tandai sekuens kekalahan aktif
                switchToBossMusic(); // Pastikan musik bos tetap berjalan
                setupAndTriggerBossVictoryVN(); // Mulai adegan VN
            } else {
                // Jika game over bukan karena bos, langsung ke layar GameOver
                game.setScreen(new GameOverScreen(game, stageNumber));
                return;
            }
        }

        // 2. Cek apakah ada VN yang sedang aktif (baik intro, kemenangan, atau kekalahan)
        if (vnManager.isActive()) {
            vnManager.update(delta); // Update logika VN (efek ketik, input)

            // Render VN di atas segalanya
            spriteBatch.setProjectionMatrix(uiViewport.getCamera().combined);
            spriteBatch.begin();
            vnManager.render(spriteBatch);
            spriteBatch.end();

            // --- BARU: Cek jika sekuens kekalahan baru saja selesai ---
            if (isDefeatSequenceActive && !vnManager.isActive()) {
                // Jika VN selesai, hentikan musik dan pindah ke layar GameOver
                if(bossMusic.isPlaying()) bossMusic.stop();
                game.setScreen(new GameOverScreen(game, stageNumber));
                return; // Penting untuk menghentikan frame ini
            }

            return; // Hentikan render di sini, jangan render dunia game saat VN berjalan.
        }

        // 3. Logika kemenangan (setelah bos dikalahkan dan VN kemenangan selesai)
        if (tankBoss != null && !tankBoss.isAlive() && !isVictorySequenceActive) {
            isVictorySequenceActive = true;
            victoryDelayTimer = 2.0f;
        }

        if (isVictorySequenceActive) {
            victoryDelayTimer -= delta;
            if (victoryDelayTimer <= 0) {
                if(bossMusic.isPlaying()) bossMusic.stop();
                game.setScreen(new GameVictoryScreen(game, stageNumber));
                return;
            }
        }

        // 4. Jika tidak ada sekuens atau VN yang aktif, render menu pause atau dunia game
        if (pauseMenu.renderIfActive(delta)) {
            return;
        }

        renderer.render(this, delta);
    }
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        uiViewport.update(width, height, true);
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
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    public void switchToBossMusic() {
        if (backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
        }
        if (!bossMusic.isPlaying()) {
            bossMusic.play();
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose(); //
        spriteBatch.dispose(); //
        font.dispose(); //
        backgroundTexture.dispose(); //
        bgTreeTexture.dispose(); //
        bgMountainTexture.dispose(); //
        terrainTexture.dispose(); //
        terrain2Texture.dispose(); //
        wallTexture.dispose(); //
        if (grenadeTexture != null) grenadeTexture.dispose(); //
        if (explosionTexture != null) explosionTexture.dispose(); //
        if (vnScene1Bg != null) vnScene1Bg.dispose();
        if (vnScene2Bg != null) vnScene2Bg.dispose();
        if (vnScene3Bg != null) vnScene3Bg.dispose(); // if (vnScene4Bg != null) vnScene4Bg.dispose();
        if (vnScene5Bg != null) vnScene5Bg.dispose();
        if (vnScene6Bg != null) vnScene6Bg.dispose();
        if (vnScene7Bg != null) vnScene7Bg.dispose();
        // NEW: Dispose VN background
        player.dispose(); //
        AssetLoader.dispose(); //
        vnManager.dispose(); // // NEW: Dispose VNManager

        if (backgroundMusic != null) backgroundMusic.dispose();
        if (bossMusic != null) bossMusic.dispose();


    }

    public VNManager getVnManager() {
        return vnManager;
    }

    // NEW: Getter and Setter for bossIntroPlayed flag
    public boolean isBossIntroPlayed() {
        return bossIntroPlayed;
    }

    public void setBossIntroPlayed(boolean bossIntroPlayed) {
        this.bossIntroPlayed = bossIntroPlayed;
    }

    // === Getter untuk komponen yang dibutuhkan oleh renderer dan logic ===

    public Main getGame() {
        return game;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    public BitmapFont getFont() {
        return font;
    }

    public TankBoss getTankBoss() {
        return tankBoss;
    }


    public Player getPlayer() {
        return player;
    }

    public Array<BasicEnemy> getEnemies() {
        return enemies;
    }

    public Array<Grenade> getGrenades() {
        return grenades;
    }

    public Array<PickupItem> getPickupItems() {
        return pickupItems;
    }

    public Array<Tree> getTrees() {
        return trees;
    }

    public Array<Building> getBuildings() {
        return buildings;
    }

    public Array<Vector2> getRespawnPoints() {
        return respawnPoints;
    }

    public Texture getBackgroundTexture() {
        return backgroundTexture;
    }

    public Texture getBgTreeTexture() {
        return bgTreeTexture;
    }

    public Texture getBgMountainTexture() {
        return bgMountainTexture;
    }

    public Texture getTerrainTexture() {
        return terrainTexture;
    }

    public Texture getTerrain2Texture() {
        return terrain2Texture;
    }

    public Texture getWallTexture() {
        return wallTexture;
    }

    public Texture getGrenadeTexture() {
        return grenadeTexture;
    }

    public Texture getExplosionTexture() {
        return explosionTexture;
    }

    public Rectangle getLeftWall() {
        return leftWall;
    }

    public Rectangle getRightWall() { // <--- Kembali tambahkan getter untuk rightWall
        return rightWall;
    }

    public float getCameraTriggerX() { // <--- Getter baru untuk cameraTriggerX
        return cameraTriggerX;
    }

    public void setCameraTriggerX(float cameraTriggerX) { // <--- Setter baru untuk cameraTriggerX
        this.cameraTriggerX = cameraTriggerX;
    }


    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean value) {
        isGameOver = value;
    }

    public boolean isPlayerRespawning() {
        return isPlayerRespawning;
    }

    public void setPlayerRespawning(boolean value) {
        isPlayerRespawning = value;
    }

    public boolean isTriggerWallTrap() {
        return triggerWallTrap;
    }

    public void setTriggerWallTrap(boolean value) {
        triggerWallTrap = value;
    }

    public boolean isWallsAreRising() {
        return wallsAreRising;
    }

    public void setWallsAreRising(boolean value) {
        wallsAreRising = value;
    }

    public float getWallRiseSpeed() {
        return wallRiseSpeed;
    }

    public float getWallTargetY() {
        return wallTargetY;
    }

    public float getMapWidth() {
        return mapWidth;
    }

    public int getStageNumber() {
        return stageNumber;
    }

    public void spawnPickup(float x, float y, PickupType type) {
        pickupItems.add(new PickupItem(x, y, type));
    }


}
