package com.koray;
 
/**
 * Card effect that freezes the enemy for a number of turns.
 * While frozen, the enemy cannot attack (its turn is skipped).
 * Multiple uses stack — applying freeze again extends the duration.
 */
public class FreezeEffect implements CardEffect {

    /** Number of turns to add to the enemy's freeze counter. */
    private int turns;

    /**
     * @param turns how many additional turns the enemy will be frozen
     */
    public FreezeEffect(int turns) {
        this.turns = turns;
    }

    /**
     * Adds freeze turns to the target enemy.
     * Stackable: if the enemy is already frozen this extends the existing freeze.
     */
    @Override
    public void apply(Player player, Enemy enemy) {
        enemy.addFreeze(turns);
    }
}