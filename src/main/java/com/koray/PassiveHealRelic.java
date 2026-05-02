package com.koray;
 
/**
 * 🌿 Regen Amulet relic.
 * Restores a fixed amount of HP at the start of every player turn.
 * Useful for sustaining through long fights without relying on Heal cards.
 * Scales with tier: base 5 HP + 3 per tier.
 */
class PassiveHealRelic extends RelicItem {

    /** HP restored per turn. */
    private int healPerTurn;

    /**
     * @param healPerTurn HP to restore at the start of each player turn
     */
    public PassiveHealRelic(int healPerTurn) {
        super("🌿 Regen Amulet",
              "Her tur başında +" + healPerTurn + " HP",
              50);
        this.healPerTurn = healPerTurn;
    }

    /**
     * Called at the start of every player turn.
     * Heals the player; capped at max HP by Player.heal().
     *
     * @param player the active player
     * @param game   the active game state (unused here)
     */
    @Override
    public void applyPassive(Player player, Game game) {
        player.heal(healPerTurn);
    }
}