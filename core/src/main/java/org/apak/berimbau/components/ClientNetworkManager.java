package org.apak.berimbau.components;

import com.badlogic.gdx.math.Vector3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apak.berimbau.network.GeneralUtils;
import org.apak.berimbau.network.NetworkManager;
import org.apak.berimbau.network.NetworkPacket;

public class ClientNetworkManager extends NetworkManager {


    private long lastSendTime = 0;
    private static final long SEND_RATE_LIMIT = 50; // 20 updates per second
    private final ConcurrentHashMap<String, RemotePlayer> players = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PlayerData> pendingPlayers = new ConcurrentHashMap<>();
    private final String yourAddress = GeneralUtils.getBestLocalIP().getHostAddress();
    private final String hostIP;
    protected DatagramSocket socket;
    
    // Class to store player state data without OpenGL operations
    private static class PlayerData {
        public final String clientKey;
        public Vector3 position = new Vector3();
        public boolean isWalking, isShuffling, isAttacking, isBlocking, isAirborne, isStanceSwitching;
        
        public PlayerData(String clientKey) {
            this.clientKey = clientKey;
        }
        
        public void updateFromPacket(NetworkPacket packet) {
            if (packet.getVector3("position") != null) {
                position.set(packet.getVector3("position"));
            }
            isWalking = packet.getBoolean("isWalking");
            isShuffling = packet.getBoolean("isShuffling");
            isAttacking = packet.getBoolean("isAttacking");
            isBlocking = packet.getBoolean("isBlocking");
            isAirborne = packet.getBoolean("isAirborne");
            isStanceSwitching = packet.getBoolean("isStanceSwitching");
        }
    }

    protected ClientNetworkManager(String serverIP, int serverPort) {
        super(serverIP, serverPort);
        this.hostIP = serverIP;
        try {
            this.socket = new DatagramSocket(null);  
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(yourAddress, serverPort));
        }
        catch (Exception E) {
            System.out.println("Something went wrong while trying to open the socket for listening to server messages.");
            socket = null;
        }
    }
    
    public int getClientSocketPort() {
        return super.getPort() + 1;
    }

    private static String getBestConnectionIP(String hostIP) {
        // Use local IP if the server is on the same machine
        if (hostIP != GeneralUtils.getBestPublicIPForHost()) {
            return hostIP; // Use provided IP
        }
        else if (hostIP.equals(GeneralUtils.getBestLocalIP().getHostName())) {
            return "127.0.0.1"; // Use localhost for direct connection
        }
        return hostIP; // Otherwise, use provided IP
    }
    public void sendData(NetworkPacket packet) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSendTime < SEND_RATE_LIMIT) {
            return; // Skip this update
        }
        lastSendTime = currentTime;
        
        // Rest of your send code
        try {
            byte[] data = super.serialize(packet);
            DatagramPacket udpPacket = new DatagramPacket(data, data.length, InetAddress.getByName((hostIP)), getPort());
            socket.send(udpPacket);
        } catch (IOException e) {
        }
    }

    public void startReceiveData() {
        new Thread(() -> {
            System.out.println("Listening for server messages...");
            while (true) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket udpPacket = new DatagramPacket(buffer, buffer.length);
                    if (socket != null) {
                        socket.receive(udpPacket);
                    }
                    NetworkPacket packet = NetworkManager.deserialize(udpPacket.getData());
                    packet.setSender(udpPacket.getAddress(), getClientSocketPort());
                    processPacket(packet);
                    
                    // Small sleep to prevent CPU hogging
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void processPacket(NetworkPacket packet) {
        String clientKey = packet.getSenderAddress().getHostAddress() + ":" + packet.getSenderPort();
        
        // Skip if this is from ourselves
        if (packet.getSenderAddress().getHostAddress().equals(yourAddress)) {
            return;
        }

        if (players.containsKey(clientKey)) {
            // Update existing player data
            RemotePlayer player = players.get(clientKey);
            player.teleportTo(packet.getVector3("position"));
            player.isWalking = packet.getBoolean("isWalking");
            player.isShuffling = packet.getBoolean("isShuffling");
            player.isAttacking = packet.getBoolean("isAttacking");
            player.isBlocking = packet.getBoolean("isBlocking");
            player.isAirborne = packet.getBoolean("isAirborne");
            player.isStanceSwitching = packet.getBoolean("isStanceSwitching");
        } else {
            // Queue creation for main thread if not exists
            PlayerData data = pendingPlayers.computeIfAbsent(clientKey, PlayerData::new);
            data.updateFromPacket(packet);
        }
    }

    // Call this from the main thread (e.g., in your render method)
    public void update() {
        if (!pendingPlayers.isEmpty()) {
            // Take a snapshot to avoid concurrent modification
            Map<String, PlayerData> pendingSnapshot = new HashMap<>(pendingPlayers);
            pendingPlayers.keySet().removeAll(pendingSnapshot.keySet());
            
            // Process all pending players at once
            for (PlayerData data : pendingSnapshot.values()) {
                if (!players.containsKey(data.clientKey)) {
                    try {
                        // Create player with OpenGL operations on main thread
                        RemotePlayer player = new RemotePlayer(data.clientKey);
                        // Apply saved state data
                        player.teleportTo((data.position));
                        
                        player.isWalking = data.isWalking;
                        player.isShuffling = data.isShuffling;
                        player.isAttacking = data.isAttacking;
                        player.isBlocking = data.isBlocking;
                        player.isAirborne = data.isAirborne;
                        player.isStanceSwitching = data.isStanceSwitching;
                        
                        players.put(data.clientKey, player);
                        System.out.println("Created new remote player: " + data.clientKey);
                    } catch (Exception e) {
                        System.err.println("Error creating remote player: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static ClientNetworkManager getClientNetworkManagerInstance(String serverIP, int serverPort) {
        serverIP = getBestConnectionIP(serverIP);
        return new ClientNetworkManager(serverIP, serverPort);
    }

    public ConcurrentHashMap<String, RemotePlayer> getPlayers() {
        return players;
    }
}