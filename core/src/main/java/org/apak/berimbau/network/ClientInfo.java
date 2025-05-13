package org.apak.berimbau.network;

import com.badlogic.gdx.math.Vector3;
import org.apak.berimbau.components.AttackStance;
import org.apak.berimbau.components.StateMachine;

import java.net.InetAddress;

public class ClientInfo {
    public final InetAddress address;
    public final int port;
    public final Vector3 position = new Vector3();
    public final Vector3 moveDirection = new Vector3();
    public boolean isWalking, isShuffling, isAttacking, isBlocking, isAirborne, isStanceSwitching;

    public ClientInfo(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }
}
