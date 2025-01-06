package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.Tile;
import tr.edu.ku.comp302.domain.models.BuildObject;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.domain.models.Monsters.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * MonsterController
 * Manages spawning, updating, and drawing of monsters.
 */
public class MonsterController {

    private final TilesController tilesController;
    private final BuildObjectController buildObjectController;
    private EnchantmentController enchantmentController;
    private final List<Monster> monsters;
    private final Random random;

    private BufferedImage fighterImage;
    private BufferedImage archerImage;
    private BufferedImage wizardImage;

    //In game time count
    private int lastSpawnTime;
    private static int spawnIntervalSeconds = GameConfig.MONSTER_SPAWN_INTERVAL;

    /**
     * NEW FIELD: The (x,y) in pixels where a Luring Gem was thrown. Null if no gem is active.
     */
    private Point luringGemLocation = null;

    public MonsterController(TilesController tilesController, BuildObjectController buildObjectController) {
        this.tilesController = tilesController;
        this.buildObjectController = buildObjectController;
        this.enchantmentController = null;
        this.monsters = new ArrayList<>();
        this.random = new Random();
        this.lastSpawnTime = 0;

        fighterImage = ResourceManager.getImage("npc_fighter");
        archerImage  = ResourceManager.getImage("npc_archer");
        wizardImage  = ResourceManager.getImage("npc_wizard");
    }

    public void setEnchantmentController(EnchantmentController enchantmentController){
        this.enchantmentController = enchantmentController;
    }

    /**
     * Called every game frame. Updates all monsters, spawns new ones if needed, etc.
     */
    public void updateAll(Player player, int inGameTime) {

        // 2) Update each monster's behavior
        for (Monster m : monsters) {
            if (m instanceof FighterMonster fighter) {
                updateFighter(fighter, player, inGameTime);
            }
            else if (m instanceof ArcherMonster archer) {
                updateArcher(archer, player, inGameTime);
            }
            else if (m instanceof WizardMonster wizard) {
                updateWizard(wizard, player, inGameTime);
            }
        }

        // 3) You can remove dead monsters, etc., if you wanted.
    }

