package tr.edu.ku.comp302.domain.models;

import tr.edu.ku.comp302.config.GameConfig;
import tr.edu.ku.comp302.domain.models.Enchantments.Enchantment;
import tr.edu.ku.comp302.domain.models.Enchantments.EnchantmentType;

/**
 * The Player class represents the player character's state in the game.
 */
public class Player extends Entity {

    private boolean walking = false;
    private int lives;
    private final Inventory inventory;

    // -- Additional fields for acceleration-based movement --
    private float velocityX = 0f;
    private float velocityY = 0f;
    /**
     * How quickly the player accelerates upon key-press.
     */
    private float acceleration = 0.5f;
    /**
     * How quickly the player decelerates.
     */
    private float friction = 0.05f;
    /**
     * Maximum speed on either axis.
     */
    private float maxSpeed = 5.0f;

    // Active buffs
    private boolean cloakActive;
    private long cloakEndTime;
    private boolean revealActive;
    private long revealEndTime;
    private int bonusTimeRequested;
    private boolean drawDamageBox = false;
    private long damageBoxEndTime;

    public Player(int x, int y, int speed) {
        super(x, y, speed);
        this.lives = GameConfig.PLAYER_LIVES; // e.g., start with 3 lives
        this.inventory = new Inventory();
        this.cloakActive = false;
        this.revealActive = false;
        this.bonusTimeRequested = 0;
    }

    // ============ Movement Mechanics ============

    /**
     * Accelerate in X direction.
     */
    public void accelerateX(float amt) {
        velocityX += amt;
    }

    /**
     * Accelerate in Y direction.
     */
    public void accelerateY(float amt) {
        velocityY += amt;
    }

    /**
     * Apply friction (or “drag”) to gradually slow the player when no keys are pressed.
     */
    public void applyFriction() {
        // If velocity is small, set to 0 to avoid floating point accumulation
        if (Math.abs(velocityX) < 0.03f) {
            velocityX = 0;
        } else {
            // reduce velocity by friction factor
            velocityX *= (0.96f - friction);
        }

        if (Math.abs(velocityY) < 0.03f) {
            velocityY = 0;
        } else {
            velocityY *= (0.96f - friction);
        }
    }

    /**
     * Ensure velocity never exceeds maxSpeed on any axis.
     */
    public void clampSpeed() {
        if (velocityX > maxSpeed) velocityX = maxSpeed;
        if (velocityX < -maxSpeed) velocityX = -maxSpeed;
        if (velocityY > maxSpeed) velocityY = maxSpeed;
        if (velocityY < -maxSpeed) velocityY = -maxSpeed;
    }

    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public float getFriction() {
        return friction;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    // ============ Getters / Setters ============

    public boolean isWalking() {
        return walking;
    }

    public void setWalking(boolean walking) {
        this.walking = walking;
    }

    public int getLives() {
        return lives;
    }

    public void loseLife() {
        this.lives--;
        if (this.lives < 0) {
            this.lives = 0;
        }
        this.drawDamageBox = true;
        this.damageBoxEndTime = System.currentTimeMillis() + 500; // 0.5 seconds
    }

    public void addLife() {
        this.lives++;
    }

    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Called by an ExtraTimeEnchantment; we accumulate requests here,
     * and the PlayModeController can poll for bonusTime each update cycle.
     */
    public void requestExtraTime(int seconds) {
        this.bonusTimeRequested += seconds;
    }

    /**
     * Retrieve requested extra time and zero out the counter.
     */
    public int consumeBonusTime() {
        int temp = bonusTimeRequested;
        bonusTimeRequested = 0;
        return temp;
    }

    // ==================== CLOAK LOGIC ====================
    public boolean isCloakActive() {
        if (cloakActive && System.currentTimeMillis() > cloakEndTime) {
            cloakActive = false;
        }
        return cloakActive;
    }

    public void useCloakOfProtection() {
        Enchantment cloak = inventory.getEnchantmentByType(EnchantmentType.CLOAK_OF_PROTECTION);
        if (cloak != null) {
            inventory.removeItem(cloak);
            cloakActive = true;
            cloakEndTime = System.currentTimeMillis() + 20_000; // 20 seconds
            System.out.println("Cloak of Protection is now active for 20s!");
        } else {
            System.out.println("No Cloak of Protection in inventory!");
        }
    }

    // ==================== REVEAL LOGIC ====================
    public boolean isRevealActive() {
        if (revealActive && System.currentTimeMillis() > revealEndTime) {
            revealActive = false;
        }
        return revealActive;
    }

    public void useReveal() {
        Enchantment reveal = inventory.getEnchantmentByType(EnchantmentType.REVEAL);
        if (reveal != null) {
            inventory.removeItem(reveal);
            revealActive = true;
            revealEndTime = System.currentTimeMillis() + 10_000; // 10 seconds
            System.out.println("Reveal is active for 10s! A 4x4 area around the rune is highlighted.");
        } else {
            System.out.println("No Reveal enchantment in inventory!");
        }
    }

    // ==================== LURING GEM LOGIC ====================
    public void useLuringGem(char direction) {
        Enchantment gem = inventory.getEnchantmentByType(EnchantmentType.LURING_GEM);
        if (gem != null) {
            inventory.removeItem(gem);
            System.out.println("Luring Gem thrown to direction: " + direction);
            // In a real game, you'd place the gem at some location and cause Fighter monsters to walk there.
        } else {
            System.out.println("No Luring Gem in inventory!");
        }
    }

    public boolean isDrawDamageBox() {
        if (drawDamageBox && System.currentTimeMillis() > damageBoxEndTime) {
            drawDamageBox = false; // reset when time is up.
        }
        return drawDamageBox;
    }

    public void resetEffects() {
        this.cloakActive = false;
        this.revealActive = false;
    }
}
