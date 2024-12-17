package tr.edu.ku.comp302.domain.controllers;

import java.awt.Graphics2D;

import tr.edu.ku.comp302.domain.models.Player;

public class PlayModeController {
    private final PlayerController playerController;
    private final TilesController tilesController;

    public PlayModeController(PlayerController playerController, TilesController tilesController) {
        this.playerController = playerController;
        this.tilesController = tilesController;

        tilesController.loadTiles(2,2);
    }

    public void update(KeyHandler keyHandler) {
        playerController.updatePlayerPosition(keyHandler);
    }

    public TilesController getTilesController() {
        return tilesController;
    }

    public Player getPlayer() {
        return playerController.getPlayer();
    }

    public void draw(Graphics2D g2, int tileSize){
        tilesController.draw(g2);
        playerController.draw(g2, tileSize);
    }
}
