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
     * Applies this effect to the given targets.
     *
     * @param player the active player (used for heals, shields, etc.)
     * @param enemy  the current enemy (used for damage, status effects, etc.)
     */
    void apply(Player player, Enemy enemy);
}