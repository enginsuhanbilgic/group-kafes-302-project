package tr.edu.ku.comp302.domain.models.Enchantments;

import tr.edu.ku.comp302.domain.models.Player;

public abstract class Enchantment {

    protected final EnchantmentType type;
    protected final int x;  // pixel X (or tile col*tileSize, whichever you prefer)
    protected final int y;  // pixel Y
    protected final long spawnTime;  // timestamp in milliseconds when spawned
    protected final int DESPAWN_DELAY = 6000; // 6 seconds

    public Enchantment(EnchantmentType type, int x, int y, long spawnTime) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.spawnTime = spawnTime;
    }

    public EnchantmentType getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toString(){
        return this.type.toString();
    }

    /**
     * Returns true if more than 6 seconds have passed since spawn.
     */
    public boolean isExpired() {
        if(this.getType()==EnchantmentType.RUNE){
            return false;
        }
        long now = System.currentTimeMillis();
        return (now - spawnTime) >= DESPAWN_DELAY;
    }

    /**
     * Called when the player collects (left-clicks) this enchantment.
     * If it is an immediate-effect enchantment, apply effect instantly.
     * Otherwise, add to inventory.
     *
     * @param player Reference to the hero (player).
     */
    public abstract void onCollect(Player player);
}
