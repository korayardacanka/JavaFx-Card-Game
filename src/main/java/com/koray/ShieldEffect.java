package com.koray;
 
public class ShieldEffect implements CardEffect {
 
    int shield;
 
    public ShieldEffect(int shield) {
        this.shield = shield;
    }
 
    public void apply(Player player, Enemy enemy) {
        player.addShield(shield);
    }
}
 