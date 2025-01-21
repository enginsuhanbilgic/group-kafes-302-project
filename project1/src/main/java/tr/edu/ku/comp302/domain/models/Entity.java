package tr.edu.ku.comp302.domain.models;

import java.io.Serializable;

public abstract class Entity implements Serializable {

    /** Entity's X and Y positions on the screen. */
    protected int x, y;

    /** Entity's movement speed. */
    protected int speed;

    /**
     * Constructor to initialize the position and speed of the entity.
     *
     * @param x      Initial X-coordinate of the entity.
     * @param y      Initial Y-coordinate of the entity.
     * @param speed  Movement speed of the entity.
     */
    public Entity(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    // Getters and setters for position and speed
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }
} 