package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.*;
import tr.edu.ku.comp302.domain.models.enchantments.Enchantment;
import tr.edu.ku.comp302.domain.models.enchantments.EnchantmentType;
import tr.edu.ku.comp302.ui.PlayModeView;

import java.awt.*;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    private HallType hallType;
    private String jsonData;
    private final MouseHandler mouseHandler;

    private final Random random;

    // Timer via GameTimerController
    private GameTimerController gameTimerController;
    private int initialTime = 60;
    private int timeRemaining;
    private int timePassed = 0; // Our "game clock" in seconds

    private boolean gameOver = false;

    private PlayModeView playModeView;

    public PlayModeController(KeyHandler keyHandler, MouseHandler mouseHandler, String jsonData, HallType hallType, Player player) {
        this.keyHandler = keyHandler;
        this.hallType = hallType;
        this.jsonData = jsonData;
        this.mouseHandler = mouseHandler;
        this.player = player;
        this.random = new Random();

        // Initialize TilesController
        this.tilesController = new TilesController();
        this.tilesController.loadTiles(hallType);

        this.buildObjectController = new BuildObjectController(hallType);
        this.buildObjectController.loadWorldFromJson(jsonData);


        buildObjectController.getWorldObjectsMap()
                .forEach((h, list) -> {
                    if (h == hallType) {
                        for (BuildObject obj : list) {
                            tilesController.setTransparentTileAt(obj.getX(), obj.getY());
                        }
                    }
                });


        // Initialize PlayerController
        this.playerController = new PlayerController(player, this.tilesController, this.keyHandler);

        this.monsterController = new MonsterController(this.tilesController, buildObjectController, initialTime);
        this.enchantmentController = new EnchantmentController(this.tilesController);
        //Very bad solution
        monsterController.setEnchantmentController(enchantmentController);

        initializePlayerLocation();
    }

    public PlayModeController(KeyHandler keyHandler,
                              MouseHandler mouseHandler,
                              GameState loadedState,
                              Player dummyPlayer) {
        this.keyHandler = keyHandler;
        this.hallType = loadedState.getCurrentHall();
        this.jsonData = null; // not using JSON
        this.mouseHandler = mouseHandler;
        this.player = dummyPlayer;
        this.random = new Random();

        // 1) Tile system
        this.tilesController = new TilesController();
        this.tilesController.loadTiles(HallType.EARTH);

        // 2) BuildObjectController (we do *not* load from JSON, just init the empty map)
        this.buildObjectController = new BuildObjectController(this.hallType);

        // 3) Player, Monster, Enchant
        this.playerController = new PlayerController(dummyPlayer, this.tilesController, this.keyHandler);
        this.monsterController = new MonsterController(this.tilesController, buildObjectController, initialTime);
        this.enchantmentController = new EnchantmentController(this.tilesController);
        monsterController.setEnchantmentController(enchantmentController);

        // 4) We do *not* call initializePlayerLocation() because we want to
        // restore the old player position from .ser
        // We'll do that in restoreFromGameState(...).

        // 5) Actually restore everything
        restoreFromGameState(loadedState);
    }


    public void initializePlayerLocation() {
        int tileSize = GameConfig.TILE_SIZE;
        int mapWidth = GameConfig.NUM_HALL_COLS;
        int mapHeight = GameConfig.NUM_HALL_ROWS;

        // We'll try a few times to find a free tile
        for (int attempt = 0; attempt < 50; attempt++) {
            int col = random.nextInt(mapWidth);
            int row = random.nextInt(mapHeight);

            Tile tile = tilesController.getTileAt(col + GameConfig.KAFES_STARTING_X, row + GameConfig.KAFES_STARTING_Y);
            if (tile != null && !tile.isCollidable && enchantmentController.isLocationAvailable(col, row)) {
                //
                playerController.setLocation((col + GameConfig.KAFES_STARTING_X) * tileSize, (row + GameConfig.KAFES_STARTING_Y) * tileSize);
            } else {
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
            monsterController.updateAll(playerController.getEntity());

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
            if (playerController.getEntity().getInventory().hasRune()) {
                onGameComplete();
            }

            // Hasar göstergeleri için tuş kontrolü
            if (keyHandler.hPressed) {
                keyHandler.hPressed = false; // Tuşu sıfırla
                playModeView.toggleDamageIndicators(); // Göstergeleri aç/kapat
            }
        }
    }

    public void draw(Graphics2D g2) {

        tilesController.draw(g2);

        buildObjectController.draw(g2, false);

        enchantmentController.draw(g2);
        
        playerController.draw(g2);

        monsterController.drawAll(g2);

        tilesController.drawInnerBottom(g2);

        buildObjectController.draw(g2, true);

        // If player has reveal active, you might highlight a 4x4 area around the rune
        if (playerController.getEntity().isRevealActive() && buildObjectController.getRuneHolder() != null) {

            int width = GameConfig.TILE_SIZE * 4;

            int objectX = buildObjectController.getRuneHolder().getX() * GameConfig.TILE_SIZE - buildObjectController.getRuneHolder().getOffset();
            int objectY = buildObjectController.getRuneHolder().getY() * GameConfig.TILE_SIZE - buildObjectController.getRuneHolder().getOffset();

            g2.setColor(new Color(255, 0, 0, 80));
            g2.fillRect(objectX, objectY, width, width);
        }
    }

    private void checkEnchantmentUsage() {
        if (keyHandler.isRPressed()) {
            playerController.getEntity().useReveal();
            keyHandler.r = false; // reset
        }
        if (keyHandler.isPPressed()) {
            playerController.getEntity().useCloakOfProtection();
            keyHandler.p = false;
        }

        // For Luring Gem: press B, then a direction
        if (keyHandler.isBPressed()) {
            char direction = ' ';
            if (keyHandler.up) direction = 'W';
            else if (keyHandler.down) direction = 'S';
            else if (keyHandler.left) direction = 'A';
            else if (keyHandler.right) direction = 'D';

            if (direction != ' ') {
                // This does both: remove gem from inventory, compute location, and inform MonsterController
                playerController.getEntity().useLuringGem(direction, monsterController);

                // reset the keys so we don’t keep throwing repeatedly
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
                    timeRemaining = time;

                    timePassed++;

                    monsterController.tick(timePassed, timeRemaining, player);
                    enchantmentController.tick(timePassed);
                    onTick.accept(time);
                },
                onTimeUp
        );
        gameTimerController.start(initialTime);
    }
    public void startTimerForNewGame(Consumer<Integer> onTick, Runnable onTimeUp) {
        // We want to start from the full initialTime
        gameTimerController = new GameTimerController(
                time -> {
                    timeRemaining = time;
                    timePassed++;
                    monsterController.tick(timePassed, timeRemaining, player);
                    enchantmentController.tick(timePassed);
                    onTick.accept(time);
                },
                onTimeUp
        );
        // Start from initialTime
        gameTimerController.start(initialTime);
    }

    public void startTimerForLoadedGame(Consumer<Integer> onTick, Runnable onTimeUp) {
        // We want to resume from timeRemaining
        gameTimerController = new GameTimerController(
                time -> {
                    timeRemaining = time;
                    timePassed++;
                    monsterController.tick(timePassed, timeRemaining, player);
                    enchantmentController.tick(timePassed);
                    onTick.accept(time);
                },
                onTimeUp
        );
        // Start from leftover timeRemaining
        gameTimerController.start(timeRemaining);
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

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public PlayerController getPlayerController() {
        return this.playerController;
    }

    public MonsterController getMonsterController() {
        return this.monsterController;
    }

    public EnchantmentController getEnchantmentController() {
        return this.enchantmentController;
    }

    public void setPlayModeView(PlayModeView view) {
        this.playModeView = view;
    }

    /**
     * Captures the entire current game state into a serializable object.
     */
    public GameState createGameState() {
        GameState gs = new GameState();
    
        gs.setCurrentHall(this.hallType);
        gs.setPlayer(this.getPlayerController().getEntity());
        gs.setMonsters(monsterController.getMonsters());
        gs.setEnchantments(enchantmentController.getEnchantments());
        gs.setWorldObjectsMap(buildObjectController.getWorldObjectsMap());
    
        gs.setTimeRemaining(this.timeRemaining);
        gs.setTimePassed(this.timePassed);
        gs.setInitialTime(this.initialTime);
    
        gs.setHasLuringGem(monsterController.hasLuringGem());
        gs.setLuringGemLocation(monsterController.getLuringGemLocation());
        gs.setGemSpawnTime(monsterController.getGemSpawnTime());
    
        // Here we get the entire tileDataGrid from TilesController
        TileData[][] td = tilesController.getTileDataGrid();
        gs.setTileDataGrid(cloneTileDataArray(td)); 
        // We do a clone to ensure no accidental references 
        // If your code doesn’t need it, you can store it directly.
    
        return gs;
    }
    
    /** Helper to clone a 2D array so we don't hold references. */
    private TileData[][] cloneTileDataArray(TileData[][] original) {
        int rows = original.length;
        int cols = original[0].length;
        TileData[][] copy = new TileData[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                copy[r][c] = original[r][c];
            }
        }
        return copy;
    }
    
    

    /**
     * Restores the controllers from a previously loaded GameState.
     */
    public void restoreFromGameState(GameState gs) {
        this.hallType = gs.getCurrentHall();
        this.timeRemaining = gs.getTimeRemaining();
        this.timePassed = gs.getTimePassed();
        this.initialTime = gs.getInitialTime();
    
        // Player
        Player loadedPlayer = gs.getPlayer();
        Player current = playerController.getEntity();
        current.setX(loadedPlayer.getX());
        current.setY(loadedPlayer.getY());
        current.setLives(loadedPlayer.getLives());
        current.setVelocityX(loadedPlayer.getVelocityX());
        current.setVelocityY(loadedPlayer.getVelocityY());
        // cloak / reveal times
        current.setCloakActive(loadedPlayer.isCloakActive());
        current.setCloakEndTime(loadedPlayer.getCloakEndTime());
        current.setRevealActive(loadedPlayer.isRevealActive());
        current.setRevealEndTime(loadedPlayer.getRevealEndTime());
        // Inventory
        current.getInventory().getAllItems().clear();
        current.getInventory().getAllItems().addAll(
            loadedPlayer.getInventory().getAllItems()
        );
        System.out.println(loadedPlayer.getInventory().getAllItems());
    
        // Monsters
        monsterController.clearMonsters();
        monsterController.getMonsters().addAll(gs.getMonsters());
    
        // Enchantments
        enchantmentController.getEnchantments().clear();
        enchantmentController.getEnchantments().addAll(gs.getEnchantments());
    
        // BuildObjects
        buildObjectController.getWorldObjectsMap().clear();
        buildObjectController.getWorldObjectsMap().putAll(gs.getWorldObjectsMap());
    
        // Luring gem
        monsterController.setGemSpawnTime(gs.getGemSpawnTime());
        if (gs.isHasLuringGem()) {
            monsterController.setLuringGemLocation(gs.getLuringGemLocation());
        } else {
            monsterController.clearLuringGemLocation();
        }
    
        // TILES: Rebuild tileData => tileGrid
        TileData[][] loadedData = gs.getTileDataGrid();
        if (loadedData != null) {
            tilesController.setTileDataGrid(loadedData);
        } else {
            // fallback: reload defaults
            tilesController.loadTiles(this.hallType);
        }
    
        // Optionally re-generate JSON if you want
        this.jsonData = exportWorldObjectsMapToJson(gs.getWorldObjectsMap());
    
        // Mark collidable for current hall objects
        List<BuildObject> objectsInCurrentHall = buildObjectController.getObjectsForHall(this.hallType);
        for (BuildObject obj : objectsInCurrentHall) {
            tilesController.setTransparentTileAt(obj.getX(), obj.getY());
        }
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

}