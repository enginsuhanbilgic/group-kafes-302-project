package tr.edu.ku.comp302.domain.models.Enchantments;

import tr.edu.ku.comp302.domain.models.Player;

public class LuringGemEnchantment extends Enchantment {

    public LuringGemEnchantment(int x, int y, long spawnTime) {
        super(EnchantmentType.LURING_GEM, x, y, spawnTime);
    }

    @Override
    public void onCollect(Player player) {
        // Store in inventory for later usage
        player.getInventory().addItem(this);
        System.out.println("[LuringGem] Luring Gem added to inventory!");
    }
}
