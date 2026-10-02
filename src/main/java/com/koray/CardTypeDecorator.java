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
        String color = effect.color();
        return color.isEmpty() ? wrapped.getBackground() : color;
    }

    /** Delegates border color to the wrapped design unchanged. */
    @Override
    public String getBorder() {
        return wrapped.getBorder();
    }
}