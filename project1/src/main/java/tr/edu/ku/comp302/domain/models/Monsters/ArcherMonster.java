package tr.edu.ku.comp302.domain.models.monsters;

import java.io.Serializable;

public class ArcherMonster extends Monster implements Serializable {

    private int lastShotTime = 0;

    public ArcherMonster(int x, int y, int speed) {
        super(x, y, speed);
    }

    // Add a no-arg constructor for the deserializer
    public ArcherMonster() {
        super(0, 0, 0);
    }

    public long getLastShotTime() {
        return lastShotTime;
    }

    public void setLastShotTime(int lastShotTime) {
        this.lastShotTime = lastShotTime;
    }


}