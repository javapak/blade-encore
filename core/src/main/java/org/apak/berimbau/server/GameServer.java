package org.apak.berimbau.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;

import org.apak.berimbau.network.GeneralUtils;
import org.apak.berimbau.network.NetworkManager;
import org.apak.berimbau.network.NetworkPacket;


public class GameServer {
    private static final int SERVER_PORT = 7777;
    private ServerNetworkManager networkManager;

    public GameServer() {
        try {
            String address = GeneralUtils.getBestLocalIP().getHostAddress();
            networkManager = new ServerNetworkManager(address, SERVER_PORT);
            System.out.println("Server started on " +  address + ":" + SERVER_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.networkManager.startListening();
    }
}
