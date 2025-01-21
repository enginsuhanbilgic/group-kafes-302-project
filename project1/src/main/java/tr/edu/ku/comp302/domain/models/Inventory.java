package tr.edu.ku.comp302.domain.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tr.edu.ku.comp302.domain.models.enchantments.*;

/**
 * A simple inventory that can store Enchantments
 * (only those not consumed instantly, typically).
 */
public class Inventory implements Serializable {

    private final List<Enchantment> items;

    public Inventory() {
        this.items = new ArrayList<>();
    }

    public void addItem(Enchantment e) {
        items.add(e);
    }

    public boolean removeItem(Enchantment e) {
        return items.remove(e);
    }

    /**
     * Returns the first enchantment of the given type from the inventory, or null if not found.
     */
    public Enchantment getEnchantmentByType(EnchantmentType type) {
        for (Enchantment e : items) {
            if (e.getType() == type) {
                return e;
            }
        }
        return null;
    }

    public boolean hasRune() {
        return getEnchantmentByType(EnchantmentType.RUNE) != null;
    }

    public boolean hasEnchantment(EnchantmentType type) {
        return getEnchantmentByType(type) != null;
    }

    public List<Enchantment> getAllItems() {
        return items;
    }
}