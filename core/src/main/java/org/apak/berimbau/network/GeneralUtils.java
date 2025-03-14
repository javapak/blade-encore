package org.apak.berimbau.network;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.net.InetAddress;
import java.net.NetworkInterface;

public class GeneralUtils {
    public static String getBestPublicIPForHost() {
        try {
            // ✅ Step 1: Get the external public IP
            URL checkIP = new URI("https://checkip.amazonaws.com/").toURL();
            BufferedReader in = new BufferedReader(new InputStreamReader(checkIP.openStream()));
            String publicIP = in.readLine().trim();

            // ✅ Step 2: Verify that this IP is actually accessible (optional, but good practice)
            if (isValidPublicIP(publicIP)) {
                System.out.println("Public I!!!: " + publicIP);
                return publicIP;
            }

            // ✅ Step 3: If public IP check fails, try finding the best local address
            return getLocalNonLoopbackAddress();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "127.0.0.1"; // Fallback: If everything fails, return localhost
    }

    private static boolean isValidPublicIP(String ip) {
        // Prevent invalid IPs or internal addresses from being used as "public"
        return !(ip.startsWith("192.") || ip.startsWith("10.") || ip.startsWith("172.") || ip.equals("127.0.0.1"));
    }

    private static String getLocalNonLoopbackAddress() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface netInterface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (!addr.isLoopbackAddress() && !addr.isSiteLocalAddress()) {
                    return addr.getHostAddress();
                }
            }
        }
        return "127.0.0.1"; // Fallback if no valid address is found
    }

      public static InetAddress getBestLocalIP() {
        try (DatagramSocket socket = new DatagramSocket()) {
            // Connect to a public IP (doesn't actually send data)
            socket.connect(InetAddress.getByName("8.8.8.8"), 53);
            return socket.getLocalAddress();
        } catch (Exception e) {
            // Fallback to localhost
            return InetAddress.getLoopbackAddress();
        }
    }
}