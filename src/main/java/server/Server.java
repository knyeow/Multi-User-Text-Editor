package server;

import common.Document;
import common.Message;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 5000;
    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clients;
    private Map<String, Document> documents;
    private ExecutorService clientThreadPool;

    public Server() {
        clients = new ConcurrentHashMap<>();
        documents = new ConcurrentHashMap<>();
        clientThreadPool = Executors.newCachedThreadPool();
        System.out.println("[SERVER] Server instance created");
    }

    public void start() {
        try {
            // Bind to all network interfaces
            serverSocket = new ServerSocket(PORT, 0, InetAddress.getByName("0.0.0.0"));
            
            // Get the server's IP address
            String serverIP = InetAddress.getLocalHost().getHostAddress();
            System.out.println("[SERVER] Server started on port " + PORT);
            System.out.println("[SERVER] Server IP address: " + serverIP);
            System.out.println("[SERVER] Waiting for client connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] New client connected from: " + clientSocket.getInetAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientThreadPool.execute(clientHandler);
                System.out.println("[SERVER] Client handler thread started for: " + clientSocket.getInetAddress());
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("[SERVER] Server socket closed");
            }
            clientThreadPool.shutdown();
            System.out.println("[SERVER] Client thread pool shutdown initiated");
        } catch (IOException e) {
            System.err.println("[SERVER] Error stopping server: " + e.getMessage());
        }
    }

    public void addClient(String username, ClientHandler client) {
        clients.put(username, client);
        System.out.println("[SERVER] User connected: " + username);
        System.out.println("[SERVER] Total connected users: " + clients.size());
        broadcastUserList();
        broadcastDocumentList();
    }

    public void removeClient(String username) {
        clients.remove(username);
        System.out.println("[SERVER] User disconnected: " + username);
        System.out.println("[SERVER] Remaining connected users: " + clients.size());
        broadcastUserList();
    }

    public Document createDocument(String name) {
        Document doc = new Document(name);
        documents.put(doc.getId(), doc);
        System.out.println("[SERVER] New document created: " + name + " (ID: " + doc.getId() + ")");
        System.out.println("[SERVER] Total documents: " + documents.size());
        broadcastDocumentList();
        return doc;
    }

    public Document getDocument(String id) {
        Document doc = documents.get(id);
        if (doc != null) {
            System.out.println("[SERVER] Document retrieved: " + doc.getName() + " (ID: " + id + ")");
        } else {
            System.out.println("[SERVER] Document not found: " + id);
        }
        return doc;
    }

    public List<Document> getAllDocuments() {
        return new ArrayList<>(documents.values());
    }

    public void broadcastMessage(Message message, String excludeUsername) {
        System.out.println("[SERVER] Broadcasting message type: " + message.getType() + 
            (excludeUsername != null ? " (excluding: " + excludeUsername + ")" : ""));
        for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
            if (!entry.getKey().equals(excludeUsername)) {
                entry.getValue().sendMessage(message);
            }
        }
    }

    private void broadcastUserList() {
        List<String> usernames = new ArrayList<>(clients.keySet());
        Message userListMessage = new Message(Message.Type.USER_LIST, 
            String.join(",", usernames));
        System.out.println("[SERVER] Broadcasting user list: " + String.join(", ", usernames));
        broadcastMessage(userListMessage, null);
    }

    private void broadcastDocumentList() {
        List<Document> docs = getAllDocuments();
        String json = new com.google.gson.Gson().toJson(docs);
        Message docListMessage = new Message(Message.Type.DOCUMENT_LIST, json);
        System.out.println("[SERVER] Broadcasting document list: " + docs.size() + " documents");
        broadcastMessage(docListMessage, null);
    }

    public static void main(String[] args) {
        System.out.println("[SERVER] Starting collaborative text editor server...");
        Server server = new Server();
        server.start();
    }
} 