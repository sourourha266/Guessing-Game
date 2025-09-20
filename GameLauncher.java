import javax.swing.*;

/**
 * Main launcher for the Guessing Game GUIs
 *
 * This class provides a simple way to start the game with the GUI interface.
 * Make sure the RMI server (Game.java) is running before starting the GUI.
 */
public class GameLauncher {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }

        // Set default font for better Arabic support
        UIManager.put("Label.font", new java.awt.Font("Arial Unicode MS", java.awt.Font.PLAIN, 12));
        UIManager.put("Button.font", new java.awt.Font("Arial Unicode MS", java.awt.Font.PLAIN, 12));
        UIManager.put("TextField.font", new java.awt.Font("Arial Unicode MS", java.awt.Font.PLAIN, 12));
        UIManager.put("List.font", new java.awt.Font("Arial Unicode MS", java.awt.Font.PLAIN, 12));

        // Start the sign-in GUI
        SwingUtilities.invokeLater(() -> {
            new SignInGUI().setVisible(true);
        });
    }
}

