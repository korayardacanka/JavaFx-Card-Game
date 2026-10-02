package com.koray;
 
/**
 * Card effect that restores HP to the player.
 * Healing is capped at the player's maximum HP.
 * Scales with tier when created via CardFactory.makeScaled().
 */
public class HealEffect implements CardEffect {

    /** Amount of HP to restore. */
    int heal;

    /**
     * @param heal the flat HP amount to restore
     */
    public HealEffect(int heal) {
        this.heal = heal;
    }

    @Override
    public String describe() {
        return "Heal " + heal + " HP";
    }

    @Override
    public String icon() { return "💚 "; }

    @Override
    public String color() { return "#CCFFCC"; }

    @Override
    public boolean isDirectDamage() { return false; }

    /**
     * Restores HP to the player (enemy is unaffected).
     */
    @Override
    public void apply(Player player, Enemy enemy) {
        player.heal(heal);
    }
}