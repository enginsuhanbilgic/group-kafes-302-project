package tr.edu.ku.comp302.domain.models;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.function.Consumer;

public class GameTimer implements Serializable {

    public volatile int timeRemaining; // Remaining time
    private Timer timer;       // Swing Timer
    private final Consumer<Integer> onTick; // Callback for each tick
    private final Runnable onTimeUp;        // Callback when time is up

    public GameTimer(Consumer<Integer> onTick, Runnable onTimeUp) {
        this.onTick = onTick;
        this.onTimeUp = onTimeUp;
    }

    public void start(int initialTime) {
        // If timer is already running, don't start again
        if (timer != null && timer.isRunning()) {
            return;
        }

        // Initialize the remaining time
        timeRemaining = initialTime;

        // Immediately notify UI of the starting time
        onTick.accept(timeRemaining);

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeRemaining--;

                // If time is up, stop and call onTimeUp
                if (timeRemaining <= 0) {
                    stop();
                    onTick.accept(0); // ensure we show 0 on UI before onTimeUp
                    onTimeUp.run();
                } else {
                    onTick.accept(timeRemaining);
                }
            }
        });

        // If you want the countdown to start without delay, setInitialDelay to 0
        timer.setInitialDelay(0);
        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    public void addTime(int extraTime) {
        timeRemaining += extraTime;
        // Update UI immediately to reflect new time
        onTick.accept(timeRemaining);
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void pause() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    public void resume() {
        if (timer != null && !timer.isRunning()) {
            timer.start();
        }
    }
}