package com.koray;
 
/**
 * Düşmana yanma uygular.
 * Her tur burnStacks kadar hasar verir, sonra 1 azalır.
 * Güçlü ama kısa süreli — Zehirden farklı olarak söner.
 */
public class BurnEffect implements CardEffect {
 
    private int stacks;
 
    public BurnEffect(int stacks) {
        this.stacks = stacks;
    }
 
    @Override
    public void apply(Player player, Enemy enemy) {
        enemy.addBurn(stacks);
    }
}
 