package tr.edu.ku.comp302.domain.controllers;

import java.awt.Graphics2D;
import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Entity;
import tr.edu.ku.comp302.domain.models.Tile;

/**
 * The abstract controller for any Entity (Player, Monster, etc.).
 */
public abstract class EntityController<T extends Entity> {
    
    protected T entity;
    protected TilesController tilesController;

    public EntityController(T entity, TilesController tilesController) {
        this.entity = entity;
        this.tilesController = tilesController;
    }

    /**
     * Updates the entity's state. For Player, this might be reading keyboard input.
     * For a Monster, this might be AI logic.
     */
    public abstract void update();

    /**
     * Draws the entity. 
     */
    public abstract void draw(Graphics2D g2);

    /**
     * A collision check method that can be used by any entity
     * that moves on the tile map.
     *
     * @param x proposed new X coordinate
     * @param y proposed new Y coordinate
     * @return true if there is a collision at (x, y), false otherwise
     */
    protected boolean checkCollision(int x, int y) {
        int tileSize = GameConfig.TILE_SIZE;
        int collisionOffset = 8;

        // Calculate the bounding box of the entity
        int leftX = x + collisionOffset;
        int rightX = x + tileSize - 1 - collisionOffset;
        int topY = y + collisionOffset;
        int bottomY = y + tileSize - 1 - collisionOffset;

        // Convert them to tile indices
        int leftCol = leftX / tileSize;
        int rightCol = rightX / tileSize;
        int topRow = topY / tileSize;
        int bottomRow = bottomY / tileSize;

        // Check each tile the entity occupies for collision
        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                Tile tile = tilesController.getTileAt(col, row);
                if (tile != null && tile.isCollidable) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the entity controlled by this controller.
     *
     * @return The entity instance.
     */
    public T getEntity() {
        return entity;
    }
}