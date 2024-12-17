package tr.edu.ku.comp302.ui;

import tr.edu.ku.comp302.domain.controllers.KeyHandler;
import tr.edu.ku.comp302.domain.controllers.NavigationController;
import tr.edu.ku.comp302.domain.controllers.PlayModeController;
import tr.edu.ku.comp302.domain.controllers.PlayerController;
import tr.edu.ku.comp302.domain.controllers.TilesController;
import tr.edu.ku.comp302.domain.models.Player;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.*;

/**
 * PlayModeView is responsible for rendering the Play Mode screen,
 * handling game logic updates, and displaying the player, tiles, and game environment.
 * This class runs a game loop to ensure consistent rendering and updating at 60 FPS.
 */
public class PlayModeView extends JPanel implements Runnable{
    
    // Tile and Screen Configurations
    final int originalTileSize = 16;
    final int scale = 3;
    final int tileSize = originalTileSize * scale;
    final int maxScreenCol = 16;
    final int maxScreenRow = 16;

    // Game Thread for running the game loop    
    private Thread gameThread;

    // Game Controllers
    private final TilesController tilesController;
    private final PlayerController playerController;
    private final PlayModeController playModeController;
    private final KeyHandler keyHandler;

    /**
     * Constructor: Initializes the PlayModeView.
     * Sets up controllers, models, and input handling.
     *
     * @param navControl The NavigationController for view transitions.
     */
    public PlayModeView(NavigationController navControl) {
        // Enable double buffering for smooth rendering
        this.setDoubleBuffered(true);

        // Initialize KeyHandler
        keyHandler = new KeyHandler();

        // Initialize models and controllers
        Player player = new Player(100, 100, 4);
        playerController = new PlayerController(player);
        tilesController = new TilesController(maxScreenRow, maxScreenCol, tileSize);
        playModeController = new PlayModeController(playerController, tilesController);

        // Add KeyHandler to listen for keyboard inputs
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
        this.requestFocusInWindow(); // Ensure focus for key events

        // Add a listener to regain focus when the view is displayed
        this.addHierarchyListener(e -> {
            if (isShowing()) {
                this.requestFocusInWindow();
            }
        });
    }

    /**
     * Starts the game thread, which runs the game loop.
     */
    public void startGameThread() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    /**
     * The game loop: Handles updating game logic and repainting the screen at 60 FPS.
     */
    @Override
    public void run() {
        long lastTime = System.nanoTime(); // Önceki zaman
        double drawInterval = 1000000000.0 / 60.0; // 60 FPS için her frame süresi (nanoseconds)
        double delta = 0;
        long currentTime;
        long timer =0;
        int drawCount = 0;

    
        while (gameThread != null) {
            currentTime = System.nanoTime();

            // Calculate delta to determine when to update
            delta += (currentTime - lastTime) / drawInterval; // Delta hesabı
            timer += (currentTime - lastTime);
            lastTime = currentTime;
           
            // Update and repaint when enough time has passed
            if (delta >= 1) {
                update();       // Oyun mantığını güncelle
                repaint(); // Oyunu yeniden çiz
                delta--;
                drawCount++;
            }
            
            // Print FPS every second for debugging purposes
            if(timer >= 1000000000) {
                System.out.println("FPS:"+drawCount);
                drawCount =0;
                timer = 0;
            }
        }
    }

    /**
     * Updates the game logic, such as player movement and tile states.
     */
    public void update() {
        playModeController.update(keyHandler);
    }

    /**
     * Renders the game screen, including tiles and the player.
     *
     * @param g The Graphics object for drawing.
     */
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Delegate drawing to PlayModeController
        playModeController.draw(g2, tileSize);

        g2.dispose(); // Clean up Graphics2D resources
    }
}
