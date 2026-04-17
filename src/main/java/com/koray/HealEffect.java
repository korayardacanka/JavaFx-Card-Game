package com.koray;

public class HealEffect implements CardEffect {

    int heal;

    public HealEffect(int heal) {
        this.heal = heal;
    }

    public void apply(Player player, Enemy enemy) {

        player.hp += heal;

        if (player.hp > player.maxhp) {
            player.hp = player.maxhp;
        }
    }
}
