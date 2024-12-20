package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.ui.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

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
        this.playModeView = null;

        // Initialize views
        cardPanel.add(new MainMenuView(this), MAIN_MENU);
        cardPanel.add(new BuildModeView(this), BUILD_MODE);

        this.frame.getContentPane().add(cardPanel, BorderLayout.CENTER);
    }

    // Navigation methods
    public void showMainMenu() {
        if (playModeView != null) {
            playModeView.stopGameThread(); // Stop the game thread
            cardPanel.remove(playModeView);
            playModeView = null;
            System.out.println("removed play mode view");
        }
        cardLayout.show(cardPanel, MAIN_MENU);
    }

    public void showBuildMode() {
        if (playModeView != null) {
            playModeView.stopGameThread(); // Stop the game thread
            cardPanel.remove(playModeView);
            playModeView=null;
        }    
        cardLayout.show(cardPanel, BUILD_MODE);        
    }

    public void showHelpMenu(ActionListener onBack) {

        //Set the previous HelpMenuView to null and delete it
        //from the cardPanel to avoid memory leak.
        for(Component comp : cardPanel.getComponents()){
            if (comp instanceof HelpMenuView){
                cardPanel.remove(comp);
                break;
            }
        }

        HelpMenuView helpMenuView = new HelpMenuView(frame, onBack);
        cardPanel.add(helpMenuView, HELP_MENU);
        cardLayout.show(cardPanel, HELP_MENU);
        frame.revalidate();
        frame.repaint();
    }

    //Creates a new PlayModeView object and adds it to the cardPanel
    //Ensures when clicked to start game, game starts from the beginning.
    public void showPlayMode(PlayModeView playModeView2) {
        this.playModeView = playModeView2;
        this.playModeView.startGameThread();
        cardLayout.show(cardPanel, PLAY_MODE);
        this.playModeView.requestFocusInWindow();
    }
    
    public void startNewPlayMode(){
        if(this.playModeView!=null){
            playModeView.stopGameThread();
            cardPanel.remove(playModeView);
        }
        PlayModeView playModeView2 = new PlayModeView(this, frame);
        cardPanel.add(playModeView2, PLAY_MODE);
        showPlayMode(playModeView2);
    }

    // end game to do's when time is up
    public void endGameAndShowMainMenu() {
        if (playModeView != null) {
            playModeView.stopGameThread(); 
            cardPanel.remove(playModeView);
            playModeView = null;
        }
    
        JOptionPane.showMessageDialog(frame, "Time is up! Try again.");
    
        showMainMenu();
    }
}