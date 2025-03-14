package org.apak.berimbau.network;

import com.badlogic.gdx.math.Vector3;
import java.util.HashMap;
import java.util.Map;

import org.apak.berimbau.components.AttackStance;
import org.apak.berimbau.components.StateMachine;
import org.apak.berimbau.controllers.CharacterState;
import java.net.DatagramPacket;
import java.net.InetAddress;

import com.badlogic.gdx.math.Vector3;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class NetworkPacket implements Serializable {
    private static final long serialVersionUID = 1L; // Required for serialization

    private int playerID;
    private Map<String, Object> data;

    private InetAddress senderAddress;
    private int senderPort;

    public void setSender(InetAddress address, int port) {
        this.senderAddress = address;
        this.senderPort = port;
    }

public InetAddress getSenderAddress() {
    return senderAddress;
}

public int getSenderPort() {
    return senderPort;
}

    public NetworkPacket(int playerID) {
        this.playerID = playerID;
        this.data = new HashMap<>();
    }

    public int getPlayerID() {
        return playerID;
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public boolean getBoolean(String key) {
        return (boolean) data.getOrDefault(key, false);
    }

    public String getString(String key) {
        return (String) data.getOrDefault(key, "");
    }

    public Vector3 getVector3(String key) {
        return (Vector3) data.getOrDefault(key, new Vector3());
    }

    public AttackStance getAttackStance(String key) {
        return (AttackStance) data.getOrDefault(key, AttackStance.FAST);
    }

    public Map<String, Object> getData() {
        return data;
    }

	public StateMachine<CharacterState> getStateMachine(String key) {
		return (StateMachine<CharacterState>) data.getOrDefault(key, new StateMachine<CharacterState>());
		
	}}

