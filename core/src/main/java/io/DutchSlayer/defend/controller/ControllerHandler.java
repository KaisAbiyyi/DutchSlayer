package io.DutchSlayer.defend.controller;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.Controllers;

public class ControllerHandler extends ControllerAdapter {

    private static ControllerHandler instance;
    private Controller currentController;

    private float moveAxis = 0;
    private float axisX = 0;
    private float axisY = 0;

    private boolean jumpPressed = false;
    private boolean firePressed = false;
    private boolean firePressedOnce = false;

    private boolean grenadePressed = false;
    private boolean grenadePressedOnce = false;

    private boolean dashPressed = false;
    private boolean pausePressed = false;

    private ControllerHandler() {
        if (Controllers.getControllers().size > 0) {
            currentController = Controllers.getControllers().first();
            currentController.addListener(this);
        }
    }

    public static ControllerHandler getInstance() {
        if (instance == null) instance = new ControllerHandler();
        return instance;
    }

    // === Public Getters ===
    public float getMoveAxis() {
        return moveAxis;
    }

    public float getAnalogX() {
        return axisX;
    }

    public float getAnalogY() {
        return axisY;
    }

    public boolean isAimingUp() {
        return axisY > 0.5f;
    }

    public boolean isJumpPressed() {
        return jumpPressed;
    }

    public boolean isDashPressed() {
        return dashPressed;
    }

    public boolean isPausePressed() {
        return pausePressed;
    }

    public boolean isFirePressed() {
        return firePressed;
    }

    public boolean isGrenadePressed() {
        return grenadePressed;
    }

    // === 1x Trigger: consume after read ===
    public boolean consumeFirePressedOnce() {
        if (firePressedOnce) {
            firePressedOnce = false;
            return true;
        }
        return false;
    }

    public boolean consumeGrenadePressedOnce() {
        if (grenadePressedOnce) {
            grenadePressedOnce = false;
            return true;
        }
        return false;
    }

    // === Input Handling ===

    @Override
    public boolean axisMoved(Controller controller, int axisIndex, float value) {
        switch (axisIndex) {
            case 0: // Analog horizontal
                moveAxis = Math.abs(value) > 0.2f ? value : 0;
                axisX = value;
                break;

            case 1: // Analog vertical
                axisY = -value; // flip Y
                break;
        }
        return true;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        switch (buttonCode) {
            case 0:
                jumpPressed = true;
                break;       // A
            case 1:
                dashPressed = true;
                break;       // B
            case 2: // X → Fire
                firePressed = true;
                firePressedOnce = true;
                break;
            case 3: // Y → Grenade
                grenadePressed = true;
                grenadePressedOnce = true;
                break;
            case 7:
                pausePressed = true;
                break;      // Start
        }
        return true;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        switch (buttonCode) {
            case 0:
                jumpPressed = false;
                break;
            case 1:
                dashPressed = false;
                break;
            case 2:
                firePressed = false;
                break;
            case 3:
                grenadePressed = false;
                break;
            case 7:
                pausePressed = false;
                break;
        }
        return true;
    }
}
