package io.DutchSlayer.defend;

public enum EnemyType {
    BASIC,      // Tentara dengan kapak - menyerang dengan mendekat
    SHOOTER,    // Enemy dengan senjata - menembak dari jarak jauh
    BOMBER,     // Enemy bomb - menaruh bomb lalu kabur
    SHIELD,     // Enemy shield - HP tinggi, tidak menyerang, melindungi basic
    BOSS        // Enemy boss - HP tinggi, damage tinggi, tetap di pojok kanan
}
