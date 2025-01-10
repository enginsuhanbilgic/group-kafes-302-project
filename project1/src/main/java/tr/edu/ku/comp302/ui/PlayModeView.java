package tr.edu.ku.comp302.ui;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.controllers.KeyHandler;
import tr.edu.ku.comp302.domain.controllers.MouseHandler;
import tr.edu.ku.comp302.domain.controllers.NavigationController;
import tr.edu.ku.comp302.domain.controllers.PlayModeController;
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
                    navigationController.endGameAndShowMainMenu("Game Over! Time is up.");
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

        // Draw damage indicators
        drawDamageIndicators(g2);

        // when lives is down 
        if (playModeController.getPlayerController().getEntity().isDrawDamageBox()) {
            g2.setColor(new Color(255, 0, 0, 120)); 
            g2.fillRect(0, 0, getWidth(), getHeight()); 
        }

        // Timer in the top-left corner
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Time Remaining: " + playModeController.getCurrentTime(), 10, 20);
        g2.drawString("Lives: " + playModeController.getPlayerController().getEntity().getLives(), 10, 46);
        int i = 0;
        g2.drawString("Inventory", 1000, 20);
        int itemsPerRow = 3; // Number of items per row
        int spacing = GameConfig.TILE_SIZE + 10; // Spacing between items (tile size + extra space)

        // Draw Active Enchantments (moved to right side)
        int activeEffectsX = 1200; // Yeni x koordinatı
        g2.drawString("Active Effects:", activeEffectsX, 20);
        if (playModeController.getPlayerController().getEntity().isCloakActive()) {
            g2.drawImage(playModeController.getEnchantmentController().getImage(EnchantmentType.CLOAK_OF_PROTECTION), 
                        activeEffectsX, 30, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
            long remainingCloakTime = (playModeController.getPlayerController().getEntity().getCloakEndTime() - System.currentTimeMillis()) / 1000;
            g2.drawString("Cloak Active (" + remainingCloakTime + "s)", activeEffectsX + GameConfig.TILE_SIZE + 5, 30 + GameConfig.TILE_SIZE/2);
            
            // Draw time bar for Cloak
            int barWidth = 100;
            int barHeight = 5;
            int barX = activeEffectsX + GameConfig.TILE_SIZE + 5;
            int barY = 30 + GameConfig.TILE_SIZE/2 + 8;
            
            // Background (gray bar)
            g2.setColor(new Color(60, 60, 60));
            g2.fillRect(barX, barY, barWidth, barHeight);
            
            // Calculate remaining ratio (20 seconds total for Cloak)
            float remainingRatio = (float)(remainingCloakTime) / 20.0f;
            remainingRatio = Math.max(0, Math.min(1, remainingRatio)); // Clamp between 0 and 1
            
            // Choose color based on remaining time
            Color barColor;
            if (remainingCloakTime <= 3) {
                // Yanıp sönme efekti için zamanı kontrol et
                if (System.currentTimeMillis() % 500 < 250) { // Her 0.5 saniyede bir yanıp sön
                    barColor = Color.RED;
                } else {
                    barColor = new Color(180, 0, 0);
                }
            } else if (remainingCloakTime <= 7) {
                barColor = new Color(255, 165, 0); // Turuncu
            } else {
                barColor = new Color(0, 255, 0); // Yeşil
            }
            
            g2.setColor(barColor);
            g2.fillRect(barX, barY, (int)(barWidth * remainingRatio), barHeight);
        }
        if (playModeController.getPlayerController().getEntity().isRevealActive()) {
            int revealY = playModeController.getPlayerController().getEntity().isCloakActive() ? 
                         30 + GameConfig.TILE_SIZE + 5 : 30;
            g2.drawImage(playModeController.getEnchantmentController().getImage(EnchantmentType.REVEAL), 
                        activeEffectsX, revealY, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
            long remainingRevealTime = (playModeController.getPlayerController().getEntity().getRevealEndTime() - System.currentTimeMillis()) / 1000;
            g2.drawString("Reveal Active (" + remainingRevealTime + "s)", activeEffectsX + GameConfig.TILE_SIZE + 5, revealY + GameConfig.TILE_SIZE/2);
            
            // Draw time bar for Reveal
            int barWidth = 100;
            int barHeight = 5;
            int barX = activeEffectsX + GameConfig.TILE_SIZE + 5;
            int barY = revealY + GameConfig.TILE_SIZE/2 + 8;
            
            // Background (gray bar)
            g2.setColor(new Color(60, 60, 60));
            g2.fillRect(barX, barY, barWidth, barHeight);
            
            // Calculate remaining ratio (10 seconds total for Reveal)
            float remainingRatio = (float)(remainingRevealTime) / 10.0f;
            remainingRatio = Math.max(0, Math.min(1, remainingRatio)); // Clamp between 0 and 1
            
            // Choose color based on remaining time
            Color barColor;
            if (remainingRevealTime <= 3) {
                // Yanıp sönme efekti için zamanı kontrol et
                if (System.currentTimeMillis() % 500 < 250) { // Her 0.5 saniyede bir yanıp sön
                    barColor = Color.RED;
                } else {
                    barColor = new Color(180, 0, 0);
                }
            } else if (remainingRevealTime <= 5) {
                barColor = new Color(255, 165, 0); // Turuncu
            } else {
                barColor = new Color(0, 255, 0); // Yeşil
            }
            
            g2.setColor(barColor);
            g2.fillRect(barX, barY, (int)(barWidth * remainingRatio), barHeight);
        }

        i = 0; // Reset i to 0
        for (Enchantment e : playModeController.getPlayerController().getEntity().getInventory().getAllItems()) {
            int col = i % itemsPerRow; // Determine the column (0, 1, or 2)
            int row = i / itemsPerRow; // Determine the row (increments every 3 items)

            int x = 1000 + col * spacing; // Calculate x position
            int y = 40 + row * spacing;   // Calculate y position (starting below the "Inventory" text)

            switch (e.getType()) {
                case EXTRA_TIME -> {
                    // Draw a blue rectangle
                    g2.setColor(Color.BLUE);
                    g2.fillRect(x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
                }
                case EXTRA_LIFE -> {
                    g2.drawImage(playModeController.getEnchantmentController().getImage(EnchantmentType.EXTRA_LIFE), x, y,
                            GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
                }
                case REVEAL -> {
                    g2.drawImage(playModeController.getEnchantmentController().getImage(EnchantmentType.REVEAL), x, y,
                            GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
                }
                case CLOAK_OF_PROTECTION -> {
                    g2.drawImage(playModeController.getEnchantmentController().getImage(EnchantmentType.CLOAK_OF_PROTECTION), x, y,
                            GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
                }
                case LURING_GEM -> {
                    g2.drawImage(playModeController.getEnchantmentController().getImage(EnchantmentType.LURING_GEM), x, y,
                            GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
                }
            }
            i++;
        }
        g2.drawString(hallType.toString(), 300, 20);
    }

    private void drawDamageIndicators(Graphics2D g2) {
        // Archer monster range indicator
        for (var monster : playModeController.getMonsterController().getMonsters()) {
            if (monster.getType().equals("ARCHER")) {
                int monsterX = monster.getX();
                int monsterY = monster.getY();
                int range = 3 * GameConfig.TILE_SIZE;
                
                // Archer'ın menzilini gösteren yarı saydam mavi daire
                if (!playModeController.getPlayerController().getEntity().isCloakActive()) {
                    // Dış daire (maksimum menzil)
                    g2.setColor(new Color(0, 0, 255, 30)); // Açık mavi, çok saydam
                    g2.fillOval(monsterX - range, 
                               monsterY - range, 
                               range * 2 + GameConfig.TILE_SIZE, 
                               range * 2 + GameConfig.TILE_SIZE);
                    
                    // Menzil sınırını gösteren çizgi
                    g2.setColor(new Color(0, 0, 255, 50));
                    g2.drawOval(monsterX - range, 
                               monsterY - range, 
                               range * 2 + GameConfig.TILE_SIZE, 
                               range * 2 + GameConfig.TILE_SIZE);
                }
            } else if (monster.getType().equals("FIGHTER")) {
                int monsterX = monster.getX();
                int monsterY = monster.getY();
                
                // Fighter'ın yakın mesafe tehlike alanı (yarım kare mesafe)
                if (!playModeController.getPlayerController().getEntity().isCloakActive()) {
                    g2.setColor(new Color(255, 0, 0, 30)); // Kırmızı, çok saydam
                    g2.fillRect(monsterX - GameConfig.TILE_SIZE/2, 
                               monsterY - GameConfig.TILE_SIZE/2,
                               GameConfig.TILE_SIZE * 2, 
                               GameConfig.TILE_SIZE * 2);
                    
                    // Tehlike alanı sınırı
                    g2.setColor(new Color(255, 0, 0, 50));
                    g2.drawRect(monsterX - GameConfig.TILE_SIZE/2, 
                               monsterY - GameConfig.TILE_SIZE/2,
                               GameConfig.TILE_SIZE * 2, 
                               GameConfig.TILE_SIZE * 2);
                }
            }
        }
    }

    public KeyHandler getKeyHandler() {
        return keyHandler;
    }
}
