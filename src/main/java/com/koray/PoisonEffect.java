package com.koray;
 
/**
 * Düşmana zehir yığını ekler.
 * Her tur tur başında poisonStacks kadar hasar verir.
 * Yığınlar birikerek artar (stack).
 */
public class PoisonEffect implements CardEffect {
 
    private int stacks;
 
    public PoisonEffect(int stacks) {
        this.stacks = stacks;
    }
 
    @Override
    public void apply(Player player, Enemy enemy) {
        enemy.addPoison(stacks);
    }
}
 