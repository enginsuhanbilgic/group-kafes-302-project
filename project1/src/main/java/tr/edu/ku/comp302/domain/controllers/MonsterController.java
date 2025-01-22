package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.Tile;
import tr.edu.ku.comp302.domain.models.monsters.*;
import tr.edu.ku.comp302.domain.models.monsters.strategies.WizardHighTimeStrategy;
import tr.edu.ku.comp302.domain.models.monsters.strategies.WizardLowTimeStrategy;
import tr.edu.ku.comp302.domain.models.monsters.strategies.WizardMidTimeStrategy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private int initialTime;
    private int timePassed = 0;
    private int timeRemaining;

    private BufferedImage fighterImage;
    private BufferedImage archerImage;
    private BufferedImage wizardImage;
    private BufferedImage luringGemImage;

    //In game time count
    private int lastSpawnTime;
    private static int spawnIntervalSeconds = GameConfig.MONSTER_SPAWN_INTERVAL;

    /**
     * NEW FIELD: The (x,y) in pixels where a Luring Gem was thrown. Null if no gem is active.
     */
    private Point luringGemLocation = null;
    private int gemSpawnTime = -1;

    public MonsterController(TilesController tilesController, BuildObjectController buildObjectController, int initialTime) {
        this.tilesController = tilesController;
        this.buildObjectController = buildObjectController;
        this.enchantmentController = null;
        this.monsters = new CopyOnWriteArrayList<>();
        this.random = new Random();
        this.lastSpawnTime = 0;
        this.initialTime = initialTime;
        this.timeRemaining = initialTime;

        fighterImage = ResourceManager.getImage("npc_fighter");
        archerImage  = ResourceManager.getImage("npc_archer");
        wizardImage  = ResourceManager.getImage("npc_wizard");
        luringGemImage = ResourceManager.getImage("thrown_enchantment_gem");
    }

    public void setEnchantmentController(EnchantmentController enchantmentController){
        this.enchantmentController = enchantmentController;
    }

    /**
     * Called every game frame. Updates all monsters, spawns new ones if needed, etc.
     */
    public void updateAll(Player player) {

        if(!monsters.isEmpty()){
            for (Monster m : monsters) {
                if (m instanceof FighterMonster fighter) {
                    updateFighter(fighter, player);
                }
                else if (m instanceof ArcherMonster archer) {
                    updateArcher(archer, player);
                }
                else if (m instanceof WizardMonster wizard) {
                    updateWizard(wizard, player);
                }
            }

            for (Monster m : monsters) {
                if ((m instanceof WizardMonster w) && w.shouldDisappear()) {
                    revertMonsterTile(m);
                }
            }

            monsters.removeIf(m -> {
                if (m instanceof WizardMonster w) {
                    return w.shouldDisappear();
                }
                return false;
            });
        }

        
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
        if(luringGemLocation!=null){
            g2.drawImage(luringGemImage, (int) (luringGemLocation.getX()), (int) (luringGemLocation.getY()), 
                    GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
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

    private void updateFighter(FighterMonster fighter, Player player) {
        // 1) Check adjacency
        if (isAdjacentToPlayer(fighter, player) && luringGemLocation==null) {
            long elapsed = timePassed - fighter.getLastAttackTime();
            if (elapsed >= GameConfig.MONSTER_ATTACK_COOLDOWN) {
                // Attack
                player.loseLife();
                System.out.println("Fighter Monster stabbed the hero! Lives: " + player.getLives());
                // Reset cooldown
                fighter.setLastAttackTime(timePassed);
            }
            return;
        }

        if (hasLuringGem()){
            doOneStepTowardGem(fighter);
        }else{
            int dx = player.getX() - fighter.getX();
            int dy = player.getY() - fighter.getY();
            double distToPlayer = Math.sqrt(dx * dx + dy * dy);

            if (distToPlayer <= GameConfig.FIGHTER_CHASE_DISTANCE*GameConfig.TILE_SIZE) {
                // Within chase distance => move toward player
                doOneStepTowardPlayer(fighter, player);
            } else {
                // Use your old movement logic (random moves, etc.)
                handleMovementCycle(fighter, player, timePassed);
            }
        }

    }

    private void handleMovementCycle(FighterMonster fighter, Player player, int timePassed) {
        int elapsedInCycle = timePassed - fighter.getLastMoveCycleStart();
        if (elapsedInCycle >= 2) {
            fighter.setLastMoveCycleStart(timePassed);
            fighter.setMoving(true);
            elapsedInCycle = 0;
        }
        if (elapsedInCycle < 1) {
            fighter.setMoving(true);

            if (!fighter.hasPickedDirectionThisCycle()) {
                int direction = random.nextInt(4);
                fighter.setDirectionForThisCycle(direction);
                fighter.setHasPickedDirectionThisCycle(true);
            }
            doOneStepInStoredDirection(fighter);

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
    
        int speed = fighter.getSpeed();
        double distx = (fighter.getX() - luringGemLocation.getX()) * GameConfig.TILE_SIZE;
        double disty = (fighter.getY() - luringGemLocation.getY()) * GameConfig.TILE_SIZE;
        double dist = Math.sqrt(distx * distx + disty * disty);
        System.out.println("Distance before: " + dist);
        if (dist <= speed*GameConfig.TILE_SIZE) {
            System.out.println("Dist: " + dist);
            System.out.println("Fighter Monster reached the gem!");
            clearLuringGemLocation();
            return;
        }
        
        if (luringGemLocation!=null){
            double dx = luringGemLocation.getX() - (double) fighter.getX();
            double dy = luringGemLocation.getY() - (double) fighter.getY();
            double length = Math.sqrt(dx * dx + dy * dy);

            if (length != 0) {
                dx = (dx / (length/2)) * (double) speed;
                dy = (dy / (length/2)) * (double) speed;
            }
        
            int newX = fighter.getX() + (int) Math.round(dx);
            if(!checkCollision(newX, fighter.getY())){
                fighter.setX(newX);
            }

            int newY = fighter.getY() + (int) Math.round(dy);
            if(!checkCollision(fighter.getX(), newY)){
                fighter.setY(newY);
            }
        }

    }

    private void doOneStepTowardPlayer(FighterMonster fighter, Player player) {
        int speed = fighter.getSpeed();
    
        int dx = player.getX() - fighter.getX();
        int dy = player.getY() - fighter.getY();
    
        double length = Math.sqrt(dx*dx + dy*dy);
        if (length != 0) {
            // Move "speed" pixels/tiles in that direction
            dx = (int) Math.round(dx / length * speed);
            dy = (int) Math.round(dy / length * speed);
        }
    
        int newX = fighter.getX() + dx;
        int newY = fighter.getY() + dy;
    
        // Check collisions before committing
        if (!checkCollision(newX, newY)) {
            fighter.setX(newX);
            fighter.setY(newY);
        }
    }
    

    // =========================================================
    //                ARCHER AND WIZARD UPDATES
    // =========================================================

    private void updateArcher(ArcherMonster archer, Player player) {
        // He shoots every MONSTER_ATTACK_COOLDOWN if hero in range, unless hero has cloak
        long lastShotTime = archer.getLastShotTime();
        if (timePassed - lastShotTime >= GameConfig.MONSTER_ATTACK_COOLDOWN) {
            archer.setLastShotTime(timePassed);

            boolean heroHasCloak = player.isCloakActive();
            
            // Merkez noktaları
            double archerCenterX = archer.getX() + GameConfig.TILE_SIZE / 2.0;
            double archerCenterY = archer.getY() + GameConfig.TILE_SIZE / 2.0;
            double playerCenterX = player.getX() + GameConfig.TILE_SIZE / 2.0;
            double playerCenterY = player.getY() + GameConfig.TILE_SIZE / 2.0;
            
            // Öklid mesafesi hesaplama
            double distance = Math.sqrt(
                Math.pow(archerCenterX - playerCenterX, 2) + 
                Math.pow(archerCenterY - playerCenterY, 2)
            );
            
            // Mesafe 3 kare olsun
            double maxRange = 3.0 * GameConfig.TILE_SIZE;
            
            if (!heroHasCloak && distance < maxRange) {
                player.loseLife();
                System.out.println("Archer Monster shot the hero! Lives: " + player.getLives());
            }
        }
    }

    private void updateWizard(WizardMonster wizard, Player player) {

        // 1) Determine the current time-left percentage
        double ratio = (timeRemaining * 100.0) / initialTime;

        // 2) Choose strategy if ratio changed to a different bracket
        //    or if wizard has no strategy yet.
        WizardMonster w = wizard;  // just a short alias
        if (w.getStrategy() == null) {
            setStrategyByRatio(w, ratio);
        } else {
            // Check if ratio crosses thresholds
            if ((ratio < 30 && !(w.getStrategy() instanceof WizardLowTimeStrategy)) ||
                (ratio > 70 && !(w.getStrategy() instanceof WizardHighTimeStrategy)) ||
                (ratio >= 30 && ratio <= 70 && !(w.getStrategy() instanceof WizardMidTimeStrategy))) {
                setStrategyByRatio(w, ratio);
            }
        }

        // 3) Let the strategy do its thing
        if (w.getStrategy() != null) {
            w.getStrategy().updateBehavior(
                w, 
                player, 
                this, 
                buildObjectController, 
                timePassed, 
                initialTime, 
                timeRemaining
            );
        }
    }

    private void setStrategyByRatio(WizardMonster wizard, double ratio) {
        if (ratio < 30) {
            wizard.setStrategy(new WizardLowTimeStrategy(), timePassed);
            System.out.println("Wizard: switching to LowTime strategy.");
        } else if (ratio > 70) {
            wizard.setStrategy(new WizardHighTimeStrategy(), timePassed);
            System.out.println("Wizard: switching to HighTime strategy.");
        } else {
            wizard.setStrategy(new WizardMidTimeStrategy(), timePassed);
            System.out.println("Wizard: switching to MidTime strategy.");
        }
    }

    public void teleportPlayerToRandomEmptyLocation(Player player) {
        int tileSize = GameConfig.TILE_SIZE;
        int mapWidth = GameConfig.NUM_HALL_COLS;
        int mapHeight = GameConfig.NUM_HALL_ROWS;

        // We'll try up to 50 times to find a free tile
        for (int attempt = 0; attempt < 50; attempt++) {
            int col = random.nextInt(mapWidth);
            int row = random.nextInt(mapHeight);

            Tile tile = tilesController.getTileAt(col + GameConfig.KAFES_STARTING_X, row + GameConfig.KAFES_STARTING_Y);
            if (tile != null && !tile.isCollidable && 
                enchantmentController.isLocationAvailable(col, row)) 
            {
                // Teleport player here
                player.setX((col + GameConfig.KAFES_STARTING_X) * tileSize);
                player.setY((row + GameConfig.KAFES_STARTING_Y) * tileSize);
                player.setVelocityX(0);
                player.setVelocityY(0);
                System.out.println("Wizard teleported the hero to a random location!");
                return;
            }
        }
    }

    // =========================================================
    //                   MONSTER SPAWNING
    // =========================================================

    private void spawnRandomMonster(int timePassed, Player player) {
        int tileSize = GameConfig.TILE_SIZE;
        int mapWidth = GameConfig.NUM_HALL_COLS;
        int mapHeight = GameConfig.NUM_HALL_ROWS;

        for (int attempt = 0; attempt < 50; attempt++) {
            int col = random.nextInt(mapWidth);
            int row = random.nextInt(mapHeight);
            
            Boolean isPlayerNear = false;
            if (Math.sqrt(
                    Math.pow(player.getX() - col * GameConfig.TILE_SIZE, 2) +
                    Math.pow(player.getY() - row * GameConfig.TILE_SIZE, 2)
                ) <= GameConfig.TILE_SIZE * 2) {
                isPlayerNear = true;
            }

            Tile tile = tilesController.getTileAt(col + GameConfig.KAFES_STARTING_X, row + GameConfig.KAFES_STARTING_Y);
            if (tile != null && !tile.isCollidable && enchantmentController.isLocationAvailable(col, row) && !isPlayerNear) {
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
            case 2 -> new WizardMonster(x, y, 0, timePassed);
            default -> null;
        };
    }

    public void tick(int timePassed2, int timeRemaining2, Player player) {
        this.timePassed = timePassed2;
        this.timeRemaining = timeRemaining2;
        if(timePassed - lastSpawnTime >= spawnIntervalSeconds) {
            spawnRandomMonster(timePassed, player);
            lastSpawnTime = timePassed;
        }
        if (luringGemLocation!=null && timePassed-gemSpawnTime>=GameConfig.GEM_LIFETIME_SECONDS){
            luringGemLocation=null;
            gemSpawnTime=-1;
        }
    }

    private void revertMonsterTile(Monster m) {
        int tileSize = GameConfig.TILE_SIZE;
        int col = m.getX() / tileSize;
        int row = m.getY() / tileSize;
        System.out.println("Col: " + col + " " + "Row: " + row);
        tilesController.setFloorTileAt(col, row);
    }

    // =========================================================
    //               HELPER METHODS + GEM LOGIC
    // =========================================================


    /**
     * Clear the gem location (when reached or expired).
     */
    public void clearLuringGemLocation() {
        this.luringGemLocation = null;
    }

    public boolean hasLuringGem() {
        return (luringGemLocation != null);
    }

    public Point getLuringGemLocation() {
        return luringGemLocation;
    }

    public int getGemSpawnTime() {
        return gemSpawnTime;
    }

    public void setGemSpawnTime(int gemSpawnTime) {
        this.gemSpawnTime = gemSpawnTime;
    }
    
    // When we throw a gem:
    public void setLuringGemLocation(Point gemLoc) {
        this.luringGemLocation = gemLoc;
        this.gemSpawnTime = timePassed; // store the current time in this field
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
        // Grid bazlı mesafe hesaplama
        int monsterCol = monster.getX() / GameConfig.TILE_SIZE;
        int monsterRow = monster.getY() / GameConfig.TILE_SIZE;
        int playerCol = player.getX() / GameConfig.TILE_SIZE;
        int playerRow = player.getY() / GameConfig.TILE_SIZE;
        
        // Tam yanında olma kontrolü (yatay veya dikey komşu)
        return (Math.abs(monsterCol - playerCol) + Math.abs(monsterRow - playerRow)) <= 1;
    }

    private int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public List<Monster> getMonsters() {
        return monsters;
    }
}