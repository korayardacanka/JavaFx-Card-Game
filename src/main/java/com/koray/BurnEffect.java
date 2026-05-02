package com.koray;
 
/**
 * Card effect that applies burn stacks to the enemy.
 * Each turn the enemy takes damage equal to its current burn stacks,
 * then the stacks decrease by 1 (burns out over time).
 * Unlike poison, burn eventually extinguishes on its own.
 */
public class BurnEffect implements CardEffect {

    /** Number of burn stacks to add. */
    private int stacks;

    /**
     * @param stacks number of burn stacks to apply
     */
    public BurnEffect(int stacks) {
        this.stacks = stacks;
    }

    /**
     * Adds burn stacks to the target enemy.
     * Multiple uses stack — the enemy keeps accumulating burn.
     */
    @Override
    public void apply(Player player, Enemy enemy) {
        enemy.addBurn(stacks);
    }
}