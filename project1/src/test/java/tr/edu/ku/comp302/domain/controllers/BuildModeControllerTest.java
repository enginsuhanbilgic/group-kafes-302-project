package tr.edu.ku.comp302.domain.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import tr.edu.ku.comp302.domain.models.BuildObject;
import tr.edu.ku.comp302.domain.models.HallType;

import java.util.List;

class BuildModeControllerTest {

    private BuildModeController buildModeController;

    @BeforeEach
    void setUp() {
        buildModeController = new BuildModeController();
    }

    @Test
    void testImportFromJson_Valid() {
        String json = """
            {
              "EARTH": [
                {"x":0,"y":0,"objectType":"box","hasRune":false},
                {"x":1,"y":1,"objectType":"chest_closed","hasRune":false}
              ],
              "AIR": []
            }
            """;

        buildModeController.importFromJson(json);

        List<BuildObject> earthList = buildModeController.getObjectsForHall(HallType.EARTH);
        assertEquals(2, earthList.size(), "Should have 2 build objects in Earth hall");
        assertEquals("box", earthList.get(0).getObjectType());
        assertEquals("chest_closed", earthList.get(1).getObjectType());

        // "AIR" should exist but have 0 objects
        List<BuildObject> airList = buildModeController.getObjectsForHall(HallType.AIR);
        assertTrue(airList.isEmpty(), "Air hall should be empty");
    }

    @Test
    void testImportFromJson_Null() {
        // If JSON is null, the map should remain with initial empty lists
        buildModeController.importFromJson(null);

        // Each hall list should remain empty
        for (HallType hall : HallType.values()) {
            List<BuildObject> hallList = buildModeController.getObjectsForHall(hall);
            assertNotNull(hallList, "Hall list should not be null after null JSON");
            assertTrue(hallList.isEmpty(), "Hall " + hall + " should remain empty after null JSON");
        }
    }

    @Test
    void testImportFromJson_InvalidFormat_ThrowsException() {
        // Completely invalid JSON string to ensure GSON fails to parse and throws JsonSyntaxException
        String badJson = "This is not a valid JSON string";

        // Assert that importing invalid JSON throws JsonSyntaxException
        assertThrows(com.google.gson.JsonSyntaxException.class, () -> {
            buildModeController.importFromJson(badJson);
        }, "Importing invalid JSON should throw JsonSyntaxException");
    }
}
