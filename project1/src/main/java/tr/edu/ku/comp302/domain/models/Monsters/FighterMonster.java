package tr.edu.ku.comp302.domain.models.monsters;

import java.io.Serializable;

public class FighterMonster extends Monster implements Serializable {

    // Fields for the "walk-stop" cycle
    private int lastMoveCycleStart;
    private boolean isMoving; 
    private boolean hasPickedDirectionThisCycle;
    private int directionForThisCycle;

    private int lastAttackTime;

    private boolean hasMovedThisCycle;

    public FighterMonster(int x, int y, int speed) {
        super(x, y, speed);
        this.isMoving = true; // start by moving 
        this.hasPickedDirectionThisCycle = false;
        this.hasMovedThisCycle = false;
        this.directionForThisCycle = -1;
        this.lastAttackTime = 0;
        this.lastMoveCycleStart = 0;

    }

    public int getLastAttackTime() {
        return lastAttackTime;
    }

    public void setLastAttackTime(int t) {
        lastAttackTime = t;
    }
    public int getLastMoveCycleStart() {
        return lastMoveCycleStart;
    }

    public void setLastMoveCycleStart(int t) {
        this.lastMoveCycleStart = t;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        this.isMoving = moving;
    }

    public boolean hasPickedDirectionThisCycle() { return hasPickedDirectionThisCycle; }
    public void setHasPickedDirectionThisCycle(boolean val) { this.hasPickedDirectionThisCycle = val; }

    public int getDirectionForThisCycle() { return directionForThisCycle; }
    public void setDirectionForThisCycle(int dir) { this.directionForThisCycle = dir; }

    public boolean hasMovedThisCycle() { return this.hasMovedThisCycle;}
    public void setHasMovedThisCycle(boolean val) {this.hasMovedThisCycle = val;}
}