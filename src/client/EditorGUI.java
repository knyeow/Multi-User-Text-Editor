package client;

import common.Message;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class EditorGUI extends JFrame {
    private Client client;
    private JTextArea editorArea;
    private JList<String> documentList;
    private DefaultListModel<String> documentListModel;
    private JTextField newDocumentField;
    private String currentDocumentId;
    private Gson gson;

    public EditorGUI(Client client) {
        this.client = client;
        this.gson = new Gson();
        setupGUI();
    }

    private void setupGUI() {
        setTitle("Collaborative Text Editor - " + client.getUsername());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Document list panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        documentListModel = new DefaultListModel<>();
        documentList = new JList<>(documentListModel);
        documentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        documentList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedDoc = documentList.getSelectedValue();
                if (selectedDoc != null) {
                    openDocument(selectedDoc);
                }
            }
        });

        JScrollPane listScrollPane = new JScrollPane(documentList);
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        // New document panel
        JPanel newDocPanel = new JPanel(new BorderLayout());
        newDocumentField = new JTextField();
        JButton createButton = new JButton("New Document");
        createButton.addActionListener(e -> createNewDocument());
        newDocPanel.add(newDocumentField, BorderLayout.CENTER);
        newDocPanel.add(createButton, BorderLayout.EAST);
        leftPanel.add(newDocPanel, BorderLayout.SOUTH);

        // Editor panel
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorArea = new JTextArea();
        editorArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        editorArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                sendTextUpdate(e.getOffset(), e.getLength(), "", false);
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                try {
                    String newText = editorArea.getText(e.getOffset(), e.getLength());
                    sendTextUpdate(e.getOffset(), e.getLength(), newText, true);
                } catch (javax.swing.text.BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        });

        JScrollPane editorScrollPane = new JScrollPane(editorArea);
        editorPanel.add(editorScrollPane, BorderLayout.CENTER);

        // Add panels to main frame
        add(leftPanel, BorderLayout.WEST);
        add(editorPanel, BorderLayout.CENTER);

        // Start message handling thread
        new Thread(this::handleMessages).start();

        // Request document list
        client.sendMessage(new Message(Message.Type.LIST_DOCUMENTS, ""));
    }

    private void createNewDocument() {
        String docName = newDocumentField.getText().trim();
        if (!docName.isEmpty()) {
            client.sendMessage(new Message(Message.Type.CREATE_DOCUMENT, docName));
            newDocumentField.setText("");
        }
    }

    private void openDocument(String documentId) {
        currentDocumentId = documentId;
        client.sendMessage(new Message(Message.Type.OPEN_DOCUMENT, documentId));
    }

    private void sendTextUpdate(int position, int length, String text, boolean isInsert) {
        if (currentDocumentId != null) {
            TextUpdate update = new TextUpdate(currentDocumentId, position, text, isInsert);
            String json = gson.toJson(update);
            client.sendMessage(new Message(Message.Type.TEXT_UPDATE, json));
        }
    }

    private void handleMessages() {
        while (true) {
            Message message = client.waitForMessage(null, 1000);
            if (message != null) {
                switch (message.getType()) {
                    case DOCUMENT_LIST:
                        updateDocumentList(message.getBody());
                        break;
                    case DOCUMENT_CONTENT:
                        updateEditorContent(message.getBody());
                        break;
                    case TEXT_UPDATE_BROADCAST:
                        handleTextUpdate(message.getBody());
                        break;
                }
            }
        }
    }

    private void updateDocumentList(String json) {
        List<String> documents = gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
        SwingUtilities.invokeLater(() -> {
            documentListModel.clear();
            for (String doc : documents) {
                documentListModel.addElement(doc);
            }
        });
    }

    private void updateEditorContent(String content) {
        SwingUtilities.invokeLater(() -> {
            editorArea.setText(content);
        });
    }

    private void handleTextUpdate(String updateJson) {
        TextUpdate update = gson.fromJson(updateJson, TextUpdate.class);
        if (update.getDocumentId().equals(currentDocumentId)) {
            SwingUtilities.invokeLater(() -> {
                try {
                    if (update.isInsert()) {
                        editorArea.insert(update.getText(), update.getPosition());
                    } else {
                        editorArea.replaceRange("", update.getPosition(), 
                            update.getPosition() + update.getText().length());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static class TextUpdate {
        private String documentId;
        private int position;
        private String text;
        private boolean isInsert;

        public TextUpdate(String documentId, int position, String text, boolean isInsert) {
            this.documentId = documentId;
            this.position = position;
            this.text = text;
            this.isInsert = isInsert;
        }
    }

    public static void main(String[] args) {
        String username = JOptionPane.showInputDialog("Enter your username:");
        if (username != null && !username.trim().isEmpty()) {
            Client client = new Client(username.trim());
            if (client.connect()) {
                SwingUtilities.invokeLater(() -> {
                    new EditorGUI(client).setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(null, "Failed to connect to server");
            }
        }
    }
} 