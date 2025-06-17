package io.DutchSlayer.attack.screens.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.DutchSlayer.attack.boss.TankBoss;
import io.DutchSlayer.attack.enemy.BasicEnemy;
import io.DutchSlayer.attack.objects.Building;
import io.DutchSlayer.attack.objects.PickupItem;
import io.DutchSlayer.attack.objects.Tree;
import io.DutchSlayer.attack.player.Player;
import io.DutchSlayer.attack.player.weapon.Bullet;
import io.DutchSlayer.attack.screens.GameScreen;
import io.DutchSlayer.utils.Constant;

public class GameRenderer {

    public void render(GameScreen screen, float delta) {
        OrthographicCamera camera = screen.getCamera();
        SpriteBatch spriteBatch = screen.getSpriteBatch();
        ShapeRenderer shapeRenderer = screen.getShapeRenderer();

        // Ambil semua texture yang dibutuhkan
        Texture backgroundTexture = screen.getBackgroundTexture();
        Texture bgTreeTexture = screen.getBgTreeTexture();
        Texture bgMountainTexture = screen.getBgMountainTexture();
        Texture terrainTexture = screen.getTerrainTexture();
        Texture terrain2Texture = screen.getTerrain2Texture();
        Texture wallTexture = screen.getWallTexture();
        BitmapFont font = screen.getFont();

        // =======================================================================
        // FASE 1: RENDERING DENGAN SpriteBatch (sebelum Boss dan Shape)
        // =======================================================================
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        // 1. Background
        spriteBatch.draw(backgroundTexture, camera.position.x - Constant.SCREEN_WIDTH / 2f, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);

        // 2. Parallax Layers
        drawParallaxLayer(spriteBatch, camera, bgMountainTexture, 0.1f, Constant.TERRAIN_HEIGHT + 100f, 0.5f * 4);
        drawParallaxLayer(spriteBatch, camera, bgTreeTexture, 0.2f, Constant.TERRAIN_HEIGHT + 200f, 0.175f * 4);
        drawParallaxLayer(spriteBatch, camera, terrain2Texture, 0.2f, Constant.TERRAIN_HEIGHT + 50f, 0.27f * 4);

        // 3. Background Objects (Pohon & Bangunan)
        float worldViewLeft = camera.position.x - Constant.SCREEN_WIDTH / 2f;
        for (Building b : screen.getBuildings()) {
            float offsetX = (1f - 0.8f) * (b.getX() - worldViewLeft);
            b.render(spriteBatch, offsetX);
        }
        for (Tree t : screen.getTrees()) {
            t.render(spriteBatch, 0);
        }

        // 4. Terrain
        float scaledWidth = 550f;
        float scaledHeight = Constant.TERRAIN_HEIGHT + 225f;
        for (float x = 0; x < screen.getMapWidth(); x += scaledWidth) {
            spriteBatch.draw(terrainTexture, x, -50f, scaledWidth, scaledHeight);
        }

        // 5. Player
        screen.getPlayer().render(spriteBatch, delta);

        // 6. Enemies
        for (BasicEnemy enemy : screen.getEnemies()) {
            enemy.render(spriteBatch, delta);
        }

        // 7. Player Grenades
        for (io.DutchSlayer.attack.player.weapon.Grenade grenade : screen.getGrenades()) {
            grenade.render(spriteBatch);
        }

        // 8. Wall Trap
        if (screen.isTriggerWallTrap()) {
            spriteBatch.draw(wallTexture, screen.getLeftWall().x, screen.getLeftWall().y, screen.getLeftWall().width, screen.getLeftWall().height);
            spriteBatch.draw(wallTexture, screen.getRightWall().x, screen.getRightWall().y, screen.getRightWall().width, screen.getRightWall().height);
        }

        spriteBatch.end(); // <-- Akhiri batch utama SEBELUM memanggil render boss/shape

        // =======================================================================
        // FASE 2: RENDERING DENGAN ShapeRenderer DAN RENDER BOSS
        // =======================================================================
        TankBoss boss = screen.getTankBoss();
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Mulai ShapeRenderer untuk debug musuh biasa, pickup, dan boss
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 1. Render debug untuk musuh biasa dan pickup
        for (BasicEnemy e : screen.getEnemies()) {
            e.render(shapeRenderer);
        }
        for (PickupItem p : screen.getPickupItems()) {
            p.renderShape(shapeRenderer);
        }

        // 2. Panggil metode render gabungan milik boss.
        // Metode ini mengharapkan ShapeRenderer sudah aktif.
        if (boss != null) {
            boss.render(shapeRenderer, spriteBatch); // <-- PANGGIL METODE ASLI DI SINI
        }

        shapeRenderer.end(); // Akhiri ShapeRenderer setelah semua shape digambar

        // Gambar Health Bar Boss (metode ini sudah mandiri dan aman dipanggil di sini)
        drawBossHealthBar(screen, boss);

        // =======================================================================
        // FASE 3: RENDERING DENGAN SpriteBatch (setelah Boss dan Shape)
        // =======================================================================
        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin(); // <-- Mulai batch baru untuk UI dan proyektil

        // 1. Enemy Bullets
        for (BasicEnemy enemy : screen.getEnemies()) {
            for (Bullet bullet : enemy.getBullets()) {
                if (bullet.isAlive()) bullet.render(spriteBatch);
            }
        }

        // 2. HUD dan label pickup
        drawHUD(screen, spriteBatch, font);
        for (PickupItem item : screen.getPickupItems()) {
            item.renderLabel(spriteBatch, font);
        }

        spriteBatch.end();
    }

