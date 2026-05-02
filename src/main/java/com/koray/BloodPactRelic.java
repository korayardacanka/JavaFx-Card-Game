package com.koray;
 
/**
 * 🩸 Blood Pact relic.
 * High-risk, high-reward: costs 30 HP on purchase but permanently
 * increases the player's maximum energy by 2.
 * Priced at 0 gold — the HP loss is the price.
 */
class BloodPactRelic extends RelicItem {

    public BloodPactRelic() {
        super("🩸 Kan Antlaşması",
              "Al: -30 HP  |  Kazanç: Max Enerji +2 (kalıcı)",
              0); // free to buy, but costs 30 HP
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
        player.takeDamage(30);
        game.maxEnergy += 2;
        game.lastEvent = "🩸 Kan Antlaşması! -30 HP, Max Enerji +" + game.maxEnergy;
    }
}