package io.DutchSlayer.attack.screens.logic;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import io.DutchSlayer.attack.boss.TankBoss;
import io.DutchSlayer.attack.boss.fsm.TankBossState;
import io.DutchSlayer.attack.enemy.BasicEnemy;
import io.DutchSlayer.attack.enemy.fsm.EnemyState;
import io.DutchSlayer.attack.player.Player;
import io.DutchSlayer.attack.player.PlayerState;
import io.DutchSlayer.attack.player.weapon.Bullet;
import io.DutchSlayer.attack.player.weapon.Grenade;
import io.DutchSlayer.attack.objects.PickupType;
import io.DutchSlayer.attack.screens.GameOverScreen;
import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.attack.screens.ui.VNManager;
import io.DutchSlayer.utils.Constant;

public class GameLogicHandler {
    private float pickupSpawnTimer; // <--- Timer untuk spawn pickup item
    private final float PICKUP_SPAWN_INTERVAL = 2.0f;

    public GameLogicHandler() { // <--- Tambahkan konstruktor ini
        this.pickupSpawnTimer = 0f;
    }

    public void update(GameScreen screen, float delta) {
        if (screen.getVnManager().isActive()) { //
            return; // VNManager.update() sudah dipanggil di GameScreen
        }
        if (!screen.isGameOver()) {
            checkEnemyBulletHitsPlayer(screen);
            checkBossBulletHitsPlayer(screen);
        }

        screen.getPlayer().update(delta);

        if (screen.getPlayer().isDead() && screen.getPlayer().getLives() <= 0 && !screen.getPlayer().getPlayerState().isWaitingToRespawn) {
            screen.setGameOver(true);
            return;
        }

        updatePickupItems(screen, delta);
        updateEnemies(screen, delta);
        updateGrenades(screen, delta);
        checkBulletEnemyCollision(screen);

        // Jika dinding sedang naik
        if (screen.isWallsAreRising()) {
            float deltaY = screen.getWallRiseSpeed() * delta;
            screen.getLeftWall().y += deltaY;
            screen.getRightWall().y += deltaY;

            // Batasi ketinggian dinding agar tidak melewati targetY
            if (screen.getLeftWall().y >= screen.getWallTargetY()) {
                screen.getLeftWall().y = screen.getWallTargetY();
                screen.getRightWall().y = screen.getWallTargetY();
                screen.setWallsAreRising(false); // Dinding berhenti

                // Mulai VN jika belum diputar. Jangan spawn boss di sini.
                if (!screen.isBossIntroPlayed()) {
                    VNManager vnManager = screen.getVnManager();
                    if (vnManager.getSceneCount() > 0 && !vnManager.isActive()) {
                        vnManager.start();
                        screen.setBossIntroPlayed(true);
                    }
                }
            }
        }

        if (screen.isBossIntroPlayed() && !screen.getVnManager().isActive() && screen.getTankBoss() == null) {
            screen.spawnTankBoss();
            screen.switchToBossMusic();
        }

        TankBoss boss = screen.getTankBoss();
        if (!screen.getVnManager().isActive() && boss != null && boss.isAlive()) { //
            boss.update(delta);

            // Pindahkan logika spawn pickup item ke sini, hanya jika bos sudah aktif
            if (boss.getCurrentState() == TankBossState.IDLE) { //
                pickupSpawnTimer += delta; //
                if (pickupSpawnTimer >= PICKUP_SPAWN_INTERVAL) { //
                    spawnRandomBossPickup(screen); //
                    pickupSpawnTimer = 0; // Reset timer
                }
            }
        }

        updateCamera(screen);

        // Logika untuk player terperangkap di dinding kiri dan kanan saat wall trap aktif
        if (screen.isTriggerWallTrap()) {
            Rectangle playerBounds = screen.getPlayer().getBounds();
            Rectangle leftWallBounds = screen.getLeftWall();
            Rectangle rightWallBounds = screen.getRightWall(); // <--- Dapatkan bounds dinding kanan

            // Batasi pemain agar tidak melewati dinding kiri
            if (playerBounds.x < leftWallBounds.x + leftWallBounds.width) {
                screen.getPlayer().setX(leftWallBounds.x + leftWallBounds.width);
            }
            // Batasi pemain agar tidak melewati dinding kanan
            if (playerBounds.x + playerBounds.width > rightWallBounds.x) { // <--- Batasi dengan dinding kanan
                screen.getPlayer().setX(rightWallBounds.x - playerBounds.width);
            }
        }
    }

