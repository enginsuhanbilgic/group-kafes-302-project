package tr.edu.ku.comp302.ui;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.controllers.KeyHandler;
import tr.edu.ku.comp302.domain.controllers.MouseHandler;
import tr.edu.ku.comp302.domain.controllers.NavigationController;
import tr.edu.ku.comp302.domain.controllers.PlayModeController;
import tr.edu.ku.comp302.domain.controllers.TilesController;
import tr.edu.ku.comp302.domain.models.Enchantments.Enchantment;
import tr.edu.ku.comp302.domain.models.Enchantments.EnchantmentType;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.domain.models.Player;

import javax.swing.*;
import java.awt.*;

public class PlayModeView extends JPanel implements Runnable {

    private Thread gameThread;
    private final NavigationController navigationController;
    private PlayModeController playModeController = null;
    private final KeyHandler keyHandler;
    private volatile boolean running = true;
    private boolean pauseMenuShown = false;
    private final HallType hallType;
    private final String jsonData;
    private int currentFPS = 0;

    // Eski constructor (no JSON)
    public PlayModeView(NavigationController navigationController, JFrame parentFrame, HallType hallType, Player player) {
        this(navigationController, parentFrame, null, hallType, player);
        // "null" diyerek alt constructor'a yönlendiriyoruz
    }
    /* 
    // Daha eski Constructor
    public PlayModeView(NavigationController navigationController, JFrame frame) {
        this(navigationController, frame, null, null);
    }*/

    /**
     * Yeni constructor: BuildMode’dan gelen JSON data’sını alır.
     */
    public PlayModeView(NavigationController navigationController, JFrame parentFrame, String jsonData, HallType hallType, Player player) {
        this.setDoubleBuffered(true);
        this.setBackground(new Color(66, 40, 53));
        this.hallType = hallType;
        this.jsonData = jsonData;

        // Initialize KeyHandler and PlayModeController
        keyHandler = new KeyHandler();
        MouseHandler mouseHandler = new MouseHandler();
        
        this.navigationController = navigationController;
        playModeController = new PlayModeController(keyHandler, mouseHandler, jsonData, hallType, player);
        playModeController.setNavigationController(this.navigationController);

        // Timer
        playModeController.startGameTimer(
                time -> SwingUtilities.invokeLater(() -> {
                    parentFrame.revalidate();
                    parentFrame.repaint();
                    /* 
                    Component[] allPanels = navigationController.getAllPanelsInCardLayout();
                    System.out.println("All Panels:");
                    for (Component panel : allPanels) {
                        System.out.println(panel.getClass().getName());
                    }
                    System.out.println();
                    System.out.println();
                    */
                }),
                () -> SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Süre doldu! Oyun bitti.");
                    keyHandler.resetKeys();
                    navigationController.endGameAndShowMainMenu();
                })
        );

        // Input
        this.addKeyListener(keyHandler);
        this.addMouseListener(mouseHandler);
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
        int frames = 0;
        long timer = System.currentTimeMillis();

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
                repaint();
                frames++;
                delta--;
            }

            // FPS sayacı ve performans optimizasyonu
            if (System.currentTimeMillis() - timer > 1000) {
                currentFPS = frames;
                System.out.println("FPS: " + frames);
                frames = 0;
                timer = System.currentTimeMillis();
                System.gc(); // Her saniyede bir çöp toplama işlemi
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
        
        // Grafik optimizasyonları
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        
        playModeController.draw(g2);

        // Timer in the top-left corner
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Time Remaining: " + playModeController.getCurrentTime(), 10, 20);
        g2.drawString("Lives: " + playModeController.getPlayerController().getEntity().getLives(), 10, 46);
        
        // FPS bilgisini sağ üst köşeye çiz
        g2.drawString("FPS: " + currentFPS, getWidth() - 100, 20);
        
        // Inventory çizimi
        drawInventory(g2);
        
        g2.drawString(hallType.toString(), 300, 20);
        g2.dispose();
    }

    private void drawInventory(Graphics2D g2) {
        g2.drawString("Inventory", 1000, 20);
        int itemsPerRow = 3;
        int spacing = GameConfig.TILE_SIZE + 10;
        int i = 0;
        
        for (Enchantment e : playModeController.getPlayerController().getEntity().getInventory().getAllItems()) {
            int col = i % itemsPerRow;
            int row = i / itemsPerRow;
            int x = 1000 + col * spacing;
            int y = 40 + row * spacing;
            
            drawEnchantmentItem(g2, e, x, y);
            i++;
        }
    }

    private void drawEnchantmentItem(Graphics2D g2, Enchantment e, int x, int y) {
        switch (e.getType()) {
            case EXTRA_TIME -> {
                g2.setColor(Color.BLUE);
                g2.fillRect(x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
            }
            case EXTRA_LIFE -> g2.drawImage(playModeController.getEnchantmentController().getImage(EnchantmentType.EXTRA_LIFE), 
                x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
            case REVEAL -> g2.drawImage(playModeController.getEnchantmentController().getImage(EnchantmentType.REVEAL), 
                x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
            case CLOAK_OF_PROTECTION -> g2.drawImage(playModeController.getEnchantmentController().getImage(EnchantmentType.CLOAK_OF_PROTECTION), 
                x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
            case LURING_GEM -> g2.drawImage(playModeController.getEnchantmentController().getImage(EnchantmentType.LURING_GEM), 
                x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
        }
    }

    public KeyHandler getKeyHandler() {
        return keyHandler;
    }
}
