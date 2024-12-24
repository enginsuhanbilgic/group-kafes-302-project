package tr.edu.ku.comp302.domain.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.BuildObject;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.Tile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * PlayModeController manages the game logic and interactions between components.
 */
public class PlayModeController {

    private final PlayerController playerController;
    private final TilesController tilesController;
    private final KeyHandler keyHandler;

    // Timer via GameTimerController
    private GameTimerController gameTimerController;
    private int initialTime = 20;
    private NavigationController navigationController;
    private int currentTime;

    // JSON’dan yüklenen objeler: HallType -> List<BuildObject>
    private Map<HallType, List<BuildObject>> worldObjectsMap = new HashMap<>();

    public PlayModeController(KeyHandler keyHandler) {
        this.keyHandler = keyHandler;

        // Initialize Player
        Player player = new Player(GameConfig.PLAYER_START_X, GameConfig.PLAYER_START_Y, GameConfig.PLAYER_SPEED);

        // Initialize TilesController
        this.tilesController = new TilesController();
        // Ekrana sığması için startX=2, startY=2
        this.tilesController.loadTiles(2, 2);

        // Initialize PlayerController
        this.playerController = new PlayerController(player, this.tilesController);
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
        if(!keyHandler.isEscPressed()){
            playerController.updatePlayerPosition(keyHandler);
        }
    }

    public void draw(Graphics2D g2) {
        // Tiles (zemin + duvar)
        tilesController.draw(g2);

        // HallType.EARTH vs. hero hangi hall'daysa o mu çizilsin?
        // İlk prototip için: Tüm hall’daki objeleri basitçe çizelim (ya da sadece Earth).
        // Örnek: Sadece Earth'ü çizelim.
        // Gelişmiş: Hero bir hall'dan diğerine geçiyorsa logic eklemek gerek.
        List<BuildObject> earthObjects = worldObjectsMap.get(HallType.EARTH);
        if (earthObjects != null) {
            for (BuildObject obj : earthObjects) {
                drawSingleObject(g2, obj);
            }
        }

        // vs. isterseniz AIR, WATER, FIRE da çizebilirsiniz

        // Oyuncu
        playerController.draw(g2);
    }

    private void drawSingleObject(Graphics2D g2, BuildObject obj) {
        // Sadece demonstration amaçlı, "objectType" string'ine bakarak resim seçebiliriz.
        // Örn: "box", "chest", "skull"
        // Gerçekte bu resimleri de bir Map<String, BufferedImage>’te tutmalıyız.
        // Şimdilik basit bir kare çizelim:
        int px = obj.getX() * GameConfig.TILE_SIZE;
        int py = obj.getY() * GameConfig.TILE_SIZE;
        g2.setColor(Color.GREEN);
        g2.fillRect(px, py, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
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
