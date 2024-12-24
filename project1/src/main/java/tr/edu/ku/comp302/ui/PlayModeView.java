package tr.edu.ku.comp302.ui;

import tr.edu.ku.comp302.domain.controllers.KeyHandler;
import tr.edu.ku.comp302.domain.controllers.NavigationController;
import tr.edu.ku.comp302.domain.controllers.PlayModeController;

import javax.swing.*;
import java.awt.*;

public class PlayModeView extends JPanel implements Runnable {

    private Thread gameThread;
    private final NavigationController navigationController;
    private final PlayModeController playModeController;
    private final KeyHandler keyHandler;
    private volatile boolean running = true;
    private boolean pauseMenuShown = false;

    // Eski constructor (no JSON)
    public PlayModeView(NavigationController navigationController, JFrame parentFrame) {
        this(navigationController, parentFrame, null);
        // "null" diyerek alt constructor'a yönlendiriyoruz
    }

    /**
     * Yeni constructor: BuildMode’dan gelen JSON data’sını alır.
     */
    public PlayModeView(NavigationController navigationController, JFrame parentFrame, String jsonData) {
        this.setDoubleBuffered(true);
        this.setBackground(new Color(66, 40, 53));

        // Initialize KeyHandler and PlayModeController
        keyHandler = new KeyHandler();
        this.navigationController = navigationController;
        playModeController = new PlayModeController(keyHandler);
        playModeController.setNavigationController(this.navigationController);

        // JSON’dan world objelerini yükleyelim (eğer JSON varsa)
        if(jsonData != null && !jsonData.isEmpty()){
            playModeController.loadWorldFromJson(jsonData);
        }

        // Timer
        playModeController.startGameTimer(
                time -> SwingUtilities.invokeLater(() -> {
                    parentFrame.revalidate();
                    parentFrame.repaint();
                }),
                () -> SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Süre doldu! Oyun bitti.");
                    keyHandler.resetKeys();
                    navigationController.endGameAndShowMainMenu();
                })
        );

        // Input
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

    public void startGameThread() {
        if (gameThread == null || !gameThread.isAlive()) {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    public void stopRunning() {
        running = false;
    }

    public void stopGameThread(){
        running = false;
        gameThread = null;
    }

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
                    if (!pauseMenuShown) {
                        pauseMenuShown = true;
                        showPauseMenu();
                    }
                } else {
                    if(playModeController.getCurrentTime() != 0){
                        update();
                    }
                }
                delta--;
                repaint();
            }
        }
    }

    public void update() {
        playModeController.update();
    }

    private void showPauseMenu() {
        SwingUtilities.invokeLater(() -> {
            playModeController.pauseGameTimer();
            PauseMenuView pauseMenu = new PauseMenuView(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    e -> { // Resume
                        keyHandler.escPressed = !keyHandler.escPressed;
                        pauseMenuShown = false;
                        this.requestFocusInWindow();
                        playModeController.resumeGameTimer();
                    },
                    e -> { // Help
                        pauseMenuShown = true;
                        navigationController.showHelpMenu(evt -> {
                            pauseMenuShown = false;
                            playModeController.resumeGameTimer();
                            keyHandler.escPressed = !keyHandler.escPressed;
                            navigationController.showPlayMode(this);
                            this.requestFocusInWindow();
                        });
                    },
                    e -> { // Return MainMenu
                        keyHandler.resetKeys();
                        pauseMenuShown = false;
                        navigationController.showMainMenu();
                    }
            );
            pauseMenu.setVisible(true);
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        playModeController.draw(g2);

        // Timer in the top-left corner
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Kalan Süre: " + playModeController.getCurrentTime(), 10, 20);

        g2.dispose();
    }

    public KeyHandler getKeyHandler() {
        return keyHandler;
    }
}
