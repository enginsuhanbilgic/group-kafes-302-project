package tr.edu.ku.comp302.domain.models.enchantments;

import tr.edu.ku.comp302.domain.models.Player;

public class ExtraTimeEnchantment extends Enchantment {

    // +5 seconds added to player's timer immediately
    public ExtraTimeEnchantment(int x, int y, int spawnGameTime) {
        super(EnchantmentType.EXTRA_TIME, x, y, spawnGameTime);
    }

    @Override
    public void onCollect(Player player) {
        // In your game, you probably have a reference to your timer in the PlayModeController.
        // But let's assume the player can somehow reference or request +5s from the controller.
        // For demonstration, we’ll just store the “time bonus needed” in the player,
        // or call a method like player.requestExtraTime(5).
        player.requestExtraTime(5);
        System.out.println("[ExtraTime] Player gained +5 seconds!");
    }
}