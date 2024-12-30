package tr.edu.ku.comp302.domain.models.Monsters;

public class WizardMonster extends Monster {

    public WizardMonster(int x, int y, int speed) {
        super(x, y, speed);
    }

    // Possibly store last teleport time, 
    // but logic will be in MonsterController
    private int lastTeleportTime = 0;

    public int getLastTeleportTime() {
        return lastTeleportTime;
    }

    public void setLastTeleportTime(int t) {
        this.lastTeleportTime = t;
    }
}
