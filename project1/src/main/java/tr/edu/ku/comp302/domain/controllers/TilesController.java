package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.domain.models.Tile;

import java.awt.*;
import java.awt.image.BufferedImage;

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

    private Tile transparentCollidableTile;

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
     */
    public void loadTiles(HallType hallType) {

            // Load floor and wall tile images
            BufferedImage floorImage       = ResourceManager.getImage("floor_plain");
            BufferedImage wallOuterEast     = ResourceManager.getImage("wall_outer_e");
            BufferedImage wallOuterWest     = ResourceManager.getImage("wall_outer_w");
            BufferedImage wallOuterNorth    = ResourceManager.getImage("wall_outer_n");
            BufferedImage wallOuterNorthWest= ResourceManager.getImage("wall_outer_nw");
            BufferedImage wallOuterNorthEast= ResourceManager.getImage("wall_outer_ne");
            BufferedImage wallOuterSouthWest= ResourceManager.getImage("wall_outer_sw");
            BufferedImage wallOuterSouthEast= ResourceManager.getImage("wall_outer_se");
            BufferedImage wallInnerNW       = ResourceManager.getImage("wall_inner_nw");
            BufferedImage wallInnerNE       = ResourceManager.getImage("wall_inner_ne");
            BufferedImage wallInnerSW       = ResourceManager.getImage("wall_inner_sw");
            BufferedImage wallInnerSE       = ResourceManager.getImage("wall_inner_se");
            BufferedImage wallInnerS        = ResourceManager.getImage("wall_outer_n");
            BufferedImage wallCenter        = ResourceManager.getImage("wall_center");
            BufferedImage transparentImage  = ResourceManager.getImage("transparent_tile");

            this.transparentCollidableTile = new Tile(transparentImage, true);

            BufferedImage flagImage;
            switch (hallType) {
                case EARTH:
                    flagImage = ResourceManager.getImage("wall_flag_green");
                    break;
                case WATER:
                    flagImage = ResourceManager.getImage("wall_flag_blue");
                    break;
                case FIRE:
                    flagImage = ResourceManager.getImage("wall_flag_red");
                    break;
                case AIR:
                    flagImage = ResourceManager.getImage("wall_flag_yellow");
                    break;
                default:
                    flagImage = ResourceManager.getImage("wall_center"); // fallback
                    break;
            }

            Tile floorTile            = new Tile(floorImage, false);
            Tile wallOuterWestTile    = new Tile(wallOuterWest, true);
            Tile wallOuterEastTile    = new Tile(wallOuterEast, true);
            Tile wallOuterNorthTile   = new Tile(wallOuterNorth, true);
            Tile wallOuterNWTile      = new Tile(wallOuterNorthWest, true);
            Tile wallOuterNETile      = new Tile(wallOuterNorthEast, true);
            Tile wallOuterSWTile      = new Tile(wallOuterSouthWest, true);
            Tile wallOuterSETile      = new Tile(wallOuterSouthEast, true);
            Tile wallInnerNWTile      = new Tile(wallInnerNW, true);
            Tile wallInnerNETile      = new Tile(wallInnerNE, true);
            Tile wallInnerSWTile      = new Tile(wallInnerSW, true);
            Tile wallInnerSETile      = new Tile(wallInnerSE, true);
            Tile wallInnerSTile       = new Tile(wallInnerS, false); // Not collidable
            Tile wallCenterTile       = new Tile(wallCenter, true);
            Tile flagTile             = new Tile(flagImage, true);
        

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
                        tileGrid[y][x] = wallOuterNorthTile;
                    }
                    if (y == startingY + 1){
                        tileGrid[y][x] = wallCenterTile;
                    }

                    // Bottom border
                    if (y == startingY + kafesRows - 1) {
                        tileGrid[y][x] = wallCenterTile;
                    }
                    if (y == startingY + kafesRows - 2) {
                        tileGrid[y][x] = wallInnerSTile;
                    }

                    // Left border
                    if (x == startingX) {
                        tileGrid[y][x] = wallOuterWestTile;
                    }
                    if (x == startingX && y == startingY){
                        tileGrid[y][x] = wallOuterNWTile;
                    }
                    /*if (x == startingX && y == startingY + 1){
                        tileGrid[y][x] = wallInnerNWTile;
                    }
                    if (x == startingX && y == startingY + kafesRows - 2){
                        tileGrid[y][x] = wallInnerSWTile;
                    }*/
                    if (x == startingX && y == startingY + kafesRows - 1){
                        tileGrid[y][x] = wallOuterSWTile;
                    }

                    // Right border
                    if (x == startingX + kafesCols - 1) {
                        tileGrid[y][x] = wallOuterEastTile;
                    }
                    if (x == startingX + kafesCols - 1 && y == startingY){
                        tileGrid[y][x] = wallOuterNETile;
                    }
                    /*if (x == startingX + kafesCols - 1 && y == startingY + 1){
                        tileGrid[y][x] = wallInnerNETile;
                    }
                    if (x == startingX + kafesCols - 2 && y == startingY + kafesRows - 2){
                        tileGrid[y][x] = wallInnerSETile;
                    }*/
                    if (x == startingX + kafesCols - 1 && y == startingY + kafesRows - 1){
                        tileGrid[y][x] = wallOuterSETile;
                    }

                    if(x == (GameConfig.NUM_HALL_COLS*3)/4 && y == startingY + kafesRows - 1){
                        tileGrid[y][x] = flagTile;
                    }
                    

                }
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

    public void setTransparentTileAt(int x, int y) {
        if (x >= 0 && x < maxCols && y >= 0 && y < maxRows) {
            tileGrid[y][x] = transparentCollidableTile;
        }
    }

    public void setFloorTileAt(int x, int y) {
        BufferedImage floorImage = ResourceManager.getImage("floor_plain");
        Tile floorTile = new Tile(floorImage, false);
        if (x >= 0 && x < maxCols && y >= 0 && y < maxRows) {
            tileGrid[y][x] = floorTile; 
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

    public void drawInnerBottom(Graphics2D g2){
        for (int y = 0; y < maxRows; y++) {
            for (int x = 0; x < maxCols; x++) {
                Tile tile = tileGrid[y][x];
                if (tile != null && tile.image != null && (y == startingY + kafesRows - 1 || y == startingY + kafesRows - 2) && x != startingX + kafesCols - 1 && x != startingX) {
                    // Draw the tile image at the appropriate grid position
                    g2.drawImage(tile.image, x * tileSize, y * tileSize, tileSize, tileSize, null);
                }
            }
        }
    }
}
