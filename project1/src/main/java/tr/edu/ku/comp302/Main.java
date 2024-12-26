package tr.edu.ku.comp302;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.controllers.NavigationController;
import tr.edu.ku.comp302.domain.models.Player;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("KAFES Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(GameConfig.RES_HORIZONTAL, GameConfig.RES_VERTICAL);
            frame.setResizable(GameConfig.RESIZABLE);
            frame.setLocationRelativeTo(null); // Center the frame
            frame.setTitle("KAFES Game");
            
            
            //Create a new navigation controller with the frame and call the main menu
            NavigationController navigationController = new NavigationController(frame);
            navigationController.showMainMenu();

            frame.setVisible(true);
        });
    }
}