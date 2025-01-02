package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.BuildObject;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.Tile;
import tr.edu.ku.comp302.domain.models.Enchantments.Enchantment;
import tr.edu.ku.comp302.domain.models.Enchantments.EnchantmentType;

import java.awt.*;
import java.util.Random;
import java.util.function.Consumer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * PlayModeController manages the game logic and interactions between components.
 */
public class PlayModeController {

    private final Player player;
    private final PlayerController playerController;
    private final TilesController tilesController;
    private NavigationController navigationController;
    private MonsterController monsterController;
    private EnchantmentController enchantmentController;
    private BuildObjectController buildObjectController;
    private final KeyHandler keyHandler;
    private final HallType hallType;
    private final String jsonData;
    private final MouseHandler mouseHandler;
    

    // Timer via GameTimerController
    private GameTimerController gameTimerController;
    private int initialTime = 60;
    private int currentTime;
    private int inGameTime = 0; // Our "game clock" in seconds

    private boolean gameOver = false;

    public PlayModeController(KeyHandler keyHandler, MouseHandler mouseHandler, String jsonData, HallType hallType, Player player) {
        this.keyHandler = keyHandler;
        this.hallType = hallType;
        this.jsonData = jsonData;
        this.mouseHandler = mouseHandler;
        this.player = player;
        
        // Initialize TilesController
        this.tilesController = new TilesController();
        this.tilesController.loadTiles();

        this.buildObjectController = new BuildObjectController(hallType);
        this.buildObjectController.loadWorldFromJson(jsonData);

        buildObjectController.getWorldObjectsMap()
            .forEach((h, list) -> {
                if(h==hallType){
                    for (BuildObject obj : list) {
                        tilesController.setTransparentTileAt(obj.getX(), obj.getY());
                    }
                }
            });
        
        // Initialize PlayerController
        this.playerController = new PlayerController(player, this.tilesController, this.keyHandler);

        this.monsterController = new MonsterController(this.tilesController, buildObjectController);
        this.enchantmentController = new EnchantmentController(this.tilesController);
        //Very bad solution
        monsterController.setEnchantmentController(enchantmentController);

        initializePlayerLocation();
    }  
    
    public void initializePlayerLocation(){
        int tileSize = GameConfig.TILE_SIZE;
        int mapWidth = GameConfig.NUM_HALL_COLS;
        int mapHeight = GameConfig.NUM_HALL_ROWS;
        Random random = new Random();

        // We'll try a few times to find a free tile
        for (int attempt = 0; attempt < 50; attempt++) {
            int col = random.nextInt(mapWidth);
            int row = random.nextInt(mapHeight);

            Tile tile = tilesController.getTileAt(col+GameConfig.KAFES_STARTING_X, row+GameConfig.KAFES_STARTING_Y);
            if (tile != null && !tile.isCollidable && enchantmentController.isLocationAvailable(col, row)) {
                //
                playerController.setLocation((col + GameConfig.KAFES_STARTING_X) * tileSize, (row + GameConfig.KAFES_STARTING_Y) * tileSize);
            } else{
                //System.out.println("Unsuccesful location: " + col + " " + row);
            }
        }
    }


    public void update() {
        if (gameOver) return;
        if (!keyHandler.isEscPressed()) {
            // 1) Update Player
            playerController.update();

            // 2) Update Monsters
            monsterController.updateAll(playerController.getEntity(), inGameTime);

            Point clickPos = mouseHandler.getLastClickAndConsume();
            // 3) Update Enchantments
            enchantmentController.update(playerController.getEntity(), clickPos);

            // 4) Let BuildObjectController do any per-frame logic
            buildObjectController.update(hallType, playerController.getEntity(), clickPos);

            // 5) Check enchantment usage
            checkEnchantmentUsage();

            // 6) Any bonus time?
            int bonusTime = playerController.getEntity().consumeBonusTime();
            if (bonusTime > 0 && gameTimerController != null) {
                gameTimerController.addTime(bonusTime);
            }

            // 7) Check hero’s lives
            if (playerController.getEntity().getLives() <= 0) {
                onGameOver();
            }

            // 8) Check if rune is collected
            if (playerController.getEntity().getInventory().hasRune()){
                onGameComplete();
            }
        }
    }

    public void draw(Graphics2D g2) {
        // 1) Draw tiles
        tilesController.draw(g2);

        // 2) Draw build objects
        buildObjectController.draw(g2);

        // 3) Draw monsters
        monsterController.drawAll(g2);

        // 4) Draw enchantments
        enchantmentController.draw(g2);

        // 5) Draw player
        playerController.draw(g2);

        

        // If player has reveal active, you might highlight a 4x4 area around the rune
        if (playerController.getEntity().isRevealActive() && buildObjectController.getRuneHolder()!=null) {
            // For demonstration, let's just draw a red rectangle somewhere
            // e.g., if your game tracks the rune location as (runeX, runeY) in TilesController
            // we do a simple 4x4 highlight
            
            //Point runeTile = tilesController.getRuneTile(); // hypothetical
            //if (runeTile != null) {
                int rectX = buildObjectController.getRuneHolder().getX() * GameConfig.TILE_SIZE - GameConfig.TILE_SIZE;
                int rectY = buildObjectController.getRuneHolder().getY() * GameConfig.TILE_SIZE - GameConfig.TILE_SIZE;
                int rectWidth = 4 * GameConfig.TILE_SIZE;
                int rectHeight = 4 * GameConfig.TILE_SIZE;

                g2.setColor(new Color(255, 0, 0, 80));
                g2.fillRect(rectX, rectY, rectWidth, rectHeight);
            //}
        }
    }

