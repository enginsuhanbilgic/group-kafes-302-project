package tr.edu.ku.comp302.domain.models;

public class BuildObject {
    private int x;              // grid koordinatı
    private int y;              // grid koordinatı
    private String objectType;  // "box", "chest", "skull" vs.

    public BuildObject(int x, int y, String objectType) {
        this.x = x;
        this.y = y;
        this.objectType = objectType;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public String getObjectType() { return objectType; }

    @Override
    public String toString() {
        return "BuildObject{" +
                "x=" + x +
                ", y=" + y +
                ", type='" + objectType + '\'' +
                '}';
    }
}