    /**
     * Draw all monsters on the screen.
     */
    public void drawAll(Graphics2D g2) {
        for (Monster m : monsters) {
            if (m instanceof FighterMonster) {
                if (fighterImage != null) {
                    g2.drawImage(fighterImage, m.getX(), m.getY(),
                            GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
                }
            }
            else if (m instanceof ArcherMonster) {
                if (archerImage != null) {
                    g2.drawImage(archerImage, m.getX(), m.getY(),
                            GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
                }
            }
            else if (m instanceof WizardMonster) {
                if (wizardImage != null) {
                    g2.drawImage(wizardImage, m.getX(), m.getY(),
                            GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
                }
            }
            else {
                // fallback if some other monster type
                g2.setColor(Color.GRAY);
                g2.fillRect(m.getX(), m.getY(), GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
            }
        }
    }

    /**
     * If the hero moves to the next hall, we can clear the list
     * so they won't follow.
     */
    public void clearMonsters() {
        monsters.clear();
    }

    // =========================================================
    //              FIGHTER MONSTER UPDATE
    // =========================================================

    private void updateFighter(FighterMonster fighter, Player player, int inGameTime) {
        // 1) Check adjacency
        if (isAdjacentToPlayer(fighter, player)) {
            long elapsed = inGameTime - fighter.getLastAttackTime();
            if (elapsed >= GameConfig.MONSTER_ATTACK_COOLDOWN) {
                // Attack
                player.loseLife();
                System.out.println("Fighter Monster stabbed the hero! Lives: " + player.getLives());
                // Reset cooldown
                fighter.setLastAttackTime(inGameTime);
            }
        } else {
            // If not adjacent, handle normal or gem logic
            handleMovementCycle(fighter, player, inGameTime);
        }
    }

    private void handleMovementCycle(FighterMonster fighter, Player player, int inGameTime) {
        int elapsedInCycle = inGameTime - fighter.getLastMoveCycleStart();
        if (elapsedInCycle >= 2) {
            fighter.setLastMoveCycleStart(inGameTime);
            fighter.setMoving(true);
            elapsedInCycle = 0;
        }
        if (elapsedInCycle < 1) {
            fighter.setMoving(true);

            // If a Luring Gem is active, chase gem
            if (hasLuringGem()) {
                if (!fighter.hasMovedThisCycle()) {
                    doOneStepTowardGem(fighter);
                    fighter.setHasMovedThisCycle(true);
                }
            }
            // Otherwise, do random movement or chase the player if you want:
            else {
                if (!fighter.hasPickedDirectionThisCycle()) {
                    int direction = random.nextInt(4);
                    fighter.setDirectionForThisCycle(direction);
                    fighter.setHasPickedDirectionThisCycle(true);
                }
                doOneStepInStoredDirection(fighter);
            }
        } else {
            fighter.setMoving(false);
            fighter.setHasPickedDirectionThisCycle(false);
            fighter.setHasMovedThisCycle(false);
        }
    }

    private void doOneStepInStoredDirection(FighterMonster fighter) {
        int direction = fighter.getDirectionForThisCycle();
        int newX = fighter.getX();
        int newY = fighter.getY();

        int speed = fighter.getSpeed(); // typically 1 tile
        switch (direction) {
            case 0 -> newY -= speed; // up
            case 1 -> newY += speed; // down
            case 2 -> newX -= speed; // left
            case 3 -> newX += speed; // right
        }

        if (!checkCollision(newX, newY)) {
            fighter.setX(newX);
            fighter.setY(newY);
        }
    }

    /**
     * Move one tile in the direction of the Luring Gem.
     * If the monster is adjacent to gem, remove the gem.
     */
    private void doOneStepTowardGem(FighterMonster fighter) {
        if (luringGemLocation == null) return;

        int tileSize = GameConfig.TILE_SIZE;
        // Convert monster's coords to tile coords
        int fighterCol = fighter.getX() / tileSize;
        int fighterRow = fighter.getY() / tileSize;

        // Convert gem's coords to tile coords
        int gemCol = luringGemLocation.x / tileSize;
        int gemRow = luringGemLocation.y / tileSize;

        // Check distance in pixels to see if we've "reached" it
        int distPx = manhattanDistance(fighter.getX(), fighter.getY(), luringGemLocation.x, luringGemLocation.y);
        if (distPx <= tileSize) {
            System.out.println("Fighter Monster reached the gem!");
            clearLuringGemLocation();
            return;
        }

        // Move 1 tile toward gem
        int dCol = 0;
        int dRow = 0;
        if (gemCol > fighterCol) dCol = 1;
        else if (gemCol < fighterCol) dCol = -1;
        if (gemRow > fighterRow) dRow = 1;
        else if (gemRow < fighterRow) dRow = -1;

        int newCol = fighterCol + dCol;
        int newRow = fighterRow + dRow;
        int newX = newCol * tileSize;
        int newY = newRow * tileSize;

        if (!checkCollision(newX, newY)) {
            fighter.setX(newX);
            fighter.setY(newY);
        }
    }

    // =========================================================
    //                ARCHER AND WIZARD UPDATES
    // =========================================================

    private void updateArcher(ArcherMonster archer, Player player, int inGameTime) {
        // He shoots every MONSTER_ATTACK_COOLDOWN if hero in range (4 tiles), unless hero has cloak
        long lastShotTime = archer.getLastShotTime();
        if (inGameTime - lastShotTime >= GameConfig.MONSTER_ATTACK_COOLDOWN) {
            archer.setLastShotTime(inGameTime);

            boolean heroHasCloak = player.isCloakActive();
            int distancePx = manhattanDistance(archer.getX(), archer.getY(), player.getX(), player.getY());
            int rangePx = 4 * GameConfig.TILE_SIZE;
            if (!heroHasCloak && distancePx < rangePx) {
                player.loseLife();
                System.out.println("Archer Monster shot the hero! Lives: " + player.getLives());
            }
        }
    }

    private void updateWizard(WizardMonster wizard, Player player, int inGameTime) {
        // Teleport rune every 5 seconds
        int lastTeleportTime = wizard.getLastTeleportTime();
        if (inGameTime - lastTeleportTime >= 5) {
            wizard.setLastTeleportTime(inGameTime);
            buildObjectController.transferRune();
            System.out.println("Wizard teleported the rune!");
        }
    }

    // =========================================================
    //                   MONSTER SPAWNING
    // =========================================================

    public void tick(int inGameTime) {
        inGameTime++;
        if (inGameTime - lastSpawnTime >= spawnIntervalSeconds) {
            spawnRandomMonster(inGameTime);
            lastSpawnTime = inGameTime;
        }
    }

    private void spawnRandomMonster(int inGameTime) {
        int tileSize = GameConfig.TILE_SIZE;
        int mapWidth = GameConfig.NUM_HALL_COLS;
        int mapHeight = GameConfig.NUM_HALL_ROWS;

        for (int attempt = 0; attempt < 50; attempt++) {
            int col = random.nextInt(mapWidth);
            int row = random.nextInt(mapHeight);

            Tile tile = tilesController.getTileAt(col + GameConfig.KAFES_STARTING_X, row + GameConfig.KAFES_STARTING_Y);
            if (tile != null && !tile.isCollidable && enchantmentController.isLocationAvailable(col, row)) {
                Monster monster = createRandomMonster((col + GameConfig.KAFES_STARTING_X) * tileSize,
                        (row + GameConfig.KAFES_STARTING_Y) * tileSize);
                monsters.add(monster);

                if (!(monster instanceof FighterMonster))
                    tilesController.setTransparentTileAt((col + GameConfig.KAFES_STARTING_X),
                            (row + GameConfig.KAFES_STARTING_Y));
                System.out.println("Spawned " + monster.getClass().getSimpleName()
                        + " at col=" + col + ", row=" + row);
                return;
            }
        }
    }

    private Monster createRandomMonster(int x, int y) {
        int r = random.nextInt(3); // 0=Fighter, 1=Archer, 2=Wizard
        return switch (r) {
            case 0 -> new FighterMonster(x, y, 1);
            case 1 -> new ArcherMonster(x, y, 0);
            case 2 -> new WizardMonster(x, y, 0);
            default -> null;
        };
    }

    // =========================================================
    //               HELPER METHODS + GEM LOGIC
    // =========================================================

    /**
     * Set the location of the thrown Luring Gem.
     */
    public void setLuringGemLocation(Point gemLoc) {
        this.luringGemLocation = gemLoc;
    }

    /**
     * Clear the gem location (when reached or expired).
     */
    public void clearLuringGemLocation() {
        this.luringGemLocation = null;
    }

    /**
     * Whether we currently have a gem thrown.
     */
    public boolean hasLuringGem() {
        return (luringGemLocation != null);
    }

    /**
     * Return the gem location (or null).
     */
    public Point getLuringGemLocation() {
        return luringGemLocation;
    }

    private boolean checkCollision(int x, int y) {
        int tileSize = GameConfig.TILE_SIZE;

        int leftX   = x;
        int rightX  = x + tileSize - 1;
        int topY    = y;
        int bottomY = y + tileSize - 1;

        int leftCol   = leftX / tileSize;
        int rightCol  = rightX / tileSize;
        int topRow    = topY / tileSize;
        int bottomRow = bottomY / tileSize;

        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                Tile tile = tilesController.getTileAt(col, row);
                if (tile != null && tile.isCollidable) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAdjacentToPlayer(Monster monster, Player player) {
        int tileSize = GameConfig.TILE_SIZE;
        int dx = Math.abs(monster.getX() - player.getX());
        int dy = Math.abs(monster.getY() - player.getY());
        return (dx <= tileSize && dy <= tileSize);
    }

    private int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
}
