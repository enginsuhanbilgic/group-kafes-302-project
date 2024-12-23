package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.Tile;

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
    private final TilesController tilesController; // COLLISION UPDATE

    // Animation Images
    private List<BufferedImage> walkImages;
    private BufferedImage standImage;

    // Animation State
    private int currentFrame = 0;
    private int frameCounter = 0;
    private final int frameDelay = 15;

    public PlayerController(Player player, TilesController tilesController) { // COLLISION UPDATE
        this.player = player;
        this.tilesController = tilesController; // COLLISION UPDATE
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
        // COLLISION UPDATE
        boolean isWalking = false;

        // Gelecek konumları hesapla
        int newX = player.playerX;
        int newY = player.playerY;

        if (keyH.up) {
            newY -= player.playerSpeed;
        }
        if (keyH.down) {
            newY += player.playerSpeed;
        }
        if (keyH.left) {
            newX -= player.playerSpeed;
        }
        if (keyH.right) {
            newX += player.playerSpeed;
        }

        // Kafes (duvar) çarpışmasını kontrol et
        if (!checkCollision(newX, newY)) {
            player.playerX = newX;
            player.playerY = newY;
            // Hareket varsa animasyon yürüme durumuna geçsin
            isWalking = keyH.up || keyH.down || keyH.left || keyH.right;
        } else {
            // Duvara çarpıyorsak hareket etme
            isWalking = false;
        }

        player.setWalking(isWalking);
        updateAnimationFrame();
    }

    /**
     * Çarpışma kontrolü:
     * Oyuncunun x,y koordinatları tile grid içindeki collidable tile'a denk geliyor mu?
     */
    private boolean checkCollision(int x, int y) { // COLLISION UPDATE
        int tileSize = GameConfig.TILE_SIZE;

        // Oyuncunun bounding box'ını (kutusunu) hesapla
        int leftX = x;
        int rightX = x + tileSize - 1;
        int topY = y;
        int bottomY = y + tileSize - 1;

        // Tile indexlerine dönüştür
        int leftCol = leftX / tileSize;
        int rightCol = rightX / tileSize;
        int topRow = topY / tileSize;
        int bottomRow = bottomY / tileSize;

        // Dört köşe (veya gerekirse aradaki tile'lar) collidable mı kontrol et
        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                Tile tile = tilesController.getTileAt(col, row);
                if (tile != null && tile.isCollidable) {
                    return true; // Çarpışma var
                }
            }
        }
        return false; // Çarpışma yok
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
     */
    public void draw(Graphics2D g2) {
        BufferedImage currentImage = player.isWalking() ? walkImages.get(currentFrame) : standImage;
        g2.drawImage(currentImage, player.playerX, player.playerY, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
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
