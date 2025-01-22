package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.domain.models.Tile;
import tr.edu.ku.comp302.domain.models.TileData;
import tr.edu.ku.comp302.domain.controllers.ResourceManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * Manages a 2D grid of Tiles for drawing and collisions,
 * while also providing a 2D array of TileData for serialization.
 */
public class TilesController implements Serializable {

    private static final long serialVersionUID = 1L;

    // The actual Tile objects with images—transient so we don't try to serialize large images
    private transient Tile[][] tileGrid;

    // A parallel 2D array that stores tile "type" + collidable info, which is serializable
    private TileData[][] tileDataGrid;

    private final int tileSize = GameConfig.TILE_SIZE;

    // Full “screen” dimension in tiles
    private final int maxRows = GameConfig.RES_VERTICAL / tileSize;
    private final int maxCols = GameConfig.RES_HORIZONTAL / tileSize;

    // The "cage" or "hall" dimension
    private final int kafesRows = GameConfig.NUM_HALL_ROWS;
    private final int kafesCols = GameConfig.NUM_HALL_COLS;

    // Starting offset for the hall
    private final int startingX = GameConfig.KAFES_STARTING_X;
    private final int startingY = GameConfig.KAFES_STARTING_Y;

    // A special tile for things like "transparent collidable"
    private transient Tile transparentCollidableTile;

    /**
     * Constructor initializes the 2D arrays for tile info.
     */
    public TilesController() {
        // We'll create the arrays here
        tileGrid = new Tile[maxRows][maxCols];
        tileDataGrid = new TileData[maxRows][maxCols];
    }

    /**
     * Called after deserialization if we need to rebuild the tileGrid
     * from tileDataGrid.
     */
    public void reinitTileGridFromData() {
        // Re-create the transient tileGrid array
        tileGrid = new Tile[maxRows][maxCols];

        // Also re-create special references
        BufferedImage transparentImage = ResourceManager.getImage("transparent_tile");
        this.transparentCollidableTile = new Tile(transparentImage, true);

        // For each TileData, pick the correct image from ResourceManager.
        for (int row = 0; row < maxRows; row++) {
            for (int col = 0; col < maxCols; col++) {
                TileData td = tileDataGrid[row][col];
                if (td == null) {
                    // default to floor if missing
                    BufferedImage floor = ResourceManager.getImage("floor_plain");
                    tileGrid[row][col] = new Tile(floor, false);
                    tileDataGrid[row][col] = new TileData("floor_plain", false);
                } else {
                    // pick the correct image
                    BufferedImage img = pickImageByKey(td.getTileTypeKey());
                    tileGrid[row][col] = new Tile(img, td.isCollidable());
                }
            }
        }
    }

    /**
     * Picks the correct image from ResourceManager by a known string key.
     */
    private BufferedImage pickImageByKey(String key) {
        // If we haven't assigned a key, assume floor
        if (key == null) {
            return ResourceManager.getImage("floor_plain");
        }
        // Otherwise, we rely on the same string that we used in loadTiles.
        // e.g. "floor_plain", "wall_outer_n", "transparent_tile", etc.
        return ResourceManager.getImage(key);
    }

