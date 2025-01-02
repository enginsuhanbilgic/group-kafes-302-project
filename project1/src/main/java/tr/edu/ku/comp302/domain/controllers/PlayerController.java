package tr.edu.ku.comp302.domain.controllers;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Player;

/**
 * The PlayerController class manages the player's movement, state, and animations.
 */
public class PlayerController extends EntityController<Player>{

    private Player player;
    private boolean isFacingLeft = false; // Default facing right
    // Animation Images
    private List<BufferedImage> walkImages;
    private BufferedImage standImage;

    // Animation State
    private int currentFrame = 0;
    private int frameCounter = 0;
    private final int frameDelay = 15;

    private final KeyHandler keyHandler; // We can store this if we want direct access each update

    public PlayerController(Player player, TilesController tilesController, KeyHandler keyHandler) { // COLLISION UPDATE
        super(player, tilesController);
        this.player = player;
        this.keyHandler = keyHandler;
        loadPlayerImages();
    }

    /**
     * Loads images for walking animation and standing state.
     */
    private void loadPlayerImages() {
        walkImages = new ArrayList<>();
        walkImages.add(ResourceManager.getImage("player_walk_1"));
        walkImages.add(ResourceManager.getImage("player_walk_2"));
        standImage = ResourceManager.getImage("player_stand");
        
    }

    @Override
    public void update() {
        boolean isWalking = false;
        boolean isLeftWalking = false;


        // Calculate new proposed positions
        int newX = entity.getX();

        int newY = entity.getY();
        boolean movingHorizontally = keyHandler.left || keyHandler.right;
        boolean movingVertically = keyHandler.up || keyHandler.down;

        // For normalizing speed
        double speed = entity.getSpeed();
        if (movingHorizontally && movingVertically) {
            speed /= Math.sqrt(2);
        }

        if (keyHandler.up) {
            newY -= speed;
        }
        if (keyHandler.down) {
            newY += speed;
        }
        if (keyHandler.left) {
            newX -= speed;
            isFacingLeft = true; // Flip to face left
        }
        if (keyHandler.right) {
            newX += speed;
            isFacingLeft = false; // Flip to face left
        }

        // Use the inherited collision check
        if (!checkCollision(newX, newY)) {
            // Move the player
            //System.out.println("Player location: " + newX + " " + newY);
            setLocation(newX, newY);
            // Are we moving at all?
            isWalking = (keyHandler.up || keyHandler.down ||  keyHandler.right ||  keyHandler.left);
        } else {
            isWalking = false;
        }

        entity.setWalking(isWalking);
        updateAnimationFrame();
    }

    /**
     * Updates the current animation frame based on the walking state.
     */
    private void updateAnimationFrame() {
        if (entity.isWalking()) {
            frameCounter++;
            if (frameCounter >= frameDelay) {
                currentFrame = (currentFrame + 1) % walkImages.size();
                frameCounter = 0;
            }
        } else {
            currentFrame = 0; // reset to standing
        }
    }

    @Override
    public void draw(Graphics2D g2) {
    BufferedImage currentImage = entity.isWalking() ? walkImages.get(currentFrame) : standImage;

    int x = entity.getX();
    int y = entity.getY();
    int width = GameConfig.TILE_SIZE;
    int height = GameConfig.TILE_SIZE;

    if (isFacingLeft) {
        g2.drawImage(currentImage, x + width, y, -width, height, null);
    } else {
        g2.drawImage(currentImage, x, y, width, height, null);
    }
}

    public void setLocation(int x, int y){
        player.setX(x);
        player.setY(y);
    }
}