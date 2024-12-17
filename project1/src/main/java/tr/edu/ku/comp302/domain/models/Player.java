package tr.edu.ku.comp302.domain.models;

/**
 * The Player class represents the player character's state in the game.
 * It stores position, movement speed, and walking status.
 */
public class Player {

    /** Player's X and Y positions on the screen. */
    public int playerX, playerY;

    /** Speed at which the player moves. */
    public final int playerSpeed;

    /** Indicates whether the player is currently walking. */
    private boolean walking = false;

    /**
     * Constructs a Player object with the given starting position and speed.
     *
     * @param startX Starting X-coordinate of the player.
     * @param startY Starting Y-coordinate of the player.
     * @param speed  Movement speed of the player.
     */
    public Player(int startX, int startY, int speed) {
        this.playerX = startX;
        this.playerY = startY;
        this.playerSpeed = speed;
    }

    /**
     * Sets the walking state of the player.
     *
     * @param walking true if the player is walking, false otherwise.
     */
    public void setWalking(boolean walking) {
        this.walking = walking;
    }

    /**
     * Returns the walking state of the player.
     *
     * @return true if the player is walking, false otherwise.
     */
    public boolean isWalking() {
        return walking;
    }
}
