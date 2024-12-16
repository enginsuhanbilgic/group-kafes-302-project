import javax.swing.*;
import ui.MainMenuView;

public class Main {
    public static void main(String[] args) {
        // Use SwingUtilities to ensure thread safety
        SwingUtilities.invokeLater(() -> {
            // Create and display the main menu view
            MainMenuView mainMenu = new MainMenuView();
            mainMenu.showFrame();

        });
    }
}
