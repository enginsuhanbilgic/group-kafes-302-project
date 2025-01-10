package tr.edu.ku.comp302.domain.models.Monsters;

import tr.edu.ku.comp302.domain.models.Entity;

public abstract class Monster extends Entity {

    public Monster(int x, int y, int speed) {
        super(x, y, speed);
    }

    public String getType() {
        if (this instanceof FighterMonster) return "FIGHTER";
        if (this instanceof ArcherMonster) return "ARCHER";
        if (this instanceof WizardMonster) return "WIZARD";
        return "UNKNOWN";
    }
}
