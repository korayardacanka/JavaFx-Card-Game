package com.koray;
 
/**
 * 🩸 Blood Pact relic.
 * High-risk, high-reward: costs 30 HP on purchase but permanently
 * increases the player's maximum energy by 2.
 * Priced at 0 gold — the HP loss is the price.
 */
class BloodPactRelic extends RelicItem {

    public BloodPactRelic() {
        super("🩸 Blood Pact",
              "Cost: -30 HP  |  Gain: +2 Max Energy (permanent)",
              0); // free to buy, but costs 30 HP
    }

    @Override
    public boolean canPurchase(Game game) {
        return game.player.getHp() + game.player.getShield() > 30;
    }

    /**
     * Called once when the player buys this relic.
     * Deals 30 damage to the player and raises max energy by 2.
     *
     * @param player the current player
     * @param game   the active game state
     */
    @Override
    public void applyOnBuy(Player player, Game game) {
        if (!canPurchase(game)) {
            game.lastEvent = "❌ You need more than 30 combined HP and Shield to buy Blood Pact.";
            return;
        }

        player.takeDamage(30);
        game.maxEnergy += 2;
        game.lastEvent = "🩸 Blood Pact! -30 HP, +2 Max Energy";
    }
}