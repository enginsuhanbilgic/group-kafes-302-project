package tr.edu.ku.comp302.domain.models;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * The Player class represents the player character in the game.
 * It handles the player's position, movement speed, walking animation,
 * and standing image.
 */
public class Player {

    public int playerX, playerY;
    public final int playerSpeed;

    private List<BufferedImage> walkImages;
    private BufferedImage standImage;
    private int currentFrame = 0;
    private boolean walking = false;
    private int frameCounter = 0;
    private final int frameDelay = 15;

    public Player(int startX, int startY, int speed) {
        this.playerX = startX;
        this.playerY = startY;
        this.playerSpeed = speed;

        //Load the images when the Player is instantiated
        loadPlayerImages();
    }

    //We add two walk images for animation and one stand image.
    private void loadPlayerImages() {
        walkImages = new ArrayList<>();
        try {
            // Add images for walking animation
            walkImages.add(ImageIO.read(getClass().getResourceAsStream("/assets/player_walk_1.png")));
            walkImages.add(ImageIO.read(getClass().getResourceAsStream("/assets/player_walk_2.png")));
            standImage = ImageIO.read(getClass().getResourceAsStream("/assets/player_stand.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setWalking(boolean walking) {
        this.walking = walking;
    }

    //Change the walk image according to frame
    public void updateAnimationFrame() {
        if (walking) {
            frameCounter++;
            //When the frame counter is more than a predefined value
            //the walking animation will change. Frame delay ensures 
            //the walking animation is at a reasonable speed.
            if (frameCounter >= frameDelay){
                currentFrame = (currentFrame + 1) % walkImages.size(); // Cycle through frames
                frameCounter=0;
            }
        } else {
            currentFrame = 0; // Reset to the first frame if not walking
        }
    }

    //If the player is walking, get one of the walk images
    //If the player is standing, get the stand image
    public BufferedImage getCurrentImage() {
        if (walkImages == null || walkImages.isEmpty()) {
            System.err.println("Player images not loaded properly.");
            return null; // Return null to avoid further errors
        }
        if(this.walking==true){
            return walkImages.get(currentFrame);
        }
        else{
            return standImage;
        }
    }
    
}
