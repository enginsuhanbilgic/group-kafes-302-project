package tr.edu.ku.comp302;

import tr.edu.ku.comp302.domain.controllers.NavigationController;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Game Application");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1920, 1080);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null); // Center the frame
            frame.setTitle("KAFES Game");

            //Create a new navigation controller with the frame and call the main menu
            NavigationController navigationController = new NavigationController(frame);
            navigationController.showMainMenu();

            frame.setVisible(true);
        });
    }
}