    private void checkEnchantmentUsage(){
        if (keyHandler.isRPressed()) {
            playerController.getEntity().useReveal();
            keyHandler.r = false; // reset
        }
        if (keyHandler.isPPressed()) {
            playerController.getEntity().useCloakOfProtection();
            keyHandler.p = false;
        }
        // For Luring Gem: we press B, then a direction:
        if (keyHandler.isBPressed() && buildObjectController.getRuneHolder()!=null) {
            char direction = ' ';
            if (keyHandler.up) direction = 'W';
            else if (keyHandler.down) direction = 'S';
            else if (keyHandler.left) direction = 'A';
            else if (keyHandler.right) direction = 'D';
            if (direction != ' ') {
                playerController.getEntity().useLuringGem(direction);
                
                keyHandler.b = false;
                keyHandler.up = keyHandler.down = keyHandler.left = keyHandler.right = false;
            }
        }
    }

    private void onGameOver() {
        gameOver = true;
        pauseGameTimer();
        System.out.println("Game Over! The hero has no more lives.");
        if (navigationController != null) {
            navigationController.endGameAndShowMainMenu("Game Over! The hero has no more lives.");
        }
    }

    private void onGameComplete() {
        gameOver = true;
        pauseGameTimer();
        SwingUtilities.invokeLater(() -> {
        // Create a dialog with a message and button
        int result = JOptionPane.showOptionDialog(
            null,
            "Hall is completed! Ready for the next challenge?",
            "Game Completed",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            new String[]{"Next"}, // Button text
            "Next"
        );

        // Handle button click
        if (result == JOptionPane.OK_OPTION) {
            switch (hallType) {
                case EARTH:
                    // Navigate to AIR hall
                    Enchantment e1 = player.getInventory().getEnchantmentByType(EnchantmentType.RUNE);
                    player.getInventory().removeItem(e1);
                    player.resetEffects();
                    navigationController.startNewPlayModeFromJson(jsonData, HallType.AIR);
                    break;
                case AIR:
                    // Navigate to WATER hall
                    Enchantment e2 = player.getInventory().getEnchantmentByType(EnchantmentType.RUNE);
                    player.getInventory().removeItem(e2);
                    player.resetEffects();
                    navigationController.startNewPlayModeFromJson(jsonData, HallType.WATER);
                    break;
                case WATER:
                    // Navigate to FIRE hall
                    Enchantment e3 = player.getInventory().getEnchantmentByType(EnchantmentType.RUNE);
                    player.getInventory().removeItem(e3);
                    player.resetEffects();
                    navigationController.startNewPlayModeFromJson(jsonData, HallType.FIRE);
                    break;
                case FIRE:
                default:
                    // End the game and show the main menu
                    Enchantment e4 = player.getInventory().getEnchantmentByType(EnchantmentType.RUNE);
                    player.getInventory().removeItem(e4);
                    player.resetEffects();
                    navigationController.endGameAndShowMainMenu("Congratulations! You have completed the game.");
                    break;
            }
        }
    });
    }

    public boolean isPaused() {
        return keyHandler.isEscPressed();
    }

    // ========== TIMER ==========
    public void startGameTimer(Consumer<Integer> onTick, Runnable onTimeUp) {
        gameTimerController = new GameTimerController(
                time -> {
                    currentTime = time;

                    inGameTime++;

                    monsterController.tick(inGameTime);
                    enchantmentController.tick(inGameTime);
                    onTick.accept(time);
                    },
                onTimeUp
        );
        gameTimerController.start(initialTime);
    }

    private void onTimeUp() {
        System.out.println("Süre doldu! Oyun bitti.");
        if (navigationController != null) {
            navigationController.endGameAndShowMainMenu("Game Over! Time is up.");
        } else {
            System.out.println("NavigationController atanmamış!");
        }
    }

    public void pauseGameTimer() {
        if (gameTimerController != null) {
            gameTimerController.pause();
        }
        keyHandler.resetKeys();
    }

    public void resumeGameTimer() {
        if (gameTimerController != null) {
            gameTimerController.resume();
        }
    }

    public void setNavigationController(NavigationController navigationController) {
        this.navigationController = navigationController;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public PlayerController getPlayerController() {
        return this.playerController;
    }

    public MonsterController getMonsterController() {
        return this.monsterController;
    }

    public EnchantmentController getEnchantmentController(){
        return this.enchantmentController;
    }
}
