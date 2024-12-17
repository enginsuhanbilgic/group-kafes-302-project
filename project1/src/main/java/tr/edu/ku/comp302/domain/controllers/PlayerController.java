package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.domain.models.Player;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The PlayerController class manages the player's movement, state, and animations.
 */
public class PlayerController {

    private final Player player;

    // Animation Images
    private List<BufferedImage> walkImages;
    private BufferedImage standImage;

    // Animation State
    private int currentFrame = 0;
    private int frameCounter = 0;
    private final int frameDelay = 15;

    public PlayerController(Player player) {
        this.player = player;
        loadPlayerImages();
    }

    /**
     * Loads images for walking animation and standing state.
     */
    private void loadPlayerImages() {
        walkImages = new ArrayList<>();
        try {
            walkImages.add(ImageIO.read(getClass().getResourceAsStream("/assets/player_walk_1.png")));
            walkImages.add(ImageIO.read(getClass().getResourceAsStream("/assets/player_walk_2.png")));
            standImage = ImageIO.read(getClass().getResourceAsStream("/assets/player_stand.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the player's position and walking state based on keyboard input.
     *
     * @param keyH KeyHandler for input.
     */
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

        // Update walking state
        player.setWalking(isWalking);
        updateAnimationFrame();
    }

    /**
     * Updates the current animation frame based on the walking state.
     */
    private void updateAnimationFrame() {
        if (player.isWalking()) {
            frameCounter++;
            if (frameCounter >= frameDelay) {
                currentFrame = (currentFrame + 1) % walkImages.size(); // Cycle through frames
                frameCounter = 0;
            }
        } else {
            currentFrame = 0; // Reset to standing when not walking
        }
    }

    /**
     * Draws the player on the screen.
     *
     * @param g2       Graphics2D object for rendering.
     * @param tileSize Size of the tile for scaling the player image.
     */
    public void draw(Graphics2D g2, int tileSize) {
        BufferedImage currentImage = player.isWalking() ? walkImages.get(currentFrame) : standImage;
        g2.drawImage(currentImage, player.playerX, player.playerY, tileSize, tileSize, null);
    }

    /**
     * Returns the Player object.
     *
     * @return Player object.
     */
    public Player getPlayer() {
        return player;
    }
}
