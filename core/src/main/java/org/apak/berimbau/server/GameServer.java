package org.apak.berimbau.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;


public class GameServer {
    private static final int SERVER_PORT = 7777;
    private DatagramSocket socket;
    private ServerNetworkManager networkManager;

    public GameServer() {
        try {

            socket = new DatagramSocket(null); // Do not bind from constructor immediately....
            socket.setReuseAddress(true);
            URL url = new URI("https://api.ipify.org").toURL();
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String ip = in.readLine();
            System.out.println("Public IP Address: " + ip);
            socket.bind(new InetSocketAddress("10.0.0.61", SERVER_PORT));
            networkManager = new ServerNetworkManager(ip, SERVER_PORT);
            System.out.println("Server started on port " + SERVER_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void start() {
        new Thread(() -> {
            while (true) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    // Assuming each packet received is from a client, print the client's address
                    System.out.println("Received packet from client: " + packet.getAddress().getHostAddress());

                    networkManager.processPacket(packet); // Now correctly calls processPacket()
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }
}
