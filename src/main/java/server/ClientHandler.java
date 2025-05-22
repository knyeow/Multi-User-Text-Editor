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
        } catch (IOException e) {
            System.err.println("Error creating streams: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message message = (Message) in.readObject();
                handleMessage(message);
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected: " + username);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling client: " + e.getMessage());
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
        }
    }

    private void handleLogin(String username) {
        this.username = username;
        server.addClient(username, this);
        sendMessage(new Message(Message.Type.LOGIN_RESPONSE, "SUCCESS"));
    }

    private void handleCreateDocument(String documentName) {
        Document doc = server.createDocument(documentName);
        sendMessage(new Message(Message.Type.DOCUMENT_CREATED, doc.getId()));
    }

    private void handleListDocuments() {
        List<Document> documents = server.getAllDocuments();
        String json = gson.toJson(documents);
        sendMessage(new Message(Message.Type.DOCUMENT_LIST, json));
    }

    private void handleOpenDocument(String documentId) {
        Document doc = server.getDocument(documentId);
        if (doc != null) {
            sendMessage(new Message(Message.Type.DOCUMENT_CONTENT, doc.getContent()));
        } else {
            sendMessage(new Message(Message.Type.ERROR, "Document not found"));
        }
    }

    private void handleTextUpdate(String updateJson) {
        try {
            TextUpdate update = gson.fromJson(updateJson, TextUpdate.class);
            Document doc = server.getDocument(update.getDocumentId());
            if (doc != null) {
                doc.updateContent(null, update.getPosition(), update.getText(), update.isInsert());
                Message broadcast = new Message(Message.Type.TEXT_UPDATE_BROADCAST, updateJson);
                server.broadcastMessage(broadcast, username);
            }
        } catch (Exception e) {
            sendMessage(new Message(Message.Type.ERROR, "Invalid update format"));
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

    private void cleanup() {
        try {
            if (username != null) {
                server.removeClient(username);
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
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