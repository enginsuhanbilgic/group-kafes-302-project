package tr.edu.ku.comp302.domain.models.enchantments;

import tr.edu.ku.comp302.domain.models.Player;

public class ExtraLifeEnchantment extends Enchantment {

    public ExtraLifeEnchantment(int x, int y, int spawnGameTime) {
        super(EnchantmentType.EXTRA_LIFE, x, y, spawnGameTime);
    }

    @Override
    public void onCollect(Player player) {
        // Immediately add 1 life
        player.addLife();
        System.out.println("[ExtraLife] Player gained +1 life! Lives now: " + player.getLives());
    }
}
