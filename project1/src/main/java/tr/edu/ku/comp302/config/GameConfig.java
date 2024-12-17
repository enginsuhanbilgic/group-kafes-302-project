package tr.edu.ku.comp302.config;

/**
 * GameConfig holds global configuration settings for the game.
 * These values can be accessed by any class that requires game-specific parameters.
 */
public class GameConfig {
    // Tile configuration
    public static final int ORIGINAL_TILE_SIZE = 16; // Base tile size
    public static final int SCALE = 3;
    public static final int TILE_SIZE = ORIGINAL_TILE_SIZE * SCALE;

    // Screen configuration
    public static final int RES_HORIZONTAL = 1920;
    public static final int RES_VERTICAL = 1080;
    public static final int NUM_HALL_COLS = 16; // Number of columns
    public static final int NUM_HALL_ROWS = 16; // Number of rows

    // Player default properties
    public static final int PLAYER_START_X = 100; // Default player starting X-coordinate
    public static final int PLAYER_START_Y = 100; // Default player starting Y-coordinate
    public static final int PLAYER_SPEED = 4;     // Player speed
}
