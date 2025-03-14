package org.apak.berimbau.components;

import org.apak.berimbau.network.NetworkManager;
import org.apak.berimbau.network.NetworkPacket;

import com.badlogic.gdx.math.Vector3;

import org.apak.berimbau.controllers.CharacterController;

public class NetworkingComponent {
    private int playerID;
    private final ClientNetworkManager networkManager;
    private String clientAddress;

    public NetworkingComponent(int id, String serverIP, int serverPort) {
        this.playerID = id;
        this.networkManager = new ClientNetworkManager(serverIP, serverPort);


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
