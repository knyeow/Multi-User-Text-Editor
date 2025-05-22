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
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientThreadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            clientThreadPool.shutdown();
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

    public void addClient(String username, ClientHandler client) {
        clients.put(username, client);
        broadcastUserList();
        broadcastDocumentList();
    }

    public void removeClient(String username) {
        clients.remove(username);
        broadcastUserList();
    }

    public Document createDocument(String name) {
        Document doc = new Document(name);
        documents.put(doc.getId(), doc);
        broadcastDocumentList();
        return doc;
    }

    public Document getDocument(String id) {
        return documents.get(id);
    }

    public List<Document> getAllDocuments() {
        return new ArrayList<>(documents.values());
    }

    public void broadcastMessage(Message message, String excludeUsername) {
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
        broadcastMessage(userListMessage, null);
    }

    private void broadcastDocumentList() {
        List<Document> docs = getAllDocuments();
        String json = new com.google.gson.Gson().toJson(docs);
        Message docListMessage = new Message(Message.Type.DOCUMENT_LIST, json);
        broadcastMessage(docListMessage, null);
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
} 