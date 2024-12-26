package tr.edu.ku.comp302.domain.models.Monsters;

public class FighterMonster extends Monster {

    // Fields for the "walk-stop" cycle
    private long lastMoveCycleStart;
    private boolean isMoving; 
    private boolean hasPickedDirectionThisCycle;
    private int directionForThisCycle;

    private long lastAttackTime = 0;

    private boolean hasMovedThisCycle;

    public FighterMonster(int x, int y, int speed) {
        super(x, y, speed);
        this.lastMoveCycleStart = System.currentTimeMillis();
        this.isMoving = true; // start by moving 
        this.hasPickedDirectionThisCycle = false;
        this.hasMovedThisCycle = false;
        this.directionForThisCycle = -1;
    }

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public void setLastAttackTime(long t) {
        lastAttackTime = t;
    }
    public long getLastMoveCycleStart() {
        return lastMoveCycleStart;
    }

    public void setLastMoveCycleStart(long t) {
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
