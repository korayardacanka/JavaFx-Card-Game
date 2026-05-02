package com.koray;
 
/**
 * 🧛 Vampire Fang relic.
 * Restores HP equal to a percentage of every damage instance
 * the player deals to an enemy (lifesteal).
 * Default ratio is 20% — dealing 50 damage heals 10 HP.
 * Healing is capped at max HP by Player.heal().
 */
class VampireRelic extends RelicItem {

    /** Lifesteal ratio (0.20 = 20%). */
    private double ratio;

    /**
     * @param ratio fraction of damage dealt to convert to healing (e.g. 0.20)
     */
    public VampireRelic(double ratio) {
        super("🧛 Vampir Dişi",
              "Verilen hasarın %" + (int)(ratio * 100) + "'i kadar HP kazan",
              65);
        this.ratio = ratio;
    }

    /**
     * Called every time the player deals damage to the enemy.
     * Converts the given damage amount to HP using the lifesteal ratio.
     * Fractional values are truncated — minimum effective steal is 1 HP.
     *
     * @param damage the damage amount just dealt (must be > 0 to trigger)
     */
    @Override
    public void onEnemyDamaged(Player player, Enemy enemy, Game game, int damage) {
        int heal = (int)(damage * ratio);
        if (heal > 0) {
            player.heal(heal);
            game.lastEvent += "  🧛 +" + heal + " HP";
        }
    }
}