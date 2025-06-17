package io.DutchSlayer.attack.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import io.DutchSlayer.attack.player.Player;
import io.DutchSlayer.attack.player.weapon.AssaultRifle;
import io.DutchSlayer.attack.player.weapon.Weapon;
import io.DutchSlayer.utils.Constant;

/**
 * Representasi item yang bisa diambil oleh Player.
 * Bisa berupa senjata baru atau tambahan amunisi granat.
 */
public class PickupItem {

    private final float x, y;
    private final float width = 50f;
    private final float height = 50f;
    private float velocityY = 150f; // ke atas dulu
    private float gravity = -500f;  // gravitasi turun
    private float offsetY = 0f;     // offset Y dari posisi dasar
    private boolean falling = true;
    private final float groundY;

    private final PickupType type;
    private boolean collected = false;
    private final GlyphLayout layout = new GlyphLayout();

    public PickupItem(float x, float y, PickupType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.groundY = Constant.TERRAIN_HEIGHT;
    }

    public void update(Player player, float delta) {
        if (!collected && falling) {
            velocityY += gravity * delta;
            offsetY += velocityY * delta;

            float currentY = y + offsetY;
            if (currentY <= groundY) {
                offsetY = groundY - y;
                falling = false;
            }
        }


        if (collected) return;

        if (overlaps(player)) {
            applyEffect(player);
            collected = true;
        }
    }


    private boolean overlaps(Player player) {
        Rectangle playerRect = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        Rectangle itemRect = new Rectangle(x, y + offsetY, width, height); // ✅ gunakan posisi aktual saat jatuh
        return playerRect.overlaps(itemRect);
    }


    private void applyEffect(Player player) {
        Weapon current = player.getCurrentWeapon();

        switch (type) {
            case ASSAULT -> {
                if (current instanceof AssaultRifle rifle) {
                    rifle.addAmmo(30);
                    System.out.println("[PICKUP] +30 AR Ammo. Total: " + rifle.getAmmo());
                } else {
                    AssaultRifle newAR = new AssaultRifle();
                    player.setWeapon(newAR);
                    System.out.println("[PICKUP] Equipped: Assault Rifle");
                }
            }
            case GRENADE -> {
                player.pickupGrenade(2);
                System.out.println("[PICKUP] +2 Grenades. Total: " + player.getGrenadeAmmo());
            }
        }
    }

    public boolean isAlive() {
        return !collected;
    }

    public void renderShape(ShapeRenderer sr) {
        if (collected) return;

        float drawX = x;
        float drawY = y + offsetY;
        float borderSize = 2f;
        float radius = 6f;

        // Border hitam
        sr.setColor(Color.BLACK);
        sr.rect(drawX - borderSize, drawY - borderSize, width + borderSize * 2, height + borderSize * 2);

        // Isi kotak
        sr.setColor(getColor());
        sr.rect(drawX, drawY, width, height);
    }

    public void renderLabel(SpriteBatch batch, BitmapFont font) {
        if (collected) return;

        font.getData().setScale(1.8f);
        font.setColor(Color.BLACK);

        String label = switch (type) {
            case ASSAULT -> "AR";
            case GRENADE -> "G";
        };

        // Hitung lebar & tinggi teks aktual
        layout.setText(font, label);
        float textWidth = layout.width;
        float textHeight = layout.height;

        // Pusatkan di dalam kotak
        float centerX = x + width / 2f;
        float centerY = y + offsetY + height / 2f;

        float textX = centerX - textWidth / 2f;
        float textY = centerY + textHeight / 2f;

        // Efek bold dengan menggambar 4 arah
        font.draw(batch, label, textX - 1, textY); // kiri
        font.draw(batch, label, textX + 1, textY); // kanan
        font.draw(batch, label, textX, textY - 1); // bawah
        font.draw(batch, label, textX, textY + 1); // atas

        // Gambar utama (di tengah)
        font.setColor(Color.BLACK);
        font.draw(batch, label, textX, textY);
    }


    private Color getColor() {
        return switch (type) {
            case ASSAULT -> new Color(0.2f, 0.7f, 1f, 1);
            case GRENADE -> new Color(0.4f, 1f, 0.4f, 1);
        };
    }

    public boolean isCollected() {
        return collected;
    }
}
