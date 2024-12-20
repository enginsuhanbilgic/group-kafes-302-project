package tr.edu.ku.comp302.domain.controllers;

import java.awt.*;
import java.util.function.Consumer;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.GameTimer;

/**
 * PlayModeController manages the game logic and interactions between components.
 */
public class PlayModeController {
    
    private final PlayerController playerController;
    private final TilesController tilesController;
    private final KeyHandler keyHandler;

    // Timer via GameTimerController
    private GameTimerController gameTimerController;
    private int initialTime = 10; // initial countdown time
    private NavigationController navigationController;
    private int currentTime;
    //end of timer

    /**
     * Constructs the PlayModeController using global GameConfig settings.
     */
    public PlayModeController(KeyHandler keyHandler) {
        //Initialize Key Handler
        this.keyHandler = keyHandler;

        // Initialize Player and PlayerController
        Player player = new Player(GameConfig.PLAYER_START_X, GameConfig.PLAYER_START_Y, GameConfig.PLAYER_SPEED);
        this.playerController = new PlayerController(player);

        // Initialize TilesController with grid dimensions and tile size
        this.tilesController = new TilesController();

        // Load tile data into the controller
        this.tilesController.loadTiles(2, 2);
    }

    /**
     * Updates the game logic, such as player movement.
     *
     * @param keyHandler The KeyHandler for user input.
     */
    public void update() {
        if(!keyHandler.isEscPressed()){
            playerController.updatePlayerPosition(keyHandler);
        }
    }

    /**
     * Draws the game components (tiles and player).
     *
     * @param g2 Graphics2D object for rendering.
     */
    public void draw(Graphics2D g2) {
        tilesController.draw(g2);
        playerController.draw(g2);
    }

    public boolean isPaused() {
        return keyHandler.isEscPressed();
    }

    //------
    //timer--
    /**
     * Starts the game timer.
     */
    public void startGameTimer(Consumer<Integer> onTick, Runnable onTimeUp) {
        gameTimerController = new GameTimerController(
        time -> {
            currentTime = time; // store the current time for later use
            onTick.accept(time);
        },
        onTimeUp
        );
        gameTimerController.start(initialTime);
    }
    
    private void onTimeUp() {
        System.out.println("Süre doldu! Oyun bitti.");
        if (navigationController != null) {
            navigationController.endGameAndShowMainMenu();
        } else {
            System.out.println("NavigationController atanmamış!");
        }
    }

    public void pauseGameTimer() {
        if (gameTimerController != null) {
            gameTimerController.pause();
        }
        keyHandler.resetKeys();
        System.out.println("Timer is paused");
        System.out.println("Remaining time:" + gameTimerController.getTimeRemaining());
    }
    
    public void resumeGameTimer() {
        if (gameTimerController != null) {
            gameTimerController.resume();
        }
        System.out.println("Timer is resumed");
    }

    public void setNavigationController(NavigationController navigationController) {
        this.navigationController = navigationController;
    }

    public int getCurrentTime() {
        return currentTime;
    }

}
