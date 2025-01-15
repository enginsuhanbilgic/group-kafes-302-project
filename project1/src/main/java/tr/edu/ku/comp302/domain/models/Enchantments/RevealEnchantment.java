package tr.edu.ku.comp302.domain.models.enchantments;

import tr.edu.ku.comp302.domain.models.Player;

public class RevealEnchantment extends Enchantment {

    public RevealEnchantment(int x, int y, int spawnGameTime) {
        super(EnchantmentType.REVEAL, x, y, spawnGameTime);
    }

    @Override
    public void onCollect(Player player) {
        // This is stored for later usage
        // So we add an item to the player's inventory
        player.getInventory().addItem(this);
        System.out.println("[Reveal] Reveal enchantment added to inventory!");
    }
}