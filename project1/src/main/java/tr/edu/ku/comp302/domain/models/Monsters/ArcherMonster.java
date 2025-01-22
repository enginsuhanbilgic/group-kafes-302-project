package tr.edu.ku.comp302.domain.models.monsters;

public class ArcherMonster extends Monster {

    private int lastShotTime = 0;

    public ArcherMonster(int x, int y, int speed) {
        super(x, y, speed);
    }

    public long getLastShotTime() {
        return lastShotTime;
    }

    public void setLastShotTime(int lastShotTime) {
        this.lastShotTime = lastShotTime;
    }
}
