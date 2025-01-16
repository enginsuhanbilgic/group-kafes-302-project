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

    // Hall bazında yerleştirilen obje listesi tutabiliriz.
    // Key: Hangi Hall, Value: Bu hall'daki yerleştirilen BuildObject listesi
    private Map<HallType, List<BuildObject>> hallObjectsMap;

    // Minimum objeler (Earth, Air, Water, Fire sırasına göre)
    private final int[] minRequired = {6, 9, 13, 17};

    // Gson örneği (json'a çevirme / geri çevirme)
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final String DESIGN_SAVES_DIRECTORY = "design_saves";

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
     * Hangi Hall'deyiz?
     */
    public HallType getCurrentHall() {
        return hallSequence[currentHallIndex];
    }

    /**
     * Şu anki Hall'a obje ekleme.
     */
    public void placeObject(int gridX, int gridY, String objectType) {
        HallType currentHall = getCurrentHall();
        List<BuildObject> objects = hallObjectsMap.get(currentHall);

        BuildObject obj = new BuildObject(gridX, gridY, objectType);
        objects.add(obj);
    }

    /**
     * Şu anki Hall'da minimum obje sayısına ulaştık mı?
     */
    public boolean isCurrentHallValid() {
        HallType hall = getCurrentHall();
        int required = minRequired[currentHallIndex];
        int currentCount = hallObjectsMap.get(hall).size();
        return currentCount >= required;
    }

    /**
     * Bir sonraki Hall'a geçer.
     *
     * @return false ise zaten son hall'dayız.
     */
    public boolean goToNextHall() {
        if (currentHallIndex >= hallSequence.length - 1) {
            return false;
        }
        currentHallIndex++;
        return true;
    }

    /**
     * Bir önceki Hall'a geçer.
     *
     * @return false ise zaten ilk hall'dayız.
     */
    public boolean goToPreviousHall() {
        if (currentHallIndex <= 0) {
            return false;
        }
        currentHallIndex--;
        return true;
    }

    /**
     * Tüm hall objelerini JSON formatına çevirir.
     */
    public String exportToJson() {
        return gson.toJson(hallObjectsMap);
    }
    

    /**
     * Reads hall data from JSON and replaces the local hallObjectsMap.
     *
     * @requires json may be null or empty
     * @modifies this.hallObjectsMap
     * @effects 
     *   - If json is null or empty, hallObjectsMap will remain as-is or be replaced with an empty map (implementation choice).
     *   - If json is valid, replaces existing hallObjectsMap with the parsed data.
     *   - If json is invalid, may result in a partial or empty map.
     */
    public void importFromJson(String json) {
        Type type = new TypeToken<Map<HallType, List<BuildObject>>>(){}.getType();
        Map<HallType, List<BuildObject>> data = gson.fromJson(json, type);
        if(data != null) {
            this.hallObjectsMap = data;
        }
    }

    public List<BuildObject> getObjectsForHall(HallType hallType) {
        return hallObjectsMap.getOrDefault(hallType, Collections.emptyList());
    }

    /**
     * Debug amaçlı console'a yazdırır. (Opsiyonel)
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

    // Örnek "saveWorld" fonksiyonunu console'a yazacak şekilde koruyabilirsiniz
    public void saveWorld(String worldName) {
        System.out.println("Saving world: " + worldName);
        printAllObjects();
        System.out.println("Done saving " + worldName);
    }

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
