package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.Tile;
import tr.edu.ku.comp302.domain.models.enchantments.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Constructor: Initializes the EnchantmentController with necessary resources.
 *
 * @param tilesController The TilesController instance used to verify tile properties.
 * @requires tilesController is not null.
 * @modifies this.enchantments, this.random, this.extraTimeImage, this.heartImage,
 *           this.revealImage, this.cloakImage, this.gemImage, this.runeImage
 * @effects 
 *   - Initializes enchantments as an empty list.
 *   - Initializes random number generator.
 *   - Loads images for various enchantments using ResourceManager.
 */
public class EnchantmentController {

    private final TilesController tilesController;

    protected final List<Enchantment> enchantments;
    private final Random random;

    protected int lastSpawnTime;
    
    private BufferedImage extraTimeImage;
    private BufferedImage heartImage;
    private BufferedImage revealImage;
    private BufferedImage cloakImage;
    private BufferedImage gemImage;
    private BufferedImage runeImage;

    public EnchantmentController(TilesController tilesController) {
        this.enchantments = new CopyOnWriteArrayList<>();
        this.random = new Random();
        this.tilesController = tilesController;
        this.lastSpawnTime = 0;

        extraTimeImage = ResourceManager.getImage("enchantment_extratime");
        heartImage     = ResourceManager.getImage("enchantment_heart");
        revealImage    = ResourceManager.getImage("enchantment_reveal");
        cloakImage     = ResourceManager.getImage("enchantment_cloak");
        gemImage       = ResourceManager.getImage("enchantment_gem");
        runeImage      = ResourceManager.getImage("rune");
    }

    /**
     * Updates the state of enchantments each frame.
     * - Spawns new enchantments if the spawn interval has passed.
     * - Removes expired enchantments.
     * - Handles collection of enchantments via player click.
     *
     * @param player   The Player instance collecting enchantments.
     * @param clickPos The position where the player clicked, or null if no click occurred.
     * @requires player is not null.
     * @modifies this.enchantments, player.inventory, this.lastSpawnTime
     * @effects 
     *   - If spawn interval has passed, spawns a new enchantment.
     *   - Removes enchantments that have exceeded their lifetime.
     *   - If clickPos is within an enchantment's bounds, collects that enchantment.
     */
    public void update(Player player, Point clickPos) {

        //Most of the enchantment logic is handled in tick() which is not good

        // Check for user left-click
        if (clickPos != null) {
            handleClickCollection(clickPos, player);
        }
    }

