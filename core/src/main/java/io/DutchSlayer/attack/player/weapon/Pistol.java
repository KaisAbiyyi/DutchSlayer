package io.DutchSlayer.attack.player.weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import io.DutchSlayer.attack.player.Player;

public class Pistol implements Weapon {
    private final Sound fireSound;

    public Pistol() {
        fireSound = Gdx.audio.newSound(Gdx.files.internal("player/pistol.mp3"));
    }

    @Override
    public void fire(Player player) {
        fireSound.play(0.7f);

        float offsetX = -20f;
        float bulletX = player.isFacingRight()
            ? player.getX() + player.getWidth() - offsetX
            : player.getX() + offsetX;

        float fireY = player.getFireY();
        if (player.isDucking()) {
            fireY += 10f;
        }
        float angle = player.getFireAngle();
        Texture playerBulletTexture = new Texture(Gdx.files.internal("player/bullet.png"));

        Bullet bullet = new Bullet(bulletX, fireY, angle, false);
        bullet.setTexture(playerBulletTexture);
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

