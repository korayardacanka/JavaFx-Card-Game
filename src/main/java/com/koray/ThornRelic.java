package com.koray;
 
/**
 * 🌵 Thorn Armor relic.
 * Reflects a fixed amount of damage back to the enemy whenever
 * the player is hit. The reflected damage is independent of the
 * incoming damage — it is always a flat value.
 * Scales with tier: base 4 + 2 per tier.
 */
class ThornRelic extends RelicItem {

    /** Flat damage reflected to the enemy on each hit. */
    private int thornDamage;

    /**
     * @param thornDamage damage to deal back to the enemy when the player is hit
     */
    public ThornRelic(int thornDamage) {
        super("🌵 Dikenli Zırh",
              "Hasar alınca düşmana +" + thornDamage + " hasar yansıt",
              55);
        this.thornDamage = thornDamage;
    }

    /**
     * Called whenever the player takes damage.
     * Deals the configured thorn damage directly to the enemy.
     * The reflected damage can kill the enemy (checked in handleEndTurn).
     *
     * @param damage the damage the player just received (must be > 0 to trigger)
     */
    @Override
    public void onDamageTaken(Player player, Enemy enemy, Game game, int damage) {
        if (damage > 0) {
            enemy.takeDamage(thornDamage);
            game.lastEvent += "  🌵 +" + thornDamage + " yansıma";
        }
    }
}