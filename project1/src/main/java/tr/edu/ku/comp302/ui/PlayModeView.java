package tr.edu.ku.comp302.ui;

import tr.edu.ku.comp302.domain.controllers.KeyHandler;
import tr.edu.ku.comp302.domain.controllers.NavigationController;
import tr.edu.ku.comp302.domain.controllers.PlayModeController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * PlayModeView renders the Play Mode screen and handles the game loop.
 * It interacts only with the PlayModeController.
 */
public class PlayModeView extends JPanel implements Runnable {

    private Thread gameThread;
    private final NavigationController navigationController;
    private final PlayModeController playModeController;
    private final KeyHandler keyHandler;
    private volatile boolean running = true;
    private boolean pauseMenuShown = false;

    /**
     * Constructor: Initializes the PlayModeView.
     *
     * @param navigationController NavigationController for managing views.
     */
    public PlayModeView(NavigationController navigationController, JFrame parentFrame) {
        this.setDoubleBuffered(true);
        this.setBackground(new Color(66, 40, 53));

        // Initialize KeyHandler and PlayModeController
        keyHandler = new KeyHandler();
        this.navigationController = navigationController;
        playModeController = new PlayModeController(keyHandler);

        // Add KeyHandler for input
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
        this.requestFocusInWindow();

        // Regain focus when hierarchy changes
        this.addHierarchyListener(e -> {
            if (isShowing()) {
                this.requestFocusInWindow();
            }
        });
    }

    /**
     * Starts the game thread for the game loop.
     */
    public void startGameThread() {
        if (gameThread == null || !gameThread.isAlive()) {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    /**
     * Stops the game thread.
     */
    public void stopRunning() {
        running = false; // Mark the game as not running
    }

    public void stopGameThread(){
        running = false;
        gameThread = null;
    }

    /**
     * The game loop: Handles updates and rendering at 60 FPS.
     */
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double drawInterval = 1000000000.0 / 60.0;
        double delta = 0;

        while (running) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                if (playModeController.isPaused()) {
                    if (!pauseMenuShown) { // Check if pause menu is already shown
                        pauseMenuShown = true;
                        showPauseMenu(); // Show pause menu on EDT
                    }
                } else {
                    update();
                }
                delta--;
                repaint();
            }
        }
    }

    /**
     * Updates game logic via PlayModeController.
     */
    public void update() {
        playModeController.update();
    }

    private void showPauseMenu() {
        SwingUtilities.invokeLater(() -> {
            PauseMenuView pauseMenu = new PauseMenuView(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                e -> { // Resume Action
                    keyHandler.escPressed = !keyHandler.escPressed; // Toggle pause state
                    pauseMenuShown = false; // Reset flag
                    this.requestFocusInWindow();
                },
                e -> { // Help Menu Action
                    keyHandler.escPressed = !keyHandler.escPressed; // Toggle pause state
                    pauseMenuShown = false; // Reset flag
                    navigationController.showHelpMenu();
                },
                e -> { // Return to Main Menu Action
                    keyHandler.escPressed = !keyHandler.escPressed; // Toggle pause state
                    pauseMenuShown = false; // Reset flag
                    navigationController.showMainMenu();
                }
            );
            pauseMenu.setVisible(true);
        });
    }

    /**
     * Renders game components via PlayModeController.
     *
     * @param g Graphics object for rendering.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        playModeController.draw(g2);

        g2.dispose();
    }
}
