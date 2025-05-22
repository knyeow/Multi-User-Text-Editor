package client;

import common.Message;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;
    
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    private BlockingQueue<Message> messageQueue;
    private volatile boolean running;

    public Client(String username) {
        this.username = username;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.running = true;
    }

    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            // Start message receiver thread
            new Thread(this::receiveMessages).start();
            
            // Send login request
            sendMessage(new Message(Message.Type.LOGIN_REQUEST, username));
            
            // Wait for login response
            Message response = waitForMessage(Message.Type.LOGIN_RESPONSE, 5000);
            if (response == null || response.getType() != Message.Type.LOGIN_RESPONSE) {
                disconnect();
                return false;
            }
            
            return response.getBody().equals("SUCCESS");
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private void receiveMessages() {
        try {
            while (running) {
                Message message = (Message) in.readObject();
                messageQueue.offer(message);
            }
        } catch (EOFException e) {
            System.out.println("Server disconnected");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error receiving message: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public Message waitForMessage(Message.Type type, long timeout) {
        try {
            long startTime = System.currentTimeMillis();
            java.util.List<Message> temp = new java.util.ArrayList<>();
            while (System.currentTimeMillis() - startTime < timeout) {
                Message message = messageQueue.poll(timeout, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (message != null) {
                    if (type == null || message.getType() == type) {
                        // Re-queue the others
                        for (Message m : temp) messageQueue.offer(m);
                        return message;
                    } else {
                        temp.add(message);
                    }
                }
            }
            // Re-queue the others
            for (Message m : temp) messageQueue.offer(m);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public String getUsername() {
        return username;
    }
} 