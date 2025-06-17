package io.DutchSlayer.attack.player.weapon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import io.DutchSlayer.attack.player.Player;
import io.DutchSlayer.utils.Constant;

/**
 * Assault Rifle: menembakkan 3 peluru burst secara berurutan dalam arah yang sama.
 * Total peluru 60 per pickup, habis = fallback ke pistol.
 */
public class AssaultRifle implements Weapon {

    private int ammo;
    private boolean firedThisPress = false;
    private float burstTimer = 0f;
    private int burstIndex = 0;
    private static final float BURST_DELAY = 0.08f; // jeda antar peluru burst

    private final Sound fireSound; // <-- Deklarasi Sound

    public AssaultRifle() {
        this.ammo = 60;
        // Muat suara AR sekali saat objek AssaultRifle dibuat
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
        // Jangan mainkan suara di sini, karena fire() hanya mempersiapkan burst.
        // Suara akan dimainkan di updateBurst() untuk setiap peluru.
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

            fireSound.play(0.6f); // Mainkan suara AR untuk setiap peluru dalam burst (volume 0.6 opsional)

            // Logika penembakan AR Anda yang sudah ada
            float centerX = player.getX() + player.getWidth() + 10f;
            float fireY = player.getFireY();
            if (!player.isDucking()) {
                fireY -= 10f;
            }
            float angle = player.getFireAngle();

            // Sebaiknya Texture juga dimuat sekali dan di-reuse
            Texture playerBulletTexture = new Texture(Gdx.files.internal("player/bullet.png"));

            Bullet bullet = new Bullet(centerX, fireY, angle, false);
            bullet.setTexture(playerBulletTexture); // Sebaiknya setTextureRegion
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