    /**
     * Fills the entire tile grid with floor tiles, then places walls, borders, etc.
     */
    public void loadTiles(HallType hallType) {
        // Create or re-create the in-memory tile arrays
        tileGrid = new Tile[maxRows][maxCols];
        tileDataGrid = new TileData[maxRows][maxCols];

        // Basic images
        BufferedImage floorImage         = ResourceManager.getImage("floor_plain");
        BufferedImage wallOuterEast      = ResourceManager.getImage("wall_outer_e");
        BufferedImage wallOuterWest      = ResourceManager.getImage("wall_outer_w");
        BufferedImage wallOuterNorth     = ResourceManager.getImage("wall_outer_n");
        BufferedImage wallOuterNorthWest = ResourceManager.getImage("wall_outer_nw");
        BufferedImage wallOuterNorthEast = ResourceManager.getImage("wall_outer_ne");
        BufferedImage wallOuterSouthWest = ResourceManager.getImage("wall_outer_sw");
        BufferedImage wallOuterSouthEast = ResourceManager.getImage("wall_outer_se");
        BufferedImage wallInnerNW        = ResourceManager.getImage("wall_inner_nw");
        BufferedImage wallInnerNE        = ResourceManager.getImage("wall_inner_ne");
        BufferedImage wallInnerSW        = ResourceManager.getImage("wall_inner_sw");
        BufferedImage wallInnerSE        = ResourceManager.getImage("wall_inner_se");
        BufferedImage wallInnerS         = ResourceManager.getImage("wall_outer_n"); // if you want an S tile
        BufferedImage wallCenter         = ResourceManager.getImage("wall_center");
        BufferedImage transparentImage   = ResourceManager.getImage("transparent_tile");

        // For flags
        BufferedImage flagImage;
        switch (hallType) {
            case EARTH -> flagImage = ResourceManager.getImage("wall_flag_green");
            case WATER -> flagImage = ResourceManager.getImage("wall_flag_blue");
            case FIRE -> flagImage = ResourceManager.getImage("wall_flag_red");
            case AIR -> flagImage = ResourceManager.getImage("wall_flag_yellow");
            default -> flagImage = ResourceManager.getImage("wall_center");
        }

        // Create special references
        this.transparentCollidableTile = new Tile(transparentImage, true);

        // We'll define some convenience wrappers:
        Tile floorTile = new Tile(floorImage, false);
        TileData floorTileData = new TileData("floor_plain", false);

        // fill entire grid with floor
        for (int row = 0; row < maxRows; row++) {
            for (int col = 0; col < maxCols; col++) {
                tileGrid[row][col] = floorTile;
                tileDataGrid[row][col] = floorTileData;
            }
        }

        // Now place the "cage" or "hall" walls
        // We'll create some specialized tiles
        Tile wallOuterWestTile  = new Tile(wallOuterWest, true);
        TileData wallOuterWestData  = new TileData("wall_outer_w", true);

        Tile wallOuterEastTile  = new Tile(wallOuterEast, true);
        TileData wallOuterEastData  = new TileData("wall_outer_e", true);

        Tile wallOuterNorthTile = new Tile(wallOuterNorth, true);
        TileData wallOuterNorthData = new TileData("wall_outer_n", true);

        Tile wallOuterNWTile    = new Tile(wallOuterNorthWest, true);
        TileData wallOuterNWData    = new TileData("wall_outer_nw", true);

        Tile wallOuterNETile    = new Tile(wallOuterNorthEast, true);
        TileData wallOuterNEData    = new TileData("wall_outer_ne", true);

        Tile wallOuterSWTile    = new Tile(wallOuterSouthWest, true);
        TileData wallOuterSWData    = new TileData("wall_outer_sw", true);

        Tile wallOuterSETile    = new Tile(wallOuterSouthEast, true);
        TileData wallOuterSEData    = new TileData("wall_outer_se", true);

        Tile wallInnerNWTile = new Tile(wallInnerNW, true);
        TileData wallInnerNWData = new TileData("wall_inner_nw", true);

        Tile wallInnerNETile = new Tile(wallInnerNE, true);
        TileData wallInnerNEData = new TileData("wall_inner_ne", true);

        Tile wallInnerSWTile = new Tile(wallInnerSW, true);
        TileData wallInnerSWData = new TileData("wall_inner_sw", true);

        Tile wallInnerSETile = new Tile(wallInnerSE, true);
        TileData wallInnerSEData = new TileData("wall_inner_se", true);

        Tile wallInnerSTile  = new Tile(wallInnerS, false); // Not collidable
        TileData wallInnerSData  = new TileData("wall_outer_n", false);

        Tile wallCenterTile  = new Tile(wallCenter, true);
        TileData wallCenterData  = new TileData("wall_center", true);

        BufferedImage flg = (flagImage != null) ? flagImage : wallCenter;
        Tile flagTile     = new Tile(flg, true);
        TileData flagData = new TileData(keyForFlag(hallType), true);

        // Fill the cage borders
        for (int y = startingY; y < startingY + kafesRows; y++) {
            for (int x = startingX; x < startingX + kafesCols; x++) {

                // Top border
                if (y == startingY) {
                    tileGrid[y][x] = wallOuterNorthTile;
                    tileDataGrid[y][x] = wallOuterNorthData;
                }
                if (y == startingY + 1) {
                    tileGrid[y][x] = wallCenterTile;
                    tileDataGrid[y][x] = wallCenterData;
                }

                // Bottom border
                if (y == startingY + kafesRows - 1) {
                    tileGrid[y][x] = wallCenterTile;
                    tileDataGrid[y][x] = wallCenterData;
                }
                if (y == startingY + kafesRows - 2) {
                    tileGrid[y][x] = wallInnerSTile;
                    tileDataGrid[y][x] = wallInnerSData;
                }

                // Left border
                if (x == startingX) {
                    tileGrid[y][x] = wallOuterWestTile;
                    tileDataGrid[y][x] = wallOuterWestData;
                }
                if (x == startingX && y == startingY) {
                    tileGrid[y][x] = wallOuterNWTile;
                    tileDataGrid[y][x] = wallOuterNWData;
                }
                if (x == startingX && y == startingY + kafesRows - 1) {
                    tileGrid[y][x] = wallOuterSWTile;
                    tileDataGrid[y][x] = wallOuterSWData;
                }

                // Right border
                if (x == startingX + kafesCols - 1) {
                    tileGrid[y][x] = wallOuterEastTile;
                    tileDataGrid[y][x] = wallOuterEastData;
                }
                if (x == startingX + kafesCols - 1 && y == startingY) {
                    tileGrid[y][x] = wallOuterNETile;
                    tileDataGrid[y][x] = wallOuterNEData;
                }
                if (x == startingX + kafesCols - 1 && y == startingY + kafesRows - 1) {
                    tileGrid[y][x] = wallOuterSETile;
                    tileDataGrid[y][x] = wallOuterSEData;
                }

                // place a flag
                if (x == (GameConfig.NUM_HALL_COLS * 3) / 4 && y == startingY + kafesRows - 1) {
                    tileGrid[y][x] = flagTile;
                    tileDataGrid[y][x] = flagData;
                }
            }
        }
    }

