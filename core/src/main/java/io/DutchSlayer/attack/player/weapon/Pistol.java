package io.DutchSlayer.attack.player.weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import io.DutchSlayer.attack.player.Player;

public class Pistol implements Weapon {
    private final Sound fireSound; // <-- Deklarasi Sound

    public Pistol() { // <-- Konstruktor untuk memuat suara
        // Muat suara pistol sekali saat objek Pistol dibuat
        fireSound = Gdx.audio.newSound(Gdx.files.internal("player/pistol.mp3"));
    }

    @Override
    public void fire(Player player) {
        fireSound.play(0.7f); // Mainkan suara pistol dengan volume 0.7 (opsional)

        // Logika penembakan pistol Anda yang sudah ada
        float offsetX = -20f;
        float bulletX = player.isFacingRight()
            ? player.getX() + player.getWidth() - offsetX
            : player.getX() + offsetX;

        float fireY = player.getFireY();
        if (player.isDucking()) {
            fireY += 10f;
        }
        float angle = player.getFireAngle();
        // Sebaiknya Texture juga dimuat sekali dan di-reuse, misalnya via AssetLoader
        Texture playerBulletTexture = new Texture(Gdx.files.internal("player/bullet.png"));

        Bullet bullet = new Bullet(bulletX, fireY, angle, false);
        bullet.setTexture(playerBulletTexture); // Sebaiknya setTextureRegion jika Texture dimuat global
        player.getBullets().add(bullet);
    }


    @Override
    public boolean isOutOfAmmo() {
        return false;
    }

    @Override
    public String getName() {
        return "Pistol";
    }
}

