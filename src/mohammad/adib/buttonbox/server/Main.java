package mohammad.adib.buttonbox.server;

import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Map;
import java.util.Random;

public class Main {

    private static boolean debug;
    private static boolean isRunning = true;
    private static Robot robot;
    private static String computerName;

    public static void main(String[] args) {
        computerName = getComputerName();
        debug = args.length > 0 && args[0].equals("--debug");
        System.out.println("______ _   _ _____ _____ _____ _   _ ______  _______   __\n" +
                "| ___ \\ | | |_   _|_   _|  _  | \\ | || ___ \\|  _  \\ \\ / /\n" +
                "| |_/ / | | | | |   | | | | | |  \\| || |_/ /| | | |\\ V / \n" +
                "| ___ \\ | | | | |   | | | | | | . ` || ___ \\| | | |/   \\ \n" +
                "| |_/ / |_| | | |   | | \\ \\_/ / |\\  || |_/ /\\ \\_/ / /^\\ \\\n" +
                "\\____/ \\___/  \\_/   \\_/  \\___/\\_| \\_/\\____/  \\___/\\/   \\/");
        new Thread(Main::startUDPServer).start();
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private static String getComputerName() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME"))
            return env.get("COMPUTERNAME");
        else if (env.containsKey("HOSTNAME"))
            return env.get("HOSTNAME");
        else
            return "BUTTONBOX-" + new Random().nextInt(500);
    }

    private static void startUDPServer() {
        DatagramSocket wSocket = null;
        DatagramPacket wPacket = null;
        byte[] wBuffer = null;

        try {
            wSocket = new DatagramSocket(18250);
            System.out.println("\nServer started. Please connect your phone to the same Wi-Fi network and enjoy");
            while (isRunning) {
                try {
                    wBuffer = new byte[2048];
                    wPacket = new DatagramPacket(wBuffer, wBuffer.length);
                    wSocket.receive(wPacket);
                    handlePacket(wPacket, wBuffer);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (SocketException e) {
            System.out.println("Error: \n" + e.getMessage());
            System.exit(1);
        }
    }

    private static void handlePacket(DatagramPacket p, byte[] b) throws Exception {
        String rawMessage = new String(b).trim();
        if(debug) System.out.println(rawMessage + " from " + p.getAddress() + ":" + p.getPort());
        if (rawMessage.startsWith("buttonbox-ack")) {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(computerName.getBytes(), computerName.length(), p.getAddress(), 18250);
            socket.send(packet);
        } else {
            int keyCode = Integer.parseInt(new String(b).toLowerCase().trim());
            System.out.println("Button Press: " + (char) keyCode);
            simulateKeyPress(keyCode);
        }
    }

    private static void simulateKeyPress(int key) {
        new Thread(() -> {
            robot.keyPress(key);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            robot.keyRelease(key);
        }).start();
    }
}