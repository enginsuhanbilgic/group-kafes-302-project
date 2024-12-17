package tr.edu.ku.comp302.domain.controllers;

import java.awt.Graphics2D;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Player;

/**
 * PlayModeController manages the game logic and interactions between components.
 */
public class PlayModeController {

    private final PlayerController playerController;
    private final TilesController tilesController;

    /**
     * Constructs the PlayModeController using global GameConfig settings.
     */
    public PlayModeController() {
        // Initialize Player and PlayerController
        Player player = new Player(GameConfig.PLAYER_START_X, GameConfig.PLAYER_START_Y, GameConfig.PLAYER_SPEED);
        this.playerController = new PlayerController(player);

        // Initialize TilesController with grid dimensions and tile size
        this.tilesController = new TilesController();

        // Load tile data into the controller
        this.tilesController.loadTiles(2, 2);
    }

    /**
     * Updates the game logic, such as player movement.
     *
     * @param keyHandler The KeyHandler for user input.
     */
    public void update(KeyHandler keyHandler) {
        playerController.updatePlayerPosition(keyHandler);
    }

    /**
     * Draws the game components (tiles and player).
     *
     * @param g2 Graphics2D object for rendering.
     */
    public void draw(Graphics2D g2) {
        tilesController.draw(g2);
        playerController.draw(g2, GameConfig.TILE_SIZE);
    }
}
