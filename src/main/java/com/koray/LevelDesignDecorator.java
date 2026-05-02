package com.koray;

/**
 * Decorator that sets the card border color based on the card's tier.
 * Sits at the outermost layer of the design chain:
 *   BaseDesign → CardTypeDecorator → LevelDesignDecorator
 *
 * Border colors by tier:
 *   Tier 0 (levels 1-5)  → gray   (#AAAAAA) — common
 *   Tier 1 (levels 6-10) → gold   (#DAA520) — uncommon
 *   Tier 2+  (level 11+) → purple (#9B30FF) — rare
 */
public class LevelDesignDecorator extends DesignDecorator {

    /** Tier index derived from the level at construction time. */
    private int tier;

    /**
     * @param wrapped the inner design (CardTypeDecorator) to decorate
     * @param level   the game level used to compute the tier
     */
    public LevelDesignDecorator(CardDesign wrapped, int level) {
        super(wrapped);
        this.tier = CardFactory.tierForLevel(level);
    }

    /** Delegates background color to the inner design (type-based coloring). */
    @Override
    public String getBackground() { return wrapped.getBackground(); }

    /**
     * Returns a border color that reflects the card's rarity tier.
     */
    @Override
    public String getBorder() {
        return switch (tier) {
            case 0  -> "#AAAAAA"; // gray   — common
            case 1  -> "#DAA520"; // gold   — uncommon
            default -> "#9B30FF"; // purple — rare
        };
    }
}