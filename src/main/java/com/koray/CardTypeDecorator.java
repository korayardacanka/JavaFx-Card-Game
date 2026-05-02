package com.koray;
 
/**
 * Decorator that sets the card background color based on its effect type.
 * Sits in the middle of the design chain:
 *   BaseDesign → CardTypeDecorator → LevelDesignDecorator
 *
 * Each effect type maps to a distinct color family so the player can
 * identify card categories at a glance.
 */
public class CardTypeDecorator extends DesignDecorator {

    /** The effect instance used to determine the background color. */
    private CardEffect effect;

    /**
     * @param wrapped the inner design to delegate to when no match is found
     * @param effect  the card's effect, used for type-checking
     */
    public CardTypeDecorator(CardDesign wrapped, CardEffect effect) {
        super(wrapped);
        this.effect = effect;
    }

    /**
     * Returns a color based on the effect type:
     * red for damage, green for heal, blue for shield,
     * dark green for poison, orange for burn, ice blue for freeze.
     * Falls back to the wrapped design's background if unrecognised.
     */
    @Override
    public String getBackground() {
        if (effect instanceof DamageEffect) return "#FFCCCC"; // red   — damage
        if (effect instanceof HealEffect)   return "#CCFFCC"; // green — heal
        if (effect instanceof ShieldEffect) return "#CCE5FF"; // blue  — shield
        if (effect instanceof PoisonEffect) return "#D8FFD8"; // dark green — poison
        if (effect instanceof BurnEffect)   return "#FFE0B2"; // orange — burn
        if (effect instanceof FreezeEffect) return "#E0F4FF"; // ice blue — freeze
        return wrapped.getBackground();
    }

    /** Delegates border color to the wrapped design unchanged. */
    @Override
    public String getBorder() {
        return wrapped.getBorder();
    }
}