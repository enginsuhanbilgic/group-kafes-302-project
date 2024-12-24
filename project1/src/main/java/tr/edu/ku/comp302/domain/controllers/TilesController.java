package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Tile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * TilesController is responsible for managing the grid of tiles in the game.
 * It initializes and renders the grid, including floor tiles and wall tiles,
 * to create the game environment.
 */
public class TilesController {

    private Tile[][] tileGrid; // 2D array representing the grid of tiles

    private final int tileSize = GameConfig.TILE_SIZE; // Size of a single tile in pixels

    // Dimensions of the screen (calculated from screen resolution and tile size)
    private final int maxRows = (int) GameConfig.RES_VERTICAL / tileSize; // Number of rows based on screen height
    private final int maxCols = (int) GameConfig.RES_HORIZONTAL / tileSize; // Number of columns based on screen width

    // Dimensions for the "kafes" (hall or inner grid)
    private final int kafesRows = GameConfig.NUM_HALL_ROWS; // Number of rows for the inner area
    private final int kafesCols = GameConfig.NUM_HALL_COLS; // Number of columns for the inner area

    private final int startingX = GameConfig.KAFES_STARTING_X;
    private final int startingY = GameConfig.KAFES_STARTING_Y;

    /**
     * Constructor: Initializes the TilesController with dimensions for the kafes area.
     */
    public TilesController() {
        // Initialize the grid with the maximum screen rows and columns
        tileGrid = new Tile[maxRows][maxCols];
    }

    /**
     * Loads tile images and populates the tile grid.
     *
     * @param startingX Starting X-coordinate for placing the "kafes" (hall area).
     * @param startingY Starting Y-coordinate for placing the "kafes" (hall area).
     */
    public void loadTiles() {

        try {
            // Load floor and wall tile images
            BufferedImage floorImage = ImageIO.read(getClass().getResourceAsStream("/assets/floor_plain.png"));
            BufferedImage wallOuterEastImage = ImageIO.read(getClass().getResourceAsStream("/assets/wall_outer_e.png"));
            BufferedImage wallOuterWestImage = ImageIO.read(getClass().getResourceAsStream("/assets/wall_outer_w.png"));
            BufferedImage wallCenterImage = ImageIO.read(getClass().getResourceAsStream("/assets/wall_center.png"));

            // Create reusable tile objects
            Tile floorTile = new Tile(floorImage, false);
            Tile wallOuterEastTile = new Tile(wallOuterEastImage, true);
            Tile wallOuterWestTile = new Tile(wallOuterWestImage, true);
            Tile wallCenterTile = new Tile(wallCenterImage, true);

            // Populate the entire grid with floor tiles
            for (int y = 0; y < maxRows; y++) {
                for (int x = 0; x < maxCols; x++) {
                  
                        tileGrid[y][x] = floorTile;
                    
                    
                }
            }

            // Populate the "kafes" (hall) with wall tiles to form a border
            for (int y = startingY; y < startingY + kafesRows; y++) {
                for (int x = startingX; x < startingX + kafesCols; x++) {
                    // Top border
                    if (y == startingY) {
                        tileGrid[y][x] = wallCenterTile;
                    }
                    // Bottom border
                    if (y == startingY + kafesRows - 1) {
                        tileGrid[y][x] = wallCenterTile;
                    }
                    // Left border
                    if (x == startingX) {
                        tileGrid[y][x] = wallOuterWestTile;
                    }
                    // Right border
                    if (x == startingX + kafesCols - 1) {
                        tileGrid[y][x] = wallOuterEastTile;
                    }
                }
            }
            

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading tile images!"); // Debug message
        }
    }


    /**
     * Returns the Tile object at the specified grid coordinates.
     *
     * @param x The x-coordinate of the tile.
     * @param y The y-coordinate of the tile.
     * @return The Tile object at the specified coordinates, or null if out of bounds.
     */
    public Tile getTileAt(int x, int y) {
        if (x >= 0 && x < maxCols && y >= 0 && y < maxRows) {
            return tileGrid[y][x];
        }
        return null;
    }
    
    

    /**
     * Draws the tile grid onto the given Graphics2D object.
     *
     * @param g2 Graphics2D object used for rendering.
     */
    public void draw(Graphics2D g2) {
        for (int y = 0; y < maxRows; y++) {
            for (int x = 0; x < maxCols; x++) {
                Tile tile = tileGrid[y][x];
                if (tile != null && tile.image != null) {
                    // Draw the tile image at the appropriate grid position
                    g2.drawImage(tile.image, x * tileSize, y * tileSize, tileSize, tileSize, null);
                }
            }
        }
    }
}
