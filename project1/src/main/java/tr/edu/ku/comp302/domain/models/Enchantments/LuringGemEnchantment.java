package tr.edu.ku.comp302.domain.models.enchantments;

import tr.edu.ku.comp302.domain.models.Player;

public class LuringGemEnchantment extends Enchantment {

    public LuringGemEnchantment(int x, int y, int spawnGameTime) {
        super(EnchantmentType.LURING_GEM, x, y, spawnGameTime);
    }

    @Override
    public void onCollect(Player player) {
        // Store in inventory for later usage
        player.getInventory().addItem(this);
        System.out.println("[LuringGem] Luring Gem added to inventory!");
    }
}
