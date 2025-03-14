package org.apak.berimbau.server;

import org.apak.berimbau.network.ClientInfo;
import org.apak.berimbau.network.NetworkManager;
import org.apak.berimbau.network.NetworkPacket;
import com.badlogic.gdx.math.Vector3;
import org.apak.berimbau.components.AttackStance;
import org.apak.berimbau.components.StateMachine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ServerNetworkManager extends NetworkManager {
    private DatagramSocket socket;
    private final ConcurrentHashMap<String, ClientInfo> players = new ConcurrentHashMap<>();

    public ServerNetworkManager(String serverIP, int serverPort) {
        super(serverIP, serverPort);
        try {
            if (this.socket == null) {
                this.socket = new DatagramSocket(null);
                this.socket.setReuseAddress(true); // Allows reusing the port immediately
                this.socket.bind(new InetSocketAddress(serverPort)); //Binds safely
            }
            startListening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        

    private void startListening() {
        new Thread(() -> {
            while (true) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket udpPacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(udpPacket);
    
                    NetworkPacket packet = NetworkManager.deserialize(udpPacket.getData());
                    packet.setSender(udpPacket.getAddress(), udpPacket.getPort()); // ✅ Store sender info
    
                    processPacket(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private byte[] serialize(NetworkPacket packet) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(bos)) {
        oos.writeObject(packet);
        return bos.toByteArray();
    } catch (Exception e) {
        e.printStackTrace();
    }
    return new byte[0];
}

public void processPacket(NetworkPacket packet) {
    InetAddress clientAddress = packet.getSenderAddress();
    int clientPort = packet.getSenderPort();
    String clientKey = clientAddress.getHostAddress() + ":" + clientPort; // ✅ Unique identifier

    //  Register new clients if not already tracked
    if (!players.containsKey(clientKey)) {
        players.put(clientKey, new ClientInfo(clientAddress, clientPort));
        System.out.println("✅ New client connected: " + clientKey);
    }

    // Update the client's state
    ClientInfo client = players.get(clientKey);
    if (client == null) return;

    client.position.set(packet.getVector3("position"));
    client.moveDirection.set(packet.getVector3("moveDirection"));
    client.isWalking = packet.getBoolean("isWalking");
    client.isShuffling = packet.getBoolean("isShuffling");
    client.isAttacking = packet.getBoolean("isAttacking");
    client.isBlocking = packet.getBoolean("isBlocking");
    client.isAirborne = packet.getBoolean("isAirborne");
    client.isStanceSwitching = packet.getBoolean("isStanceSwitching");

    // Log the update
    System.out.println("📡 Updated player " + clientKey + " | Position: " + client.position);

    // Broadcast this update to all clients
    broadcastToAllPlayers(packet, clientKey);
}



private boolean isValidMove(int playerID, Vector3 moveDirection) {
    // Implement movement validation logic
    return true;
}

public void broadcastChatToAllPlayers(NetworkPacket packet, int senderID) {
    for (Entry<String, ClientInfo> entry : players.entrySet()) {
        String clientAddress = entry.getKey();
        ClientInfo clientInfo = entry.getValue();
        InetAddress address = clientInfo.address;
        int port = clientInfo.port;

        // Don't send the message back to the sender
        if (packet.getSenderAddress().toString() == address.toString()) continue;

        try {
            byte[] data = serialize(packet);
            DatagramPacket udpPacket = new DatagramPacket(data, data.length, address, port);
            socket.send(udpPacket);
        } catch (IOException e) {
        }
    }
}

private void updatePlayerState(int playerID, Vector3 moveDirection, boolean isWalking, boolean isShuffling, boolean isAttacking, boolean isBlocking, boolean isAirborne, boolean isStanceSwitching) {
    // Apply validated inputs to the player's state
}


private void broadcastPlayerUpdate(int senderID) {
    try {
        if (this.socket == null) { // Ensure socket is never null
            System.err.println("ERROR: DatagramSocket is null in broadcastPlayerUpdate()");
            return;
        }


        

        ClientInfo sender = players.get(senderID);
        if (sender == null) {
            System.err.println("ERROR: Sender ID " + senderID + " not found.");
            return;
        }

        NetworkPacket packet = new NetworkPacket(senderID);
        packet.put("position", sender.position);

        byte[] data = serialize(packet);

        for (String entry : players.keySet()) {
            ClientInfo receiver = players.get(entry);
            if (receiver == null || receiver.address == null || receiver.port <= 0) {
                System.err.println("ERROR: Invalid client address/port for player " + entry);
                continue;
            }

            DatagramPacket udpPacket = new DatagramPacket(data, data.length, receiver.address, receiver.port);
            socket.send(udpPacket);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
private void broadcastToAllPlayers(NetworkPacket packet, String senderKey) {
    try {
        if (this.socket == null) {
            System.err.println("ERROR: DatagramSocket is null in broadcastToAllPlayers()");
            return;
        }

        byte[] data = serialize(packet); 
        for (String clientKey : players.keySet()) {
            if (clientKey.equals(senderKey)) continue; 

            ClientInfo receiver = players.get(clientKey);
            if (receiver == null || receiver.address == null || receiver.port <= 0) {
                System.err.println("ERROR: Invalid client address/port.");
                continue;
            }

            DatagramPacket udpPacket = new DatagramPacket(data, data.length, receiver.address, receiver.port);
            socket.send(udpPacket);
            System.out.println("📡 Sent broadcast update to " + receiver.address + ":" + receiver.port);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    public void shutdown() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("Server socket closed.");
        }
    }
}
