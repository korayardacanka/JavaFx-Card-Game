package com.koray;
 
/**
 * Düşmanı dondurur: freezeTurns kadar saldıramaz.
 * Stack'lenebilir — birden fazla oynayınca süre uzar.
 */
public class FreezeEffect implements CardEffect {
 
    private int turns;
 
    public FreezeEffect(int turns) {
        this.turns = turns;
    }
 
    @Override
    public void apply(Player player, Enemy enemy) {
        enemy.addFreeze(turns);
    }
}
 