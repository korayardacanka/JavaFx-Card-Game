package com.koray;
 
/**
 * 🧛 Vampir Dişi
 * Düşmana verilen hasarın %20'si kadar HP kazanır.
 */
class VampireRelic extends RelicItem {
    private double ratio;
 
    public VampireRelic(double ratio) {
        super("🧛 Vampir Dişi",
              "Verilen hasarın %" + (int)(ratio * 100) + "'i kadar HP kazan",
              65);
        this.ratio = ratio;
    }
 
    @Override
    public void onEnemyDamaged(Player player, Enemy enemy, Game game, int damage) {
        int heal = (int)(damage * ratio);
        if (heal > 0) {
            player.heal(heal);
            game.lastEvent += "  🧛 +" + heal + " HP";
        }
    }
}
 
