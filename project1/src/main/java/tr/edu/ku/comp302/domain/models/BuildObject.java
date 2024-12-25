package tr.edu.ku.comp302.domain.models;

public class BuildObject {
    private int x;              // grid koordinatı
    private int y;              // grid koordinatı
    private String objectType;  // "box", "chest", "skull" vs.
    private boolean hasRune;

    public BuildObject(int x, int y, String objectType) {
        this.x = x;
        this.y = y;
        this.objectType = objectType;
        this.hasRune = false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public String getObjectType() { return objectType; }
    public boolean getHasRune(){return this.hasRune;}
    public void setHasRune(boolean val){this.hasRune = val;}

    @Override
    public String toString() {
        return "BuildObject{" +
                "x=" + x +
                ", y=" + y +
                ", type='" + objectType + '\'' +
                ", hasRune=" + hasRune +
                '}';
    }
}
