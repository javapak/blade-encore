package org.apak.berimbau.network;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkManager {
    private static NetworkManager instance;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;
    private InetAddress clientAddress;

    private final Queue<NetworkPacket> receivedPackets = new ConcurrentLinkedQueue<>();

    protected NetworkManager(String serverIP, int port, String clientAddress) {
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(serverIP);
            serverPort = port;
            this.clientAddress = InetAddress.getByName(clientAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected NetworkManager(String serverIP, int port) {
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(serverIP);
            serverPort = port;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static NetworkManager getInstance(String serverIP, int port, String clientAddress) {
        if (instance == null) {
            instance = new NetworkManager(serverIP, port, clientAddress);
        }
        return instance;
    }

    public static NetworkManager getInstance() {
        return instance;
    }

    /**
     * Send a network packet to the server.
     */
    public void sendData(NetworkPacket packet) {
        try {
            packet.setSender(clientAddress, serverPort);
            byte[] data = serialize(packet);
            DatagramPacket udpPacket = new DatagramPacket(data, data.length, serverAddress, serverPort);
            socket.send(udpPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receive data from the server (should be called in a separate thread).
     */
    public void receiveData() {
        new Thread(() -> {
            while (true) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket udpPacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(udpPacket);
                    
                    NetworkPacket packet = deserialize(udpPacket.getData());
                    if (packet != null) {
                        receivedPackets.add(packet);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Retrieve the next received packet from the queue.
     */
    public NetworkPacket getNextPacket() {
        return receivedPackets.poll(); // Non-blocking
    }

    /**
     * Serialize a NetworkPacket into bytes.
     */
    private byte[] serialize(NetworkPacket packet) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(packet);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * Deserialize bytes into a NetworkPacket.
     */
    public static NetworkPacket deserialize(byte[] data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (NetworkPacket) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
