package tr.edu.ku.comp302.domain.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.*;
import tr.edu.ku.comp302.domain.models.enchantments.Enchantment;
import tr.edu.ku.comp302.domain.models.enchantments.Rune;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class BuildObjectController {

    private final HallType hallType;

    // Key: HallType, Value: list of BuildObjects in that hall
    private Map<HallType, List<BuildObject>> worldObjectsMap;

    // The single BuildObject that currently holds the rune (or null)
    private BuildObject runeHolder;

    private Random random = new Random();

    private Rectangle clickEffectRect = null;
    private long clickEffectStart = 0L;
    private static final long CLICK_EFFECT_DURATION = 500;

    public BuildObjectController(HallType hallType) {
        this.hallType = hallType;
        // Initialize an empty map
        this.worldObjectsMap = new HashMap<>();
        this.runeHolder = null;
    }

    /**
     * Loads the BuildObjects from JSON data, storing them by HallType.
     * If any objects were previously loaded, they are replaced.
     *
     * @requires jsonData may be null or empty (in which case the controller resets).
     * @modifies this.worldObjectsMap, this.runeHolder
     * @effects
     *   - If jsonData is valid, replaces worldObjectsMap with the contents of the JSON.
     *   - Picks one random object from the specified hallType to hold the rune (if any exist).
     *   - If jsonData is null/empty or invalid, resets the map and runeHolder to empty.
     */
    public void loadWorldFromJson(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            worldObjectsMap = new HashMap<>();
            this.runeHolder = null;
            return;
        }
        try {
            Gson gson = new Gson();
            Type rawType = new TypeToken<Map<String, List<BuildObject>>>() {}.getType();
            Map<String, List<BuildObject>> rawMap = gson.fromJson(jsonData, rawType);

            if (rawMap == null) {
                rawMap = new HashMap<>();
            }

            // Convert string keys to HallType
            Map<HallType, List<BuildObject>> finalMap = new HashMap<>();
            for (Map.Entry<String, List<BuildObject>> entry : rawMap.entrySet()) {
                try {
                    HallType hall = HallType.valueOf(entry.getKey().toUpperCase());
                    finalMap.put(hall, entry.getValue());
                } catch (IllegalArgumentException iae) {
                    System.err.println("Skipping invalid hall type: " + entry.getKey());
                }
            }
            this.worldObjectsMap = finalMap;

            // Reinitialize Random for each BuildObject
            for (List<BuildObject> objects : worldObjectsMap.values()) {
                for (BuildObject obj : objects) {
                    obj.initializeRandom();
                }
            }

            // Randomly assign the rune to one object in the given hallType
            List<BuildObject> currentHallObjects = getObjectsForHall(hallType);
            if (!currentHallObjects.isEmpty()) {
                int randIndex = random.nextInt(currentHallObjects.size());
                BuildObject selectedObj = currentHallObjects.get(randIndex);
                selectedObj.setHasRune(true);
                this.runeHolder = selectedObj;
            } else {
                this.runeHolder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.worldObjectsMap = new HashMap<>();
            this.runeHolder = null;
        }
    }

    /**
     * @requires No special external requirements
     * @modifies May modify the runeHolder if the user clicks on it,
     *           might modify player's inventory.
     * @effects  If the user clicked on a BuildObject with a rune and is close enough,
     *           the rune is removed from that object and added to the player's inventory.
     */
    public void update(HallType hallType, Player player, Point clickPos){
        if (clickPos != null) {
            handleClickCollection(clickPos, player);
        }
    }

    private void handleClickCollection(Point clickPos, Player player) {
        int clickX = clickPos.x;
        int clickY = clickPos.y;

        // We check each BuildObject in the current hall
        List<BuildObject> objects = getObjectsForHall(hallType);
        for (BuildObject obj : objects) {
            int tileSize = GameConfig.TILE_SIZE;
            int objPX = obj.getX() * tileSize;
            int objPY = obj.getY() * tileSize;
            if(obj.getHasRune()){
                System.out.println("Object coordinates: " + objPX + " " + objPY);
                System.out.println("Click coordinates: " + clickX + " " + clickY);
            }
            boolean clickedOnObject = 
                (clickX >= objPX && clickX < objPX + tileSize) &&
                (clickY >= objPY && clickY < objPY + tileSize);

            if (clickedOnObject && obj.getHasRune()) {
                System.out.println("Clicked on object");
                // Check if player is close enough
                if (isPlayerCloseEnough(player, obj)) {
                    System.out.println("Player is close enough");
                    // Remove the rune from the object
                    obj.setHasRune(false);
                    if (runeHolder == obj) {
                        runeHolder = null;
                    }

                    int enchantX = objPX;
                    int enchantY = objPY - tileSize; // "above" the object

                    System.out.println("Spawned a Rune Enchantment at " + enchantX + ", " + enchantY);

                    Enchantment rune = new Rune(enchantX, enchantY, 0); // 0 is not correct but it doesn't matter here
                    player.getInventory().addItem(rune);
                    break;
                }
            }
            else if (clickedOnObject && !obj.getHasRune() && isPlayerCloseEnough(player, obj)){
                clickEffectRect = new Rectangle(objPX, objPY, tileSize, tileSize);
                clickEffectStart = System.currentTimeMillis();
            }
        }
    }

    private boolean isPlayerCloseEnough(Player player, BuildObject obj) {
        int tileSize = GameConfig.TILE_SIZE;

        // Convert the object's tile coords to pixel coords
        int objPX = obj.getX() * tileSize;
        int objPY = obj.getY() * tileSize;

        int dx = Math.abs(player.getX() - objPX);
        int dy = Math.abs(player.getY() - objPY);
        // If within 1 tile distance in each direction:
        if(dx + dy < 3 * tileSize) return true;
        return false;
    }

    public void draw(Graphics2D g2, boolean drawColumn) {
        List<BuildObject> hallObjects = getObjectsForHall(this.hallType);
        for (BuildObject obj : hallObjects) {
            if(obj!=null){
                drawSingleObject(g2, obj, drawColumn);
            }
        }

        if (clickEffectRect != null) {
            long elapsed = System.currentTimeMillis() - clickEffectStart;
            if (elapsed < CLICK_EFFECT_DURATION) {
                // Customize color/shape/alpha as you wish
                g2.setColor(new Color(255, 0, 0, 80)); // semi-transparent red
                g2.fillRect(
                    clickEffectRect.x,
                    clickEffectRect.y,
                    clickEffectRect.width,
                    clickEffectRect.height
                );
            } else {
                // Reset once expired
                clickEffectRect = null;
            }
        }
    }

    private void drawSingleObject(Graphics2D g2, BuildObject obj, boolean drawColumn) {
        String imageName = obj.getObjectType();
        int tileSize = GameConfig.TILE_SIZE;
        int px = obj.getX() * tileSize;
        int py = obj.getY() * tileSize;

        // 1) Retrieve the cached image from ResourceManager
        BufferedImage image = ResourceManager.getImage(imageName);

        // 2) If found, draw it. Otherwise fallback.
        if (image != null) {
            if((imageName.trim().equals("column_wall") || imageName.trim().equals("boxes_stacked")) && drawColumn){
                g2.drawImage(image, px, py - tileSize/2, tileSize, tileSize + tileSize/2, null);
            }
            else if (!drawColumn){
                g2.drawImage(image, px, py, tileSize, tileSize, null);
            }
        } else {
            // fallback
            g2.setColor(Color.GREEN);
            g2.fillRect(px, py, tileSize, tileSize);
        }

        // 3) Optional overlay if hasRune
        if (obj.getHasRune()) {
            g2.setColor(new Color(255, 215, 0, 128));
            g2.fillRect(px, py, tileSize, tileSize);
        }
    }

    /**
     * Returns the entire map of HallType -> List<BuildObject>.
     * Useful if external classes (like PlayModeController) want to draw objects, etc.
     */
    public Map<HallType, List<BuildObject>> getWorldObjectsMap() {
        return this.worldObjectsMap;
    }

    /**
     * Finds the first BuildObject that currently has the rune, or null if none.
     */
    public BuildObject getRuneHolder() {
        return this.runeHolder;
    }

    /**
     * @requires newHolder != null and must exist in worldObjectsMap
     * @modifies this.runeHolder, the hasRune property of oldHolder and newHolder
     * @effects 
     *   - Removes the rune from the old holder (if any).
     *   - Sets hasRune = true on newHolder.
     *   - Updates this.runeHolder accordingly.
     */
    public void setRune(BuildObject newHolder) {
        // If we have an old holder, remove the rune from it
        if (this.runeHolder != null && this.runeHolder != newHolder) {
            this.runeHolder.setHasRune(false);
        }
        // Mark the new holder as having the rune
        newHolder.setHasRune(true);
        this.runeHolder = newHolder;
    }

    /**
     * @requires none
     * @modifies this.runeHolder, the hasRune property of that holder
     * @effects If a runeHolder exists, sets hasRune = false on it and sets runeHolder to null.
     */
    public void removeRune() {
        if (this.runeHolder != null) {
            this.runeHolder.setHasRune(false);
            this.runeHolder = null;
        }
    }

    /**
     * A “non-trivial” method to randomly choose a new BuildObject for the rune and move it there.
     *
     * @requires getObjectsForHall(hallType) is not empty (otherwise does nothing)
     * @modifies this.runeHolder, hasRune on build objects
     * @effects 
     *  - If a build object already holds the rune, unsets that object’s hasRune.
     *  - Picks a different random BuildObject in the current hall, sets hasRune = true, updates runeHolder.
     *  - Might do nothing if it fails to find a new valid object after 50 tries.
     */
    public void transferRune() {
        List<BuildObject> objects = getObjectsForHall(hallType);
        if (objects.isEmpty()) return;

        BuildObject newObj = null;

        // Attempt to find a new object that doesn't currently hold the rune
        for(int i = 0; i < 50; i++) {
            int randIndex = random.nextInt(objects.size());
            BuildObject candidate = objects.get(randIndex);
            if(!candidate.getHasRune()) {
                newObj = candidate;
                break; // Exit loop once a suitable object is found
            }
        }

        if(newObj != null && getRuneHolder() != null) {
            System.out.println("Transferring rune from (" + getRuneHolder().getX() + ", " + getRuneHolder().getY() + ") to (" + newObj.getX() + ", " + newObj.getY() + ")");
            getRuneHolder().setHasRune(false);
            newObj.setHasRune(true);
            this.runeHolder = newObj;
        }
    }

    /**
     * Returns the BuildObjects for a given hall, or an empty list if none.
     */
    public List<BuildObject> getObjectsForHall(HallType hall) {
        return worldObjectsMap.getOrDefault(hall, List.of());
    }

    /**
     * Example method to find a BuildObject by (x, y, hall).
     * Returns null if not found.
     */
    public BuildObject findObjectAt(HallType hall, int x, int y) {
        List<BuildObject> objects = getObjectsForHall(hall);
        for (BuildObject obj : objects) {
            if (obj.getX() == x && obj.getY() == y) {
                return obj;
            }
        }
        return null;
    }

    /**
     * Checks the representation invariant.
     */
    public boolean repOk() {
        if (worldObjectsMap == null) return false;

        int runeCount = 0;
        BuildObject actualRuneObject = null;

        // For each HallType and its objects
        for (Map.Entry<HallType, List<BuildObject>> entry : worldObjectsMap.entrySet()) {
            List<BuildObject> objects = entry.getValue();
            if (objects == null) return false;  // lists should not be null

            // Count how many BuildObjects have the Rune
            for (BuildObject obj : objects) {
                if (obj.getHasRune()) {
                    runeCount++;
                    actualRuneObject = obj;
                }
            }
        }

        // If there's more than one object with hasRune=true, break the invariant
        if (runeCount > 1) return false;

        // If runeHolder is not null, it must be the same as the object we found
        if (runeHolder != null) {
            if (!runeHolder.getHasRune()) {
                // The runeHolder must always have 'hasRune' set to true
                return false;
            }
            if (runeHolder != actualRuneObject) {
                // If there's exactly 1 runed object, it must be the same as runeHolder
                return false;
            }
        } else {
            // If runeHolder == null, then no object should have hasRune=true
            if (runeCount != 0) return false;
        }

        return true;
    }
}