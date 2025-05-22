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
    private java.util.Map<String, String> m_DocNameToId = new java.util.HashMap<>();
    private boolean m_IsApplyingRemoteUpdate = false;
    private javax.swing.Timer m_AutoSaveTimer;
    private static final int AUTO_SAVE_INTERVAL = 5000; // 5 seconds
    private boolean m_IsFirstLoad = true;

    public EditorGUI(Client client) {
        this.client = client;
        this.gson = new Gson();
        setupGUI();
        setupAutoSave();
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
                if (!m_IsApplyingRemoteUpdate) {
                    sendTextUpdate(e.getOffset(), e.getLength(), "", false);
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                if (!m_IsApplyingRemoteUpdate) {
                    try {
                        String newText = editorArea.getText(e.getOffset(), e.getLength());
                        sendTextUpdate(e.getOffset(), e.getLength(), newText, true);
                    } catch (javax.swing.text.BadLocationException ex) {
                        ex.printStackTrace();
                    }
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

    private void setupAutoSave() {
        m_AutoSaveTimer = new javax.swing.Timer(AUTO_SAVE_INTERVAL, e -> {
            if (currentDocumentId != null && !m_IsApplyingRemoteUpdate) {
                String content = editorArea.getText();
                client.sendMessage(new Message(Message.Type.TEXT_UPDATE, 
                    gson.toJson(new TextUpdate(currentDocumentId, 0, content, true))));
            }
        });
        m_AutoSaveTimer.start();
    }

    private void createNewDocument() {
        String docName = newDocumentField.getText().trim();
        if (!docName.isEmpty()) {
            client.sendMessage(new Message(Message.Type.CREATE_DOCUMENT, docName));
            newDocumentField.setText("");
        }
    }

    private void openDocument(String documentName) {
        String documentId = m_DocNameToId.get(documentName);
        if (documentId != null) {
            currentDocumentId = documentId;
            client.sendMessage(new Message(Message.Type.OPEN_DOCUMENT, documentId));
        }
    }

    private void sendTextUpdate(int position, int length, String text, boolean isInsert) {
        if (currentDocumentId != null && !m_IsApplyingRemoteUpdate) {
            // For removal, we need to get the actual text being removed
            String updateText = text;
            if (!isInsert && length > 0) {
                try {
                    updateText = editorArea.getText(position, length);
                } catch (javax.swing.text.BadLocationException e) {
                    e.printStackTrace();
                    return;
                }
            }
            
            TextUpdate update = new TextUpdate(currentDocumentId, position, updateText, isInsert);
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
        java.util.List<common.Document> documents = gson.fromJson(json, new com.google.gson.reflect.TypeToken<java.util.List<common.Document>>(){}.getType());
        SwingUtilities.invokeLater(() -> {
            documentListModel.clear();
            m_DocNameToId.clear();
            for (common.Document doc : documents) {
                documentListModel.addElement(doc.getName());
                m_DocNameToId.put(doc.getName(), doc.getId());
            }
        });
    }

    private void updateEditorContent(String content) {
        SwingUtilities.invokeLater(() -> {
            m_IsApplyingRemoteUpdate = true;
            editorArea.setText(content);
            m_IsFirstLoad = false;
            m_IsApplyingRemoteUpdate = false;
        });
    }

    private void handleTextUpdate(String updateJson) {
        TextUpdate update = gson.fromJson(updateJson, TextUpdate.class);
        if (update.getDocumentId().equals(currentDocumentId)) {
            SwingUtilities.invokeLater(() -> {
                try {
                    m_IsApplyingRemoteUpdate = true;
                    // Skip the first update if it's an insert and we just loaded the document
                    if (m_IsFirstLoad && update.isInsert()) {
                        return;
                    }
                    
                    // If position is 0 and it's an insert, treat it as a full content update
                    if (update.getPosition() == 0 && update.isInsert()) {
                        editorArea.setText(update.getText());
                    } else {
                        // For other updates, ensure the position is valid
                        int docLength = editorArea.getDocument().getLength();
                        int position = Math.min(update.getPosition(), docLength);
                        
                        if (update.isInsert()) {
                            editorArea.insert(update.getText(), position);
                        } else {
                            // For removal, ensure we don't try to remove more than what's available
                            int endPos = Math.min(position + update.getText().length(), docLength);
                            if (endPos > position) {
                                editorArea.replaceRange("", position, endPos);
                            }
                        }
                    }
                    m_IsApplyingRemoteUpdate = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    // If there's an error, request full content
                    client.sendMessage(new Message(Message.Type.OPEN_DOCUMENT, currentDocumentId));
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

        public String getDocumentId() {
            return documentId;
        }

        public int getPosition() {
            return position;
        }

        public String getText() {
            return text;
        }

        public boolean isInsert() {
            return isInsert;
        }
    }

    @Override
    public void dispose() {
        if (m_AutoSaveTimer != null) {
            m_AutoSaveTimer.stop();
        }
        super.dispose();
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