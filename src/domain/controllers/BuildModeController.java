package domain.controllers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class BuildModeController {
    private int rows, cols; // Harita boyutları
    private List<Point> placedObjects; // Objelerin konumları (x, y)
    private int minObjects; // Minimum obje sayısı

    public BuildModeController(int rows, int cols, int minObjects) {
        this.rows = rows;
        this.cols = cols;
        this.minObjects = minObjects;
        this.placedObjects = new ArrayList<>();
    }

    public boolean addObject(int x, int y) {
        Point point = new Point(x, y);
        if (!placedObjects.contains(point)) { // Aynı yere ikinci kez obje eklemeyi engelle
            placedObjects.add(point);
            return true;
        }
        return false;
    }

    public boolean removeObject(int x, int y) {
        Point point = new Point(x, y);
        return placedObjects.remove(point);
    }

    public boolean isValid() {
        return placedObjects.size() >= minObjects;
    }

    public int getObjectCount() {
        return placedObjects.size();
    }

    public List<Point> getPlacedObjects() {
        return placedObjects;
    }
}
