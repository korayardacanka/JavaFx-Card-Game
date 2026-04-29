package com.koray;
 
/**
 * 🪓 Celladın Baltası
 * Düşmanın HP'si %30'un altındayken verilen hasarı %50 artırır.
 */
class ExecutionerRelic extends RelicItem {
 
    public ExecutionerRelic() {
        super("🪓 Celladın Baltası",
              "Düşman HP < %30 iken +%50 hasar ver",
              70);
    }
 
    @Override
    public void onEnemyDamaged(Player player, Enemy enemy, Game game, int damage) {
        double hpRatio = (double) enemy.getHp() / enemy.getMaxHp();
        if (hpRatio < 0.30 && damage > 0) {
            int bonus = (int)(damage * 0.5);
            enemy.takeDamage(bonus);
            game.lastEvent += "  🪓 +" + bonus + " execute";
        }
    }
}
 