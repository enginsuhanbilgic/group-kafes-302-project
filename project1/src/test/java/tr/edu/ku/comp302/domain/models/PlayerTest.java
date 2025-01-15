package tr.edu.ku.comp302.domain.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.controllers.MonsterController;
import tr.edu.ku.comp302.domain.models.enchantments.EnchantmentType;
import tr.edu.ku.comp302.domain.models.enchantments.LuringGemEnchantment;

import java.awt.*;

class PlayerTest {

    private Player player;
    private MonsterController monsterController;


    /**
     * Test valid usage of the Luring Gem.
     *
     * Requires:
     * - The player has a Luring Gem in their inventory.
     * - A valid direction ('W', 'A', 'S', 'D') is provided.
     *
     * Modifies:
     * - The player's inventory (removes the Luring Gem).
     * - The MonsterController's gem location.
     *
     * Effects:
     * - Removes the Luring Gem from the player's inventory.
     * - Sets the gem location in the MonsterController based on the player's position and direction.
     */

    @BeforeEach
    void setUp() {
        player = new Player(64, 64, 3);
        monsterController = new MonsterController(null, null, 30);
    }

    @Test
    void testUseLuringGem_ValidUsage() {
        // Arrange: Add a LuringGemEnchantment to the player's inventory using its dedicated class
        LuringGemEnchantment luringGem = new LuringGemEnchantment(0, 0, 0);
        luringGem.onCollect(player);  // Adds the gem to the inventory

        // Act: Use the gem with a valid direction
        player.useLuringGem('W', monsterController);

        // Assert: The gem should be removed from the inventory and set in the monster controller
        assertFalse(player.getInventory().hasEnchantment(EnchantmentType.LURING_GEM),
                "Luring Gem should be removed after use.");
        
        // Verify the gem location was updated correctly
        Point gemLocation = monsterController.getLuringGemLocation();
        int expectedY = player.getY() - (2 * GameConfig.TILE_SIZE);
        assertNotNull(gemLocation, "Gem location should be set.");
        assertEquals(player.getX(), gemLocation.x, "X-coordinate should match the player’s adjusted position.");
        assertEquals(expectedY, gemLocation.y, "Y-coordinate should match the offset position.");
    }

    @Test
    void testUseLuringGem_NoGemAvailable() {
        // Act: Attempt to use the gem when no gem is available
        player.useLuringGem('S', monsterController);

        // Assert: No changes should occur, as there was no gem to use
        assertFalse(player.getInventory().hasEnchantment(EnchantmentType.LURING_GEM),
                "Inventory should still be empty.");
        assertNull(monsterController.getLuringGemLocation(), 
                "No gem location should be set without a gem.");
    }

    @Test
    void testUseLuringGem_InvalidDirection() {
        // Arrange: Add a LuringGemEnchantment to the player's inventory using its dedicated class
        LuringGemEnchantment luringGem = new LuringGemEnchantment(0, 0, 0);
        luringGem.onCollect(player);  // Adds the gem to the inventory

        // Act: Try using the gem with an invalid direction
        player.useLuringGem('X', monsterController);

        // Assert: The gem should remain in the inventory and no gem location should be set
        assertTrue(player.getInventory().hasEnchantment(EnchantmentType.LURING_GEM),
                "Gem should remain in the inventory after invalid usage.");
        assertNull(monsterController.getLuringGemLocation(),
                "No gem location should be set with an invalid direction.");
    }
}
