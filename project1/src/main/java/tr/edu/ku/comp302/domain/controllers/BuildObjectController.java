package tr.edu.ku.comp302.domain.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.BuildObject;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.domain.models.Inventory;
import tr.edu.ku.comp302.domain.models.Player;
import tr.edu.ku.comp302.domain.models.Enchantments.Enchantment;
import tr.edu.ku.comp302.domain.models.Enchantments.Rune;

import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * BuildObjectController manages all BuildObjects across all halls,
 * including loading from JSON and ensuring only one BuildObject can have a rune.
 */
public class BuildObjectController {

    private final HallType hallType;

    private Map<HallType, List<BuildObject>> worldObjectsMap;
    private BuildObject runeHolder;
    private Rune rune;

    public BuildObjectController(HallType hallType) {
        this.hallType = hallType;
        // Initialize an empty map
        this.worldObjectsMap = new HashMap<>();
        this.runeHolder = null;
    }

    /**
     * Loads the BuildObjects from JSON data, storing them by HallType.
     * If any objects were previously loaded, they are replaced.
     */
    public void loadWorldFromJson(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            worldObjectsMap = new HashMap<>();
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

            Random rand = new Random();
            int randIndex = rand.nextInt(getObjectsForHall(hallType).size());
            // Clear the existing runeHolder, because we reloaded data
            getObjectsForHall(hallType).get(randIndex).setHasRune(true);
            this.runeHolder = getObjectsForHall(hallType).get(randIndex);
            // If you want to preserve some previous state of the rune, you'd handle that differently.
        } catch (Exception e) {
            e.printStackTrace();
            this.worldObjectsMap = new HashMap<>();
            this.runeHolder = null;
        }
    }

    public void update (HallType hallType, Player player, Point clickPos){
        if (clickPos != null) {
            handleClickCollection(clickPos, player);
        }
    }

    private void handleClickCollection(Point clickPos, Player player) {
        int clickX = clickPos.x;
        int clickY = clickPos.y;

        for(BuildObject obj : getObjectsForHall(hallType)){
            System.out.println(obj.toString());
            System.out.println();
        }

        // We check each BuildObject in the current hall
        List<BuildObject> objects = getObjectsForHall(hallType);
        for (BuildObject obj : objects) {
            // 1) Does the bounding box contain the click?
            int tileSize = GameConfig.TILE_SIZE;
            int objPX = obj.getX() * tileSize;
            int objPY = obj.getY() * tileSize;
            if(obj.getHasRune()){
                System.out.println("Object coordinates: " + objPX + " " + "" + objPY);
                System.out.println("Click coordinates: " + clickX+ " " + "" + clickY);
            }
            boolean clickedOnObject = 
                (clickX >= objPX && clickX < objPX + tileSize) &&
                (clickY >= objPY && clickY < objPY + tileSize);

            if (clickedOnObject && obj.getHasRune()) {
                System.out.println("Clicked on object");
                // 2) Check if player is close enough
                if (isPlayerCloseEnough(player, obj)) {
                    System.out.println("Player is close enough");
                    // 3) "Remove" the rune from the object
                    obj.setHasRune(false);
                    // also un-set the global holder if it's the same
                    if (runeHolder == obj) {
                        runeHolder = null;
                    }

                    // 4) Spawn a new "rune enchantment" above the object 
                    //    (slightly above means e.g. objPY - tileSize)
                    int enchantX = objPX;
                    int enchantY = objPY - tileSize; // "above" the object

                    System.out.println("Spawned a Rune Enchantment at " + enchantX + ", " + enchantY);

                    Enchantment rune = new Rune(enchantX, enchantY, System.currentTimeMillis());
                    player.getInventory().addItem(rune);
                    // We found the clicked object, so break to avoid multiple clicks registering
                    break;
                }
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
        if(dx + dy < 3*tileSize) return true;
        return false;
    }

    public void draw(Graphics2D g2) {
        List<BuildObject> hallObjects = getObjectsForHall(this.hallType);
        for (BuildObject obj : hallObjects) {
            drawSingleObject(g2, obj);
        }
    }

    private void drawSingleObject(Graphics2D g2, BuildObject obj) {
        String imageName = obj.getObjectType();
        int tileSize = GameConfig.TILE_SIZE;
        int px = obj.getX() * tileSize;
        int py = obj.getY() * tileSize;

        try {
            BufferedImage image = ImageIO.read(
                getClass().getResourceAsStream("/assets/" + imageName + ".png"));
            g2.drawImage(image, px, py, tileSize, tileSize, null);
        } catch (IOException e) {
            // fallback if not found
            g2.setColor(Color.GREEN);
            g2.fillRect(px, py, tileSize, tileSize);
        }

        // Optional: If hasRune==true, you could draw a small icon overlay, etc.
        if (obj.getHasRune()) {
            g2.setColor(new Color(255, 215, 0, 128)); // goldish overlay
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
     * Sets the rune on the specified BuildObject, removing it from any previous holder.
     * If the object is already the holder, this does nothing special.
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
     * Removes the rune from whichever BuildObject currently has it, if any.
     */
    public void removeRune() {
        if (this.runeHolder != null) {
            this.runeHolder.setHasRune(false);
            this.runeHolder = null;
        }
    }

    /**
     * Transfers the rune from an old BuildObject to a new BuildObject.
     * If oldObj does not actually have the rune, it just sets it on newObj.
     */
    public void transferRune() {
        Random rand = new Random();
        BuildObject newObj = null;
        if(getObjectsForHall(hallType).size()!=0){
            for(int i = 0; i<50; i++){
                int randIndex = rand.nextInt(getObjectsForHall(hallType).size());
                if(getObjectsForHall(hallType).get(randIndex).getHasRune()!=true){
                    newObj = getObjectsForHall(hallType).get(randIndex);
                }
            }
        }
        
        if(newObj!=null && getRuneHolder()!=null){
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

}
