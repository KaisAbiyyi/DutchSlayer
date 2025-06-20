package io.DutchSlayer.attack.player.weapon;

import io.DutchSlayer.attack.player.Player;

/**
 * Interface dasar untuk semua jenis senjata yang bisa digunakan oleh Player.
 * Setiap senjata memiliki cara menembak, nama, dan sistem peluru (jika ada).
 */
public interface Weapon {

    /**
     * Melakukan aksi menembak dari senjata ini.
     * Implementasi tergantung jenis senjata (burst, spread, dll).
     *
     * @param player Referensi ke Player agar bisa mengakses posisi, arah, dan menambahkan peluru.
     */
    void fire(Player player);

    /**
     * Dipanggil setiap frame untuk senjata yang memerlukan update, seperti burst.
     * Default tidak melakukan apa-apa.
     *
     * @param player Referensi Player
     * @param delta  Waktu antar frame
     */
    default void updateBurst(Player player, float delta) {
    }

    /**
     * Dipanggil ketika tombol tembak dilepas. Digunakan untuk reset state internal.
     * Default tidak melakukan apa-apa.
     */
    default void resetFireFlag() {
    }

    /**
     * Mengecek apakah senjata ini sudah tidak memiliki peluru.
     * Jika ya, biasanya Player akan otomatis beralih ke senjata default (misalnya Pistol).
     *
     * @return true jika kehabisan peluru, false jika masih bisa digunakan.
     */
    boolean isOutOfAmmo();

    /**
     * Mengembalikan nama senjata, digunakan untuk HUD / UI.
     *
     * @return nama senjata.
     */
    String getName();

    default void addAmmo(int amount) {
    }


}
