package com.koray;
 
class MaxHpRelic extends RelicItem {
    private int amount;
 
    public MaxHpRelic(int amount) {
        super("❤️ Vital Stone",
              "Max HP +" + amount + " (anında iyileşir)",
              60);
        this.amount = amount;
    }
 
    @Override
    public void applyOnBuy(Player player, Game game) {
        player.increaseMaxHp(amount);
    }
}
 