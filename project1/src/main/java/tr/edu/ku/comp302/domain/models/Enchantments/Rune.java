package tr.edu.ku.comp302.domain.models.enchantments;

import tr.edu.ku.comp302.domain.models.Player;

public class Rune extends Enchantment {

    public Rune(int x, int y, int spawnGameTime) {
        super(EnchantmentType.RUNE, x, y, spawnGameTime);
    }

    @Override
    public void onCollect(Player player) {
        // Immediately add 1 life
        player.getInventory().addItem(this);;
        System.out.println("Runes is added to player's inventory.");
    }
}