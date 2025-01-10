package tr.edu.ku.comp302.domain.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.event.KeyEvent;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for KeyHandler
 * 
 * Specifications for KeyHandler:
 * requires: KeyEvent objects must not be null
 * modifies: boolean flags for various key states
 * effects: updates the state of key flags based on key events
 */
class KeyHandlerTest {
    private KeyHandler keyHandler;
    private KeyEvent mockUpKeyPress;
    private KeyEvent mockDownKeyPress;
    private KeyEvent mockLeftKeyPress;
    private KeyEvent mockRightKeyPress;
    private KeyEvent mockEscKeyPress;
    private KeyEvent mockHKeyPress;
    private KeyEvent mockBKeyPress;
    private KeyEvent mockPKeyPress;
    private KeyEvent mockRKeyPress;

    @BeforeEach
    void setUp() {
        keyHandler = new KeyHandler();
        // Initialize mock KeyEvents
        mockUpKeyPress = new KeyEvent(new java.awt.Component(){}, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED);
        mockDownKeyPress = new KeyEvent(new java.awt.Component(){}, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED);
        mockLeftKeyPress = new KeyEvent(new java.awt.Component(){}, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED);
        mockRightKeyPress = new KeyEvent(new java.awt.Component(){}, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED);
        mockEscKeyPress = new KeyEvent(new java.awt.Component(){}, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
        mockHKeyPress = new KeyEvent(new java.awt.Component(){}, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_H, KeyEvent.CHAR_UNDEFINED);
        mockBKeyPress = new KeyEvent(new java.awt.Component(){}, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_B, KeyEvent.CHAR_UNDEFINED);
        mockPKeyPress = new KeyEvent(new java.awt.Component(){}, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_P, KeyEvent.CHAR_UNDEFINED);
        mockRKeyPress = new KeyEvent(new java.awt.Component(){}, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_R, KeyEvent.CHAR_UNDEFINED);
    }

    @Test
    void testMovementKeysPressed() {
        // Test UP key
        keyHandler.keyPressed(mockUpKeyPress);
        assertTrue(keyHandler.up, "UP key should be registered as pressed");
        assertFalse(keyHandler.down, "DOWN key should not be affected");

        // Test DOWN key
        keyHandler.keyPressed(mockDownKeyPress);
        assertTrue(keyHandler.down, "DOWN key should be registered as pressed");

        // Test LEFT key
        keyHandler.keyPressed(mockLeftKeyPress);
        assertTrue(keyHandler.left, "LEFT key should be registered as pressed");

        // Test RIGHT key
        keyHandler.keyPressed(mockRightKeyPress);
        assertTrue(keyHandler.right, "RIGHT key should be registered as pressed");
    }

    @Test
    void testSpecialKeysPressed() {
        // Test ESC key
        keyHandler.keyPressed(mockEscKeyPress);
        assertTrue(keyHandler.isEscPressed(), "ESC key should toggle to true");
        keyHandler.keyPressed(mockEscKeyPress);
        assertFalse(keyHandler.isEscPressed(), "ESC key should toggle to false");

        // Test H key
        keyHandler.keyPressed(mockHKeyPress);
        assertTrue(keyHandler.hPressed, "H key should be registered as pressed");

        // Test B key
        keyHandler.keyPressed(mockBKeyPress);
        assertTrue(keyHandler.isBPressed(), "B key should be registered as pressed");

        // Test P key
        keyHandler.keyPressed(mockPKeyPress);
        assertTrue(keyHandler.isPPressed(), "P key should be registered as pressed");

        // Test R key
        keyHandler.keyPressed(mockRKeyPress);
        assertTrue(keyHandler.isRPressed(), "R key should be registered as pressed");
    }

    @Test
    void testKeyReleased() {
        // Press and release UP key
        keyHandler.keyPressed(mockUpKeyPress);
        keyHandler.keyReleased(mockUpKeyPress);
        assertFalse(keyHandler.up, "UP key should be registered as released");

        // Press and release DOWN key
        keyHandler.keyPressed(mockDownKeyPress);
        keyHandler.keyReleased(mockDownKeyPress);
        assertFalse(keyHandler.down, "DOWN key should be registered as released");

        // Press and release LEFT key
        keyHandler.keyPressed(mockLeftKeyPress);
        keyHandler.keyReleased(mockLeftKeyPress);
        assertFalse(keyHandler.left, "LEFT key should be registered as released");

        // Press and release RIGHT key
        keyHandler.keyPressed(mockRightKeyPress);
        keyHandler.keyReleased(mockRightKeyPress);
        assertFalse(keyHandler.right, "RIGHT key should be registered as released");

        // Press and release H key
        keyHandler.keyPressed(mockHKeyPress);
        keyHandler.keyReleased(mockHKeyPress);
        assertFalse(keyHandler.hPressed, "H key should be registered as released");
    }

    @Test
    void testResetKeys() {
        // Press all movement keys
        keyHandler.keyPressed(mockUpKeyPress);
        keyHandler.keyPressed(mockDownKeyPress);
        keyHandler.keyPressed(mockLeftKeyPress);
        keyHandler.keyPressed(mockRightKeyPress);

        // Reset keys
        keyHandler.resetKeys();

        // Verify all movement keys are reset
        assertFalse(keyHandler.up, "UP key should be reset");
        assertFalse(keyHandler.down, "DOWN key should be reset");
        assertFalse(keyHandler.left, "LEFT key should be reset");
        assertFalse(keyHandler.right, "RIGHT key should be reset");
    }

    @Test
    void testMultipleKeyPresses() {
        // Press multiple keys simultaneously
        keyHandler.keyPressed(mockUpKeyPress);
        keyHandler.keyPressed(mockRightKeyPress);
        keyHandler.keyPressed(mockBKeyPress);

        // Verify all pressed keys are registered
        assertTrue(keyHandler.up, "UP key should be registered as pressed");
        assertTrue(keyHandler.right, "RIGHT key should be registered as pressed");
        assertTrue(keyHandler.isBPressed(), "B key should be registered as pressed");
        assertFalse(keyHandler.down, "DOWN key should not be affected");
        assertFalse(keyHandler.left, "LEFT key should not be affected");
    }
} 