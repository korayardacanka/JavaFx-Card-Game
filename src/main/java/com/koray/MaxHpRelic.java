package com.koray;
 
/**
 * ❤️ Vital Stone relic.
 * Permanently increases the player's maximum HP on purchase.
 * The player's current HP is also raised by the same amount,
 * effectively acting as both a max-HP upgrade and an instant heal.
 * Scales with tier: base 25 HP + 10 per tier.
 */
class MaxHpRelic extends RelicItem {

    /** The amount by which max HP (and current HP) is increased. */
    private int amount;

    /**
     * @param amount HP increase to apply on purchase
     */
    public MaxHpRelic(int amount) {
        super("❤️ Vital Stone",
              "Max HP +" + amount + " (anında iyileşir)",
              60);
        this.amount = amount;
    }

    /**
     * Called once when the player buys this relic.
     * Raises both max HP and current HP by the configured amount.
     *
     * @param player the active player
     * @param game   the active game state (unused here)
     */
    @Override
    public void applyOnBuy(Player player, Game game) {
        player.increaseMaxHp(amount);
    }
}
 