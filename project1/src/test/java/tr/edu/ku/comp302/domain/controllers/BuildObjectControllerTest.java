package tr.edu.ku.comp302.domain.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import tr.edu.ku.comp302.domain.models.BuildObject;
import tr.edu.ku.comp302.domain.models.HallType;
import tr.edu.ku.comp302.domain.models.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BuildObjectControllerTest {


    /**
     * OVERVIEW:
     * BuildObjectController manages all BuildObjects across all halls,
     * including loading from JSON and ensuring only one BuildObject can have a rune.
     *
     * ABSTRACT FUNCTION:
     *   AF(c) = {
     *      hallType -> the HallType associated with this controller,
     *      worldObjectsMap -> a mapping from each HallType h to a List<BuildObject> belonging to hall h,
     *      runeHolder -> the (unique) BuildObject that currently holds the Rune (or null if none)
     *   }
     *
     * REPRESENTATION INVARIANT:
     *   1) worldObjectsMap is never null.
     *   2) For each (HallType -> List<BuildObject>) entry in worldObjectsMap, that list is non-null (though it may be empty).
     *   3) At most one BuildObject in all lists (across all HallTypes) has hasRune == true.
     *   4) If runeHolder != null, then runeHolder.getHasRune() is true, and runeHolder is present in the corresponding list in worldObjectsMap.
     *   5) If there is any BuildObject with hasRune == true, it must be the same object as runeHolder.
     *
     *
     *   NOTE:  repOK method is under BuildObjectController.java class
     */

    private BuildObjectController controller;
    private HallType testHall;
    private Player mockPlayer;

    @BeforeEach
    void setUp() {
        testHall = HallType.EARTH;
        controller = new BuildObjectController(testHall);
        mockPlayer = new Player(100, 100, 3);
    }

    @Test
    void testLoadWorldFromJson_ValidData() {
        String validJson = 
            "{ \"earth\": [" +
            "    {\"x\":0,\"y\":0,\"objectType\":\"box\",\"hasRune\":false}," +
            "    {\"x\":1,\"y\":1,\"objectType\":\"chest_closed\",\"hasRune\":false}" +
            "  ]" +
            "}";

        controller.loadWorldFromJson(validJson);

        assertFalse(controller.getWorldObjectsMap().isEmpty(), "Expected map to be non-empty");
        assertEquals(2, controller.getObjectsForHall(HallType.EARTH).size(), "Should have 2 objects in Earth");
        assertTrue(controller.repOk(), "Representation invariant must hold after loading valid JSON");
    }

    @Test
    void testLoadWorldFromJson_EmptyData() {
        // Empty data => resets map
        controller.loadWorldFromJson("");
        assertTrue(controller.getWorldObjectsMap().isEmpty(), "World map should be empty after empty JSON");
        assertTrue(controller.repOk());

        // Null => also resets map
        controller.loadWorldFromJson(null);
        assertTrue(controller.getWorldObjectsMap().isEmpty(), "World map should be empty after null JSON");
        assertTrue(controller.repOk());
    }

    @Test
    void testSetRune_AndRemoveRune() {
        // Manually populate
        List<BuildObject> earthObjects = new ArrayList<>();
        BuildObject obj1 = new BuildObject(0,0,"box");
        BuildObject obj2 = new BuildObject(1,1,"chest_closed");
        earthObjects.add(obj1);
        earthObjects.add(obj2);

        Map<HallType, List<BuildObject>> customMap = new HashMap<>();
        customMap.put(HallType.EARTH, earthObjects);

        // Reflectively set it in the controller
        controller.getWorldObjectsMap().putAll(customMap);

        // setRune on obj1
        controller.setRune(obj1);
        assertTrue(obj1.getHasRune(), "obj1 should have the rune");
        assertSame(obj1, controller.getRuneHolder(), "runeHolder should be obj1");
        assertTrue(controller.repOk());

        // removeRune
        controller.removeRune();
        assertFalse(obj1.getHasRune(), "obj1 should no longer have the rune");
        assertNull(controller.getRuneHolder(), "No rune holder after removing the rune");
        assertTrue(controller.repOk());
    }

    @Test
    void testTransferRune() {
        // Manually populate
        BuildObject objA = new BuildObject(0,0,"box");
        BuildObject objB = new BuildObject(1,1,"chest_closed");
        BuildObject objC = new BuildObject(2,2,"skull");

        List<BuildObject> earthObjects = new ArrayList<>();
        earthObjects.add(objA);
        earthObjects.add(objB);
        earthObjects.add(objC);

        controller.getWorldObjectsMap().put(HallType.EARTH, earthObjects);

        // Set the rune on objA
        controller.setRune(objA);
        assertTrue(controller.repOk());

        // Now transfer it
        controller.transferRune();

        // Either objB or objC might hold the rune, but not objA (in typical logic).
        // Or it's possible that the random selection fails to find a new object if it tries 50 times
        // and always picks objA, but let's assume it eventually picks a different one.
        // We'll just check repOk and that exactly one object hasRune==true.
        assertTrue(controller.repOk(), "repOk should hold after transferRune");
        int countWithRune = 0;
        for (BuildObject bo : earthObjects) {
            if (bo.getHasRune()) {
                countWithRune++;
            }
        }
        assertEquals(1, countWithRune, "Exactly one object should have the rune after transfer");
    }

    @Test
    void testUpdate_ClickRune_Success() {
        BuildObject obj = new BuildObject(3,3,"chest_closed");
        obj.setHasRune(true);
        controller.getWorldObjectsMap().put(HallType.EARTH, List.of(obj));

        // tileSize assumed 48 => object bounding box is [144..192)
        mockPlayer.setX(144);
        mockPlayer.setY(192); // close enough for the code's distance check

        Point click = new Point(145, 145); // inside bounding box

        controller.update(HallType.EARTH, mockPlayer, click);

        assertFalse(obj.getHasRune(), "Rune should be removed from the object after a valid click.");
        assertNull(controller.getRuneHolder(), "runeHolder should be null after collecting the rune.");
        assertTrue(mockPlayer.getInventory().hasRune(), "Player should now have the Rune in inventory.");
        assertTrue(controller.repOk());
    }





}
