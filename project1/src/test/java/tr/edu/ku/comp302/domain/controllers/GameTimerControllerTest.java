package tr.edu.ku.comp302.domain.controllers;


import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tr.edu.ku.comp302.domain.models.GameTimer;

/**
 * Test suite for the GameTimerController class.
 * Verifies the controller's functionality for managing the game timer's lifecycle.
 */
class GameTimerControllerTest {

    private GameTimerController gameTimerController; // The controller being tested
    private GameTimer mockGameTimer; // Mocked GameTimer instance
    private Consumer<Integer> mockOnTick; // Mocked Consumer for tick updates
    private Runnable mockOnTimeUp; // Mocked Runnable for time-up callback

    /**
     * Sets up the test environment before each test.
     * Initializes mocks and injects the mocked GameTimer into the controller.
     */
    @BeforeEach
    void setUp() {
        mockOnTick = mock(Consumer.class); // Mock the onTick callback
        mockOnTimeUp = mock(Runnable.class); // Mock the onTimeUp callback
        mockGameTimer = mock(GameTimer.class); // Mock the GameTimer object

        // Initialize the GameTimerController with mocked callbacks
        gameTimerController = new GameTimerController(mockOnTick, mockOnTimeUp);

        // Inject the mocked GameTimer into the controller
        GameTimerController gameTimerControllerSpy = spy(gameTimerController);
        doReturn(mockGameTimer).when(gameTimerControllerSpy).start(anyInt());
    }

    @Test
    void testStart_ValidInitialTime() {
        // Act: Start the timer with an initial time of 60 seconds
        gameTimerController.start(60);

        // Assert: Verify that the start method is called on the mockGameTimer
        verify(mockGameTimer).start(60);
    }

    @Test
    void testPause_WhenGameTimerExists() {
        // Arrange: Start the timer to initialize the GameTimer object
        gameTimerController.start(60);

        // Act: Pause the timer
        gameTimerController.pause();

        // Assert: Verify that the pause method is called on the mockGameTimer
        verify(mockGameTimer).pause();
    }

    @Test
    void testResume_WhenGameTimerExists() {
        // Arrange: Start the timer to initialize the GameTimer object
        gameTimerController.start(60);

        // Act: Resume the timer
        gameTimerController.resume();

        // Assert: Verify that the resume method is called on the mockGameTimer
        verify(mockGameTimer).resume();
    }

    @Test
    void testStop_WhenGameTimerExists() {
        // Arrange: Start the timer to initialize the GameTimer object
        gameTimerController.start(60);

        // Act: Stop the timer
        gameTimerController.stop();

        // Assert: Verify that the stop method is called on the mockGameTimer
        verify(mockGameTimer).stop();
    }

    @Test
    void testAddTime_WhenGameTimerExists() {
        // Arrange: Start the timer to initialize the GameTimer object
        gameTimerController.start(60);

        // Act: Add extra time to the timer
        gameTimerController.addTime(30);

        // Assert: Verify that the addTime method is called on the mockGameTimer with the correct value
        verify(mockGameTimer).addTime(30);
    }

    @Test
    void testGetTimeRemaining_WhenGameTimerExists() {
        // Arrange: Mock the return value for getTimeRemaining
        when(mockGameTimer.getTimeRemaining()).thenReturn(45);

        // Act: Start the timer and get the remaining time
        gameTimerController.start(60);
        int timeRemaining = gameTimerController.getTimeRemaining();

        // Assert: Verify that the remaining time is returned correctly
        assertEquals(45, timeRemaining, "The remaining time should match the mocked value.");
    }

   
}
