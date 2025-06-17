package io.DutchSlayer.utils;

public class Timer {
    private float time = 0f;

    public void update(float delta) {
        time += delta;
    }

    public boolean elapsed(float seconds) {
        return time >= seconds;
    }

    public void reset() {
        time = 0f;
    }
}
