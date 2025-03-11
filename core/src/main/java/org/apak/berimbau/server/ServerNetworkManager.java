package org.apak.berimbau.server;

import org.apak.berimbau.network.ClientInfo;
import org.apak.berimbau.network.NetworkManager;
import org.apak.berimbau.network.NetworkPacket;
import com.badlogic.gdx.math.Vector3;
import org.apak.berimbau.components.AttackStance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class ServerNetworkManager extends NetworkManager {
    private DatagramSocket socket;
    private final ConcurrentHashMap<Integer, ClientInfo> players = new ConcurrentHashMap<>();

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
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); 

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

private NetworkPacket deserialize(byte[] data) {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
         ObjectInputStream ois = new ObjectInputStream(bis)) {
        return (NetworkPacket) ois.readObject();
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}

public void processPacket(NetworkPacket packet) {
    int playerID = packet.getPlayerID();
    Vector3 moveDirection = packet.getVector3("moveDirection");
    boolean isWalking = packet.getBoolean("isWalking");
    boolean isShuffling = packet.getBoolean("isShuffling");
    boolean isAttacking = packet.getBoolean("isAttacking");
    boolean isBlocking = packet.getBoolean("isBlocking");
    boolean isAirborne = packet.getBoolean("isAirborne");
    boolean isStanceSwitching = packet.getBoolean("isStanceSwitching");

    System.out.println("Received input from player " + playerID);
    System.out.println("Move Direction: " + moveDirection);
    System.out.println("Walking: " + isWalking + ", Shuffling: " + isShuffling);
    System.out.println("Attacking: " + isAttacking + ", Blocking: " + isBlocking);
    System.out.println("Airborne: " + isAirborne + ", StanceSwitching: " + isStanceSwitching);

    // Apply validation logic (e.g., check if the player is allowed to move)
    if (!isValidMove(playerID, moveDirection)) {
        System.out.println("Invalid movement detected for player " + playerID);
        return;
    }

    // Update the player's state on the server
    updatePlayerState(playerID, moveDirection, isWalking, isShuffling, isAttacking, isBlocking, isAirborne, isStanceSwitching);
}

private boolean isValidMove(int playerID, Vector3 moveDirection) {
    // Implement movement validation logic
    return true;
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
        packet.put("state", sender.state);
        packet.put("attackStance", sender.attackStance.name());

        byte[] data = serialize(packet);

        for (Integer playerID : players.keySet()) {
            ClientInfo receiver = players.get(playerID);
            if (receiver == null || receiver.address == null || receiver.port <= 0) {
                System.err.println("ERROR: Invalid client address/port for player " + playerID);
                continue;
            }

            DatagramPacket udpPacket = new DatagramPacket(data, data.length, receiver.address, receiver.port);
            socket.send(udpPacket);
            System.out.println("Sent update to player " + playerID + " at " + receiver.address + ":" + receiver.port);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public void processPacket(DatagramPacket packet) {
        try {
            NetworkPacket receivedPacket = deserialize(packet.getData());
            if (receivedPacket == null) return;

            int playerID = receivedPacket.getPlayerID();
            Vector3 newPosition = receivedPacket.getVector3("position");
            String newState = receivedPacket.getString("state");
            AttackStance newStance = receivedPacket.getAttackStance("attackStance");
            Boolean isAttacking = receivedPacket.getBoolean("isAttacking");
            Boolean isBlocking = receivedPacket.getBoolean("isBlocking");
            Boolean isAirborne = receivedPacket.getBoolean("isAirborne");
            Boolean isStanceSwitching = receivedPacket.getBoolean("isStanceSwitching");
            Boolean isWalking = receivedPacket.getBoolean("isWalking");
            Boolean isShuffling = receivedPacket.getBoolean("isShuffling");
            int clientPort = packet.getPort();
            InetAddress clientAddress = packet.getAddress();

            if (clientPort == 0) {
                System.err.println("ERROR: Client port is 0! Unable to send packets back.");
                return;
            }

            // Store player information, including address and port
            players.put(playerID, new ClientInfo(clientAddress, clientPort, newPosition, newState, newStance));

            // Debugging information
            System.out.println("Stored client: " + clientAddress + ":" + clientPort + ", Client position: " + newPosition + ", State: " + newState + ", Stance: " + newStance + ", Attacking: " + isAttacking + ", Blocking: " + isBlocking + ", Airborne: " + isAirborne + ", StanceSwitching: " + isStanceSwitching + ", Walking: " + isWalking + ", Shuffling: " + isShuffling);

            // Broadcast the updated state
            broadcastPlayerUpdate(playerID);
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
