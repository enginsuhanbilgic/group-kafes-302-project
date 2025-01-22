package tr.edu.ku.comp302.domain.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import tr.edu.ku.comp302.domain.models.BuildObject;
import tr.edu.ku.comp302.domain.models.HallType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * BuildModeController manages the logic for building the 4 halls.
 */
public class BuildModeController {

    private int currentHallIndex;
    private final HallType[] hallSequence = {
            HallType.EARTH,
            HallType.AIR,
            HallType.WATER,
            HallType.FIRE
    };

    // Key: HallType, Value: List<BuildObject> in that hall
    private Map<HallType, List<BuildObject>> hallObjectsMap;

    // Minimum required objects in each hall, in order (Earth, Air, Water, Fire).
    private final int[] minRequired = {6, 9, 13, 17};

    // JSON helper
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final String DESIGN_SAVES_DIRECTORY = "design_saves";

    // A comparator to sort BuildObjects by (x ascending, then y ascending).
    private static final Comparator<BuildObject> BUILD_OBJECT_COMPARATOR =
            Comparator.comparingInt(BuildObject::getX)
                      .thenComparingInt(BuildObject::getY);

    public BuildModeController() {
        this.currentHallIndex = 0;
        this.hallObjectsMap = new HashMap<>();
        for (HallType ht : hallSequence) {
            hallObjectsMap.put(ht, new ArrayList<>());
        }
        createSavesDirectory();
    }

    private void createSavesDirectory() {
        try {
            Files.createDirectories(Paths.get(DESIGN_SAVES_DIRECTORY));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the currently active hall.
     */
    public HallType getCurrentHall() {
        return hallSequence[currentHallIndex];
    }

    /**
     * Places a new BuildObject into the current hall, then sorts the list
     * so objects remain in ascending coordinate order (x, then y).
     */
    public void placeObject(int gridX, int gridY, String objectType) {
        HallType currentHall = getCurrentHall();
        List<BuildObject> objects = hallObjectsMap.get(currentHall);

        BuildObject obj = new BuildObject(gridX, gridY, objectType);
        objects.add(obj);

        // Sort by x ascending, then y ascending
        objects.sort(BUILD_OBJECT_COMPARATOR);
    }

    /**
     * Checks if the current hall has at least the minimum number of objects.
     */
    public boolean isCurrentHallValid() {
        HallType hall = getCurrentHall();
        int required = minRequired[currentHallIndex];
        int currentCount = hallObjectsMap.get(hall).size();
        return currentCount >= required;
    }

    /**
     * Moves to the next hall (returns false if already at the last one).
     */
    public boolean goToNextHall() {
        if (currentHallIndex >= hallSequence.length - 1) {
            return false;
        }
        currentHallIndex++;
        return true;
    }

    /**
     * Moves to the previous hall (returns false if already at the first one).
     */
    public boolean goToPreviousHall() {
        if (currentHallIndex <= 0) {
            return false;
        }
        currentHallIndex--;
        return true;
    }

    /**
     * Exports all halls' data to a JSON string.
     */
    public String exportToJson() {
        return gson.toJson(hallObjectsMap);
    }

    /**
     * Imports hall data from a JSON string, replaces the local hallObjectsMap,
     * and re-sorts each hall's list in ascending (x, y) order.
     */
    public void importFromJson(String json) {
        if (json == null || json.isEmpty()) {
            // If desired, you could clear the map or do nothing.
            return;
        }
        Type type = new TypeToken<Map<HallType, List<BuildObject>>>(){}.getType();
        Map<HallType, List<BuildObject>> data = gson.fromJson(json, type);
        if (data != null) {
            // Sort each hall's list to maintain the order
            for (Map.Entry<HallType, List<BuildObject>> entry : data.entrySet()) {
                entry.getValue().sort(BUILD_OBJECT_COMPARATOR);
            }
            this.hallObjectsMap = data;
        }
    }

    /**
     * Retrieves the list of BuildObjects for the given hall (read-only).
     */
    public List<BuildObject> getObjectsForHall(HallType hallType) {
        return hallObjectsMap.getOrDefault(hallType, Collections.emptyList());
    }

    /**
     * Prints all objects to console for debugging.
     */
    public void printAllObjects() {
        for (HallType hall : hallSequence) {
            List<BuildObject> objs = hallObjectsMap.get(hall);
            System.out.println("Hall: " + hall + " objects count = " + objs.size());
            for (BuildObject o : objs) {
                System.out.println("   " + o);
            }
        }
    }

    /**
     * Example method; not fully implemented.
     */
    public void saveWorld(String worldName) {
        System.out.println("Saving world: " + worldName);
        printAllObjects();
        System.out.println("Done saving " + worldName);
    }

    /**
     * Saves the current design to a JSON file in 'design_saves/' directory.
     */
    public void saveDesign(String designName) {
        String json = exportToJson();
        Path savePath = Paths.get(DESIGN_SAVES_DIRECTORY, designName + ".json");

        try {
            Files.write(savePath, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save design: " + e.getMessage());
        }
    }

    /**
     * Loads a design from 'design_saves/' directory and updates hallObjectsMap.
     */
    public void loadDesign(String designName) {
        Path loadPath = Paths.get(DESIGN_SAVES_DIRECTORY, designName + ".json");

        try {
            String json = new String(Files.readAllBytes(loadPath), StandardCharsets.UTF_8);
            importFromJson(json);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load design: " + e.getMessage());
        }
    }

    /**
     * Returns the list of saved designs in the 'design_saves' folder (without .json extension).
     */
    public List<String> getSavedDesigns() {
        try {
            return Files.list(Paths.get(DESIGN_SAVES_DIRECTORY))
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(path -> path.getFileName().toString().replace(".json", ""))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}