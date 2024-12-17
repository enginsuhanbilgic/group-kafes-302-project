package tr.edu.ku.comp302.domain.controllers;

import java.awt.Graphics2D;

import tr.edu.ku.comp302.domain.models.Player;

public class PlayerController {
    private final Player player;

    public PlayerController(Player player){
        this.player = player;
    }

    public void updatePlayerPosition(KeyHandler keyH) {
        boolean isWalking = false;

        if (keyH.up) {
            player.playerY -= player.playerSpeed;
            isWalking = true;
        }
        if (keyH.down) {
            player.playerY += player.playerSpeed;
            isWalking = true;
        }
        if (keyH.left) {
            player.playerX -= player.playerSpeed;
            isWalking = true;
        }
        if (keyH.right) {
            player.playerX += player.playerSpeed;
            isWalking = true;
        }

        // Update walking state and animation frame
        player.setWalking(isWalking);
        player.updateAnimationFrame();
    }

    public void draw(Graphics2D g2, int tileSize){
        g2.drawImage(player.getCurrentImage(), player.playerX, player.playerY, tileSize, tileSize, null);
    }

    public Player getPlayer(){
        return player;
    }
}
