package io.DutchSlayer.attack.player.weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import io.DutchSlayer.attack.player.Player;

public class AssaultRifle implements Weapon {

    private int ammo;
    private boolean firedThisPress = false;
    private float burstTimer = 0f;
    private int burstIndex = 0;
    private static final float BURST_DELAY = 0.08f;

    private final Sound fireSound;

    public AssaultRifle() {
        this.ammo = 60;
        this.fireSound = Gdx.audio.newSound(Gdx.files.internal("player/pistol.mp3"));
    }


    public int getAmmo() {
        return ammo;
    }

    @Override
    public void addAmmo(int amount) {
        ammo += amount; // max ammo opsional
    }


    @Override
    public void fire(Player player) {
        if (ammo < 3 || firedThisPress) return;

        firedThisPress = true;
        burstTimer = 0f;
        burstIndex = 0;
    }

    public void updateBurst(Player player, float delta) {
        if (!firedThisPress || burstIndex >= 3 || ammo < 3) return;

        burstTimer += delta;
        if (burstTimer >= BURST_DELAY) {
            burstTimer -= BURST_DELAY;

            fireSound.play(0.6f);

            float centerX = player.getX() + player.getWidth() + 10f;
            float fireY = player.getFireY();
            if (!player.isDucking()) {
                fireY -= 10f;
            }
            float angle = player.getFireAngle();

            Texture playerBulletTexture = new Texture(Gdx.files.internal("player/bullet.png"));

            Bullet bullet = new Bullet(centerX, fireY, angle, false);
            bullet.setTexture(playerBulletTexture);
            player.getBullets().add(bullet);
            burstIndex++;

            if (burstIndex == 3) {
                ammo -= 3;
                resetFireFlag();
            }
        }
    }


    @Override
    public boolean isOutOfAmmo() {
        return ammo < 3;
    }

    @Override
    public String getName() {
        return "Assault Rifle";
    }

    @Override
    public void resetFireFlag() {
        firedThisPress = false;
        burstIndex = 0;
        burstTimer = 0f;
    }
}