    private void spawnRandomBossPickup(GameScreen screen) {
        // Tentukan posisi spawn: di tengah layar yang terkunci, di atas tanah
        float spawnX = screen.getCamera().position.x; // Pusat kamera adalah pusat layar
        float spawnY = Constant.TERRAIN_HEIGHT + 100f; // Sedikit di atas tanah agar jatuh

        PickupType type;
        float randomValue = MathUtils.random(); // Nilai acak antara 0.0 dan 1.0

        // Strategi "try hard": Prioritaskan granat, lalu shotgun, assault rifle paling jarang
        if (randomValue < 0.5f) { // 50% Grenade
            type = PickupType.GRENADE;
        } else { // 20% Assault Rifle (0.8f - 1.0f)
            type = PickupType.ASSAULT;
        }

        screen.spawnPickup(spawnX, spawnY, type);
        System.out.println("[PICKUP] Spawned " + type.name() + " at " + spawnX + ", " + spawnY);
    }

    private void updateCamera(GameScreen screen) {
        float playerX = screen.getPlayer().getX();
        float mapWidth = screen.getMapWidth();
        float screenHalfWidth = Constant.SCREEN_WIDTH / 2f;

        float camX;

        // Jika wall trap aktif, kamera dikunci di posisi triggerX pemain
        if (screen.isTriggerWallTrap()) {
            camX = screen.getCameraTriggerX(); // <--- Kamera terkunci di posisi X pemain saat trigger
        } else {
            // Kamera mengikuti pemain, dikunci di batas peta
            camX = MathUtils.clamp(
                playerX,
                screenHalfWidth,
                mapWidth - screenHalfWidth
            );
        }

        screen.getCamera().position.set(camX, Constant.SCREEN_HEIGHT / 2f, 0);
        screen.getCamera().update();
        screen.getViewport().apply();
    }


    private void updatePickupItems(GameScreen screen, float delta) {
        screen.getPickupItems().forEach(item -> item.update(screen.getPlayer(), delta));
    }

    private void updateEnemies(GameScreen screen, float delta) {
        for (BasicEnemy enemy : screen.getEnemies()) {
            if (enemy.isAlive()) {
                enemy.update(delta, screen.getPlayer().getPosition());
            } else {
                for (Bullet b : enemy.getBullets()) {
                    b.update(delta, screen.getCamera().position.x - Constant.SCREEN_WIDTH / 2f,
                        screen.getCamera().position.x + Constant.SCREEN_WIDTH / 2f);
                }
            }
            if (!enemy.isAlive() && !enemy.hasDropBeenChecked()) {
                // Tandai bahwa drop sudah dicek untuk musuh ini, agar tidak drop berkali-kali
                enemy.setDropChecked(true);

                // Peluang 30% untuk drop item
                if (MathUtils.randomBoolean(0.3f)) {
                    PickupType type;
                    // Logika pemilihan tipe item berdasarkan stage (sama seperti sebelumnya)
                    switch (screen.getStageNumber()) {
                        case 1, 2:
                            type = MathUtils.randomBoolean() ? PickupType.ASSAULT : PickupType.GRENADE;
                            break;
                        default:
                            type = switch (MathUtils.random(2)) {
                                case 0 -> PickupType.ASSAULT;
                                default -> PickupType.GRENADE;
                            };
                            break;
                    }
                    // Spawn pickup di posisi musuh mati
                    screen.spawnPickup(enemy.getX(), enemy.getY() + enemy.getHeight() / 2f, type);
                }
            }
        }


        boolean allBasicEnemiesDefeated = true;
        for (BasicEnemy e : screen.getEnemies()) {
            if (e.isAlive()) {
                allBasicEnemiesDefeated = false;
                break;
            }
        }

        // Kondisi untuk memicu wall trap:
        // Semua musuh dasar dikalahkan DAN pemain sudah berada di area yang akan menjadi ujung kanan layar terkunci.
        // Pemicu akan aktif saat semua musuh dasar dikalahkan DAN pemain sudah berada di ujung peta.
        // Kita tidak lagi perlu playerAtEndOfMap, karena pemicu bisa di mana saja.
        // Cukup cek allBasicEnemiesDefeated.
        if (allBasicEnemiesDefeated && !screen.isTriggerWallTrap()) { // <--- Hapus kondisi playerAtEndOfMap
            screen.setTriggerWallTrap(true);
            screen.setWallsAreRising(true);

            // Simpan posisi X pemain saat ini sebagai titik tengah kamera yang terkunci
            screen.setCameraTriggerX(screen.getPlayer().getX() + screen.getPlayer().getWidth() / 2f); // <--- Simpan posisi tengah pemain

            // Set posisi awal dinding kiri dan kanan relatif terhadap cameraTriggerX
            float wallWidth = screen.getLeftWall().width; // Asumsi lebar dinding sama
            float screenHalfWidth = Constant.SCREEN_WIDTH / 2f;

            // Dinding kiri akan muncul di tepi kiri layar yang terkunci
            screen.getLeftWall().x = screen.getCameraTriggerX() - screenHalfWidth;
            // Dinding kanan akan muncul di tepi kanan layar yang terkunci
            screen.getRightWall().x = screen.getCameraTriggerX() + screenHalfWidth - wallWidth;
        }
    }

