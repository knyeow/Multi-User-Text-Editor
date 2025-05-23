package server;

import common.Document;
import common.Message;
import java.io.*;
import java.net.Socket;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Server server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    private Gson gson;

    public ClientHandler(Socket socket, Server server) {
        this.clientSocket = socket;
        this.server = server;
        this.gson = new Gson();
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("[CLIENT_HANDLER] Streams initialized for client: " + socket.getInetAddress());
        } catch (IOException e) {
            System.err.println("[CLIENT_HANDLER] Error creating streams: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("[CLIENT_HANDLER] Starting message handling loop for client: " + clientSocket.getInetAddress());
            while (true) {
                Message message = (Message) in.readObject();
                System.out.println("[CLIENT_HANDLER] Received message type: " + message.getType() + " from client: " + 
                    (username != null ? username : clientSocket.getInetAddress()));
                handleMessage(message);
            }
        } catch (EOFException e) {
            System.out.println("[CLIENT_HANDLER] Client disconnected: " + 
                (username != null ? username : clientSocket.getInetAddress()));
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[CLIENT_HANDLER] Error handling client: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handleMessage(Message message) {
        switch (message.getType()) {
            case LOGIN_REQUEST:
                handleLogin(message.getBody());
                break;
            case CREATE_DOCUMENT:
                handleCreateDocument(message.getBody());
                break;
            case LIST_DOCUMENTS:
                handleListDocuments();
                break;
            case OPEN_DOCUMENT:
                handleOpenDocument(message.getBody());
                break;
            case TEXT_UPDATE:
                handleTextUpdate(message.getBody());
                break;
            default:
                System.out.println("[CLIENT_HANDLER] Unknown message type received: " + message.getType());
        }
    }

    private void handleLogin(String username) {
        System.out.println("[CLIENT_HANDLER] Processing login request for user: " + username);
        this.username = username;
        server.addClient(username, this);
        sendMessage(new Message(Message.Type.LOGIN_RESPONSE, "SUCCESS"));
        System.out.println("[CLIENT_HANDLER] Login successful for user: " + username);
    }

    private void handleCreateDocument(String documentName) {
        System.out.println("[CLIENT_HANDLER] Creating new document: " + documentName + " for user: " + username);
        Document doc = server.createDocument(documentName);
        sendMessage(new Message(Message.Type.DOCUMENT_CREATED, doc.getId()));
        System.out.println("[CLIENT_HANDLER] Document created successfully: " + documentName + " (ID: " + doc.getId() + ")");
    }

    private void handleListDocuments() {
        System.out.println("[CLIENT_HANDLER] Sending document list to user: " + username);
        List<Document> documents = server.getAllDocuments();
        String json = gson.toJson(documents);
        sendMessage(new Message(Message.Type.DOCUMENT_LIST, json));
        System.out.println("[CLIENT_HANDLER] Document list sent to user: " + username + " (" + documents.size() + " documents)");
    }

    private void handleOpenDocument(String documentId) {
        System.out.println("[CLIENT_HANDLER] User " + username + " requesting to open document: " + documentId);
        Document doc = server.getDocument(documentId);
        if (doc != null) {
            sendMessage(new Message(Message.Type.DOCUMENT_CONTENT, doc.getContent()));
            System.out.println("[CLIENT_HANDLER] Document content sent to user: " + username + " for document: " + doc.getName());
        } else {
            sendMessage(new Message(Message.Type.ERROR, "Document not found"));
            System.out.println("[CLIENT_HANDLER] Document not found error sent to user: " + username + " for document ID: " + documentId);
        }
    }

    private void handleTextUpdate(String updateJson) {
        try {
            TextUpdate update = gson.fromJson(updateJson, TextUpdate.class);
            System.out.println("[CLIENT_HANDLER] Processing text update from user: " + username + 
                " for document: " + update.getDocumentId());
            
            Document doc = server.getDocument(update.getDocumentId());
            if (doc != null) {
                doc.updateContent(null, update.getPosition(), update.getText(), update.isInsert());
                Message broadcast = new Message(Message.Type.TEXT_UPDATE_BROADCAST, updateJson);
                server.broadcastMessage(broadcast, username);
                System.out.println("[CLIENT_HANDLER] Text update processed and broadcasted for document: " + doc.getName());
            } else {
                System.out.println("[CLIENT_HANDLER] Text update failed - document not found: " + update.getDocumentId());
            }
        } catch (Exception e) {
            System.err.println("[CLIENT_HANDLER] Error processing text update: " + e.getMessage());
            sendMessage(new Message(Message.Type.ERROR, "Invalid update format"));
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
            System.out.println("[CLIENT_HANDLER] Message sent to user: " + username + " (Type: " + message.getType() + ")");
        } catch (IOException e) {
            System.err.println("[CLIENT_HANDLER] Error sending message to user " + username + ": " + e.getMessage());
        }
    }

    private void cleanup() {
        try {
            if (username != null) {
                System.out.println("[CLIENT_HANDLER] Cleaning up resources for user: " + username);
                server.removeClient(username);
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
            System.out.println("[CLIENT_HANDLER] Cleanup completed for client: " + 
                (username != null ? username : clientSocket.getInetAddress()));
        } catch (IOException e) {
            System.err.println("[CLIENT_HANDLER] Error during cleanup: " + e.getMessage());
        }
    }

    private static class TextUpdate {
        private String documentId;
        private int position;
        private String text;
        private boolean isInsert;

        public String getDocumentId() { return documentId; }
        public int getPosition() { return position; }
        public String getText() { return text; }
        public boolean isInsert() { return isInsert; }
    }
} 