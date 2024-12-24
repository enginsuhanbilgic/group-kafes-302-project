package tr.edu.ku.comp302.domain.models.Monsters;

public class FighterMonster extends Monster {
    private long lastAttackTime = 0;

    public FighterMonster(int x, int y, int speed) {
        super(x, y, speed);
    }

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public void setLastAttackTime(long t) {
        lastAttackTime = t;
    }
}
