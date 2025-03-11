package org.apak.berimbau.components;

import com.badlogic.gdx.math.Vector3;

public interface InputHandler {
    Vector3 getMoveDirection();
    boolean isWalking();
    boolean isShuffling();
    boolean isAttacking();
    boolean isBlocking();
    boolean isAirborne(); // Detects if player is in the air
    boolean isStanceSwitching(); // Detects if `Q` is pressed
}