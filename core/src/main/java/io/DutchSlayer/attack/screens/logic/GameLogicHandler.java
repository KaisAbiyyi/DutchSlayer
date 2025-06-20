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
import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.attack.screens.ui.VNManager;
import io.DutchSlayer.utils.Constant;

public class GameLogicHandler {
    private float pickupSpawnTimer;

    public GameLogicHandler() { // <--- Tambahkan konstruktor ini
        this.pickupSpawnTimer = 0f;
    }

    public void update(GameScreen screen, float delta) {
        if (screen.getVnManager().isActive()) {
            return;
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

        if (screen.isWallsAreRising()) {
            float deltaY = screen.getWallRiseSpeed() * delta;
            screen.getLeftWall().y += deltaY;
            screen.getRightWall().y += deltaY;

            if (screen.getLeftWall().y >= screen.getWallTargetY()) {
                screen.getLeftWall().y = screen.getWallTargetY();
                screen.getRightWall().y = screen.getWallTargetY();
                screen.setWallsAreRising(false);

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
        if (!screen.getVnManager().isActive() && boss != null && boss.isAlive()) {
            boss.update(delta);

            if (boss.getCurrentState() == TankBossState.IDLE) {
                pickupSpawnTimer += delta;
                float PICKUP_SPAWN_INTERVAL = 2.0f;
                if (pickupSpawnTimer >= PICKUP_SPAWN_INTERVAL) {
                    spawnRandomBossPickup(screen);
                    pickupSpawnTimer = 0;
                }
            }
        }

        updateCamera(screen);

        if (screen.isTriggerWallTrap()) {
            Rectangle playerBounds = screen.getPlayer().getBounds();
            Rectangle leftWallBounds = screen.getLeftWall();
            Rectangle rightWallBounds = screen.getRightWall();

            if (playerBounds.x < leftWallBounds.x + leftWallBounds.width) {
                screen.getPlayer().setX(leftWallBounds.x + leftWallBounds.width);
            }
            if (playerBounds.x + playerBounds.width > rightWallBounds.x) {
                screen.getPlayer().setX(rightWallBounds.x - playerBounds.width);
            }
        }
    }

    private void spawnRandomBossPickup(GameScreen screen) {
        float spawnX = screen.getCamera().position.x;
        float spawnY = Constant.TERRAIN_HEIGHT + 100f;

        PickupType type;
        float randomValue = MathUtils.random();

        if (randomValue < 0.5f) {
            type = PickupType.GRENADE;
        } else {
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

        if (screen.isTriggerWallTrap()) {
            camX = screen.getCameraTriggerX();
        } else {
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
                enemy.setDropChecked(true);

                if (MathUtils.randomBoolean(0.3f)) {
                    PickupType type = switch (screen.getStageNumber()) {
                        case 1, 2 -> MathUtils.randomBoolean() ? PickupType.ASSAULT : PickupType.GRENADE;
                        default -> switch (MathUtils.random(2)) {
                            case 0 -> PickupType.ASSAULT;
                            default -> PickupType.GRENADE;
                        };
                    };
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

        if (allBasicEnemiesDefeated && !screen.isTriggerWallTrap()) {
            screen.setTriggerWallTrap(true);
            screen.setWallsAreRising(true);

            screen.setCameraTriggerX(screen.getPlayer().getX() + screen.getPlayer().getWidth() / 2f);

            float wallWidth = screen.getLeftWall().width;
            float screenHalfWidth = Constant.SCREEN_WIDTH / 2f;

            screen.getLeftWall().x = screen.getCameraTriggerX() - screenHalfWidth;
            screen.getRightWall().x = screen.getCameraTriggerX() + screenHalfWidth - wallWidth;
        }
    }

    private void updateGrenades(GameScreen screen, float delta) {
        for (Grenade grenade : screen.getGrenades()) {
            grenade.update(delta);

            if (!grenade.isExploded()) {
                Rectangle grenadeRect = new Rectangle(grenade.getX() - 6f, grenade.getY() - 6f, 12f, 12f);

                for (BasicEnemy enemy : screen.getEnemies()) {
                    if (enemy.isAlive()) {
                        if (enemy.getCurrentState() == EnemyState.DYING) {
                            continue;
                        }
                        Rectangle enemyRect = new Rectangle(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
                        if (grenadeRect.overlaps(enemyRect)) {
                            if (grenade.isEnemyGrenade()) {
                                continue;
                            }

                            float impactPointY = enemy.getY() + enemy.getHeight() / 2f;
                            grenade.forceExplode(impactPointY);
                            break;
                        }
                    }
                }

                if (grenade.isExploded()) {

                } else {
                    TankBoss boss = screen.getTankBoss();
                    if (boss != null && boss.isAlive()) {
                        Rectangle bossRect = new Rectangle(boss.getPosition().x, boss.getPosition().y, boss.getWidth(), boss.getHeight());
                        if (grenadeRect.overlaps(bossRect)) {
                            if (grenade.isEnemyGrenade()) {
                                continue;
                            }

                            float impactPointY = boss.getPosition().y + boss.getHeight() / 2f;
                            grenade.forceExplode(impactPointY);
                        }
                    }
                }
            }

            if (grenade.shouldDealDamage()) {
                float explosionX = grenade.getX();
                float explosionY = grenade.getY();
                float explosionRadius = grenade.getRadius();
                int explosionDamage = grenade.getDamage();

                if (!grenade.isEnemyGrenade()) {
                    for (BasicEnemy enemy : screen.getEnemies()) {
                        if (enemy.isAlive()) {
                            enemy.checkHitByExplosion(explosionX, explosionY, explosionRadius, explosionDamage);
                        }
                    }
                }

                TankBoss boss = screen.getTankBoss();
                if (boss != null && boss.isAlive()) {
                    Rectangle bossRect = new Rectangle(boss.getPosition().x, boss.getPosition().y, boss.getWidth(), boss.getHeight());

                    float closestX = Math.max(bossRect.x, Math.min(explosionX, bossRect.x + bossRect.width));
                    float closestY = Math.max(bossRect.y, Math.min(explosionY, bossRect.y + bossRect.height));

                    float distanceX = explosionX - closestX;
                    float distanceY = explosionY - closestY;
                    float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);

                    if (distanceSquared < (explosionRadius * explosionRadius)) {
                        boss.takeHit(explosionDamage);
                    }
                }

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
                    if (enemy.getCurrentState() == EnemyState.DYING) {
                        continue;
                    }

                    bullet.kill();
                    enemy.takeHit();
                    break;
                }
            }
        }
    }

    private void checkEnemyBulletHitsPlayer(GameScreen screen) {
        Player player = screen.getPlayer();
        PlayerState ps = player.getPlayerState();

        for (BasicEnemy enemy : screen.getEnemies()) {
            if (!enemy.isAlive()) continue;

            for (Bullet bullet : enemy.getBullets()) {
                if (!bullet.isAlive()) continue;

                boolean physicalOverlap = bullet.getX() < ps.x + ps.playerWidth &&
                    bullet.getX() + bullet.getWidth() > ps.x &&
                    bullet.getY() < ps.y + ps.playerHeight &&
                    bullet.getY() + bullet.getHeight() > ps.y;

                if (physicalOverlap) {
                    boolean playerIsOutOfPlay = ps.isDead && !ps.isWaitingToRespawn;
                    boolean playerIsRespawning = ps.isWaitingToRespawn;

                    if (playerIsOutOfPlay || playerIsRespawning) {
                        continue;
                    }

                    if (player.isInvincible()) {
                        bullet.kill();
                    } else {
                        bullet.kill();
                        player.takeDeath();
                    }

                    break;
                }
            }
        }

        TankBoss boss = screen.getTankBoss();
        if (boss != null && boss.isAlive()) {
            for (Bullet bullet : screen.getPlayer().getBullets()) {
                if (!bullet.isAlive()) continue;

                boolean overlap = bullet.getX() < boss.getPosition().x + boss.getWidth() &&
                    bullet.getX() + bullet.getWidth() > boss.getPosition().x &&
                    bullet.getY() < boss.getPosition().y + boss.getHeight() &&
                    bullet.getY() + bullet.getHeight() > boss.getPosition().y;

                if (overlap) {
                    bullet.kill();
                    float playerBulletDamage = 1f;

                    boss.takeHit(playerBulletDamage);
                    break;
                }
            }
        }
    }

    private void checkBossBulletHitsPlayer(GameScreen screen) {
        TankBoss boss = screen.getTankBoss();
        if (boss == null || !boss.isAlive() || boss.getBullets().isEmpty()) {
            return;
        }

        Player player = screen.getPlayer();
        PlayerState ps = player.getPlayerState();

        for (io.DutchSlayer.attack.boss.BossBullet bullet : boss.getBullets()) {
            if (!bullet.isAlive()) continue;

            float bulletWidth = 10f;
            float bulletHeight = 10f;

            boolean physicalOverlap = bullet.getX() < ps.x + ps.playerWidth &&
                bullet.getX() + bulletWidth > ps.x &&
                bullet.getY() < ps.y + ps.playerHeight &&
                bullet.getY() + bulletHeight > ps.y;

            if (physicalOverlap) {
                boolean playerIsOutOfPlay = ps.isDead && !ps.isWaitingToRespawn;
                boolean playerIsRespawning = ps.isWaitingToRespawn;

                if (playerIsOutOfPlay || playerIsRespawning) {
                    continue;
                }

                if (player.isInvincible()) {
                    bullet.kill();
                } else {
                    bullet.kill();
                    player.takeDeath();
                }
                break;
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