    private void updateGrenades(GameScreen screen, float delta) {
        for (Grenade grenade : screen.getGrenades()) {
            grenade.update(delta);

            // Hanya cek tabrakan jika granat belum meledak.
            if (!grenade.isExploded()) { //
                Rectangle grenadeRect = new Rectangle(grenade.getX() - 6f, grenade.getY() - 6f, 12f, 12f); //

                // 1. Cek tabrakan dengan BasicEnemy
                for (BasicEnemy enemy : screen.getEnemies()) { //
                    if (enemy.isAlive()) { //
                        if (enemy.getCurrentState() == EnemyState.DYING) { //
                            continue; // Lanjut ke musuh berikutnya.
                        }
                        Rectangle enemyRect = new Rectangle(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight()); //
                        if (grenadeRect.overlaps(enemyRect)) { //
                            // --- MODIFIKASI DIMULAI DI SINI ---
                            // Jika granat adalah granat musuh dan menabrak sesama musuh, JANGAN meledak.
                            // Granat musuh hanya boleh meledak saat menabrak pemain atau karena timer.
                            if (grenade.isEnemyGrenade()) { //
                                // Biarkan granat musuh menembus sesama musuh jika kondisi ini terpenuhi.
                                // Jangan memanggil forceExplode() atau melakukan break.
                                continue; // Lanjut ke musuh berikutnya atau granat berikutnya.
                            }
                            // --- MODIFIKASI SELESAI ---

                            // Hitung titik tengah vertikal musuh sebagai titik impact
                            float impactPointY = enemy.getY() + enemy.getHeight() / 2f; //
                            grenade.forceExplode(impactPointY); // Panggil ledakan dengan posisi Y
                            break; // Granat ini sudah meledak, tidak perlu cek musuh lain
                        }
                    }
                }

                // Jika granat sudah meledak karena kena musuh, tidak perlu cek boss lagi.
                if (grenade.isExploded()) { //
                    // Lanjutkan ke bagian damage di bawah
                } else { //
                    // 2. Cek tabrakan dengan TankBoss
                    TankBoss boss = screen.getTankBoss(); //
                    if (boss != null && boss.isAlive()) { //
                        Rectangle bossRect = new Rectangle(boss.getPosition().x, boss.getPosition().y, boss.getWidth(), boss.getHeight()); //
                        if (grenadeRect.overlaps(bossRect)) { //
                            // --- MODIFIKASI DIMULAI DI SINI ---
                            // Jika granat adalah granat musuh dan menabrak boss (sesama musuh), JANGAN meledak.
                            if (grenade.isEnemyGrenade()) { //
                                continue; // Biarkan granat musuh menembus boss.
                            }
                            // --- MODIFIKASI SELESAI ---

                            // Hitung titik tengah vertikal boss sebagai titik impact
                            float impactPointY = boss.getPosition().y + boss.getHeight() / 2f; //
                            grenade.forceExplode(impactPointY); // Panggil ledakan dengan posisi Y
                        }
                    }
                }
            }

            // Logika damage setelah ledakan
            if (grenade.shouldDealDamage()) {
                float explosionX = grenade.getX();
                float explosionY = grenade.getY(); // Kita gunakan Y asli granat untuk pusat ledakan
                float explosionRadius = grenade.getRadius();
                int explosionDamage = grenade.getDamage();

                // Damage BasicEnemy (tidak ada perubahan)
                if (!grenade.isEnemyGrenade()) { //
                    for (BasicEnemy enemy : screen.getEnemies()) { //
                        if (enemy.isAlive()) { //
                            enemy.checkHitByExplosion(explosionX, explosionY, explosionRadius, explosionDamage); //
                        }
                    }
                }


                // --- PERUBAHAN LOGIKA DAMAGE PADA BOSS DIMULAI DI SINI ---

                TankBoss boss = screen.getTankBoss();
                if (boss != null && boss.isAlive()) {
                    // Buat Bounding Box untuk boss
                    Rectangle bossRect = new Rectangle(boss.getPosition().x, boss.getPosition().y, boss.getWidth(), boss.getHeight());

                    // Temukan titik terdekat pada kotak boss ke pusat lingkaran ledakan
                    float closestX = Math.max(bossRect.x, Math.min(explosionX, bossRect.x + bossRect.width));
                    float closestY = Math.max(bossRect.y, Math.min(explosionY, bossRect.y + bossRect.height));

                    // Hitung jarak kuadrat dari titik terdekat ke pusat ledakan
                    float distanceX = explosionX - closestX;
                    float distanceY = explosionY - closestY;
                    float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);

                    // Jika jarak kuadrat lebih kecil dari radius ledakan kuadrat, maka terjadi tabrakan
                    if (distanceSquared < (explosionRadius * explosionRadius)) {
                        boss.takeHit(explosionDamage); // Langsung berikan damage ke boss
                    }
                }

                // --- PERUBAHAN LOGIKA DAMAGE PADA BOSS SELESAI ---


                // Damage ke Player (jika granat dari musuh)
                if (grenade.isEnemyGrenade()) {
                    Player player = screen.getPlayer();
                    if (player.isHitByExplosion(explosionX, explosionY, explosionRadius)) {
                        player.takeDeath();
                        System.out.println("[Player] Hit by enemy grenade explosion! Taking death.");
                    }
                }

                grenade.markDamageDealt();
            }
        }

