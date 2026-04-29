package com.koray;
 
/**
 * 💰 Altın Kese
 * Her tur başında sabit miktarda gold kazanır.
 */
class GoldRushRelic extends RelicItem {
    private int goldPerTurn;
 
    public GoldRushRelic(int goldPerTurn) {
        super("💰 Altın Kese",
              "Her tur başında +" + goldPerTurn + " gold",
              45);
        this.goldPerTurn = goldPerTurn;
    }
 
    @Override
    public void applyPassive(Player player, Game game) {
        player.addGold(goldPerTurn);
    }
}