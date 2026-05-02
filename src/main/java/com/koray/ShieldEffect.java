package com.koray;
 
/**
 * Card effect that grants shield to the player.
 * Shield absorbs incoming damage before HP is reduced.
 * Scales with tier when created via CardFactory.makeScaled().
 */
public class ShieldEffect implements CardEffect {

    /** Amount of shield to add. */
    int shield;

    /**
     * @param shield the flat shield amount to grant
     */
    public ShieldEffect(int shield) {
        this.shield = shield;
    }

    /**
     * Adds shield to the player (enemy is unaffected).
     */
    @Override
    public void apply(Player player, Enemy enemy) {
        player.addShield(shield);
    }
}
 