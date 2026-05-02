package com.koray;
 
/**
 * 🪓 Executioner's Axe relic.
 * Deals 50% bonus damage whenever the enemy's HP drops below 30%.
 * Encourages aggressive finishing moves — the bonus only activates
 * when the enemy is already near death.
 */
class ExecutionerRelic extends RelicItem {

    public ExecutionerRelic() {
        super("🪓 Celladın Baltası",
              "Düşman HP < %30 iken +%50 hasar ver",
              70);
    }

    /**
     * Called every time the player deals damage to the enemy.
     * If the enemy's remaining HP ratio is below 30%, applies
     * an additional 50% of the dealt damage as a bonus hit.
     *
     * @param player the active player
     * @param enemy  the target enemy
     * @param game   the active game state (used for event logging)
     * @param damage the damage amount just dealt
     */
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
 