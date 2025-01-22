package tr.edu.ku.comp302.domain.models;

import tr.edu.ku.comp302.domain.models.enchantments.Enchantment;
import tr.edu.ku.comp302.domain.models.monsters.Monster;

import java.awt.Point;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A serializable snapshot of the entire game so we can save and load it.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    // Which hall we are currently in
    private HallType currentHall;

    // Player data (including position, lives, velocity, cloak times, inventory)
    private Player player;

    // List of active monsters in the current hall
    private List<Monster> monsters;

    // All active enchantments in the current hall
    private List<Enchantment> enchantments;

    // The BuildObjects in all halls
    private Map<HallType, List<BuildObject>> worldObjectsMap;

    // Timers
    private int timeRemaining;
    private int timePassed;
    private int initialTime;

    // -- NEW FIELDS FOR LURING GEM --
    private boolean hasLuringGem;       // monsterController.hasLuringGem()
    private Point luringGemLocation;    // monsterController.getLuringGemLocation()
    private int gemSpawnTime;           // monsterController.getGemSpawnTime()

    private TileData[][] tileDataGrid; // same size as your TilesController

    public GameState() {
    }

    // Getters / Setters
    public HallType getCurrentHall() {
        return currentHall;
    }
    public void setCurrentHall(HallType currentHall) {
        this.currentHall = currentHall;
    }

    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<Monster> getMonsters() {
        return monsters;
    }
    public void setMonsters(List<Monster> monsters) {
        this.monsters = monsters;
    }

    public List<Enchantment> getEnchantments() {
        return enchantments;
    }
    public void setEnchantments(List<Enchantment> enchantments) {
        this.enchantments = enchantments;
    }

    public Map<HallType, List<BuildObject>> getWorldObjectsMap() {
        return worldObjectsMap;
    }
    public void setWorldObjectsMap(Map<HallType, List<BuildObject>> worldObjectsMap) {
        this.worldObjectsMap = worldObjectsMap;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }
    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public int getTimePassed() {
        return timePassed;
    }
    public void setTimePassed(int timePassed) {
        this.timePassed = timePassed;
    }

    public int getInitialTime() {
        return initialTime;
    }
    public void setInitialTime(int initialTime) {
        this.initialTime = initialTime;
    }

    public boolean isHasLuringGem() {
        return hasLuringGem;
    }
    public void setHasLuringGem(boolean hasLuringGem) {
        this.hasLuringGem = hasLuringGem;
    }

    public Point getLuringGemLocation() {
        return luringGemLocation;
    }
    public void setLuringGemLocation(Point luringGemLocation) {
        this.luringGemLocation = luringGemLocation;
    }

    public int getGemSpawnTime() {
        return gemSpawnTime;
    }
    public void setGemSpawnTime(int gemSpawnTime) {
        this.gemSpawnTime = gemSpawnTime;
    }
    public TileData[][] getTileDataGrid() {
        return tileDataGrid;
    }
    public void setTileDataGrid(TileData[][] tileDataGrid) {
        this.tileDataGrid = tileDataGrid;
    }
}