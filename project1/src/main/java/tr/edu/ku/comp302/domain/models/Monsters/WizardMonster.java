package tr.edu.ku.comp302.domain.models.monsters;

import tr.edu.ku.comp302.domain.models.monsters.strategies.WizardStrategy;

import java.io.Serializable;

public class WizardMonster extends Monster implements Serializable {

    // ========== NEW FIELDS ==========
    private WizardStrategy strategy; 
    private int spawnTime;
    private boolean shouldDisappear = false;
    private boolean hasTeleportedPlayer = false;
    private int lastRuneTeleportTime = 0;
    private int strategyStartTime;

    public WizardMonster(int x, int y, int speed, int spawnTime) {
        super(x, y, speed);
        this.spawnTime = spawnTime;
        this.strategyStartTime = spawnTime;
    }

    // Strategy pattern: wizard delegates its logic to a Strategy
    public WizardStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(WizardStrategy strategy, int timePassed) {
        this.strategy = strategy;
        this.strategyStartTime = timePassed;
    }

    // ========== GETTERS/SETTERS ==========

    public int getSpawnTime() {
        return spawnTime;
    }

    public boolean shouldDisappear() {
        return shouldDisappear;
    }

    public void setShouldDisappear(boolean shouldDisappear) {
        this.shouldDisappear = shouldDisappear;
    }

    public boolean hasTeleportedPlayer() {
        return hasTeleportedPlayer;
    }

    public void setHasTeleportedPlayer(boolean hasTeleportedPlayer) {
        this.hasTeleportedPlayer = hasTeleportedPlayer;
    }

    public int getLastRuneTeleportTime() {
        return lastRuneTeleportTime;
    }

    public void setLastRuneTeleportTime(int lastRuneTeleportTime) {
        this.lastRuneTeleportTime = lastRuneTeleportTime;
    }

    public int getStrategyStartTime(){
        return this.strategyStartTime;
    }
}