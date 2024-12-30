package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Player;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * The PlayerController class manages the player's movement, state, and animations.
 */
public class PlayerController extends EntityController<Player>{

    private Player player;
    
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

        // Calculate new proposed positions
        int newX = entity.getX();
        int newY = entity.getY();

        if (keyHandler.up) {
            newY -= entity.getSpeed();
        }
        if (keyHandler.down) {
            newY += entity.getSpeed();
        }
        if (keyHandler.left) {
            newX -= entity.getSpeed();
        }
        if (keyHandler.right) {
            newX += entity.getSpeed();
        }

        // Use the inherited collision check
        if (!checkCollision(newX, newY)) {
            // Move the player
            //System.out.println("Player location: " + newX + " " + newY);
            setLocation(newX, newY);
            // Are we moving at all?
            isWalking = (keyHandler.up || keyHandler.down || keyHandler.left || keyHandler.right);
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
        g2.drawImage(currentImage, entity.getX(), entity.getY(),
                     GameConfig.TILE_SIZE,
                     GameConfig.TILE_SIZE, null);
    }

    public void setLocation(int x, int y){
        player.setX(x);
        player.setY(y);
    }
}