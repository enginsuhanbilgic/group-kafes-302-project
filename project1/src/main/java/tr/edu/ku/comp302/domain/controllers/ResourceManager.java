package tr.edu.ku.comp302.domain.controllers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and caches images from /assets so we don't read from disk each frame.
 *
 * 1) Call ResourceManager.init() once, near game startup (e.g., in Main).
 * 2) Use ResourceManager.getImage("someKey") in your controllers/draw methods.
 */
public class ResourceManager {

    private static final Map<String, BufferedImage> imageCache = new HashMap<>();

    /**
     * Load all images you might need during the game. Call once.
     */
    public static void init() {
        // Example: load object images
        loadImage("box",               "/assets/box.png");
        loadImage("chest_closed",      "/assets/chest_closed.png");
        loadImage("column_wall",       "/assets/column_wall.png");
        loadImage("skull",             "/assets/skull.png");
        loadImage("boxes_stacked", "/assets/boxes_stacked.png");

        // Example: load enchantments
        loadImage("enchantment_extratime", "/assets/enchantment_extratime.png");
        loadImage("enchantment_heart",     "/assets/enchantment_heart.png");
        loadImage("enchantment_reveal",    "/assets/enchantment_reveal.png");
        loadImage("enchantment_cloak",     "/assets/enchantment_cloak.png");
        loadImage("enchantment_gem",       "/assets/enchantment_gem.png");
        loadImage("rune",                  "/assets/rune.png");

        loadImage("thrown_enchantment_gem", "/assets/throw_rune.png");

        // Example: load monster images
        loadImage("npc_fighter", "/assets/npc_fighter.png");
        loadImage("npc_archer",  "/assets/npc_archer.png");
        loadImage("npc_wizard",  "/assets/npc_wizard.png");

        // Example: load player images
        loadImage("player_walk_1", "/assets/player_walk_1.png");
        loadImage("player_walk_2", "/assets/player_walk_2.png");
        loadImage("player_stand",  "/assets/player_stand.png");

        // Example: load tile images
        loadImage("floor_plain",       "/assets/floor_plain.png");
        loadImage("wall_outer_e",      "/assets/wall_outer_e.png");
        loadImage("wall_outer_w",      "/assets/wall_outer_w.png");
        loadImage("wall_outer_n",      "/assets/wall_outer_n.png");
        loadImage("wall_outer_ne",     "/assets/wall_outer_ne.png");
        loadImage("wall_outer_nw",     "/assets/wall_outer_nw.png");
        loadImage("wall_outer_se",     "/assets/wall_outer_se.png");
        loadImage("wall_outer_sw",     "/assets/wall_outer_sw.png");
        loadImage("wall_inner_ne",     "/assets/wall_inner_ne.png");
        loadImage("wall_inner_nw",     "/assets/wall_inner_nw.png");
        loadImage("wall_inner_sw",     "/assets/wall_inner_sw.png");
        loadImage("wall_inner_se",     "/assets/wall_inner_se.png");
        loadImage("wall_inner_s",      "/assets/wall_outer_n.png");
        loadImage("wall_center",       "/assets/wall_center.png");
        loadImage("wall_flag_blue",    "/assets/wall_flag_blue.png");
        loadImage("wall_flag_green",   "/assets/wall_flag_green.png");
        loadImage("wall_flag_red",     "/assets/wall_flag_red.png");
        loadImage("wall_flag_yellow",  "/assets/wall_flag_yellow.png");
        loadImage("transparent_tile",  "/assets/transparent_tile.png");
        // ...
    }

    /**
     * Returns a cached BufferedImage by key, or null if not found.
     */
    public static BufferedImage getImage(String key) {
        return imageCache.get(key);
    }

    /**
     * Private helper to load and store an image in our cache.
     */
    private static void loadImage(String key, String path) {
        try {
            BufferedImage img = ImageIO.read(ResourceManager.class.getResourceAsStream(path));
            imageCache.put(key, img);
            //System.out.println("Loaded the image successfully: " + path);
        } catch (IOException | NullPointerException e) {
            System.err.println("Failed to load image: " + path + " (key: " + key + ")");
            e.printStackTrace();
        }
    }
}