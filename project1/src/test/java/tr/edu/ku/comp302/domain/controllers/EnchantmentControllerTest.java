package tr.edu.ku.comp302.domain.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.Tile;
import tr.edu.ku.comp302.domain.models.enchantments.*;

import java.awt.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * OVERVIEW:
 * Tests the EnchantmentController class, ensuring that enchantments are spawned, despawned, and collected correctly.
 *
 * ABSTRACTION FUNCTION:
 * AF(c) = {
 *     enchantments: List of active enchantments managed by the controller,
 *     lastSpawnTime: The last in-game time when an enchantment was spawned
 * }
 *
 * REPRESENTATION INVARIANT:
 * - enchantments is not null.
 * - Each enchantment in enchantments has a unique position.
 * - lastSpawnTime is non-negative.
 */
class EnchantmentControllerTest {

    private EnchantmentController enchantmentController;
    private TilesController mockTilesController;
    private Player mockPlayer;

    @BeforeEach
    void setUp() {
        // Mock the TilesController
        mockTilesController = mock(TilesController.class);

        // Initialize EnchantmentController with mocked TilesController
        enchantmentController = new EnchantmentController(mockTilesController);

        // Initialize a Player instance
        mockPlayer = new Player(100, 100, 3); // Position arbitrary for tests
    }

    /**
     * Test Case 1: Verify that a new enchantment is spawned after the spawn interval.
     *
     * @requires 
     *   - No enchantments are present initially.
     *   - spawn interval has passed.
     *   - A free tile is available for spawning.
     * @modifies enchantmentController.enchantments, enchantmentController.lastSpawnTime
     * @effects 
     *   - A new enchantment is added to enchantments list.
     */
    @Test
    void testSpawnEnchantment_AfterSpawnInterval() {
        // Arrange
        int initialTime = 0;
        enchantmentController.tick(initialTime);

        // Setup TilesController to return a non-collidable tile and location is available
        when(mockTilesController.getTileAt(anyInt(), anyInt())).thenReturn(new Tile(null, false));

        // Act
        int spawnTime = GameConfig.ENCHANTMENT_SPAWN_INTERVAL;
        enchantmentController.tick(spawnTime); // Trigger spawn

        // Assert
        List<Enchantment> activeEnchantments = enchantmentController.getEnchantments();
        assertEquals(1, activeEnchantments.size(), "A new enchantment should be spawned after spawn interval.");

        Enchantment spawned = activeEnchantments.get(0);
        assertNotNull(spawned, "Spawned enchantment should not be null.");
        assertTrue(spawned.getLifetimeSeconds() > 0, "Spawned enchantment should have a positive lifetime.");

        // Verify lastSpawnTime was updated
        assertEquals(spawnTime, enchantmentController.lastSpawnTime, "lastSpawnTime should be updated to spawnTime.");
    }

    /**
     * Test Case 2: Verify that expired enchantments are removed after their lifetime.
     *
     * @requires 
     *   - At least one enchantment is present with a known spawn time.
     *   - Current in-game time exceeds the enchantment's lifetime.
     * @modifies enchantmentController.enchantments
     * @effects 
     *   - Expired enchantments are removed from enchantments list.
     */
    @Test
    void testDespawnEnchantment_AfterLifetime() {
        // Arrange
        int spawnTime = 10;
        Enchantment enchantment = new ExtraTimeEnchantment(200, 200, spawnTime); // Lifetime: 10s
        enchantmentController.enchantments.add(enchantment);

        // Act
        int currentTime = spawnTime + enchantment.getLifetimeSeconds() + 1; // Exceeds lifetime
        enchantmentController.tick(currentTime);

        // Assert
        List<Enchantment> activeEnchantments = enchantmentController.getEnchantments();
        assertFalse(activeEnchantments.contains(enchantment), "Expired enchantment should be removed.");
    }

    /**
     * Test Case 3: Verify that clicking on an enchantment collects it and adds it to the player's inventory.
     *
     * @requires 
     *   - An enchantment is present at the clicked location.
     *   - Player's inventory can accept the enchantment.
     * @modifies enchantmentController.enchantments, player.inventory
     * @effects 
     *   - The clicked enchantment is removed from enchantments list.
     *   - The enchantment is added to player's inventory via its onCollect method.
     */
    @Test
    void testCollectEnchantment_OnClick() {
        // Arrange
        Enchantment enchantment = new CloakOfProtectionEnchantment(150, 150, 20); // Positioned at (150,150)
        enchantmentController.enchantments.add(enchantment);

        // Define click position within the enchantment's bounds
        Point clickPos = new Point(150 + GameConfig.TILE_SIZE / 2, 150 + GameConfig.TILE_SIZE / 2);

        // Act
        enchantmentController.update(mockPlayer, clickPos);

        // Assert
        // The enchantment should be removed from the controller
        List<Enchantment> activeEnchantments = enchantmentController.getEnchantments();
        assertFalse(activeEnchantments.contains(enchantment), "Enchantment should be removed after collection.");

        // The enchantment should be added to the player's inventory
        assertTrue(mockPlayer.getInventory().hasEnchantment(enchantment.getType()), 
                   "Player should have the collected enchantment in inventory.");
    }
}
