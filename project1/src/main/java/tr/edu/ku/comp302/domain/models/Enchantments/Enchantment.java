package tr.edu.ku.comp302.domain.models.enchantments;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Player;

import java.io.Serializable;

public abstract class Enchantment implements Serializable {

    protected final EnchantmentType type;
    protected final int x;  // pixel X (or tile col*tileSize, whichever you prefer)
    protected final int y;  // pixel Y
    protected int spawnGameTime;
    protected int lifetimeSeconds = GameConfig.ENCHANTMENT_SPAWN_INTERVAL; // or however long it stays

    public Enchantment(EnchantmentType type, int x, int y, int spawnGameTime) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.spawnGameTime = spawnGameTime;
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

    public int getSpawnGameTime() {
        return spawnGameTime;
    }

    public int getLifetimeSeconds() {
        return lifetimeSeconds;
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