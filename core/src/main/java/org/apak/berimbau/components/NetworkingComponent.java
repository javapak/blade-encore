package org.apak.berimbau.components;

import org.apak.berimbau.network.NetworkPacket;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import org.apak.berimbau.controllers.CharacterController;

public class NetworkingComponent {
    private int playerID;
    private final ClientNetworkManager networkManager;
    private String clientAddress;
    private String serverAddress;
    private boolean positionUpdatedFromServer = false;
    private long lastServerUpdate = 0;
    private boolean serverConnectionVerified = false;
    private long lastConnectionCheck = 0;
    private static final long CONNECTION_CHECK_INTERVAL = 5000; // Check every 5 seconds

    public NetworkingComponent(int id, String serverIP, int serverPort) {
        this.playerID = id;
        this.serverAddress = serverIP;
        this.networkManager = new ClientNetworkManager(serverIP, serverPort);
        
        // Initial verification
        serverConnectionVerified = verifyServerConnection(serverIP, serverPort);
        if (!serverConnectionVerified) {
            System.err.println("WARNING: Cannot connect to server at " + serverIP + ":" + serverPort);
            System.err.println("Network position updates will not work!");
        } else {
            System.out.println(" Server connection verified at " + serverIP + ":" + serverPort);
        }
        lastConnectionCheck = System.currentTimeMillis();
    }
      public boolean verifyServerConnection(String serverIP, int serverPort) {
          try {
              // Use DatagramSocket for UDP, but don't send any test data
              java.net.DatagramSocket testSocket = new java.net.DatagramSocket();
              testSocket.connect(new java.net.InetSocketAddress(serverIP, serverPort));
              testSocket.close();
              return true;
          } catch (Exception e) {
              System.err.println("UDP Connection test failed: " + e.getMessage());
              return false;
          }
      }

    public ClientNetworkManager getNetworkManager() {
        return networkManager;
    }
    public String getServerAddress() {
        return serverAddress;
    }

    public void sync(CharacterController character) {
        // Periodically check connection
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastConnectionCheck > CONNECTION_CHECK_INTERVAL) {
            serverConnectionVerified = verifyServerConnection(
                this.getServerAddress(), 
                networkManager.getPort());
            
            if (serverConnectionVerified) {
                System.out.println("Server connection still active");
            } else {
                System.err.println("WARNING: Server connection lost");
            }
            lastConnectionCheck = currentTime;
        }
        
        // Only attempt network operations if connection verified
        if (serverConnectionVerified) {
            NetworkPacket packet = new NetworkPacket(playerID);
            
            Vector3 position = character.getMovement().getPosition();
            
            packet.put("playerID", playerID);
            packet.put("position", position);
            packet.put("clientAddress", clientAddress);
            
            // Send current input state to server
            networkManager.sendData(packet);
            
            // Process any incoming packets from server
            NetworkPacket serverPacket = networkManager.getNextPacket();
            if (serverPacket != null) {
                applyNetworkData(character, serverPacket);
            }
        }
    }
    
    public void applyNetworkData(CharacterController character, NetworkPacket packet) {
        // Check if this packet is for our player
        if (packet.getPlayerID() == this.playerID) {
            System.out.println("Received validation packet from server");
            
            // Store previous position for logging
            Vector3 oldPosition = new Vector3(character.getMovement().getPosition());
            
            // Get new position from server
            Vector3 serverPosition = packet.getVector3("position");
            
            // Apply server position to physics body
            Matrix4 transform = new Matrix4();
            character.getRigidBody().getWorldTransform(transform);
            transform.setTranslation(serverPosition);
            character.getRigidBody().setWorldTransform(transform);
            
            // Update movement component
            character.getMovement().update();
            
            // Record that we've applied a server update
            positionUpdatedFromServer = true;
            lastServerUpdate = System.currentTimeMillis();
            
            System.out.println("NETWORK AUTHORITY: Position changed from " + 
                oldPosition + " to " + serverPosition);
            
            // Apply other state updates
            if (packet.getData().containsKey("state")) {
                character.getStateMachine().setState(packet.getStateMachine("state"));
            }
            
            // Only update other states if they're included in the packet
            if (packet.getData().containsKey("attackStance")) {
                character.getStateMachine().setAttackStance(
                    AttackStance.valueOf(packet.getString("attackStance")));
            }
        }
    }
    
    public boolean wasRecentlyUpdatedFromServer() {
        return positionUpdatedFromServer && 
               (System.currentTimeMillis() - lastServerUpdate < 100); // Within last 100ms
    }
    
    public boolean isServerConnectionVerified() {
        return serverConnectionVerified;
    }
}