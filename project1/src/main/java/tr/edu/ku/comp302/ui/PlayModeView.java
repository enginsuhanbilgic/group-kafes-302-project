package tr.edu.ku.comp302.ui;

import tr.edu.ku.comp302.domain.controllers.KeyHandler;
import tr.edu.ku.comp302.domain.controllers.NavigationController;
import tr.edu.ku.comp302.domain.controllers.PlayModeController;

import javax.swing.*;
import java.awt.*;

/**
 * PlayModeView renders the Play Mode screen and handles the game loop.
 * It interacts only with the PlayModeController.
 */
public class PlayModeView extends JPanel implements Runnable {

    private Thread gameThread;
    private final PlayModeController playModeController;
    private final KeyHandler keyHandler;

    /**
     * Constructor: Initializes the PlayModeView.
     *
     * @param navControl NavigationController for managing views.
     */
    public PlayModeView(NavigationController navControl) {
        this.setDoubleBuffered(true);

        // Initialize KeyHandler and PlayModeController
        keyHandler = new KeyHandler();
        playModeController = new PlayModeController();

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
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    /**
     * The game loop: Handles updates and rendering at 60 FPS.
     */
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double drawInterval = 1000000000.0 / 60.0;
        double delta = 0;

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    /**
     * Updates game logic via PlayModeController.
     */
    public void update() {
        playModeController.update(keyHandler);
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
