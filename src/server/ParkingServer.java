package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import controllers.ParkingController;
import controllers.ReportController;
import entities.Message;
import entities.Message.MessageType;
import entities.ParkingOrder;
import entities.ParkingReport;
import entities.ParkingSubscriber;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import serverGUI.ServerPortFrame;

public class ParkingServer extends AbstractServer {
    final public static Integer DEFAULT_PORT = 5555;

    public static ParkingController parkingController;
    public static ReportController reportController;
    public static ServerPortFrame spf;

    public Map<ConnectionToClient, String> clientsMap = new HashMap<>();
    public static String serverIp;

    private ScheduledExecutorService connectionPoolTimer;
    private final int POOL_SIZE = 5;
    private final int TIMER_INTERVAL = 30;

    public ParkingServer(int port) {
        super(port);
        try {
            serverIp = InetAddress.getLocalHost().getHostAddress() + ":" + port;
        } catch (Exception e) {
            e.printStackTrace();
        }
        initializeConnectionPool();
    }

    private void initializeConnectionPool() {
        connectionPoolTimer = Executors.newScheduledThreadPool(POOL_SIZE);
        connectionPoolTimer.scheduleAtFixedRate(() -> {
            synchronized (clientsMap) {
                System.out.println("Connection Pool Status - Active connections: " + clientsMap.size());
                cleanupInactiveConnections();
            }
        }, 0, TIMER_INTERVAL, TimeUnit.SECONDS);
    }

    private synchronized void cleanupInactiveConnections() {
        clientsMap.entrySet().removeIf(entry -> !entry.getKey().isAlive());
    }

    public synchronized void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);
        
        try {
            if (msg instanceof byte[]) {
                try {
                    msg = deserialize(msg);
                } catch (Exception ex) {
                    System.err.println("Error during deserialization: " + ex.getMessage());
                    ex.printStackTrace();
                    return;
                }
            }

            if (msg instanceof Message) {
                try {
                    handleMessageObject((Message) msg, client);
                } catch (Exception ex) {
                    System.err.println("Error handling Message object: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else if (msg instanceof String) {
                try {
                    handleStringMessage((String) msg, client);
                } catch (Exception ex) {
                    System.err.println("Error handling String message: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.err.println("General error in handleMessageFromClient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private synchronized void handleMessageObject(Message message, ConnectionToClient client) throws IOException {
        Message ret;

        try {
            switch (message.getType()) {

                case KIOSK_ID_LOGIN:
                    String combined = (String) message.getContent();
                    String[] parts = combined.split(",");
                    if (parts.length != 2) {
                        ret = new Message(MessageType.KIOSK_LOGIN_RESPONSE, "");
                        client.sendToClient(serialize(ret));
                        return;
                    }

                    String username = parts[0].trim();
                    int userID;
                    try {
                        userID = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        ret = new Message(MessageType.KIOSK_LOGIN_RESPONSE, "");
                        client.sendToClient(serialize(ret));
                        return;
                    }

                    String name = parkingController.getNameByUsernameAndUserID(username, userID);
                    if (name != null) {
                        ret = new Message(MessageType.KIOSK_LOGIN_RESPONSE, name + "," + userID);
                    } else {
                        ret = new Message(MessageType.KIOSK_LOGIN_RESPONSE, "");
                    }
                    client.sendToClient(serialize(ret));
                    break;

                case KIOSK_RF_LOGIN:
                    int rfUserID = (Integer) message.getContent();
                    String nameByID = parkingController.getNameByUserID(rfUserID);
                    if (nameByID != null) {
                        ret = new Message(MessageType.KIOSK_LOGIN_RESPONSE, nameByID + "," + rfUserID);
                    } else {
                        ret = new Message(MessageType.KIOSK_LOGIN_RESPONSE, "");
                    }
                    client.sendToClient(serialize(ret));
                    break;

                case ENTER_PARKING_KIOSK:
                    int enteringUserID = (Integer) message.getContent();
                    if (parkingController.isParkingFull()) {
                        ret = new Message(MessageType.ENTER_PARKING_KIOSK_RESPONSE, "FULL");
                    } else {
                        String entryResult = parkingController.enterParking(enteringUserID);
                        ret = new Message(MessageType.ENTER_PARKING_KIOSK_RESPONSE, entryResult);
                    }
                    client.sendToClient(serialize(ret));
                    break;

                case RETRIEVE_CAR_KIOSK:
                    int parkingCode = (Integer) message.getContent();
                    String retrievalResult = parkingController.retrieveCarByCode(parkingCode);
                    ret = new Message(MessageType.RETRIEVE_CAR_KIOSK_RESPONSE, retrievalResult);
                    client.sendToClient(serialize(ret));
                    break;

                case FORGOT_CODE_KIOSK:
                    int forgotUserID = (Integer) message.getContent();
                    String code = parkingController.sendLostParkingCode(forgotUserID);
                    ret = new Message(MessageType.FORGOT_CODE_KIOSK_RESPONSE, code);
                    client.sendToClient(serialize(ret));
                    break;

                default:
                    System.out.println("Unknown message type: " + message.getType());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ret = new Message(MessageType.KIOSK_LOGIN_RESPONSE, "Server error");
            client.sendToClient(serialize(ret));
        }
    }
    
    private synchronized void handleStringMessage(String message, ConnectionToClient client) {
        String[] arr = message.split("\\s");

        try {
            switch (arr[0]) {
                case "ClientDisconnect":
                    disconnect(client);
                    break;

                default:
                    System.out.println("Unknown string command: " + arr[0]);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient("error " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private byte[] serialize(Message msg) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteStream);
            out.writeObject(msg);
            out.flush();
            return byteStream.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Object deserialize(Object msg) {
        try {
            byte[] messageBytes = (byte[]) msg;
            ByteArrayInputStream byteStream = new ByteArrayInputStream(messageBytes);
            ObjectInputStream objectStream = new ObjectInputStream(byteStream);
            return objectStream.readObject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected void serverStarted() {
        System.out.println("ParkB Server listening for connections on port " + getPort());
        parkingController.initializeParkingSpots();
    }

    protected void serverStopped() {
        System.out.println("ParkB Server has stopped listening for connections.");
        if (parkingController != null) {
            parkingController.shutdown();
            System.out.println("Auto-cancellation service shut down successfully");
        }
        if (connectionPoolTimer != null) {
            connectionPoolTimer.shutdown();
        }
    }

    @Override
    protected synchronized void clientConnected(ConnectionToClient client) {
        String clientIP = client.getInetAddress().getHostAddress();
        String connectionStatus = "ClientIP: " + clientIP + " status: connected";
        synchronized (clientsMap) {
            clientsMap.put(client, connectionStatus);
        }
        if (spf != null) {
            spf.printConnection(clientsMap);
        }
    }

    protected synchronized void disconnect(ConnectionToClient client) {
        String clientIP = client.getInetAddress().getHostAddress();
        String disconnectionStatus = "ClientIP: " + clientIP + " status: disconnected";
        synchronized (clientsMap) {
            clientsMap.put(client, disconnectionStatus);
        }
        if (spf != null) {
            spf.printConnection(clientsMap);
        }
    }

    public static void main(String[] args) {
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Throwable t) {
            port = DEFAULT_PORT;
        }

        ParkingServer sv = new ParkingServer(port);

        try {
            sv.listen();
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
        }
    }

    public synchronized void shutdown() {
        if (parkingController != null) {
            parkingController.shutdown();
        }
        if (connectionPoolTimer != null) {
            connectionPoolTimer.shutdown();
        }
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
