package com.koray.cards;

import com.koray.core.Player;
import com.koray.enemies.Enemy;

/**
 * Card effect that deals direct damage to the enemy.
 * The damage value is fixed at construction time and scales
 * with tier when created via CardFactory.makeScaled().
 */
public class DamageEffect implements CardEffect {

    /** Amount of damage dealt to the enemy when this effect is applied. */
    int damage;

    /**
     * @param damage the flat damage amount to deal
     */
    public DamageEffect(int damage) {
        this.damage = damage;
    }

    public int getDamage() { return damage; }

    @Override
    public String describe() {
        return "Deal " + damage + " damage";
    }

    @Override
    public String icon() { return "⚔ "; }

    @Override
    public String color() { return "#FFCCCC"; }

    @Override
    public boolean isDirectDamage() { return true; }

    /**
     * Deals damage directly to the enemy's HP (ignoring player).
     */
    @Override
    public void apply(Player player, Enemy enemy) {
        enemy.takeDamage(damage);
    }
}

