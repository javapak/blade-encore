package org.apak.berimbau.network;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkManager {
    private static NetworkManager instance;
    protected DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

    private final Queue<NetworkPacket> receivedPackets = new ConcurrentLinkedQueue<>();

    protected NetworkManager(String serverIP, int port) {
        try {
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(serverIP);
            serverPort = port;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static NetworkManager getInstance(String serverIP, int port) {
        if (instance == null) {
            instance = new NetworkManager(serverIP, port);
        }
        return instance;
    }
    
    public int getPort() {
        return serverPort;
    }

    public static NetworkManager getInstance() {
        return instance;
    }
    public void sendData(NetworkPacket packet) {
        try {
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
    protected byte[] serialize(NetworkPacket packet) {
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
