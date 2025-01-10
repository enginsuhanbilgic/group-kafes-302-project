import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Random;

public class CreateNewPlayerTest {

    private GameConfig mockGameConfig;
    private TilesController mockTilesController;
    private Random mockRandom;
    
    @Before
    public void setUp() {
        // Set up default GameConfig values (could also be real values).
        // For example:
        mockGameConfig = new GameConfig();
        mockGameConfig.TILE_SIZE = 32;
        mockGameConfig.KAFES_STARTING_X = 0;
        mockGameConfig.KAFES_STARTING_Y = 0;
        mockGameConfig.NUM_HALL_COLS = 10;
        mockGameConfig.NUM_HALL_ROWS = 10;
        mockGameConfig.PLAYER_SPEED = 3;
        
        // Set up a mocked TilesController
        mockTilesController = Mockito.mock(TilesController.class);

        // You can also mock Random if you want to force specific random values
        // for deterministic testing. For some tests, real random might be okay.
        mockRandom = Mockito.mock(Random.class);
    }

    /**
     * Test Case 1:
     * Verify that createNewPlayer() always returns a Player 
     * whose position is within the valid non-collidable cage boundaries.
     */
    @Test
    public void testCreateNewPlayer_ReturnsPlayerWithinBounds() {
        // Arrange
        // Suppose every tile except boundaries is non-collidable.
        // We'll mock getTileAt(x, y) to return non-collidable Tile for the valid area.
        int validMinX = (mockGameConfig.KAFES_STARTING_X + 1) * mockGameConfig.TILE_SIZE;
        int validMaxX = (mockGameConfig.KAFES_STARTING_X + mockGameConfig.NUM_HALL_COLS - 2) * mockGameConfig.TILE_SIZE;
        int validMinY = (mockGameConfig.KAFES_STARTING_Y + 1) * mockGameConfig.TILE_SIZE;
        int validMaxY = (mockGameConfig.KAFES_STARTING_Y + mockGameConfig.NUM_HALL_ROWS - 2) * mockGameConfig.TILE_SIZE;

        // Mock the TilesController to make every tile in that region non-collidable
        for (int tileX = (mockGameConfig.KAFES_STARTING_X + 1); 
             tileX < (mockGameConfig.KAFES_STARTING_X + mockGameConfig.NUM_HALL_COLS - 1); tileX++) {
            for (int tileY = (mockGameConfig.KAFES_STARTING_Y + 1); 
                 tileY < (mockGameConfig.KAFES_STARTING_Y + mockGameConfig.NUM_HALL_ROWS - 1); tileY++) {
                Tile tile = new Tile();
                tile.isCollidable = false;
                Mockito.when(mockTilesController.getTileAt(tileX, tileY)).thenReturn(tile);
            }
        }
        
        // Act
        Player player = createNewPlayerUnderTest();

        // Assert
        int px = player.getX();
        int py = player.getY();
        assertTrue("Player X should be within valid horizontal bounds", px >= validMinX && px < validMaxX);
        assertTrue("Player Y should be within valid vertical bounds",   py >= validMinY && py < validMaxY);
    }

    /**
     * Test Case 2:
     * Verify that if some tiles are collidable, the method still finds a valid tile.
     * We will mark a few internal tiles as collidable and ensure the player
     * does not spawn there.
     */
    @Test
    public void testCreateNewPlayer_SkipsCollidableTiles() {
        // Arrange
        int collidableX = 3;  // Example collidable tile inside the cage
        int collidableY = 3;
        
        // By default, return non-collidable unless it is the specific collidable tile
        Mockito.when(mockTilesController.getTileAt(Mockito.anyInt(), Mockito.anyInt()))
               .thenAnswer(invocation -> {
                   int x = invocation.getArgument(0);
                   int y = invocation.getArgument(1);
                   Tile t = new Tile();
                   t.isCollidable = (x == collidableX && y == collidableY);
                   return t;
               });

        // Act
        Player player = createNewPlayerUnderTest();

        // Assert
        // Ensure player is not spawned on the collidableX, collidableY tile
        // Considering tile coordinates from x/TILE_SIZE, y/TILE_SIZE
        int tileX = player.getX() / mockGameConfig.TILE_SIZE;
        int tileY = player.getY() / mockGameConfig.TILE_SIZE;
        
        assertFalse("Player should not spawn on the specific collidable tile.",
                    tileX == collidableX && tileY == collidableY);
    }

    /**
     * Test Case 3:
     * Verify that if the cage boundaries are configured such that only one tile is valid,
     * the method can still find and return that exact tile (and not loop infinitely).
     */
    @Test
    public void testCreateNewPlayer_OneValidTile() {
        // Arrange
        // Let's say there's exactly one valid tile at (tileX = 5, tileY = 5) 
        // within the bounding box. Everything else is collidable.
        Mockito.when(mockTilesController.getTileAt(Mockito.anyInt(), Mockito.anyInt()))
               .thenAnswer(invocation -> {
                   int x = invocation.getArgument(0);
                   int y = invocation.getArgument(1);
                   Tile t = new Tile();
                   // Only the tile at (5,5) is non-collidable
                   t.isCollidable = !(x == 5 && y == 5);
                   return t;
               });
        
        // Act
        Player player = createNewPlayerUnderTest();

        // Assert
        // The player must be exactly at tile (5,5)
        int tileX = player.getX() / mockGameConfig.TILE_SIZE;
        int tileY = player.getY() / mockGameConfig.TILE_SIZE;
        assertEquals("Player should spawn on the single valid tile's X", 5, tileX);
        assertEquals("Player should spawn on the single valid tile's Y", 5, tileY);
    }

    /**
     * Helper method that mimics the original createNewPlayer() 
     * but uses our mocks instead of real objects.
     */
    private Player createNewPlayerUnderTest() {
        Random random = mockRandom != null ? mockRandom : new Random();
        
        // If you want real random, comment out the line above and create a new Random()
        // Or you can keep mocking to produce deterministic results.
        
        // This essentially copies your original code logic but injects the mocks.
        mockTilesController.loadTiles();  // If loadTiles() does something essential, keep it.
        
        int minX = (mockGameConfig.KAFES_STARTING_X + 1) * mockGameConfig.TILE_SIZE;
        int maxX = (mockGameConfig.KAFES_STARTING_X + mockGameConfig.NUM_HALL_COLS - 2) * mockGameConfig.TILE_SIZE;
        int minY = (mockGameConfig.KAFES_STARTING_Y + 1) * mockGameConfig.TILE_SIZE;
        int maxY = (mockGameConfig.KAFES_STARTING_Y + mockGameConfig.NUM_HALL_ROWS - 2) * mockGameConfig.TILE_SIZE;

        int x, y;
        boolean validPosition = false;

        do {
            // If you mock Random, here you could define the behavior:
            // e.g. Mockito.when(mockRandom.nextInt(anyInt())).thenReturn(someValue);
            x = minX + random.nextInt(maxX - minX);
            y = minY + random.nextInt(maxY - minY);

            int tileX = x / mockGameConfig.TILE_SIZE;
            int tileY = y / mockGameConfig.TILE_SIZE;

            Tile tile = mockTilesController.getTileAt(tileX, tileY);
            if (tile != null && !tile.isCollidable) {
                validPosition = true;
            }
        } while (!validPosition);

        return new Player(x, y, mockGameConfig.PLAYER_SPEED);
    }
}
