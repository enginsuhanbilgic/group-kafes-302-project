package tr.edu.ku.comp302.domain.models;

import java.io.Serializable;

public enum HallType implements Serializable {
    EARTH,
    AIR,
    WATER,
    FIRE,
    DEFAULT;

    public String toText() {
        switch (this) {
            case EARTH:
                return "Earth Hall";
            case AIR:
                return "Air Hall";
            case WATER:
                return "Water Hall";
            case FIRE:
                return "Fire Hall";
            default:
                throw new IllegalArgumentException("Unknown Hall Type: " + this);
        }
    }
}