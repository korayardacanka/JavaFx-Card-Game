package com.koray;

public class Enemy {
    int hp = 50;
    int attackDamage = 10;
    boolean isBoss = false;
    String name = "Enemy";
 
    public void attack(Player player) {
        dealDamage(player, attackDamage);
    }
 
    // Boss ve alt sınıflar da kullanabilsin diye yardımcı metod
    protected void dealDamage(Player player, int dmg) {
        if (player.shield > 0) {
            player.shield -= dmg;
            if (player.shield < 0) {
                player.hp += player.shield;
                player.shield = 0;
            }
        } else {
            player.hp -= dmg;
        }
    }
}
