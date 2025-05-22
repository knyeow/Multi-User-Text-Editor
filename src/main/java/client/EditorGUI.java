package client;

import javax.swing.*;

public class EditorGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Multi-User Text Editor Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 200);
            frame.add(new JLabel("Client GUI is running!"));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
} 