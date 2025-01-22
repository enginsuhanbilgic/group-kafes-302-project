package tr.edu.ku.comp302.domain.models;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * Represents a single tile in the game environment.
 * A tile consists of an image and a collidable property (for now).
 */
public class Tile implements Serializable {

    //The image representing the visual appearance of the tile.
    public BufferedImage image;

    //Indicates whether the tile is collidable (solid) or not.
    public boolean isCollidable;

    /**
     * Constructs a Tile object with a specified image and collidable property.
     *
     * @param image        The image to display for this tile.
     * @param isCollidable Whether this tile is solid or not.
     */
    public Tile(BufferedImage image, boolean isCollidable) {
        this.image = image;
        this.isCollidable = isCollidable;
    }
}