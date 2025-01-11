package tr.edu.ku.comp302.domain.controllers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameTimerControllerTest {

    private GameTimerController timerController;
    private CountDownLatch latch;
    
    @BeforeEach
    void setUp() {
        // Create a CountDownLatch for synchronization with the timer's onTick callback
        latch = new CountDownLatch(1);
        
        // Create a GameTimerController with callbacks
        timerController = new GameTimerController(
                timeRemaining -> {
                    // Dummy callback to simulate onTick, which will count down time
                    System.out.println("Time remaining: " + timeRemaining);
                    if (timeRemaining == 0) {
                        latch.countDown(); // Release the latch when time reaches 0
                    }
                },
                () -> System.out.println("Time's up!")
        );
    }

    @Test
    void testStartTimer() throws InterruptedException {
        timerController.start(5);  // Start the timer with 5 seconds

        // Wait for the timer to reach 0 (up to 6 seconds for safety)
        boolean finishedInTime = latch.await(6, TimeUnit.SECONDS);
        
        assertTrue(finishedInTime, "Timer should stop after 5 seconds.");
        assertEquals(0, timerController.getTimeRemaining(), "Time remaining should be 0 after time's up.");
    }

    @Test
    void testPauseAndResumeTimer() throws InterruptedException {
        timerController.start(5);  // Start the timer with 5 seconds

        // Wait for 2 seconds
        Thread.sleep(2000);
        timerController.pause();  // Pause the timer

        int timeBeforeResume = timerController.getTimeRemaining();
        assertTrue(timeBeforeResume <= 3, "Timer should have paused with time remaining.");

        // Wait for 2 seconds, ensuring the timer is paused
        Thread.sleep(2000);

        // Time should not have changed
        assertEquals(timeBeforeResume, timerController.getTimeRemaining(), "Time should remain the same after pausing.");

        // Resume the timer and wait for it to finish
        timerController.resume();
        boolean finishedInTime = latch.await(6, TimeUnit.SECONDS);

        assertTrue(finishedInTime, "Timer should finish after resuming.");
    }

    @Test
    void testStopTimer() throws InterruptedException {
        timerController.start(5);  // Start the timer with 5 seconds

        // Wait for 2 seconds
        Thread.sleep(2000);
        timerController.stop();  // Stop the timer

        // Timer should have stopped, time remaining should still be 3 or more
        int remainingAfterStop = timerController.getTimeRemaining();
        assertTrue(remainingAfterStop > 0, "Timer should have stopped but not reset the time.");
    }

    @Test
    void testAddTime() throws InterruptedException {
        timerController.start(3);  // Start the timer with 3 seconds

        // Wait for 1 second
        Thread.sleep(1000);

        timerController.addTime(2);  // Add 2 seconds to the timer

        assertEquals(4, timerController.getTimeRemaining(), "Time remaining should be 4 seconds after adding time.");
        
        // Wait for the timer to finish
        boolean finishedInTime = latch.await(6, TimeUnit.SECONDS);
        
        assertTrue(finishedInTime, "Timer should finish after adding time.");
    }
}
