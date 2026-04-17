package com.koray;

public class DamageEffect implements CardEffect {

    int damage;

    public DamageEffect(int damage) {
        this.damage = damage;
    }

    public void apply(Player player, Enemy enemy) {
        enemy.hp -= damage;
    }
}
