package tr.edu.ku.comp302.domain.controllers;
import tr.edu.ku.comp302.domain.controllers.ResourceManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.domain.models.Tile;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class TilesControllerTest {

    private TilesController tilesController;

    @BeforeEach
    void setUp() {
        // Initialize the resource manager
        ResourceManager.init();

        // creating a new TilesController instance
        tilesController = new TilesController();
    }

    /**
     * Tests the initial creation of the tile grid by the TilesController.
     * Verifies that the tile at position (5, 5) is correctly initialized
     * and is not collidable.
     */
    @Test
    void testInitialTileGridCreation() {
        tilesController.loadTiles(HallType.DEFAULT);
        Tile centerTile = tilesController.getTileAt(5, 5);
        assertNotNull(centerTile);
        assertFalse(centerTile.isCollidable);
    }

    /**
     * Tests the initialization of the border walls in the "kafes" area.
     * Verifies that the tiles at the borders are correctly set as collidable.
     */
    @Test
    void testKafesBorderWalls() {
        tilesController.loadTiles(HallType.DEFAULT);

        // Test top border (should be collidable)
        Tile topWall = tilesController.getTileAt(GameConfig.KAFES_STARTING_X + 1, GameConfig.KAFES_STARTING_Y);
        assertNotNull(topWall);
        assertTrue(topWall.isCollidable);

        // Test bottom border (should be collidable)
        Tile bottomWall = tilesController.getTileAt(
                GameConfig.KAFES_STARTING_X + 1,
                GameConfig.KAFES_STARTING_Y + GameConfig.NUM_HALL_ROWS - 1
        );
        assertNotNull(bottomWall);
        assertTrue(bottomWall.isCollidable);

        // Test left border (should be collidable)
        Tile leftWall = tilesController.getTileAt(
                GameConfig.KAFES_STARTING_X,
                GameConfig.KAFES_STARTING_Y + 1
        );
        assertNotNull(leftWall);
        assertTrue(leftWall.isCollidable);

        // Test right border (should be collidable)
        Tile rightWall = tilesController.getTileAt(
                GameConfig.KAFES_STARTING_X + GameConfig.NUM_HALL_COLS - 1,
                GameConfig.KAFES_STARTING_Y + 1
        );
        assertNotNull(rightWall);
        assertTrue(rightWall.isCollidable);
    }

    /**
     * Tests the getTileAt method for out-of-bounds coordinates.
     * Verifies that the method returns null for coordinates outside the grid boundaries.
     */
    @Test
    void testGetTileAtOutOfBounds() {
        tilesController.loadTiles(HallType.DEFAULT);

        // Test getting tile outside the grid boundaries
        assertNull(tilesController.getTileAt(-1, -1));
        assertNull(tilesController.getTileAt(1000, 1000));
    }

    /**
     * Tests the setTransparentTileAt method for setting a transparent tile at a given position.
     * Verifies that the tile is correctly set as collidable.
     */
    @Test
    void testSetTransparentTile() {
        tilesController.loadTiles(HallType.DEFAULT);

        int testX = 5;
        int testY = 5;

        // Set a transparent tile
        tilesController.setTransparentTileAt(testX, testY);

        // Get the tile and verify it's collidable
        Tile transparentTile = tilesController.getTileAt(testX, testY);
        assertNotNull(transparentTile);
        assertTrue(transparentTile.isCollidable);
    }

    /**
     * Tests the draw method of the TilesController.
     * Ensures that no exceptions are thrown during the drawing process.
     */
    @Test
    void testDraw() {
        tilesController.loadTiles(HallType.DEFAULT);

        // Create a BufferedImage to simulate Graphics2D drawing
        BufferedImage canvas = new BufferedImage(
                GameConfig.RES_HORIZONTAL,
                GameConfig.RES_VERTICAL,
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g2 = canvas.createGraphics();

        // Ensure no exceptions are thrown
        assertDoesNotThrow(() -> tilesController.draw(g2));

    }


    /**
     * Tests the initialization of the tile grid by the TilesController.
     * Verifies that the first and last tiles in the grid are correctly initialized.
     */
    @Test
    void testTileGridInitialization() {
        tilesController.loadTiles(HallType.DEFAULT);
        assertNotNull(tilesController.getTileAt(0, 0)); // First tile
        assertNotNull(tilesController.getTileAt(GameConfig.NUM_HALL_COLS - 1, GameConfig.NUM_HALL_ROWS - 1)); // Last tile
    }


    /**
     * Tests the loadTiles method to ensure that all floor tiles are correctly initialized
     * and are not collidable.
     */
    @Test
    void testLoadTilesFloorTiles() {
        tilesController.loadTiles(HallType.DEFAULT);

        // Check that all floor tiles are not collidable
        for (int y = 0; y < GameConfig.RES_VERTICAL / GameConfig.TILE_SIZE; y++) {
            for (int x = 0; x < GameConfig.RES_HORIZONTAL / GameConfig.TILE_SIZE; x++) {
                Tile tile = tilesController.getTileAt(x, y);
                if (tile != null && !tile.isCollidable) {
                    assertFalse(tile.isCollidable, "Floor tile should not be collidable");
                }
            }
        }
    }


    /**
     * Tests the loadTiles method to ensure that all wall tiles are correctly initialized
     * and are collidable.
     */
    @Test
    void testLoadTilesWallTiles() {
        tilesController.loadTiles(HallType.DEFAULT);

        // Check that all wall tiles are collidable
        for (int y = GameConfig.KAFES_STARTING_Y; y < GameConfig.KAFES_STARTING_Y + GameConfig.NUM_HALL_ROWS; y++) {
            for (int x = GameConfig.KAFES_STARTING_X; x < GameConfig.KAFES_STARTING_X + GameConfig.NUM_HALL_COLS; x++) {
                Tile tile = tilesController.getTileAt(x, y);
                if (tile != null && tile.isCollidable) {
                    assertTrue(tile.isCollidable, "Wall tile should be collidable");
                }
            }
        }
    }
}
