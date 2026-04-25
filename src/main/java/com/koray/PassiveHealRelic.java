package com.koray;
 
class PassiveHealRelic extends RelicItem {
    private int healPerTurn;
 
    public PassiveHealRelic(int healPerTurn) {
        super("🌿 Regen Amulet",
              "Her tur başında +" + healPerTurn + " HP",
              50);
        this.healPerTurn = healPerTurn;
    }
 
    @Override
    public void applyPassive(Player player, Game game) {
        player.heal(healPerTurn);
    }
}