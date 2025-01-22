package tr.edu.ku.comp302.domain.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.Tile;
import tr.edu.ku.comp302.domain.models.monsters.FighterMonster;
import tr.edu.ku.comp302.domain.models.monsters.Monster;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * OVERVIEW:
 * Tests MonsterController with respect to monster spawning, clearing, and fighter adjacency logic.
 *
 * ABSTRACTION FUNCTION:
 * AF(c) = {
 *   monsters -> the list of active monsters in the hall,
 *   luringGemLocation -> the (x,y) of an active luring gem, or null,
 *   buildObjectController -> used by wizard to teleport runes,
 *   enchantmentController -> used to check if a location is available,
 *   inGameTime -> current time in seconds,
 *   lastSpawnTime -> time in seconds when a monster was last spawned
 * }
 *
 * REPRESENTATION INVARIANT:
 * 1) monsters is never null.
 * 2) spawn logic only occurs if (inGameTime - lastSpawnTime) >= spawnInterval.
 * 3) If a FighterMonster is adjacent to the Player, and enough time has elapsed since last attack,
 *    player.loseLife() is called.
 */
class MonsterControllerTest {

    private MonsterController monsterController;
    private TilesController mockTilesController;
    private BuildObjectController mockBuildController;
    private EnchantmentController mockEnchantController;
    private Player mockPlayer;

    @BeforeEach
    void setUp() {
        // Mock the dependent controllers
        mockTilesController = Mockito.mock(TilesController.class);
        mockBuildController = Mockito.mock(BuildObjectController.class);
        mockEnchantController = Mockito.mock(EnchantmentController.class);

        // Create the MonsterController with mocks
        monsterController = new MonsterController(mockTilesController, mockBuildController, 30);
        monsterController.setEnchantmentController(mockEnchantController);

        // Create a simple Player
        mockPlayer = new Player(100, 100, 3);
        mockPlayer.setLives(5); // Give the player some lives to lose
    }

    /**
     * Test Case 1:
     * Verifies that a monster is spawned after enough in-game time has passed,
     * and that it does NOT spawn if not enough time has passed.
     *
     * @requires 
     *   - No monsters initially in the list.
     *   - spawnIntervalSeconds set in GameConfig.
     * @modifies 
     *   - monsterController.monsters (adds a monster if conditions are met).
     * @effects 
     *   - Increases inGameTime, calls tick(), checks if a monster is spawned.
     */
    @Test
    void testSpawnMonsterAfterInterval() {
        // Initially, no monsters
        assertTrue(monsterController.getMonsters().isEmpty(), "Initially, monster list should be empty.");

        // When not enough time has passed, tick() should NOT spawn a monster.
        int partialTime = GameConfig.MONSTER_SPAWN_INTERVAL - 1; // e.g., if spawn interval is 12, we use 11
        monsterController.tick(partialTime, 30, mockPlayer);
        assertTrue(monsterController.getMonsters().isEmpty(),
                   "No monster should spawn before reaching the spawn interval.");

        // Mock TilesController & EnchantmentController so that location is free (no collision)
        when(mockTilesController.getTileAt(anyInt(), anyInt())).thenReturn(new Tile(null, false));
        when(mockEnchantController.isLocationAvailable(anyInt(), anyInt())).thenReturn(true);

        // Once we reach the spawn interval
        monsterController.tick(GameConfig.MONSTER_SPAWN_INTERVAL, 10, mockPlayer);

        // Now we expect exactly 1 monster to appear
        List<Monster> monsters = monsterController.getMonsters();
        assertEquals(1, monsters.size(),
                     "Exactly one monster should have spawned after hitting the spawn interval.");
    }

    /**
     * Test Case 2:
     * Verifies that a FighterMonster adjacent to the player causes the player to lose life
     * (assuming the monster's attack cooldown has elapsed).
     *
     * @requires 
     *   - We manually add a FighterMonster that is directly adjacent to player's tile.
     * @modifies 
     *   - player.lives if adjacency triggers an attack.
     * @effects 
     *   - Decreases player's life by 1, provided enough cooldown time has passed.
     */
    @Test
    void testFighterMonsterAdjacent_AttacksPlayer() {
        // Create a FighterMonster next to the player
        FighterMonster fighter = new FighterMonster(mockPlayer.getX() + GameConfig.TILE_SIZE, 
                                                    mockPlayer.getY(), 
                                                    1);  // speed = 1 tile
        // Attack cooldown
        fighter.setLastAttackTime(0);

        monsterController.getMonsters().add(fighter);

        // Player has 5 lives, monster is horizontally adjacent
        // so it should attack on updateAll() if time - lastAttackTime >= cooldown
        int inGameTime = GameConfig.MONSTER_ATTACK_COOLDOWN; // ensure enough time has passed
        int timeRemaining = 60;
        monsterController.updateAll(mockPlayer);

        // Check player's life
        assertEquals(4, mockPlayer.getLives(), 
                "Player should lose 1 life due to FighterMonster adjacency attack.");
    }

    /**
     * Test Case 3:
     * Verifies that clearMonsters() removes all monsters from the list.
     *
     * @requires 
     *   - The monsterController has some monsters in the list.
     * @modifies 
     *   - monsterController.monsters
     * @effects 
     *   - monsterController.monsters should be empty after clearMonsters().
     */
    @Test
    void testClearMonsters() {
        // Pre-populate with some monsters
        monsterController.getMonsters().add(new FighterMonster(50, 50, 1));
        monsterController.getMonsters().add(new FighterMonster(60, 60, 1));
        monsterController.getMonsters().add(new FighterMonster(70, 70, 1));

        assertFalse(monsterController.getMonsters().isEmpty(),
                    "Should have monsters before clearing.");

        monsterController.clearMonsters();

        assertTrue(monsterController.getMonsters().isEmpty(),
                   "Monster list should be empty after calling clearMonsters().");
    }
}
