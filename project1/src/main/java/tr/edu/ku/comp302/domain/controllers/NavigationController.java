package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.ui.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The NavigationController class controls View switching.
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

    public NavigationController(JFrame frame) {
        this.frame = frame;
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);
        this.playModeView = null;

        // Initialize views
        cardPanel.add(new MainMenuView(this), MAIN_MENU);

        // BuildModeView ekleniyor (daha sonra da yaratabiliriz, ama basitlik için direkt ekliyoruz)
        cardPanel.add(new BuildModeView(this), BUILD_MODE);

        this.frame.getContentPane().add(cardPanel, BorderLayout.CENTER);
    }

    // Navigation methods
    public void showMainMenu() {
        if (playModeView != null) {
            playModeView.stopGameThread();
            cardPanel.remove(playModeView);
            playModeView = null;
        }
        cardLayout.show(cardPanel, MAIN_MENU);
    }

    public void showBuildMode() {
        if (playModeView != null) {
            playModeView.stopGameThread();
            cardPanel.remove(playModeView);
            playModeView = null;
        }
        BuildModeView buildModeView = new BuildModeView(this);
        cardPanel.add(buildModeView, BUILD_MODE);
        cardLayout.show(cardPanel, BUILD_MODE);
    }

    public void showHelpMenu(ActionListener onBack) {
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

    public void showPlayMode(PlayModeView playModeView2) {
        this.playModeView = playModeView2;
        this.playModeView.startGameThread();
        cardLayout.show(cardPanel, PLAY_MODE);
        this.playModeView.requestFocusInWindow();
    }

    /**
     * Start a new PlayMode with default (no JSON).
     */
    public void startNewPlayMode(){
        if(this.playModeView != null){
            playModeView.stopGameThread();
            cardPanel.remove(playModeView);
        }
        PlayModeView playModeView2 = new PlayModeView(this, frame);
        cardPanel.add(playModeView2, PLAY_MODE);
        showPlayMode(playModeView2);
    }

    /**
     * BuildMode tamamlanınca, kaydedilen JSON dosyasından
     * ya da stringden PlayMode'u başlatmak için yeni bir metot.
     */
    public void startNewPlayModeFromJson(String jsonFilePath, HallType halltType) {
        if(this.playModeView != null){
            playModeView.stopGameThread();
            cardPanel.remove(playModeView);
        }
        // Okuyup bir string haline getirelim
        String jsonData = "";
        try {
            jsonData = new String(Files.readAllBytes(Paths.get(jsonFilePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Yeni bir PlayModeView, JSON parametresi ile
        PlayModeView playModeView2 = new PlayModeView(this, frame, jsonData, halltType);
        cardPanel.add(playModeView2, PLAY_MODE);
        showPlayMode(playModeView2);
    }

    // end game to do's when time is up
    public void endGameAndShowMainMenu() {
        showMainMenu();
        if (playModeView != null) {
            playModeView.stopGameThread();
            playModeView.getKeyHandler().resetKeys();
            cardPanel.remove(playModeView);
            playModeView = null;
        }

        JOptionPane.showMessageDialog(frame, "Try again next time.");
    }
}
