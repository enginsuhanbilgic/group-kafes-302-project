import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildObjectControllerTest {

    private BuildObjectController controller;
    private HallType hallTypeMock; 
    private BuildObject mockObject1;
    private BuildObject mockObject2;

    @BeforeEach
    void setUp() {
        // Create some stub/mocked objects
        hallTypeMock = HallType.LABYRINTH; // Or any valid HallType

        // Example mock BuildObjects
        mockObject1 = new BuildObject();
        mockObject1.setX(0);
        mockObject1.setY(0);
        mockObject1.setObjectType("object1");
        mockObject1.setHasRune(false);

        mockObject2 = new BuildObject();
        mockObject2.setX(1);
        mockObject2.setY(1);
        mockObject2.setObjectType("object2");
        mockObject2.setHasRune(false);

        controller = new BuildObjectController(hallTypeMock);
    }

    /**
     * Test 1: loadWorldFromJson with valid JSON
     */
    @Test
    void testLoadWorldFromJson_ValidData() {
        // Construct example JSON data 
        // {
        //   "LABYRINTH": [
        //       {"x":0,"y":0,"objectType":"chest","hasRune":false},
        //       {"x":1,"y":1,"objectType":"torch","hasRune":false}
        //   ]
        // }
        String validJson = 
            "{ \"LABYRINTH\": " + 
            "  [" +
            "    {\"x\": 0, \"y\": 0, \"objectType\":\"chest\", \"hasRune\":false}," +
            "    {\"x\": 1, \"y\": 1, \"objectType\":\"torch\", \"hasRune\":false}" +
            "  ]" +
            "}";

        controller.loadWorldFromJson(validJson);

        // Check that the map isn't empty
        Map<HallType, List<BuildObject>> map = controller.getWorldObjectsMap();
        assertFalse(map.isEmpty(), "Expected map not to be empty after loading valid JSON");

        // Because in loadWorldFromJson, we randomly assign the rune to one object, 
        // check repOk to ensure we still have a valid representation
        assertTrue(controller.repOk(), "Representation invariant should hold after loading valid JSON");
    }

    /**
     * Test 2: loadWorldFromJson with empty or null JSON
     */
    @Test
    void testLoadWorldFromJson_EmptyData() {
        // Pass empty JSON
        controller.loadWorldFromJson("");
        assertTrue(controller.getWorldObjectsMap().isEmpty(), 
            "Expected empty map after loading empty JSON");
        assertTrue(controller.repOk(), "repOk should hold with an empty map");

        // Pass null
        controller.loadWorldFromJson(null);
        assertTrue(controller.getWorldObjectsMap().isEmpty(), 
            "Expected empty map after loading null JSON");
        assertTrue(controller.repOk(), "repOk should hold with an empty map again");
    }

    /**
     * Test 3: setRune and removeRune
     */
    @Test
    void testSetRuneAndRemoveRune() {
        // Manually populate the map
        List<BuildObject> objectsInHall = new ArrayList<>();
        objectsInHall.add(mockObject1);
        objectsInHall.add(mockObject2);

        // Simulate that the map is properly set
        controller.getWorldObjectsMap().put(hallTypeMock, objectsInHall);

        // setRune on mockObject1
        controller.setRune(mockObject1);
        assertTrue(mockObject1.getHasRune(), "mockObject1 should have the rune");
        assertEquals(mockObject1, controller.getRuneHolder(), "runeHolder should be mockObject1");
        assertTrue(controller.repOk(), "Representation should hold after setting the rune");

        // removeRune
        controller.removeRune();
        assertFalse(mockObject1.getHasRune(), "mockObject1 should no longer have the rune");
        assertNull(controller.getRuneHolder(), "No rune holder after removal");
        assertTrue(controller.repOk(), "Representation should hold after removing the rune");
    }

    /**
     * Test 4: transferRune
     */
    @Test
    void testTransferRune() {
        // Manually populate the map
        List<BuildObject> objectsInHall = new ArrayList<>();
        objectsInHall.add(mockObject1);
        objectsInHall.add(mockObject2);
        controller.getWorldObjectsMap().put(hallTypeMock, objectsInHall);

        // Set the rune on mockObject1
        controller.setRune(mockObject1);
        assertTrue(controller.repOk(), "repOk should hold initially");

        // Transfer the rune
        controller.transferRune();

        // After transferRune, the rune should be on exactly one object 
        // (randomly chosen if code so chooses).
        // We'll just check the representation is still valid, 
        // and exactly one object hasRune==true.
        assertTrue(controller.repOk(), "repOk should hold after transferring the rune");

        // Count how many have runes
        int count = 0;
        for (BuildObject bo : objectsInHall) {
            if (bo.getHasRune()) count++;
        }
        assertEquals(1, count, "Exactly one BuildObject should have the rune after transfer");
    }
}
