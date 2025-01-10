package tr.edu.ku.comp302.domain.controllers;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Player;

/**
 * The PlayerController class manages the player's movement, state, and animations.
 * 
 * This class is now responsible for: 
 * 1) Applying acceleration/velocity to the Player. 
 * 2) Checking collisions. 
 * 3) Updating animations.
 */
public class PlayerController extends EntityController<Player> {

    private final Player player;
    private boolean isFacingLeft = false; // Default: face right

    // Animation images
    private List<BufferedImage> walkImages;
    private BufferedImage standImage;

    // Animation state
    private int currentFrame = 0;
    private int frameCounter = 0;
    private final int frameDelay = 15;

    // Key input
    private final KeyHandler keyHandler;

    public PlayerController(Player player, TilesController tilesController, KeyHandler keyHandler) {
        super(player, tilesController);
        this.player = player;
        this.keyHandler = keyHandler;
        loadPlayerImages();
    }

    /**
     * Load images for walking animation and standing state.
     */
    private void loadPlayerImages() {
        walkImages = new ArrayList<>();
        // Hypothetical ResourceManager usage:
        walkImages.add(ResourceManager.getImage("player_walk_1"));
        walkImages.add(ResourceManager.getImage("player_walk_2"));
        standImage = ResourceManager.getImage("player_stand");
    }

    @Override
    public void update() {
        // 1. Apply input => accelerate
        float accel = player.getAcceleration();
        if (keyHandler.up)    player.accelerateY(-accel);
        if (keyHandler.down)  player.accelerateY(accel);
        if (keyHandler.left)  player.accelerateX(-accel);
        if (keyHandler.right) player.accelerateX(accel);

        // 2. If no key is pressed, friction slows Player.
        player.applyFriction();

        // 3. Limit max speed
        player.clampSpeed();

        // 4. Compute proposed new position
        float newX = player.getX() + player.getVelocityX();
        float newY = player.getY() + player.getVelocityY();

        // 4a. Check collision horizontally
        if (!checkCollision((int)newX, player.getY())) {
            player.setX((int)newX);
        } else {
            // collision => zero or invert velocityX
            // Easiest is to just set velocityX to 0
            player.accelerateX(-player.getVelocityX());
        }

        // 4b. Check collision vertically
        if (!checkCollision(player.getX(), (int)newY)) {
            player.setY((int)newY);
        } else {
            // collision => zero out velocityY
            player.accelerateY(-player.getVelocityY());
        }

        // 5. Are we walking (i.e., is velocity significant)?
        boolean isWalking = (Math.abs(player.getVelocityX()) > 0.1f 
                          || Math.abs(player.getVelocityY()) > 0.1f);
        player.setWalking(isWalking);

        // 6. Update facing direction based on horizontal velocity
        if (keyHandler.left && Math.abs(player.getVelocityX()) > 0.1f) {
            isFacingLeft = true;
        } else if (keyHandler.right && Math.abs(player.getVelocityX()) > 0.1f) {
            isFacingLeft = false;
        }

        // 7. Update animation
        updateAnimationFrame();
    }

    /**
     * Updates animation frames based on walking state.
     */
    private void updateAnimationFrame() {
        if (player.isWalking()) {
            frameCounter++;
            if (frameCounter >= frameDelay) {
                currentFrame = (currentFrame + 1) % walkImages.size();
                frameCounter = 0;
            }
        } else {
            currentFrame = 0; // standing frame
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        BufferedImage currentImage = player.isWalking() ? walkImages.get(currentFrame) : standImage;

        int x = player.getX();
        int y = player.getY();
        int width = GameConfig.TILE_SIZE;
        int height = GameConfig.TILE_SIZE;

        // Flip horizontally if facing left
        if (isFacingLeft) {
            g2.drawImage(currentImage, x + width, y, -width, height, null);
        } else {
            g2.drawImage(currentImage, x, y, width, height, null);
        }
    }

    /**
     * Allows forced repositioning (e.g., random spawn, teleports).
     * Often you’ll want to set velocity to 0 so you don’t “slide” after spawn.
     */
    public void setLocation(int x, int y) {
        player.setX(x);
        player.setY(y);
        // Reset velocity so we don't slide after forced placement
        player.accelerateX(-player.getVelocityX());
        player.accelerateY(-player.getVelocityY());
    }
}
