package tr.edu.ku.comp302.domain.models.Monsters;

public class ArcherMonster extends Monster {

    private long lastShotTime = 0;

    public ArcherMonster(int x, int y, int speed) {
        super(x, y, speed);
    }

    public long getLastShotTime() {
        return lastShotTime;
    }

    public void setLastShotTime(long lastShotTime) {
        this.lastShotTime = lastShotTime;
    }
}
