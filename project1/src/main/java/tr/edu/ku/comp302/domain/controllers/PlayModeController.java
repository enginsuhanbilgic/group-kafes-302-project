package tr.edu.ku.comp302.domain.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.BuildObject;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.Enchantments.Enchantment;
import tr.edu.ku.comp302.domain.models.Monsters.Monster;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

/**
 * PlayModeController manages the game logic and interactions between components.
 */
public class PlayModeController {

    private final PlayerController playerController;
    private final TilesController tilesController;
    private NavigationController navigationController;
    private MonsterController monsterController;
    private EnchantmentController enchantmentController;
    private final KeyHandler keyHandler;
    
    // Timer via GameTimerController
    private GameTimerController gameTimerController;
    private int initialTime = 60;
    private int currentTime;

    // JSON’dan yüklenen objeler: HallType -> List<BuildObject>
    private Map<HallType, List<BuildObject>> worldObjectsMap = new HashMap<>();

    private boolean gameOver = false;
    private HallType hallType;
    private String jsonData;

    public PlayModeController(KeyHandler keyHandler, MouseHandler mouseHandler, String jsonData, HallType hallType) {
        this.keyHandler = keyHandler;
        this.hallType = hallType;
        // Initialize Player
        Player player = new Player(GameConfig.PLAYER_START_X, GameConfig.PLAYER_START_Y, GameConfig.PLAYER_SPEED);

        // Initialize TilesController
        this.tilesController = new TilesController();
        this.tilesController.loadTiles();

        // Initialize PlayerController
        this.playerController = new PlayerController(player, this.tilesController, this.keyHandler);
    
        this.monsterController = new MonsterController(this.tilesController);
        this.enchantmentController = new EnchantmentController(mouseHandler, this.monsterController);
        //Very bad solution
        monsterController.setEnchantmentController(enchantmentController);
    }   

    /**
     * Build mode'dan gelen JSON data'yı bu metotla yükleyebiliriz.
     */
    public void loadWorldFromJson(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            return;
        }

        try {
            Gson gson = new Gson();
            // First parse as Map<String, List<BuildObject>> from the JSON
            Type rawType = new TypeToken<Map<String, List<BuildObject>>>() {}.getType();
            Map<String, List<BuildObject>> rawMap = gson.fromJson(jsonData, rawType);

            if (rawMap == null) {
                rawMap = new HashMap<>();
            }

            // Convert the string keys to HallType keys
            Map<HallType, List<BuildObject>> finalMap = new HashMap<>();
            for (Map.Entry<String, List<BuildObject>> entry : rawMap.entrySet()) {
                try {
                    // Make sure the key matches an enum name (e.g., "EARTH", "AIR", etc.)
                    HallType hall = HallType.valueOf(entry.getKey().toUpperCase());
                    finalMap.put(hall, entry.getValue());
                } catch (IllegalArgumentException iae) {
                    // If the key isn't a valid HallType, skip or handle as needed
                    System.err.println("Skipping invalid hall type: " + entry.getKey());
                }
            }

            // Assign the newly constructed map to your field
            worldObjectsMap = finalMap;
        } catch (Exception e) {
            e.printStackTrace();
            // If something goes wrong, fallback to an empty map
            worldObjectsMap = new HashMap<>();
        }
    }


    public void update() {
        if (gameOver) return;
        if (!keyHandler.isEscPressed()) {
            // 1) Update Player
            playerController.update();

            // 2) Update Monsters
            monsterController.updateAll(playerController.getEntity());

            // 3) Update Enchantments (spawn, despawn, mouse clicks)
            enchantmentController.update(playerController.getEntity(), tilesController);

            // 4) Check if user pressed R, P, B, etc. to use stored enchantments
            checkEnchantmentUsage();

            // 5) Apply any extra time that was requested by the player
            int bonusTime = playerController.getEntity().consumeBonusTime();
            if (bonusTime > 0 && gameTimerController != null) {
                // add time to the global timer
                gameTimerController.addTime(bonusTime);
            }

            // 6) Check hero’s lives
            if (playerController.getEntity().getLives() <= 0) {
                gameOver = true;
                onGameOver();
            }
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
        if (keyHandler.isBPressed()) {
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
        System.out.println("Game Over! The hero has no more lives.");
        // You can navigate to a game over screen, or show a dialog, etc.
        switch (hallType) {
            case EARTH:
                // Next hall is AIR
                navigationController.startNewPlayModeFromJson(jsonData, HallType.AIR);
                break;
            case AIR:
                // Next hall is WATER
                navigationController.startNewPlayModeFromJson(jsonData, HallType.WATER);
                break;
            case WATER:
                // Next hall is FIRE
                navigationController.startNewPlayModeFromJson(jsonData, HallType.FIRE);
                break;
            case FIRE:
            default:
                // For FIRE, either loop back or end the game, depending on your design
                navigationController.endGameAndShowMainMenu();
                break;
        }
        /*if (navigationController != null) {
            navigationController.endGameAndShowMainMenu();
        }*/
    }

    public void draw(Graphics2D g2) {
        // Tiles
        tilesController.draw(g2);

        // Draw objects from JSON if needed
        List<BuildObject> earthObjects = worldObjectsMap.get(hallType);
        if (earthObjects != null) {
            for (BuildObject obj : earthObjects) {
                drawSingleObject(g2, obj);
            }
        }

        // Draw monsters
        monsterController.drawAll(g2);

        // Draw enchantments
        enchantmentController.draw(g2);

        // Draw player
        playerController.draw(g2);

        // If player has reveal active, you might highlight a 4x4 area around the rune
        if (playerController.getEntity().isRevealActive()) {
            // For demonstration, let's just draw a red rectangle somewhere
            // e.g., if your game tracks the rune location as (runeX, runeY) in TilesController
            // we do a simple 4x4 highlight
            
            //Point runeTile = tilesController.getRuneTile(); // hypothetical
            //if (runeTile != null) {
                int rectX = 1 * GameConfig.TILE_SIZE - GameConfig.TILE_SIZE;
                int rectY = 1 * GameConfig.TILE_SIZE - GameConfig.TILE_SIZE;
                int rectWidth = 4 * GameConfig.TILE_SIZE;
                int rectHeight = 4 * GameConfig.TILE_SIZE;

                g2.setColor(new Color(255, 0, 0, 80));
                g2.fillRect(rectX, rectY, rectWidth, rectHeight);
            //}
        }
    }

    private void drawSingleObject(Graphics2D g2, BuildObject obj) {
        // Sadece demonstration amaçlı, "objectType" string'ine bakarak resim seçebiliriz.
        // Örn: "box", "chest", "skull"
        // Gerçekte bu resimleri de bir Map<String, BufferedImage>’te tutmalıyız.
        // Şimdilik basit bir kare çizelim:
        String imageName = obj.getObjectType();
        int px = obj.getX() * GameConfig.TILE_SIZE;
        int py = obj.getY() * GameConfig.TILE_SIZE;
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/assets/"  + imageName + ".png"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        g2.setColor(Color.GREEN);
        g2.drawImage(image, px, py ,GameConfig.TILE_SIZE , GameConfig.TILE_SIZE, null);
    }

    public boolean isPaused() {
        return keyHandler.isEscPressed();
    }

    // ========== TIMER ==========
    public void startGameTimer(Consumer<Integer> onTick, Runnable onTimeUp) {
        gameTimerController = new GameTimerController(
                time -> {
                    currentTime = time;
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
