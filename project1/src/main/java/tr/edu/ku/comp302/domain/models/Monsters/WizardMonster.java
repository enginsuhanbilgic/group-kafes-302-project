package tr.edu.ku.comp302.domain.models.Monsters;

public class WizardMonster extends Monster {

    public WizardMonster(int x, int y, int speed) {
        super(x, y, speed);
    }

    // Possibly store last teleport time, 
    // but logic will be in MonsterController
    private long lastTeleportTime = 0;

    public long getLastTeleportTime() {
        return lastTeleportTime;
    }

    public void setLastTeleportTime(long t) {
        this.lastTeleportTime = t;
    }
}
