package org.apak.berimbau.components;

import org.apak.berimbau.network.NetworkManager;
import org.apak.berimbau.network.NetworkPacket;
import org.apak.berimbau.controllers.CharacterController;

public class NetworkingComponent {
    private int playerID;
    private final NetworkManager networkManager;

    public NetworkingComponent(int id, String serverIP, int serverPort) {
        this.playerID = id;
        this.networkManager = NetworkManager.getInstance(serverIP, serverPort);
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void sync(CharacterController character) {
        NetworkPacket packet = new NetworkPacket(playerID);
        packet.put("position", character.getMovement().getPosition());
        packet.put("state", character.getStateMachine().getCurrentState());
        packet.put("attackStance", character.getStateMachine().getAttackStance());
        packet.put("isWalking", character.getStateMachine().isWalking());
        packet.put("isShuffling", character.getStateMachine().isShuffling());
        packet.put("isAttacking", character.getStateMachine().isAttacking());
        packet.put("isBlocking", character.getStateMachine().isBlocking());
        packet.put("isAirborne", character.getStateMachine().isAirborne());
        networkManager.sendData(packet);
    }
    
    public void applyNetworkData(CharacterController character, NetworkPacket packet) {
        character.getMovement().setPosition(packet.getVector3("position"));
        character.getStateMachine().setState(packet.getString("state"));
        character.getStateMachine().setAttackStance(AttackStance.valueOf(packet.getString("attackStance")));
        character.getStateMachine().isShuffling(packet.getBoolean("isShuffling"));
        character.getStateMachine().isAttacking(packet.getBoolean("isAttacking"));
        character.getStateMachine().isBlocking(packet.getBoolean("isBlocking"));
        character.getStateMachine().isAirborne(packet.getBoolean("isAirborne"));
        character.getStateMachine().isWalking(packet.getBoolean("isWalking"));  
        character.getStateMachine().isWalking(packet.getBoolean("isWalking"));
    }
}