    /**
     * Draws all active enchantments on the provided Graphics2D context.
     *
     * @param g2 The Graphics2D context used for rendering.
     * @requires g2 is not null.
     * @modifies g2
     * @effects 
     *   - Renders each enchantment's image at its (x, y) position.
     */
    public void draw(Graphics2D g2) {
        for (Enchantment e : enchantments) {
            switch (e.getType()) {
                case EXTRA_TIME -> {
                    g2.drawImage(extraTimeImage, e.getX(), e.getY(),
                                     GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
                }
                case EXTRA_LIFE -> {
                    // Draw the heart image if not null
                    if (heartImage != null) {
                        g2.drawImage(heartImage, e.getX(), e.getY(),
                                     GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
                    }
                }
                case REVEAL -> {
                    if (revealImage != null) {
                        g2.drawImage(revealImage, e.getX(), e.getY(),
                                     GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
                    }
                }
                case CLOAK_OF_PROTECTION -> {
                    if (cloakImage != null) {
                        g2.drawImage(cloakImage, e.getX(), e.getY(),
                                     GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
                    }
                }
                case LURING_GEM -> {
                    if (gemImage != null) {
                        g2.drawImage(gemImage, e.getX(), e.getY(),
                                     GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
                    }
                }
                case RUNE -> {
                    if (runeImage!=null){
                        g2.drawImage(runeImage, e.getX(), e.getY(),
                                    GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
                    }
                }
            }
        }
    }

    // ====================== Private Helpers ======================

    /**
     * Attempts to spawn a random enchantment at a free tile within the hall.
     *
     * @param inGameTime The current in-game time in seconds.
     * @requires inGameTime is non-negative.
     * @modifies this.enchantments, this.lastSpawnTime
     * @effects 
     *   - If a free tile is found within 50 attempts, adds a new Enchantment to enchantments.
     *   - Updates lastSpawnTime to inGameTime.
     *   - Logs the spawned enchantment.
     */
    private void spawnRandomEnchantment(int inGameTime) {
        int tileSize = GameConfig.TILE_SIZE;
        int mapWidth = GameConfig.NUM_HALL_COLS;
        int mapHeight = GameConfig.NUM_HALL_ROWS;

        // We'll attempt up to 50 times to find a free tile in the hall:
        for (int i = 0; i < 50; i++) {
            int col = random.nextInt(mapWidth);
            int row = random.nextInt(mapHeight);

            // Double check if tile is non-collidable
            Tile t = tilesController.getTileAt(col+GameConfig.KAFES_STARTING_Y, row+GameConfig.KAFES_STARTING_X);
            if (t != null && !t.isCollidable && isLocationAvailable(col, row)) {
                // create enchantment
                Enchantment e = createRandomEnchantment((col + GameConfig.KAFES_STARTING_X) * tileSize, (row + GameConfig.KAFES_STARTING_Y) * tileSize, inGameTime);
                enchantments.add(e);
                System.out.println("[Spawn] " + e.getType() + " at col=" + col + ", row=" + row);
                return;
            }
        }
    }

    /**
     * Updates enchantments based on the current in-game time.
     * - Spawns new enchantments if spawn interval has passed.
     * - Removes enchantments that have expired.
     *
     * @param inGameTime The current in-game time in seconds.
     * @requires inGameTime is non-negative.
     * @modifies this.enchantments, this.lastSpawnTime
     * @effects 
     *   - Potentially adds new enchantments.
     *   - Removes expired enchantments from enchantments list.
     */
    public void tick(int inGameTime) {
        // 1) Attempt spawn logic
        if (inGameTime - lastSpawnTime >= GameConfig.ENCHANTMENT_SPAWN_INTERVAL) {
            spawnRandomEnchantment(inGameTime);
            lastSpawnTime = inGameTime;
        }

        // 2) Remove expired items
        // Each enchantment knows how long it lives, e.g. 10s or so
        enchantments.removeIf(e -> (inGameTime - e.getSpawnGameTime()) > e.getLifetimeSeconds());
    }

    /**
     * Checks if a specific tile location is available for spawning an enchantment.
     *
     * @param col The column index within the hall.
     * @param row The row index within the hall.
     * @return True if the location is available, false otherwise.
     * @requires 0 <= col < GameConfig.NUM_HALL_COLS, 0 <= row < GameConfig.NUM_HALL_ROWS
     * @modifies none
     * @effects 
     *   - Returns true if no existing enchantment occupies the specified (col, row).
     */
    public boolean isLocationAvailable(int col, int row) {
        int tileSize = GameConfig.TILE_SIZE;
    
        for (Enchantment m : this.enchantments) {
            // Convert monster pixel coords to tile coords
            int enchantmentCol = m.getX() / tileSize - GameConfig.KAFES_STARTING_X;
            int enchantmentRow = m.getY() / tileSize - GameConfig.KAFES_STARTING_Y;
    
            // If the monster occupies the same tile, it's not available
            if (enchantmentCol == col && enchantmentRow == row) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a random Enchantment instance based on random selection.
     *
     * @param x          The x-coordinate (in pixels) for the enchantment.
     * @param y          The y-coordinate (in pixels) for the enchantment.
     * @param inGameTime The current in-game time in seconds.
     * @return A new Enchantment instance.
     * @requires none
     * @modifies none
     * @effects 
     *   - Returns a new instance of one of the Enchantment subclasses.
     */
    private Enchantment createRandomEnchantment(int x, int y, int inGameTime) {
        int r = random.nextInt(5); // 0..4
        return switch (r) {
          case 0 -> new ExtraTimeEnchantment(x, y, inGameTime);
            case 1 -> new ExtraLifeEnchantment(x, y, inGameTime);
            case 2 -> new RevealEnchantment(x, y, inGameTime);
            case 3 -> new CloakOfProtectionEnchantment(x, y, inGameTime);
            case 4 -> new LuringGemEnchantment(x, y, inGameTime);
            default -> null;
        };
    }

    /**
     * Handles the collection of enchantments when the player clicks on them.
     *
     * @param clickPos The position where the player clicked.
     * @param player   The Player instance collecting the enchantment.
     * @requires clickPos is not null, player is not null.
     * @modifies this.enchantments, player.inventory
     * @effects 
     *   - If an enchantment is clicked, invokes its onCollect method and removes it from enchantments.
     */
    private void handleClickCollection(Point clickPos, Player player) {
        int clickX = clickPos.x;
        int clickY = clickPos.y;

        Enchantment collected = null;
        for (Enchantment e : enchantments) {
            int ex = e.getX();
            int ey = e.getY();

            // If the click is inside the tile bounding box for the enchantment
            if (clickX >= ex && clickX < ex + GameConfig.TILE_SIZE
                && clickY >= ey && clickY < ey + GameConfig.TILE_SIZE) {
                // The user clicked on this enchantment
                e.onCollect(player);
                collected = e;
                break;
            }
        }
        if (collected != null) {
            enchantments.remove(collected);
        }
    }

    /**
     * Retrieves the image associated with a specific EnchantmentType.
     *
     * @param e The EnchantmentType for which to retrieve the image.
     * @return The corresponding BufferedImage, or null if not available.
     * @requires e is not null.
     * @modifies none
     * @effects 
     *   - Returns the image associated with the given EnchantmentType.
     */
    public BufferedImage getImage(EnchantmentType e){
        switch (e) {
            case EXTRA_TIME -> {
                return null;
            }
            case EXTRA_LIFE -> {
                return heartImage;
            }
            case REVEAL -> {
                return revealImage;
            }
            case CLOAK_OF_PROTECTION -> {
                return cloakImage;
            }
            case LURING_GEM -> {
                return gemImage;
            }
            case RUNE -> {
                return runeImage;
            }
        }
        return null;
    }

    public List<Enchantment> getEnchantments() {
        return this.enchantments;
    }
}