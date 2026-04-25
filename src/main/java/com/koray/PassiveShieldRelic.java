package com.koray;
 
class PassiveShieldRelic extends RelicItem {
    private int shieldPerTurn;
 
    public PassiveShieldRelic(int shieldPerTurn) {
        super("🛡️ Iron Skin",
              "Her tur başında +" + shieldPerTurn + " Shield",
              50);
        this.shieldPerTurn = shieldPerTurn;
    }
 
    @Override
    public void applyPassive(Player player, Game game) {
        player.addShield(shieldPerTurn);
    }
}
 
