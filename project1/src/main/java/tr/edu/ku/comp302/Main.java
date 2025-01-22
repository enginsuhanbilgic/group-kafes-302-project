package tr.edu.ku.comp302;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.controllers.MusicController;
import tr.edu.ku.comp302.domain.controllers.NavigationController;
import tr.edu.ku.comp302.domain.controllers.ResourceManager;

public class Main {
    public static void main(String[] args) throws Exception {

        ResourceManager.init();
        MusicController musicController = new MusicController("assets/gamemusic.wav");

        //Play the music at the start of the game
        musicController.play();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("KAFES Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(GameConfig.RES_HORIZONTAL, GameConfig.RES_VERTICAL);
            frame.setResizable(GameConfig.RESIZABLE);
            frame.setLocationRelativeTo(null); // Center the frame
            frame.setTitle("KAFES Game");
            System.out.println("invoked");

            //Create a new navigation controller with the frame and call the main menu
            NavigationController navigationController = new NavigationController(frame);
            System.out.println("Successfully created navigation menu");
            navigationController.showMainMenu();

            frame.setVisible(true);
        });
    }
}
//comment for testing