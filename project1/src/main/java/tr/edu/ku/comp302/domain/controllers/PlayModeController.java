package tr.edu.ku.comp302.domain.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.BuildObject;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.Enchantments.Enchantment;

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
    public PlayModeController(KeyHandler keyHandler, MouseHandler mouseHandler) {
        this.keyHandler = keyHandler;

        // Initialize Player
        Player player = new Player(GameConfig.PLAYER_START_X, GameConfig.PLAYER_START_Y, GameConfig.PLAYER_SPEED);

        // Initialize TilesController
        this.tilesController = new TilesController();
        this.tilesController.loadTiles();

        // Initialize PlayerController
        this.playerController = new PlayerController(player, this.tilesController, this.keyHandler);
    
        this.monsterController = new MonsterController(this.tilesController);

        this.enchantmentController = new EnchantmentController(mouseHandler);
    }

    /**
     * Build mode'dan gelen JSON data'yı bu metotla yükleyebiliriz.
     */
    public void loadWorldFromJson(String jsonData) {
        if(jsonData == null || jsonData.isEmpty()) return;
        try {
            // HallType -> List<BuildObject>
            Gson gson = new Gson();
            Type type = new TypeToken<Map<HallType, List<BuildObject>>>(){}.getType();
            worldObjectsMap = gson.fromJson(jsonData, type);
            if (worldObjectsMap == null) {
                worldObjectsMap = new HashMap<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        if (navigationController != null) {
            navigationController.endGameAndShowMainMenu();
        }
    }

    public void draw(Graphics2D g2)  {
        // Tiles
        tilesController.draw(g2);

        // Draw objects from JSON if needed
        List<BuildObject> earthObjects = worldObjectsMap.get(HallType.EARTH);
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

    private void drawSingleObject(Graphics2D g2, BuildObject obj)  {
        // Sadece demonstration amaçlı, "objectType" string'ine bakarak resim seçebiliriz.
        // Örn: "box", "chest", "skull"
        // Gerçekte bu resimleri de bir Map<String, BufferedImage>’te tutmalıyız.
        // Şimdilik basit bir kare çizelim:
        
        try {
            int px = obj.getX() * GameConfig.TILE_SIZE;
            int py = obj.getY() * GameConfig.TILE_SIZE;
            String imageName = obj.getObjectType();
            BufferedImage image = null;
            image = ImageIO.read(getClass().getResourceAsStream("/assets/" + imageName + "." + "png"));
            
            g2.setColor(Color.GREEN);
            g2.drawImage(image, px , py , 48, 48, null);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
}
