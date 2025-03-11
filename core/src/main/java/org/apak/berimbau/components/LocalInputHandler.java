package org.apak.berimbau.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;

public class LocalInputHandler implements InputHandler {
    private boolean isShuffling = false;
    private long lastMovePressTime = 0;
    private int lastMoveKey = -1;

    private static final long SHUFFLE_THRESHOLD = 250; // Milliseconds for double-tap detection

    @Override
    public Vector3 getMoveDirection() {
        float x = (Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0);
        float z = (Gdx.input.isKeyPressed(Input.Keys.W) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.S) ? 1 : 0);

        if (x == 0 && z == 0) return new Vector3(0, 0, 0); // No movement

        detectShuffle(); // Detect shuffle input pattern

        return new Vector3(x, 0, z).nor(); // Normalize movement vector
    }

    @Override
    public boolean isWalking() {
        return Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT); // Walking slows movement
    }

    @Override
    public boolean isShuffling() {
        return isShuffling; // Shuffle state is updated separately
    }

    @Override
    public boolean isAttacking() {
        return Gdx.input.isButtonPressed(Input.Buttons.LEFT); // Left mouse button
    }

    @Override
    public boolean isBlocking() {
        return Gdx.input.isButtonPressed(Input.Buttons.RIGHT); // Right mouse button
    }

    public boolean isStanceSwitching() {
        return Gdx.input.isKeyJustPressed(Input.Keys.Q);
    }

    private void detectShuffle() {
        int currentMoveKey = -1;
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) currentMoveKey = Input.Keys.W;
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) currentMoveKey = Input.Keys.A;
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) currentMoveKey = Input.Keys.S;
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) currentMoveKey = Input.Keys.D;

        if (currentMoveKey != -1) {
            long currentTime = System.currentTimeMillis();
            if (currentMoveKey == lastMoveKey && (currentTime - lastMovePressTime) < SHUFFLE_THRESHOLD) {
                isShuffling = true; // Detected a double-tap shuffle
            } else {
                isShuffling = false;
            }
            lastMoveKey = currentMoveKey;
            lastMovePressTime = currentTime;
        }
    }

    @Override
    public boolean isAirborne() {
        return Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
    }
}
