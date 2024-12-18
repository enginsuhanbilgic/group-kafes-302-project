package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.ui.*;
import javax.swing.*;
import java.awt.*;

/**
 * The NavigationController class controls View switching.
 * It uses Card Layout to switch between different views easily.
 */
public class NavigationController {
    private final JFrame frame;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    
    // View identifiers for CardLayout
    private static final String MAIN_MENU = "MainMenu";
    private static final String BUILD_MODE = "BuildMode";
    private static final String HELP_MENU = "HelpMenu";
    private static final String PLAY_MODE = "PlayMode";

    private PlayModeView playModeView;

    /**
     * @param frame
     * 
     * Initiate a card layout and populate it with the views the program has.
     * Then populate the frame with this new JPanel which uses Card Layout
     */
    public NavigationController(JFrame frame) {
        this.frame = frame;
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

        // Initialize views
        cardPanel.add(new MainMenuView(this), MAIN_MENU);
        cardPanel.add(new BuildModeView(this), BUILD_MODE);
        cardPanel.add(new HelpMenuView(this), HELP_MENU);

        playModeView = new PlayModeView(this);
        cardPanel.add(playModeView, PLAY_MODE);

        this.frame.getContentPane().add(cardPanel, BorderLayout.CENTER);
    }

    // Navigation methods
    public void showMainMenu() {
        cardLayout.show(cardPanel, MAIN_MENU);
    }

    public void showBuildMode() {
        cardLayout.show(cardPanel, BUILD_MODE);
    }

    public void showHelpMenu() {
        cardLayout.show(cardPanel, HELP_MENU);
    }

    //Start the game thread and then switch to Play Mode View
    public void showPlayMode() {
        playModeView.startGameThread();
        cardLayout.show(cardPanel, PLAY_MODE);
        playModeView.requestFocusInWindow();
    }
}