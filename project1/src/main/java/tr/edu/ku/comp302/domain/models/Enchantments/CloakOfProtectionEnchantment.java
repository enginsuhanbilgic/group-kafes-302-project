package tr.edu.ku.comp302.domain.models.enchantments;

import tr.edu.ku.comp302.domain.models.Player;

public class CloakOfProtectionEnchantment extends Enchantment {

    public CloakOfProtectionEnchantment(int x, int y, int spawnGameTime) {
        super(EnchantmentType.CLOAK_OF_PROTECTION, x, y, spawnGameTime);
    }

    @Override
    public void onCollect(Player player) {
        // Store in inventory for later usage
        player.getInventory().addItem(this);
        System.out.println("[Cloak] Cloak of Protection added to inventory!");
    }
}
