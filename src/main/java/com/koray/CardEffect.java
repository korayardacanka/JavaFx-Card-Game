package com.koray;

/**
 * Strategy interface for card effects.
 * Each concrete implementation defines one specific in-game action
 * (damage, heal, shield, poison, burn, freeze, etc.).
 * Cards hold a reference to a CardEffect and delegate to it when played,
 * so new effects can be added without modifying the Card class.
 */
public interface CardEffect {
    /**
     * Returns a short description of this effect for display on the card.
     */
    String describe();

    /** Returns the icon prefix used to identify this effect on a card. */
    default String icon() { return ""; }

    /** Returns the background color used for cards with this effect. */
    default String color() { return ""; }

    /** Returns whether this effect directly damages the enemy. */
    default boolean isDirectDamage() { return false; }

    /**
     * Applies this effect to the given targets.
     *
     * @param player the active player (used for heals, shields, etc.)
     * @param enemy  the current enemy (used for damage, status effects, etc.)
     */
    void apply(Player player, Enemy enemy);
}