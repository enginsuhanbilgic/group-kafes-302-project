package tr.edu.ku.comp302.domain.controllers;

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

    private Tile[][] tileGrid; //2D array representing the grid of tiles
    
    // Dimensions of the screen (calculated from screen resolution and tile size)
    private final int maxRows=(int)1080/48; // Number of rows based on screen height
    private final int maxCols=(int)1920/48; // Number of columns based on screen width

    // Dimensions for the "kafes" (hall or inner grid)
    private final int kafesRows; // Number of rows for the inner area
    private final int kafesCols; // Number of columns for the inner area

    private final int tileSize; // Size of a single tile in pixels

    /**
     * Constructor: Initializes the TilesController with dimensions for the kafes area.
     *
     * @param rows     Number of rows for the "kafes" (inner grid area)
     * @param cols     Number of columns for the "kafes" (inner grid area)
     * @param tileSize Size of a single tile in pixels
     */
    public TilesController(int rows, int cols, int tileSize) {
        this.kafesRows = rows;
        this.kafesCols = cols;
        this.tileSize = tileSize;

        // Initialize the grid with the maximum screen rows and columns
        tileGrid = new Tile[maxRows][maxCols]; // Initialize the grid
    }

    /**
     * Loads tile images and populates the tile grid.
     * 
     * @param startingX Starting X-coordinate for placing the "kafes" (hall area).
     * @param startingY Starting Y-coordinate for placing the "kafes" (hall area).
     */
    public void loadTiles(int startingX, int startingY) {
        
        try {
            // Load floor and wall tile images
            BufferedImage floorImage = ImageIO.read(getClass().getResourceAsStream("/assets/floor_plain.png"));
            BufferedImage wallImage = ImageIO.read(getClass().getResourceAsStream("/assets/wall_outer_e.png"));
            
            // Create reusable tile objects
            Tile floorTile = new Tile(floorImage, false);
            Tile wallTile = new Tile(wallImage, true);

            // Populate the entire grid with floor tiles
            for (int y = 0; y < maxRows; y++){
                for(int x = 0; x < maxCols; x++){
                    tileGrid[y][x] = floorTile;
                }
            }

            // Populate the "kafes" (hall) with wall tiles to form a border
            for (int y = startingY; y < startingY + kafesRows; y++) {
                for (int x = startingX; x < startingX + kafesCols; x++) {
                    // Top border
                    if (y == startingY) {
                        tileGrid[y][x] = wallTile;
                    }
                    // Bottom border
                    if (y == startingY + kafesRows - 1) { // Fix: Use "-1" to avoid out-of-bounds
                        tileGrid[y][x] = wallTile;
                    }
                    // Left border
                    if (x == startingX) {
                        tileGrid[y][x] = wallTile;
                    }
                    // Right border
                    if (x == startingX + kafesCols - 1) { // Fix: Use "-1" to avoid out-of-bounds
                        tileGrid[y][x] = wallTile;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading tile images!"); // Debug message
        }
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
