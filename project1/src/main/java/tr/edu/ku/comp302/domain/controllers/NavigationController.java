package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.GameState;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.Tile;
import tr.edu.ku.comp302.ui.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Random;

/**
 * The NavigationController class controls View switching.
 */
public class NavigationController {
    private final JFrame frame;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private Player player;

    // View identifiers for CardLayout
    private static final String MAIN_MENU = "MainMenu";
    private static final String BUILD_MODE = "BuildMode";
    private static final String HELP_MENU = "HelpMenu";
    private static final String PLAY_MODE = "PlayMode";

    private PlayModeView playModeView;
    private BuildModeView buildModeView;

    public NavigationController(JFrame frame) {
        this.frame = frame;
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);
        this.playModeView = null;
        this.buildModeView = null;
        this.player = createNewPlayer();

        // Initialize views
        cardPanel.add(new MainMenuView(this), MAIN_MENU);

        // BuildModeView ekleniyor (daha sonra da yaratabiliriz, ama basitlik için direkt ekliyoruz)
        cardPanel.add(new BuildModeView(this), BUILD_MODE);

        this.frame.getContentPane().add(cardPanel, BorderLayout.CENTER);
    }

    /**
     * REQUIRES:
     *  - The TilesController must have loaded tile information before this method is called.
     *  - The area defined by the cage boundaries must have at least one non-collidable tile.
     *
     * MODIFIES:
     *  - Random object is used to generate position, but does not modify external state.
     *  - Internally reads tile information from TilesController to verify collidability.
     *
     * EFFECTS:
     *  - Repeatedly attempts to find a tile (x,y) such that:
     *      - x is in the horizontal range [ (KAFES_STARTING_X + 1)*TILE_SIZE, (KAFES_STARTING_X + NUM_HALL_COLS - 2)*TILE_SIZE ).
     *      - y is in the vertical range [ (KAFES_STARTING_Y + 1)*TILE_SIZE, (KAFES_STARTING_Y + NUM_HALL_ROWS - 2)*TILE_SIZE ).
     *      - The tile at (tileX, tileY) is not collidable.
     *  - Returns a new Player object with the found (x,y) and default PLAYER_SPEED.
     */

    private Player createNewPlayer() {
        Random random = new Random();
        TilesController tilesController = new TilesController();
        tilesController.loadTiles(HallType.DEFAULT);

        // Spawn alanını belirle (duvarların içinde)
        int minX = (GameConfig.KAFES_STARTING_X + 1) * GameConfig.TILE_SIZE;
        int maxX = (GameConfig.KAFES_STARTING_X + GameConfig.NUM_HALL_COLS - 2) * GameConfig.TILE_SIZE;
        int minY = (GameConfig.KAFES_STARTING_Y + 1) * GameConfig.TILE_SIZE;
        int maxY = (GameConfig.KAFES_STARTING_Y + GameConfig.NUM_HALL_ROWS - 2) * GameConfig.TILE_SIZE;

        int x, y;
        boolean validPosition = false;

        // Geçerli bir spawn noktası bulana kadar dene
        do {
            x = minX + random.nextInt(maxX - minX);
            y = minY + random.nextInt(maxY - minY);

            // Tile koordinatlarına çevir
            int tileX = x / GameConfig.TILE_SIZE;
            int tileY = y / GameConfig.TILE_SIZE;

            // Pozisyonun geçerli olup olmadığını kontrol et
            Tile tile = tilesController.getTileAt(tileX, tileY);
            if (tile != null && !tile.isCollidable) {
                validPosition = true;
            }
        } while (!validPosition);

        return new Player(x, y, GameConfig.PLAYER_SPEED);
    }

    // Reset the Player object for a new game
    public void resetPlayer() {
        this.player = createNewPlayer();
    }

    // Navigation methods
    public void showMainMenu() {
        if (playModeView != null) {
            playModeView.stopGameThread();
            cardPanel.remove(playModeView);
            playModeView = null;
        }
        if (buildModeView != null){
            cardPanel.remove(buildModeView);
            buildModeView = null;
        }

        cardLayout.show(cardPanel, MAIN_MENU);
    }

    public void showBuildMode() {
        if (playModeView != null) {
            playModeView.stopGameThread();
            cardPanel.remove(playModeView);
            playModeView = null;
        }
        if (buildModeView != null){
            cardPanel.remove(buildModeView);
            buildModeView = null;
        }
        BuildModeView buildModeView = new BuildModeView(this);
        this.buildModeView = buildModeView;
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

        PlayModeView playModeView2 = new PlayModeView(this, frame, HallType.EARTH, player);
        cardPanel.add(playModeView2, PLAY_MODE);
        showPlayMode(playModeView2);

        if (buildModeView != null){
            cardPanel.remove(buildModeView);
            buildModeView = null;
        }
    }

    /**
     * BuildMode tamamlanınca, kaydedilen JSON dosyasından
     * ya da stringden PlayMode'u başlatmak için yeni bir metot.
     */
    public void startNewPlayModeFromJson(String jsonData, HallType hallType){
        if(this.playModeView != null){
            playModeView.stopGameThread();
            cardPanel.remove(playModeView);
        }

        // Yeni bir PlayModeView, JSON parametresi ile
        PlayModeView playModeView2 = new PlayModeView(this, frame, jsonData, hallType, player);
        cardPanel.add(playModeView2, PLAY_MODE);
        showPlayMode(playModeView2);

        if (buildModeView != null){
            cardPanel.remove(buildModeView);
            buildModeView = null;
        }
    }

    // end game to do's when time is up
    public void endGameAndShowMainMenu(String message) {
        showMainMenu();
        if (playModeView != null) {
            playModeView.stopGameThread();
            playModeView.getKeyHandler().resetKeys();
            cardPanel.remove(playModeView);
            playModeView = null;
        }

        JOptionPane.showMessageDialog(frame, message);
    }

    public Component[] getAllPanelsInCardLayout() {
        return cardPanel.getComponents();
    }

    public void startLoadedGame(GameState gameState) {
        // If a playModeView is already running, remove it
        if (this.playModeView != null) {
            this.playModeView.stopGameThread();
            cardPanel.remove(this.playModeView);
            this.playModeView = null;
        }
        if (buildModeView != null) {
            cardPanel.remove(buildModeView);
            buildModeView = null;
        }

        // Create a new PlayModeView from the loaded state
        PlayModeView pmv = new PlayModeView(this, frame, gameState, player);
        this.playModeView = pmv;

        // Add it to the CardLayout
        cardPanel.add(pmv, PLAY_MODE);    // not "PlayModeLoaded"
        showPlayMode(pmv);               // same name used here

    }
}