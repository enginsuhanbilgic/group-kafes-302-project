package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Enchantments.*;
import tr.edu.ku.comp302.domain.models.Monsters.Monster;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.Tile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * EnchantmentController handles spawning of enchantments, 
 * despawns them if not collected, and collects them via mouse click.
 */
public class EnchantmentController {

    private final List<Enchantment> enchantments;
    private final Random random;
    private final MonsterController monsterController;
    private long lastSpawnTime;
    private final long SPAWN_INTERVAL = 12_000; // 12 seconds

    private BufferedImage extraTimeImage;
    private BufferedImage heartImage;
    private BufferedImage revealImage;
    private BufferedImage cloakImage;
    private BufferedImage gemImage;
    private BufferedImage runeImage;

    public EnchantmentController(MonsterController monsterController) {
        this.enchantments = new ArrayList<>();
        this.random = new Random();
        this.lastSpawnTime = System.currentTimeMillis();
        this.monsterController = monsterController;

        try {
        extraTimeImage = ImageIO.read(getClass().getResourceAsStream("/assets/enchantment_extratime.png"));
        heartImage = ImageIO.read(getClass().getResourceAsStream("/assets/enchantment_heart.png"));
        revealImage = ImageIO.read(getClass().getResourceAsStream("/assets/enchantment_reveal.png"));
        cloakImage = ImageIO.read(getClass().getResourceAsStream("/assets/enchantment_cloak.png"));
        gemImage = ImageIO.read(getClass().getResourceAsStream("/assets/enchantment_gem.png"));
        runeImage = ImageIO.read(getClass().getResourceAsStream("/assets/rune.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called every frame from PlayModeController to update enchantments.
     * - Spawns new enchantment if 12s has passed.
     * - Removes expired.
     * - Checks if user left-clicked on an enchantment to collect it.
     */
    public void update(Player player, TilesController tilesController, Point clickPos) {
        long now = System.currentTimeMillis();

        // 1) Spawn check
        if (now - lastSpawnTime >= SPAWN_INTERVAL) {
            spawnRandomEnchantment(tilesController);
            lastSpawnTime = now;
        }

        // 2) Remove expired
        enchantments.removeIf(Enchantment::isExpired);

        // 3) Check for user left-click
        if (clickPos != null) {
            handleClickCollection(clickPos, player);
        }
    }

    /**
     * Draw the current enchantments on the screen.
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

    private void spawnRandomEnchantment(TilesController tilesController) {
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
                long now = System.currentTimeMillis();
                Enchantment e = createRandomEnchantment((col + GameConfig.KAFES_STARTING_X) * tileSize, (row + GameConfig.KAFES_STARTING_Y) * tileSize, now);
                enchantments.add(e);
                System.out.println("[Spawn] " + e.getType() + " at col=" + col + ", row=" + row);
                return;
            }
        }
    }

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

    private Enchantment createRandomEnchantment(int x, int y, long spawnTime) {
        int r = random.nextInt(5); // 0..4
        return switch (r) {
            case 0 -> new ExtraTimeEnchantment(x, y, spawnTime);
            case 1 -> new ExtraLifeEnchantment(x, y, spawnTime);
            case 2 -> new RevealEnchantment(x, y, spawnTime);
            case 3 -> new CloakOfProtectionEnchantment(x, y, spawnTime);
            case 4 -> new LuringGemEnchantment(x, y, spawnTime);
            default -> null;
        };
    }

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
}
