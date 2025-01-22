package tr.edu.ku.comp302.domain.controllers;

import tr.edu.ku.comp302.domain.models.GameState;

import java.io.*;

/**
 * Handles saving and loading GameState objects using standard Java serialization.
 */
public class SaveLoadController {

    private static final String SAVE_DIR = "saves";

    /**
     * Saves the given GameState to a file in the "saves" folder.
     * @param gameState The snapshot of the game to save.
     * @param saveName  The base name for the save file (no extension).
     * @return true if successful, false otherwise
     */
    public static boolean saveGame(GameState gameState, String saveName) {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // We store as .ser (serialized), but you could use .bin or any extension
        File saveFile = new File(dir, saveName + ".ser");

        try (FileOutputStream fos = new FileOutputStream(saveFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(gameState);
            System.out.println("Game saved to file: " + saveFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads a GameState from a file in the "saves" folder.
     * @param saveName The base name of the save file (no extension).
     * @return The deserialized GameState, or null if an error occurs.
     */
    public static GameState loadGame(String saveName) {
        File saveFile = new File(SAVE_DIR, saveName + ".ser");
        if (!saveFile.exists()) {
            System.err.println("Save file not found: " + saveFile.getAbsolutePath());
            return null;
        }

        try (FileInputStream fis = new FileInputStream(saveFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            GameState gameState = (GameState) ois.readObject();
            System.out.println("Game loaded from file: " + saveFile.getAbsolutePath());
            return gameState;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}