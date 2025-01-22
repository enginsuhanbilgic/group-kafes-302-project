package tr.edu.ku.comp302.ui;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.controllers.*;
import tr.edu.ku.comp302.domain.models.BuildObject;
import tr.edu.ku.comp302.domain.models.GameState;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.enchantments.Enchantment;
import tr.edu.ku.comp302.domain.models.enchantments.EnchantmentType;

import javax.swing.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PlayModeView extends JPanel implements Runnable {

    private Thread gameThread;
    private final NavigationController navigationController;
    private PlayModeController playModeController = null;
    private final KeyHandler keyHandler;
    private volatile boolean running = true;
    private boolean pauseMenuShown = false;
    private final HallType hallType;
    private final String jsonData;
    private boolean showDamageIndicators = false; // Varsayılan olarak kapalı

    private static final String SAVES_DIRECTORY = "saves";



    // Eski constructor (no JSON)
    public PlayModeView(NavigationController navigationController, JFrame parentFrame, HallType hallType, Player player) {
        this(navigationController, parentFrame, null, hallType, player);
        // "null" diyerek alt constructor'a yönlendiriyoruz
    }

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
        playModeController.setPlayModeView(this);

        // Timer
        playModeController.startTimerForNewGame(
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

    // A NEW constructor that restores directly from a loaded GameState.
    public PlayModeView(NavigationController navigationController, JFrame parentFrame, GameState loadedState, Player player) {
        this.setDoubleBuffered(true);
        this.setBackground(new Color(66, 40, 53));

        this.navigationController = navigationController;
        this.hallType = loadedState.getCurrentHall();
        this.jsonData = exportWorldObjectsMapToJson(loadedState.getWorldObjectsMap()); // Because we are not loading from JSON now

        // Create Key/Mouse handlers
        keyHandler = new KeyHandler();
        MouseHandler mouseHandler = new MouseHandler();
        this.setFocusable(true);

        // We build a normal PlayModeController, but we give it “dummy” or default arguments
        // Then we call restoreFromGameState(...) to apply the loaded snapshot.

        // We need a dummy Player so the constructor won't crash.
        // e.g. new Player(0,0,GameConfig.PLAYER_SPEED)

        this.playModeController = new PlayModeController(keyHandler, mouseHandler, /*json*/ null, this.hallType, player);
        this.playModeController.setNavigationController(this.navigationController);
        this.playModeController.setPlayModeView(this);

        // Now actually restore data from loadedState:
        this.playModeController.restoreFromGameState(loadedState);

        // Start the timer so the game picks up from timeRemaining
        this.playModeController.startTimerForLoadedGame(
                time -> SwingUtilities.invokeLater(() -> {
                    parentFrame.revalidate();
                    parentFrame.repaint();
                }),
                () -> SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Time is up!");
                    keyHandler.resetKeys();
                    navigationController.endGameAndShowMainMenu("Game Over! Time is up.");
                })
        );

        // Register input listeners
        this.addKeyListener(keyHandler);
        this.addMouseListener(mouseHandler);

        // Kick off the game loop
        startGameThread();
    }

    public String exportWorldObjectsMapToJson(Map<HallType, List<BuildObject>> hallMap) {
        // Convert HallType -> List<BuildObject> 
        // into a Map<String, List<BuildObject>> so that 
        // keys become "EARTH", "AIR", etc. in the JSON.
        Map<String, List<BuildObject>> rawMap = new HashMap<>();

        for (Map.Entry<HallType, List<BuildObject>> entry : hallMap.entrySet()) {
            HallType hall = entry.getKey();
            List<BuildObject> objects = entry.getValue();
            rawMap.put(hall.name(), objects); // name() => "EARTH", "AIR", "WATER", "FIRE"
        }

        // Use Gson with pretty-print:
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(rawMap);
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
                    if(playModeController.getTimeRemaining() != 0){
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
                    e->{
                        int dialogButton = JOptionPane.YES_NO_OPTION;
                        int result = JOptionPane.showConfirmDialog(this, "Do you really want to save and exit the current game session?", "Warning", dialogButton);
                        if(result == JOptionPane.YES_OPTION){
                            boolean isSuccessful = false;
                            try {
                                // 1) Create a snapshot of the current game
                                GameState gs = playModeController.createGameState();

                                // 2) Prompt for a save name, or you could auto-generate one
                                String saveName = JOptionPane.showInputDialog(
                                        this,
                                        "Enter a name for your save file (without extension):",
                                        "Save Current Game",
                                        JOptionPane.PLAIN_MESSAGE
                                );
                                if (saveName == null || saveName.isBlank()) {
                                    JOptionPane.showMessageDialog(this,
                                            "Save cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
                                    // Return to game
                                    keyHandler.escPressed = !keyHandler.escPressed;
                                    pauseMenuShown = false;
                                    this.requestFocusInWindow();
                                    playModeController.resumeGameTimer();
                                    return;
                                }

                                // 3) Use SaveLoadController
                                isSuccessful = SaveLoadController.saveGame(gs, saveName.trim());

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            //
                            //
                            //
                            //  TO DO: IMPLEMENT SAVE FUCNTIONALITY HERE
                            //  Directory saves is different from design_saves
                            //
                            //
                            //

                            if(isSuccessful){
                                JOptionPane.showMessageDialog(this, "Successfully saved!");
                                keyHandler.resetKeys();
                                pauseMenuShown = false;
                                navigationController.showMainMenu();
                            } else{
                                JOptionPane.showMessageDialog(this, "A problem occured while saving!");
                                keyHandler.escPressed = !keyHandler.escPressed;
                                pauseMenuShown = false;
                                this.requestFocusInWindow();
                                playModeController.resumeGameTimer();
                            }
                        } else{
                            keyHandler.escPressed = !keyHandler.escPressed;
                            pauseMenuShown = false;
                            this.requestFocusInWindow();
                            playModeController.resumeGameTimer();
                        }
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

        // when lives is down 
        if (playModeController.getPlayerController().getEntity().isDrawDamageBox()) {
            g2.setColor(new Color(255, 0, 0, 120)); 
            g2.fillRect(0, 0, getWidth(), getHeight()); 
        }

        // Timer in the top-left corner
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Time Remaining: " + playModeController.getTimeRemaining(), 10, 20);
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
                    //Extra time is not added to inventory
                }
                case EXTRA_LIFE -> {
                    //Extra life is not added to inventory
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
        g2.setColor(Color.WHITE);
        g2.drawString(hallType.toText(), 300, 20);

        // Draw damage indicators
        drawDamageIndicators(g2);
    }

    private void drawDamageIndicators(Graphics2D g2) {
        if (!showDamageIndicators) return; // Eğer kapalıysa hiç çizme
        
        // Orijinal çizim kalitesini sakla
        Object originalHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Composite originalComposite = g2.getComposite();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (var monster : playModeController.getMonsterController().getMonsters()) {
            if (monster.getType().equals("ARCHER")) {
                int monsterX = monster.getX();
                int monsterY = monster.getY();
                
                if (!playModeController.getPlayerController().getEntity().isCloakActive()) {
                    int range = 3 * GameConfig.TILE_SIZE;
                    int centerX = monsterX + GameConfig.TILE_SIZE/2;
                    int centerY = monsterY + GameConfig.TILE_SIZE/2;
                    
                    // Dış daire (turkuaz)
                    g2.setColor(new Color(0, 255, 255, 10));
                    g2.fillOval(centerX - range, centerY - range, range * 2, range * 2);
                    
                    // İç daire (açık mavi)
                    int innerRange = (int)(range * 0.7);
                    g2.setColor(new Color(135, 206, 235, 15));
                    g2.fillOval(centerX - innerRange, centerY - innerRange, innerRange * 2, innerRange * 2);
                }
            } else if (monster.getType().equals("FIGHTER")) {
                int monsterX = monster.getX();
                int monsterY = monster.getY();
                
                int size = GameConfig.TILE_SIZE;
                int[][] directions = {{0,-1}, {0,1}, {-1,0}, {1,0}}; // üst, alt, sol, sağ
                
                int chaseRange = GameConfig.FIGHTER_CHASE_DISTANCE * GameConfig.TILE_SIZE;
                int centerX = monsterX + GameConfig.TILE_SIZE/2;
                int centerY = monsterY + GameConfig.TILE_SIZE/2;

                // Draw the chase distance
                g2.setColor(new Color(255, 165, 0, 40));
                g2.fillOval(centerX - chaseRange, centerY - chaseRange, chaseRange * 2, chaseRange * 2);

                // Mercan kırmızısı kareler
                g2.setColor(new Color(255, 99, 71, 20));
                for (int[] dir : directions) {
                    int x = monsterX + dir[0] * size;
                    int y = monsterY + dir[1] * size;
                    g2.fillRect(x, y, size, size);
                    
                    // Hafif gradient efekti
                    g2.setColor(new Color(250, 128, 114, 12));
                    g2.fillRect(x + size/4, y + size/4, size/2, size/2);
                }
            }
        }
        
        // Orijinal çizim ayarlarını geri yükle
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, originalHint);
        g2.setComposite(originalComposite);
    }

    public KeyHandler getKeyHandler() {
        return keyHandler;
    }

    public void toggleDamageIndicators() {
        showDamageIndicators = !showDamageIndicators;
    }

    public boolean isShowingDamageIndicators() {
        return showDamageIndicators;
    }
}