package org.apak.berimbau.components;

import com.badlogic.gdx.math.Vector3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import org.apak.berimbau.network.GeneralUtils;

import org.apak.berimbau.network.NetworkManager;
import org.apak.berimbau.network.NetworkPacket;

public class ClientNetworkManager extends NetworkManager {
    private final ConcurrentHashMap<String, RemotePlayer> players = new ConcurrentHashMap<>(); // ✅ Tracks all connected players
    private final String yourAddress = GeneralUtils.getBestLocalIP().getHostAddress();
    private final String hostIP;

     protected DatagramSocket socket;

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
                    System.out.println("Received a packet " + packet.toString());
                    packet.setSender(udpPacket.getAddress(), getClientSocketPort()); // Store sender info
                    processPacket(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    

    public static ClientNetworkManager getClientNetworkManagerInstance(String serverIP, int serverPort) {
        serverIP = getBestConnectionIP(serverIP);

        return new ClientNetworkManager(serverIP, serverPort);
    }

    public void processPacket(NetworkPacket packet) {
        String clientKey = packet.getSenderAddress().getHostAddress() + ":" + packet.getSenderPort(); // add one because this is the port that is defined to be open on the client side to receive callbacks from the server.

        if (!players.containsKey(clientKey)) {
            players.put(clientKey, new RemotePlayer(clientKey));
            System.out.println("✅ New player detected: " + clientKey);
        }

        RemotePlayer player = players.get(clientKey);
        player.position.set(packet.getVector3("position"));
        player.moveDirection.set(packet.getVector3("moveDirection"));
        player.isWalking = packet.getBoolean("isWalking");
        player.isShuffling = packet.getBoolean("isShuffling");
        player.isAttacking = packet.getBoolean("isAttacking");
        player.isBlocking = packet.getBoolean("isBlocking");
        player.isAirborne = packet.getBoolean("isAirborne");
        player.isStanceSwitching = packet.getBoolean("isStanceSwitching");

    }

    public ConcurrentHashMap<String, RemotePlayer> getPlayers() {
        return players;
    }
}
