package com.koray;
 
/**
 * Card effect that applies poison stacks to the enemy.
 * Each turn the enemy takes damage equal to its total poison stacks.
 * Unlike burn, poison stacks do not decay — they accumulate indefinitely
 * until a cleanse mechanic is introduced.
 */
public class PoisonEffect implements CardEffect {

    /** Number of poison stacks to add. */
    private int stacks;

    /**
     * @param stacks number of poison stacks to apply
     */
    public PoisonEffect(int stacks) {
        this.stacks = stacks;
    }

    @Override
    public String describe() {
        return "Apply " + stacks + " poison";
    }

    @Override
    public String icon() { return "☠ "; }

    @Override
    public String color() { return "#D8FFD8"; }

    @Override
    public boolean isDirectDamage() { return false; }

    /**
     * Adds poison stacks to the target enemy.
     * Stackable — multiple uses increase the per-turn damage.
     */
    @Override
    public void apply(Player player, Enemy enemy) {
        enemy.addPoison(stacks);
    }
}