    // Helper method untuk parallax yang lebih efisien
    private void drawParallaxLayer(SpriteBatch batch, OrthographicCamera camera, Texture texture, float parallaxFactor, float yPosition, float scale) {
        float texWidth = texture.getWidth() * scale;
        float texHeight = texture.getHeight() * scale;
        float offsetX = (camera.position.x * parallaxFactor) % texWidth;
        float startX = camera.position.x - Constant.SCREEN_WIDTH / 2f - offsetX - texWidth;

        for (float x = startX; x < camera.position.x + Constant.SCREEN_WIDTH / 2f + texWidth; x += texWidth) {
            batch.draw(texture, x, yPosition, texWidth, texHeight);
        }
    }

    // Kode di bawah ini tidak perlu diubah
    private void drawBossHealthBar(GameScreen screen, TankBoss boss) {
        if (boss == null || !boss.isAlive()) {
            return;
        }
        OrthographicCamera camera = screen.getCamera();
        ShapeRenderer shapeRenderer = screen.getShapeRenderer();
        SpriteBatch spriteBatch = screen.getSpriteBatch();
        BitmapFont font = screen.getFont();
        float barWidth = Constant.SCREEN_WIDTH * 0.7f;
        float barHeight = 30f;
        float barX = camera.position.x - barWidth / 2f;
        float barY = 20f;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        float healthRatio = boss.getHealthRatio();
        float currentHealthWidth = barWidth * healthRatio;
        if (healthRatio > 0.5f) {
            shapeRenderer.setColor(0.1f, 0.8f, 0.1f, 1f);
        } else if (healthRatio > 0.2f) {
            shapeRenderer.setColor(0.8f, 0.8f, 0.1f, 1f);
        } else {
            shapeRenderer.setColor(0.8f, 0.1f, 0.1f, 1f);
        }
        shapeRenderer.rect(barX, barY, currentHealthWidth, barHeight);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        String bossName = boss.getName();
        font.setColor(1f, 1f, 1f, 1f);
        float originalScaleX = font.getScaleX();
        float originalScaleY = font.getScaleY();
        font.getData().setScale(1.0f);
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(font, bossName);
        float textWidth = layout.width;
        float textHeight = layout.height;
        float textX = barX + (barWidth - textWidth) / 2f;
        float textY = barY + (barHeight + textHeight) / 2f - 2f;
        font.draw(spriteBatch, bossName, textX, textY);
        font.getData().setScale(originalScaleX, originalScaleY);
        spriteBatch.end();
    }


    private void drawParallax(SpriteBatch batch, OrthographicCamera camera, Texture texture,
                              float parallaxFactor, float yPosition, float scale) {
        float texWidth = texture.getWidth() * scale;
        float texHeight = texture.getHeight() * scale;
        float offsetX = (camera.position.x * parallaxFactor) % texWidth;
        float startX = camera.position.x - Constant.SCREEN_WIDTH / 2f - offsetX - texWidth;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (float x = startX; x < camera.position.x + Constant.SCREEN_WIDTH / 2f + texWidth; x += texWidth) {
            batch.draw(texture, x, yPosition, texWidth, texHeight);
        }
        batch.end();
    }

    private void drawTerrain(SpriteBatch batch, OrthographicCamera camera, Texture terrainTexture, float mapWidth) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float scaledWidth = 550f;
        float scaledHeight = Constant.TERRAIN_HEIGHT + 225f;

        for (float x = 0; x < mapWidth; x += scaledWidth) {
            batch.draw(terrainTexture, x, -50f, scaledWidth, scaledHeight);
        }

        batch.end();
    }

    private void drawHUD(GameScreen screen, SpriteBatch batch, BitmapFont font) {
        Player player = screen.getPlayer();
        float baseX = screen.getCamera().position.x - Constant.SCREEN_WIDTH / 2f;

        font.getData().setScale(1.5f);
        font.setColor(1, 1, 1, 1);

        font.draw(batch, "Weapon: " + player.getCurrentWeaponName(), baseX + 20, Constant.SCREEN_HEIGHT - 140);
        if (!player.getCurrentWeaponName().equalsIgnoreCase("Pistol")) {
            font.draw(batch, "Ammo: " + player.getCurrentAmmo(), baseX + 20, Constant.SCREEN_HEIGHT - 170);
        }
        font.draw(batch, "Grenades: " + player.getGrenadeAmmo(), baseX + 20, Constant.SCREEN_HEIGHT - 200);
        font.draw(batch, "Lives: " + player.getLives(), baseX + 20, Constant.SCREEN_HEIGHT - 20);

        float duckCooldown = player.getDuckCooldownRemaining();
        float duckTime = player.getDuckTimeRemaining();
        String duckStatus = duckCooldown > 0
            ? "Duck Cooldown: " + String.format("%.1f", duckCooldown) + "s"
            : "Duck Time Left: " + String.format("%.1f", duckTime) + "s";
        font.draw(batch, duckStatus, baseX + 20, Constant.SCREEN_HEIGHT - 50);

        font.draw(batch, "Dash: " + player.getDashCount(), baseX + 20, Constant.SCREEN_HEIGHT - 80);
        if (player.getDashCooldownRemaining() > 0f) {
            font.draw(batch, "Dash Cooldown: " + String.format("%.1f", player.getDashCooldownRemaining()) + "s",
                baseX + 20, Constant.SCREEN_HEIGHT - 110);
        }

        for (PickupItem item : screen.getPickupItems()) {
            item.renderLabel(batch, font);
        }
    }
}
