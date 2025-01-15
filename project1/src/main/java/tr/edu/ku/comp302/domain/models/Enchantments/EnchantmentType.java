package tr.edu.ku.comp302.domain.models.enchantments;

public enum EnchantmentType {
    EXTRA_TIME,
    EXTRA_LIFE,
    REVEAL,
    CLOAK_OF_PROTECTION,
    LURING_GEM,
    RUNE;

    @Override
    public String toString() {
        switch (this) {
            case EXTRA_TIME:
                return "Extra Time";
            case EXTRA_LIFE:
                return "Extra Life";
            case REVEAL:
                return "Reveal";
            case CLOAK_OF_PROTECTION:
                return "Cloak of Protection";
            case LURING_GEM:
                return "Luring Gem";
            case RUNE:
                return "Rune";
            default:
                throw new IllegalArgumentException("Unknown EnchantmentType: " + this);
        }
    }
}
