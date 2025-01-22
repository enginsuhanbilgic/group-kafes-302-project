package tr.edu.ku.comp302.domain.models;

import java.io.Serializable;

/**
 * A serializable data structure representing one tile's essential info
 * (e.g., collidability, type).
 */
public class TileData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tileTypeKey;  // e.g. "FLOOR_PLAIN", "WALL_CENTER", "TRANSPARENT_COLLIDABLE"
    private boolean collidable;

    public TileData(String tileTypeKey, boolean collidable) {
        this.tileTypeKey = tileTypeKey;
        this.collidable = collidable;
    }

    public String getTileTypeKey() {
        return tileTypeKey;
    }

    public boolean isCollidable() {
        return collidable;
    }
}