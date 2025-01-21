package tr.edu.ku.comp302.domain.models;

import tr.edu.ku.comp302.domain.models.enchantments.Enchantment;
import tr.edu.ku.comp302.domain.models.Monsters.Monster;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A serializable snapshot of the entire game so we can save and load it.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private HallType currentHall;

    // Player data
    private Player player;

    // Monsters currently in the hall
    private List<Monster> monsters;

    // All active enchantments in the hall
    private List<Enchantment> enchantments;

    // The BuildObjects in all halls
    private Map<HallType, List<BuildObject>> worldObjectsMap;

    // Time counters
    private int timeRemaining;
    private int timePassed;
    private int initialTime;

    public GameState() {
    }

    // Getters and Setters
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
}
