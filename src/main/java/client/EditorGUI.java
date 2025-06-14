package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class EditorGUI extends JFrame {
    private JTextArea textArea;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private GrpcClient client;
    private String username;
    private boolean isConnected = false;

    public EditorGUI() {
        setTitle("Collaborative Text Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create text area
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane textScrollPane = new JScrollPane(textArea);
        mainPanel.add(textScrollPane, BorderLayout.CENTER);

        // Create user list
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(150, 0));
        mainPanel.add(userScrollPane, BorderLayout.EAST);

        // Create connection panel
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField usernameField = new JTextField(15);
        JTextField hostField = new JTextField("localhost", 15);
        JTextField portField = new JTextField("5000", 5);
        JButton connectButton = new JButton("Connect");

        connectionPanel.add(new JLabel("Username:"));
        connectionPanel.add(usernameField);
        connectionPanel.add(new JLabel("Host:"));
        connectionPanel.add(hostField);
        connectionPanel.add(new JLabel("Port:"));
        connectionPanel.add(portField);
        connectionPanel.add(connectButton);

        mainPanel.add(connectionPanel, BorderLayout.NORTH);

        // Add main panel to frame
        add(mainPanel);

        // Add window listener for cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null) {
                    client.shutdown();
                }
            }
        });

        // Add connect button listener
        connectButton.addActionListener(e -> {
            if (!isConnected) {
                String username = usernameField.getText().trim();
                String host = hostField.getText().trim();
                String portStr = portField.getText().trim();

                if (username.isEmpty()) {
                    showError("Please enter a username");
                    return;
                }

                try {
                    int port = Integer.parseInt(portStr);
                    connect(username, host, port);
                } catch (NumberFormatException ex) {
                    showError("Invalid port number");
                }
            }
        });

        // Add text area listener
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { sendUpdate(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { sendUpdate(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { sendUpdate(); }

            private void sendUpdate() {
                if (isConnected && client != null) {
                    client.sendTextUpdate(textArea.getText());
                }
            }
        });
    }

    private void connect(String username, String host, int port) {
        try {
            client = new GrpcClient(host, port, this);
            client.start();
            client.join(username);
        } catch (Exception e) {
            showError("Failed to connect: " + e.getMessage());
        }
    }

    public void onJoinSuccess(String username) {
        this.username = username;
        this.isConnected = true;
        textArea.setEditable(true);
        setTitle("Collaborative Text Editor - " + username);
    }

    public void updateText(String text) {
        if (!text.equals(textArea.getText())) {
            textArea.setText(text);
        }
    }

    public void updateUserList(List<String> users) {
        userListModel.clear();
        for (String user : users) {
            userListModel.addElement(user);
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EditorGUI gui = new EditorGUI();
            gui.setVisible(true);
        });
    }
} 