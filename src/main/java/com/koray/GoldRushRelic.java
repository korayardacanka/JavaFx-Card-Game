package com.koray;
 
/**
 * 💰 Gold Pouch relic.
 * Grants a fixed amount of gold at the start of every turn.
 * Scales with tier: base 3 gold + 1 per tier at the time of purchase.
 */
class GoldRushRelic extends RelicItem {

    /** Gold amount awarded each turn. */
    private int goldPerTurn;

    /**
     * @param goldPerTurn gold to award at the start of each player turn
     */
    public GoldRushRelic(int goldPerTurn) {
        super("💰 Altın Kese",
              "Her tur başında +" + goldPerTurn + " gold",
              45);
        this.goldPerTurn = goldPerTurn;
    }

    /**
     * Called at the start of every player turn.
     * Adds the configured gold amount to the player's wallet.
     *
     * @param player the active player
     * @param game   the active game state
     */
    @Override
    public void applyPassive(Player player, Game game) {
        player.addGold(goldPerTurn);
    }
}