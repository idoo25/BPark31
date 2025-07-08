package client;

import entities.Message;
import javafx.application.Application;
import javafx.application.Platform;
import ocsf.client.ObservableClient;

/**
 * Base class for BPark applications that provides common functionality
 * for server connection, message handling, and client communication.
 */
public abstract class BParkBaseApp extends Application {
    
    protected static BParkClient client;
    protected static String serverIP = "localhost";
    protected static int serverPort = 5555;
    
    /**
     * Common BPark client implementation shared between applications
     */
    protected static class BParkClient extends ObservableClient {
        public BParkClient(String host, int port) {
            super(host, port);
        }

        @Override
        protected void handleMessageFromServer(Object msg) {
            Platform.runLater(() -> {
                try {
                    Object message = msg;
                    if (message instanceof byte[]) {
                        message = ClientMessageHandler.deserialize(msg);
                    }

                    if (message instanceof Message) {
                        ClientMessageHandler.handleMessage((Message) message);
                    } else if (message instanceof String) {
                        ClientMessageHandler.handleStringMessage((String) message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        protected void connectionClosed() {
            System.out.println("Connection closed");
        }

        @Override
        protected void connectionException(Exception exception) {
            System.out.println("Connection error: " + exception.getMessage());
        }
    }
    
    /**
     * Establishes connection to the server
     */
    public static void connectToServer() {
        try {
            client = new BParkClient(serverIP, serverPort);
            client.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sends a Message object to the server
     */
    public static void sendMessage(Message msg) {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(ClientMessageHandler.serialize(msg));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sends a String message to the server
     */
    public static void sendStringMessage(String msg) {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Checks if client is connected to server
     */
    public static boolean isConnected() {
        return client != null && client.isConnected();
    }
    
    /**
     * Gets the current client instance
     */
    public static BParkClient getClient() {
        return client;
    }
    
    /**
     * Sets the server IP address
     */
    public static void setServerIP(String ip) {
        serverIP = ip;
    }
    
    /**
     * Disconnects from server and cleans up resources
     */
    protected void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.sendToServer("ClientDisconnect");
                client.closeConnection();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop() throws Exception {
        disconnect();
        super.stop();
    }
}