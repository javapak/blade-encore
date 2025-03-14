package org.apak.berimbau.components;

import org.apak.berimbau.network.NetworkManager;
import org.apak.berimbau.network.NetworkPacket;

import com.badlogic.gdx.math.Vector3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import org.apak.berimbau.controllers.CharacterController;

public class NetworkingComponent {
    private int playerID;
    private final NetworkManager networkManager;
    private String clientAddress;

    public NetworkingComponent(int id, String serverIP, int serverPort) {
        this.playerID = id;
        try {
        URL url = new URI("https://api.ipify.org").toURL();
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String ip = in.readLine();
        this.clientAddress = ip;

        }
        catch (Exception e) {
            System.out.println("Please make sure you have more than a local connection.");
        }
        this.networkManager = NetworkManager.getInstance(serverIP, serverPort, clientAddress);
        

    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

public void sync(CharacterController character) {
    NetworkPacket packet = new NetworkPacket(playerID);
    
    Vector3 position = character.getMovement().getPosition();
    
    packet.put("playerID", playerID);
    packet.put("position", position);
    packet.put("clientAddress", clientAddress);



    
    networkManager.sendData(packet);
}

    
    public void applyNetworkData(CharacterController character, NetworkPacket packet) {
        if (packet.getPlayerID() == character.getPlayerID()) {
            System.out.println("Received validation packet back from server for this client");
            character.getMovement().setPosition(packet.getVector3("position"));
            character.getStateMachine().setState(packet.getStateMachine("state"));
            character.getStateMachine().setAttackStance(AttackStance.valueOf(packet.getString("attackStance")));
            character.getStateMachine().isShuffling(packet.getBoolean("isShuffling"));
            character.getStateMachine().isAttacking(packet.getBoolean("isAttacking"));
            character.getStateMachine().isBlocking(packet.getBoolean("isBlocking"));
            character.getStateMachine().isAirborne(packet.getBoolean("isAirborne"));
            character.getStateMachine().isWalking(packet.getBoolean("isWalking"));  
            character.getStateMachine().isWalking(packet.getBoolean("isWalking"));
        }
    }
}
