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
    public static final int KAFES_STARTING_X = 1;
    public static final int KAFES_STARTING_Y = 1;

    // Screen configuration
    public static final boolean RESIZABLE = true; // Resizable propert of screen
    public static final int RES_HORIZONTAL = 1920;//1024;//1980; //
    public static final int RES_VERTICAL = 1040;//640;//1080; //
    public static final int NUM_HALL_COLS = 16; // Number of columns
    public static final int NUM_HALL_ROWS = 16; // Number of rows

    // Player default properties
    public static final int PLAYER_SPEED = 12/SCALE;     // Player speed
    public static final int PLAYER_LIVES = 500;

    // Enchantment properties
    public static final int ENCHANTMENT_SPAWN_INTERVAL = 3;

    // Monster properties
    public static final int MONSTER_SPAWN_INTERVAL = 8; // 8 seconds
    public static final int MONSTER_ATTACK_COOLDOWN = 2; // 1 second
}
