package tr.edu.ku.comp302.domain.models;

import java.io.Serializable;
import java.util.Random;
import tr.edu.ku.comp302.config.GameConfig;

public class BuildObject implements Serializable {
    private int x;              // Grid coordinate (top-left)
    private int y;              // Grid coordinate (top-left)
    private String objectType;  // "box", "chest", "skull", etc.
    private boolean hasRune;
    private int offset = 0;
    private transient Random random; // Marked as transient and removed 'final'

    // Constructor for creating new BuildObjects
    public BuildObject(int x, int y, String objectType) {
        this.x = x;
        this.y = y;
        this.objectType = objectType;
        this.hasRune = false;
        this.random = new Random();
        this.offset = random.nextInt(GameConfig.TILE_SIZE * 3);
    }

    // Default no-args constructor required for Gson deserialization
    public BuildObject() {
        this.random = new Random();
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public String getObjectType() { return objectType; }
    public boolean getHasRune() { return this.hasRune; }
    public int getOffset() { return this.offset; }

    // Setter for hasRune with correct condition
    public void setHasRune(boolean val) {
        if (!this.hasRune && val) {
            this.offset = random.nextInt(GameConfig.TILE_SIZE * 3);
        }
        this.hasRune = val;
    }

    // Optional: Method to reinitialize Random after deserialization
    public void initializeRandom() {
        if (this.random == null) {
            this.random = new Random();
        }
    }

    @Override
    public String toString() {
        return "BuildObject{" +
                "x=" + x +
                ", y=" + y +
                ", type='" + objectType + '\'' +
                ", hasRune=" + hasRune +
                '}';
    }
}