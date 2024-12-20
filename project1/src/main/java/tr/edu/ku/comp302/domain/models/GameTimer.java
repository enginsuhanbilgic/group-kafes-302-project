package tr.edu.ku.comp302.domain.models;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class GameTimer {

    private int timeRemaining; // Remaining time
    private javax.swing.Timer timer;  // Swing Timer
    private final Consumer<Integer> onTick; // Information about countdown
    private final Runnable onTimeUp;  // Executed code when time is up

    public GameTimer(Consumer<Integer> onTick, Runnable onTimeUp) {
        this.onTick = onTick;
        this.onTimeUp = onTimeUp;
    }

    public void start(int initialTime) {
        if (timer != null && timer.isRunning()) {
            return; // If timer already working do not start again
        }

        timeRemaining = initialTime;

        timer = new javax.swing.Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeRemaining--;
                onTick.accept(timeRemaining);

                if (timeRemaining <= 0) {
                    stop();
                    onTimeUp.run();
                }
            }
        });

        // Set the start delay (optional)
        timer.setInitialDelay(1000);
        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    public void addTime(int extraTime) {
        timeRemaining += extraTime;
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
