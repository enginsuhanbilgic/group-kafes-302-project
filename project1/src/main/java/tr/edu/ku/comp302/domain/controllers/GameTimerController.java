package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.domain.models.GameTimer;
import java.util.function.Consumer;

public class GameTimerController {
    private GameTimer gameTimer;
    private Consumer<Integer> onTick;
    private Runnable onTimeUp;

    public GameTimerController(Consumer<Integer> onTick, Runnable onTimeUp) {
        this.onTick = onTick;
        this.onTimeUp = onTimeUp;
        
    }

    public void start(int initialTime) {
        if (gameTimer == null) {
            gameTimer = new GameTimer(onTick, onTimeUp);
        }
        gameTimer.start(initialTime);
    }

    public void pause() {
        if (gameTimer != null) gameTimer.pause();
    }

    public void resume() {
        if (gameTimer != null) gameTimer.resume();
    }

    public void stop() {
        if (gameTimer != null) gameTimer.stop();
    }

    public void addTime(int extraTime) {
        if (gameTimer != null) gameTimer.addTime(extraTime);
    }

    public int getTimeRemaining() {
        return (gameTimer != null) ? gameTimer.getTimeRemaining() : 0;
    }
}