        // Loop untuk menghapus granat yang sudah selesai (TIDAK PERLU DIUBAH)
        for (int i = screen.getGrenades().size - 1; i >= 0; i--) {
            if (screen.getGrenades().get(i).isFinished()) {
                screen.getGrenades().removeIndex(i);
            }
        }
    }

    private void checkBulletEnemyCollision(GameScreen screen) {
        for (BasicEnemy enemy : screen.getEnemies()) {
            if (!enemy.isAlive()) continue;

            for (Bullet bullet : screen.getPlayer().getBullets()) {
                if (!bullet.isAlive()) continue;

                boolean overlap = bullet.getX() < enemy.getX() + enemy.getWidth() &&
                    bullet.getX() + bullet.getWidth() > enemy.getX() &&
                    bullet.getY() < enemy.getY() + enemy.getHeight() &&
                    bullet.getY() + bullet.getHeight() > enemy.getY();

                if (overlap) {
                    // --- PERUBAHAN LOGIKA DIMULAI DI SINI ---

                    // Cek state musuh SEBELUM memproses tabrakan.
                    // Jika musuh sedang dalam animasi kematian, jangan lakukan apa-apa (peluru tembus).
                    if (enemy.getCurrentState() == EnemyState.DYING) {
                        continue; // Lanjut ke pengecekan peluru/musuh berikutnya, abaikan tabrakan ini.
                    }

                    // --- PERUBAHAN LOGIKA SELESAI ---

                    // Jika musuh tidak sedang sekarat, baru proses tabrakan seperti biasa.
                    bullet.kill();
                    enemy.takeHit();
                    break;
                }
            }
        }
    }

    // Di dalam file GameLogicHandler.java

    private void checkEnemyBulletHitsPlayer(GameScreen screen) {
        Player player = screen.getPlayer(); // Dapatkan objek player sekali di awal
        PlayerState ps = player.getPlayerState(); // Dapatkan PlayerState untuk pemeriksaan status

        for (BasicEnemy enemy : screen.getEnemies()) {
            if (!enemy.isAlive()) continue; // Lewati musuh yang sudah mati

            for (Bullet bullet : enemy.getBullets()) {
                if (!bullet.isAlive()) continue; // Lewati peluru yang sudah tidak aktif

                // Deteksi apakah ada overlap fisik antara peluru dan pemain
                boolean physicalOverlap = bullet.getX() < ps.x + ps.playerWidth &&
                    bullet.getX() + bullet.getWidth() > ps.x &&
                    bullet.getY() < ps.y + ps.playerHeight &&
                    bullet.getY() + bullet.getHeight() > ps.y;

                if (physicalOverlap) {
                    // Kondisi di mana pemain dianggap "tidak bisa diserang" (mati permanen atau menunggu respawn)
                    boolean playerIsOutOfPlay = ps.isDead && !ps.isWaitingToRespawn; // Mati permanen (game over)
                    boolean playerIsRespawning = ps.isWaitingToRespawn; // Sedang dalam timer 2 detik untuk respawn

                    if (playerIsOutOfPlay || playerIsRespawning) {
                        // Jika pemain mati permanen atau sedang menunggu respawn, peluru menembus.
                        // Tidak ada aksi pada peluru (bullet.kill()) atau pemain.
                        continue; // Lanjut ke peluru berikutnya
                    }

                    // Jika sampai di sini, pemain tidak mati permanen dan tidak sedang menunggu respawn.
                    // Sekarang periksa apakah pemain invincible.
                    if (player.isInvincible()) { // Menggunakan metode isInvincible() dari Player
                        bullet.kill(); // Peluru mengenai pemain yang invincible, peluru hilang, tidak ada damage.
                        // Player.isInvincible() mengambil dari ps.invincibilityTimer > 0f.
                    } else {
                        // Pemain tidak mati, tidak menunggu respawn, dan tidak invincible. Pemain rentan.
                        bullet.kill();      // Peluru mengenai pemain, peluru hilang.
                        player.takeDeath(); // Pemain menerima damage/mati.
                    }

                    // Setelah peluru mengenai (baik vulnerable atau invincible) dan dimatikan,
                    // hentikan pemeriksaan peluru lain dari musuh ini untuk frame ini.
                    break;
                }
            }
        }

        TankBoss boss = screen.getTankBoss();
        if (boss != null && boss.isAlive()) {
            for (Bullet bullet : screen.getPlayer().getBullets()) {
                if (!bullet.isAlive()) continue;

                // AABB Collision check (mirip dengan BasicEnemy)
                boolean overlap = bullet.getX() < boss.getPosition().x + boss.getWidth() &&
                    bullet.getX() + bullet.getWidth() > boss.getPosition().x &&
                    bullet.getY() < boss.getPosition().y + boss.getHeight() &&
                    bullet.getY() + bullet.getHeight() > boss.getPosition().y;

                if (overlap) {
                    bullet.kill();
                    // Asumsi peluru pemain memiliki damage, misal 10f atau ambil dari bullet.getDamage() jika ada
                    float playerBulletDamage = 1f; // Ganti dengan nilai damage peluru yang sesuai

                    boss.takeHit(playerBulletDamage);
                    break; // Satu peluru hanya mengenai satu target
                }
            }
        }
    }

    private void checkBossBulletHitsPlayer(GameScreen screen) {
        TankBoss boss = screen.getTankBoss();
        // Pastikan boss ada, hidup, dan memiliki peluru sebelum melanjutkan
        if (boss == null || !boss.isAlive() || boss.getBullets().isEmpty()) {
            return;
        }

        Player player = screen.getPlayer();
        PlayerState ps = player.getPlayerState();

        // Iterasi melalui peluru boss
        for (io.DutchSlayer.attack.boss.BossBullet bullet : boss.getBullets()) { // Gunakan FQN jika ada ambiguitas nama kelas Bullet
            if (!bullet.isAlive()) continue;

            // Deteksi overlap fisik (AABB collision)
            // Asumsi BossBullet memiliki getX(), getY(), getWidth(), getHeight()
            // Jika tidak, Anda perlu menambahkannya atau menyesuaikan logika tabrakan.
            // Untuk BossBullet dari kode Anda, sepertinya ia dirender sebagai rect kecil, kita bisa asumsikan width/height kecil
            float bulletWidth = 10f; // Ganti dengan ukuran peluru boss yang sebenarnya
            float bulletHeight = 10f; // Ganti dengan ukuran peluru boss yang sebenarnya

            boolean physicalOverlap = bullet.getX() < ps.x + ps.playerWidth &&
                bullet.getX() + bulletWidth > ps.x &&
                bullet.getY() < ps.y + ps.playerHeight &&
                bullet.getY() + bulletHeight > ps.y;

            if (physicalOverlap) {
                boolean playerIsOutOfPlay = ps.isDead && !ps.isWaitingToRespawn;
                boolean playerIsRespawning = ps.isWaitingToRespawn;

                if (playerIsOutOfPlay || playerIsRespawning) {
                    continue; // Peluru menembus jika pemain mati atau sedang respawn
                }

                if (player.isInvincible()) {
                    bullet.kill(); // Asumsi BossBullet punya metode kill()
                } else {
                    bullet.kill();
                    player.takeDeath();
                }
                break; // Hentikan pemeriksaan peluru lain dari boss untuk frame ini jika satu sudah kena
            }
        }
    }

    public void setupRespawnPoints(GameScreen screen) {
        screen.getRespawnPoints().add(new Vector2(100, Constant.TERRAIN_HEIGHT));
        screen.getRespawnPoints().add(new Vector2(800, Constant.TERRAIN_HEIGHT));
        screen.getRespawnPoints().add(new Vector2(1600, Constant.TERRAIN_HEIGHT));
        screen.getRespawnPoints().add(new Vector2(2400, Constant.TERRAIN_HEIGHT));
        screen.getRespawnPoints().add(new Vector2(3200, Constant.TERRAIN_HEIGHT));
    }
}
