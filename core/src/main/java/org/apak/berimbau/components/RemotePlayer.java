package org.apak.berimbau.components;

import com.badlogic.gdx.math.Vector3;

public class RemotePlayer {
    public final String clientKey; // Format: "IP:PORT"
    public final Vector3 position = new Vector3();
    public final Vector3 moveDirection = new Vector3();
    public boolean isWalking, isShuffling, isAttacking, isBlocking, isAirborne, isStanceSwitching;

    public RemotePlayer(String clientKey) {
        this.clientKey = clientKey;
    }
}
