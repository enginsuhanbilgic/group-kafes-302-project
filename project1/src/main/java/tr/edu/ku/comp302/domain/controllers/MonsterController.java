package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.*;
import tr.edu.ku.comp302.domain.models.Monsters.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
            // else: other monster types, if any
        }

        // 3) You can also remove dead monsters, check transitions to next hall, etc.
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
    

    // =========================================================
    //                 SUB-METHODS: PER MONSTER TYPE
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
            handleMovementCycle(fighter, player, inGameTime);
        }
    }

    private void updateArcher(ArcherMonster archer, Player player, int inGameTime) {
        // He shoots every 1 second if hero in range (4 tiles), unless hero has cloak
        long lastShotTime = archer.getLastShotTime();
        if (inGameTime - lastShotTime >= GameConfig.MONSTER_ATTACK_COOLDOWN) {
            // Time to shoot
            archer.setLastShotTime(inGameTime);

            boolean heroHasCloak = player.isCloakActive();  // if you store cloak state in player
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
            // e.g., teleport the rune
            buildObjectController.transferRune();
            System.out.println("Wizard teleported the rune!");
        }
    }

    // =========================================================
    //               SPAWNING & COLLISION CHECKS
    // =========================================================

    private void spawnRandomMonster(int inGameTime) {
        int tileSize = GameConfig.TILE_SIZE;
        int mapWidth = GameConfig.NUM_HALL_COLS;
        int mapHeight = GameConfig.NUM_HALL_ROWS;

        // We'll try a few times to find a free tile
        for (int attempt = 0; attempt < 50; attempt++) {
            int col = random.nextInt(mapWidth);
            int row = random.nextInt(mapHeight);

            Tile tile = tilesController.getTileAt(col+GameConfig.KAFES_STARTING_X, row+GameConfig.KAFES_STARTING_Y);
            if (tile != null && !tile.isCollidable && enchantmentController.isLocationAvailable(col, row)) {
                // pick a random monster type
                Monster monster = createRandomMonster((col + GameConfig.KAFES_STARTING_X) * tileSize, (row + GameConfig.KAFES_STARTING_Y) * tileSize);
                monsters.add(monster);
                if(!(monster instanceof FighterMonster))
                tilesController.setTransparentTileAt((col + GameConfig.KAFES_STARTING_X), (row + GameConfig.KAFES_STARTING_Y));
                System.out.println("Spawned " + monster.getClass().getSimpleName() +
                                   " at col=" + col + ", row=" + row);
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

    public void tick(int inGameTime) {
        inGameTime++;
        if (inGameTime - lastSpawnTime >= spawnIntervalSeconds) {
            spawnRandomMonster(inGameTime);
            lastSpawnTime = inGameTime;
        }
    }

    // =========================================================
    //                  HELPER METHODS
    // =========================================================

    //BOZUK DÜZELTİLECEK
    private void handleMovementCycle(FighterMonster fighter, Player player, int inGameTime) {
        int elapsedInCycle = inGameTime - fighter.getLastMoveCycleStart();
        if (elapsedInCycle >= 2) {
            fighter.setLastMoveCycleStart(inGameTime);
            fighter.setMoving(true);
            elapsedInCycle = 0;
        }
        if (elapsedInCycle < 1) {
            fighter.setMoving(true);
            /*if (manhattanDistance(fighter.getX(), fighter.getY(), player.getX(), player.getY()) < 2*GameConfig.TILE_SIZE) {
                if (!fighter.hasMovedThisCycle()) {
                    System.out.println("Player coordinates: " + player.getX() + " " + player.getY());
                    System.out.println("Monster coordinates: " + fighter.getX() + " " + fighter.getY());
                    doOneStepTowardPlayer(fighter, player);
                    fighter.setHasMovedThisCycle(true);
                }
            } else {*/
                // random
                if (!fighter.hasPickedDirectionThisCycle()) {
                    int direction = random.nextInt(4);
                    fighter.setDirectionForThisCycle(direction);
                    fighter.setHasPickedDirectionThisCycle(true);
                }
                doOneStepInStoredDirection(fighter);
            //}
        } else {
            fighter.setMoving(false);
            fighter.setHasPickedDirectionThisCycle(false);
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
    
    //BOZUK DÜZELTİLECEK
    private void doOneStepTowardPlayer(FighterMonster fighter, Player player) {
        int tileSize = GameConfig.TILE_SIZE;
    
        // Convert fighter's pixel coords to tile coords
        int fighterCol = fighter.getX() / tileSize - GameConfig.KAFES_STARTING_X;
        int fighterRow = fighter.getY() / tileSize - GameConfig.KAFES_STARTING_Y;
    
        // Convert player's pixel coords to tile coords
        int playerCol = player.getX() / tileSize - GameConfig.KAFES_STARTING_X;
        int playerRow = player.getY() / tileSize - GameConfig.KAFES_STARTING_Y;
    
        // Move just 1 tile in the direction of the player
        int dCol = 0;
        int dRow = 0;
    
        if (playerCol > fighterCol) dCol = 1;
        else if (playerCol < fighterCol) dCol = -1;
    
        if (playerRow > fighterRow) dRow = 1;
        else if (playerRow < fighterRow) dRow = -1;
    
        // Proposed new tile coords: exactly one tile step
        int newCol = fighterCol + dCol;
        int newRow = fighterRow + dRow;
    
        // Convert back to pixel coords
        int newX = (newCol + GameConfig.KAFES_STARTING_X) * tileSize;
        int newY = (newRow + GameConfig.KAFES_STARTING_Y) * tileSize;
    
        // Check collision
        if (!checkCollision(newX, newY)) {
            fighter.setX(newX);
            fighter.setY(newY);
        }
    }
    
    

    /**
     * A bounding box approach that checks if the monster's new bounding box 
     * intersects a collidable tile. If yes, returns true for collision.
     */
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
        // Fighter monster must be right next to hero 
        // (within 1 tile in x,y or so).
        // Let's do a simple bounding box approach:
        int tileSize = GameConfig.TILE_SIZE;
        int dx = Math.abs(monster.getX() - player.getX());
        int dy = Math.abs(monster.getY() - player.getY());
        // If truly adjacent in tile terms:
        return (dx <= tileSize && dy <= tileSize);
    }

    private int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    /**
     * If the hero moves to the next hall, we can clear the list
     * so they won't follow. 
     */
    public void clearMonsters() {
        monsters.clear();
    }
}