    /**
     * Helper method to pick the tile key for a hall's flag.
     */
    private String keyForFlag(HallType hall) {
        switch (hall) {
            case EARTH: return "wall_flag_green";
            case AIR:   return "wall_flag_yellow";
            case WATER: return "wall_flag_blue";
            case FIRE:  return "wall_flag_red";
            default:    return "wall_center";
        }
    }

    /**
     * Returns a tile at the given x,y in tile coordinates,
     * or null if out of bounds.
     */
    public Tile getTileAt(int x, int y) {
        if (x < 0 || x >= maxCols || y < 0 || y >= maxRows) {
            return null;
        }
        return tileGrid[y][x];
    }

    /**
     * Set a tile at x,y to be the "transparent collidable" tile
     * for e.g. Archer or Wizard footprints. Also updates tileDataGrid.
     */
    public void setTransparentTileAt(int x, int y) {
        if (x >= 0 && x < maxCols && y >= 0 && y < maxRows) {
            tileGrid[y][x] = this.transparentCollidableTile;
            tileDataGrid[y][x] = new TileData("transparent_tile", true);
        }
    }

    /**
     * Set a tile at x,y back to "floor_plain" (non-collidable).
     */
    public void setFloorTileAt(int x, int y) {
        if (x >= 0 && x < maxCols && y >= 0 && y < maxRows) {
            BufferedImage floorImage = ResourceManager.getImage("floor_plain");
            tileGrid[y][x] = new Tile(floorImage, false);
            tileDataGrid[y][x] = new TileData("floor_plain", false);
        }
    }

    /**
     * Draw the entire tile grid onto g2.
     */
    public void draw(Graphics2D g2) {
        for (int row = 0; row < maxRows; row++) {
            for (int col = 0; col < maxCols; col++) {
                Tile tile = tileGrid[row][col];
                if (tile != null && tile.image != null) {
                    g2.drawImage(tile.image, col * tileSize, row * tileSize, tileSize, tileSize, null);
                }
            }
        }
    }

    /**
     * If you have some special logic for the bottom rows, e.g. layering
     * behind certain objects, you can draw them last.
     */
    public void drawInnerBottom(Graphics2D g2) {
        for (int y = 0; y < maxRows; y++) {
            for (int x = 0; x < maxCols; x++) {
                // e.g. re-draw the bottom row or second bottom row inside the cage
                if ((y == startingY + kafesRows - 1 || y == startingY + kafesRows - 2)
                    && x != startingX + kafesCols - 1 && x != startingX) {
                    Tile tile = tileGrid[y][x];
                    if (tile != null && tile.image != null) {
                        g2.drawImage(tile.image, x * tileSize, y * tileSize, tileSize, tileSize, null);
                    }
                }
            }
        }
    }

    /**
     * Return the entire tileDataGrid so we can store it in GameState.
     */
    public TileData[][] getTileDataGrid() {
        return tileDataGrid;
    }

    /**
     * Overwrite tileDataGrid from a loaded array, then re-build tileGrid with images.
     */
    public void setTileDataGrid(TileData[][] newData) {
        // Just copy each cell
        for (int row = 0; row < maxRows; row++) {
            for (int col = 0; col < maxCols; col++) {
                tileDataGrid[row][col] = newData[row][col];
            }
        }
        // Now rebuild tileGrid
        reinitTileGridFromData();
    }
}