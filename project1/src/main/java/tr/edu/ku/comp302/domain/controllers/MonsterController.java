package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.*;
import tr.edu.ku.comp302.domain.models.Monsters.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

public class MonsterController {

    private final TilesController tilesController;
    private final List<Monster> monsters;
    private final Random random;

    private BufferedImage fighterImage;
    private BufferedImage archerImage;
    private BufferedImage wizardImage;

    // For example, spawn a new monster every 8 seconds, etc.
    private long lastSpawnTime;
    private final long SPAWN_INTERVAL = 8000; // 8 seconds
    private final long ATTACK_COOLDOWN = 1000; // 1 second

    public MonsterController(TilesController tilesController) {
        this.tilesController = tilesController;
        this.monsters = new ArrayList<>();
        this.random = new Random();
        this.lastSpawnTime = System.currentTimeMillis();

        try {
        fighterImage = ImageIO.read(getClass().getResourceAsStream("/assets/npc_fighter.png"));
        archerImage = ImageIO.read(getClass().getResourceAsStream("/assets/npc_archer.png"));
        wizardImage = ImageIO.read(getClass().getResourceAsStream("/assets/npc_wizard.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called every game frame. Updates all monsters, spawns new ones if needed, etc.
     */
    public void updateAll(Player player) {
        long now = System.currentTimeMillis();

        // 1) Possibly spawn new monster if enough time passed
        if (now - lastSpawnTime >= SPAWN_INTERVAL) {
            spawnRandomMonster();
            lastSpawnTime = now;
        }

        // 2) Update each monster's behavior
        for (Monster m : monsters) {
            if (m instanceof FighterMonster fighter) {
                updateFighter(fighter, player, now);
            } 
            else if (m instanceof ArcherMonster archer) {
                updateArcher(archer, player, now);
            } 
            else if (m instanceof WizardMonster wizard) {
                updateWizard(wizard, player, now);
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

    private void updateFighter(FighterMonster fighter, Player player, long now) {
        // 1) Check adjacency
        if (isAdjacentToPlayer(fighter, player)) {
            long elapsed = now - fighter.getLastAttackTime();
            if (elapsed >= ATTACK_COOLDOWN) {
                // Attack
                player.loseLife();
                System.out.println("Fighter Monster stabbed the hero! Lives: " + player.getLives());
                // Reset cooldown
                fighter.setLastAttackTime(now);
        }
        }
        // 2) Possibly do random walk
        //    We'll do it every 1 second (example). We can store lastMoveTime in the monster if we want,
        //    or just do random movement every frame for demonstration:
        randomWalk(fighter);
    }

    private void updateArcher(ArcherMonster archer, Player player, long now) {
        // He shoots every 1 second if hero in range (4 tiles), unless hero has cloak
        long lastShotTime = archer.getLastShotTime();
        if (now - lastShotTime >= 1000) {
            // Time to shoot
            archer.setLastShotTime(now);

            boolean heroHasCloak = player.isCloakActive();  // if you store cloak state in player
            int distancePx = manhattanDistance(archer.getX(), archer.getY(), player.getX(), player.getY());
            int rangePx = 4 * GameConfig.TILE_SIZE;
            if (!heroHasCloak && distancePx < rangePx) {
                player.loseLife();
                System.out.println("Archer Monster shot the hero! Lives: " + player.getLives());
            }
        }
    }

    private void updateWizard(WizardMonster wizard, Player player, long now) {
        // Teleport rune every 5 seconds
        long lastTeleportTime = wizard.getLastTeleportTime();
        if (now - lastTeleportTime >= 5000) {
            wizard.setLastTeleportTime(now);
            // e.g., teleport the rune
            //tilesController.teleportRuneRandomly();
            System.out.println("Wizard teleported the rune!");
        }
    }

    // =========================================================
    //               SPAWNING & COLLISION CHECKS
    // =========================================================

    private void spawnRandomMonster() {
        int tileSize = GameConfig.TILE_SIZE;
        int mapWidth = GameConfig.NUM_HALL_COLS;
        int mapHeight = GameConfig.NUM_HALL_ROWS;

        // We'll try a few times to find a free tile
        for (int attempt = 0; attempt < 50; attempt++) {
            int col = random.nextInt(mapWidth);
            int row = random.nextInt(mapHeight);

            Tile tile = tilesController.getTileAt(col+GameConfig.KAFES_STARTING_Y, row+GameConfig.KAFES_STARTING_X);
            if (tile != null && !tile.isCollidable) {
                // pick a random monster type
                Monster monster = createRandomMonster((col + GameConfig.KAFES_STARTING_Y) * tileSize, (row + GameConfig.KAFES_STARTING_X) * tileSize);
                monsters.add(monster);
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
            case 1 -> new ArcherMonster(x, y, 2);
            case 2 -> new WizardMonster(x, y, 0);
            default -> null;
        };
    }

    // =========================================================
    //                  HELPER METHODS
    // =========================================================

    /**
     * Example random walk for FighterMonster.
     * Calls checkCollision(...) first. 
     */
    private void randomWalk(Monster monster) {
        /*int direction = random.nextInt(4); // 0=up,1=down,2=left,3=right
        int newX = monster.getX();
        int newY = monster.getY();

        int speed = monster.getSpeed();
        switch (direction) {
            case 0 -> newY -= speed;
            case 1 -> newY += speed;
            case 2 -> newX -= speed;
            case 3 -> newX += speed;
        }
        // Check collision with the environment
        if (!checkCollision(newX, newY)) {
            monster.setX(newX);
            monster.setY(newY);
        }*/
    }

    /**
     * Collision check logic pulled from EntityController approach.
     * We'll do a bounding box approach. If the new bounding box intersects a collidable tile,
     * return true for collision. Here we invert so false means "no collision, movement allowed".
     */
    private boolean checkCollision(int x, int y) {
        int tileSize = GameConfig.TILE_SIZE;

        int leftX = x;
        int rightX = x + tileSize - 1;
        int topY = y;
        int bottomY = y + tileSize - 1;

        int leftCol = leftX / tileSize;
        int rightCol = rightX / tileSize;
        int topRow = topY / tileSize;
        int bottomRow = bottomY / tileSize;

        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                Tile tile = tilesController.getTileAt(col, row);
                if (tile != null && tile.isCollidable) {
                    return true; // collision
                }
            }
        }
        return false; // no collision
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
