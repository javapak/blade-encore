package org.apak.berimbau.network;

import com.badlogic.gdx.math.Vector3;
import org.apak.berimbau.components.AttackStance;
import org.apak.berimbau.components.StateMachine;

import java.net.InetAddress;

public class ClientInfo {
    public InetAddress address;
    public int port;
    public Vector3 position;
    public StateMachine state;
    public AttackStance attackStance;

    public ClientInfo(InetAddress address, int port, Vector3 position, StateMachine state, AttackStance attackStance) {
        this.address = address;
        this.port = port;
        this.position = position;
        this.state = state;
        this.attackStance = attackStance;
    }
}