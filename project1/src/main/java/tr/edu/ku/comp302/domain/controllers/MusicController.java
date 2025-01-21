package tr.edu.ku.comp302.domain.controllers;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MusicController {
    private Clip clip;
    private AudioInputStream audioStream;
    private boolean isPaused = false;
    private long pausePosition = 0;

    // Constructor to initialize the audio file
    public MusicController(String filePath) {
        try {
            // Load the audio file as a resource stream
            InputStream audioInputStream = getClass().getClassLoader().getResourceAsStream(filePath);

            if (audioInputStream == null) {
                throw new IOException("Audio file not found: " + filePath);
            }

            audioStream = AudioSystem.getAudioInputStream(audioInputStream);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            
            // Add a listener to handle when the music ends and restart it
            clip.addLineListener(e -> {
                if (e.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                    if (clip.getFramePosition() == clip.getFrameLength()) {
                        System.out.println("Music finished, restarting...");
                        clip.setFramePosition(0);  // Reset the music to the beginning
                        play();  // Start playing again
                    }
                }
            });

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error initializing music: " + e.getMessage());
        }
    }

    // Method to play the music
    public void play() {
        if (clip == null) return;

        if (isPaused) {
            clip.setMicrosecondPosition(pausePosition);
            isPaused = false;
        }
        clip.start();
        System.out.println("Playing music...");
    }

    // Method to pause the music
    public void pause() {
        if (clip == null || !clip.isRunning()) return;

        pausePosition = clip.getMicrosecondPosition();
        clip.stop();
        isPaused = true;
        System.out.println("Music paused.");
    }

    // Method to stop the music
    public void stop() {
        if (clip == null) return;

        clip.stop();
        clip.setMicrosecondPosition(0);
        isPaused = false;
        System.out.println("Music stopped.");
    }

    // Method to close resources
    public void close() {
        if (clip != null) {
            clip.close();
        }
        try {
            if (audioStream != null) {
                audioStream.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }


}
