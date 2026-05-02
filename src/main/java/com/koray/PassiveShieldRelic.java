package com.koray;
 
/**
 * 🛡️ Iron Skin relic.
 * Grants a fixed amount of shield at the start of every player turn.
 * Shield absorbs incoming damage before HP is affected, making this relic
 * especially effective against high-frequency attacks.
 * Scales with tier: base 4 shield + 3 per tier.
 */
class PassiveShieldRelic extends RelicItem {

    /** Shield amount granted per turn. */
    private int shieldPerTurn;

    /**
     * @param shieldPerTurn shield to add at the start of each player turn
     */
    public PassiveShieldRelic(int shieldPerTurn) {
        super("🛡️ Iron Skin",
              "Her tur başında +" + shieldPerTurn + " Shield",
              50);
        this.shieldPerTurn = shieldPerTurn;
    }

    /**
     * Called at the start of every player turn.
     * Adds shield that absorbs damage before HP is reduced.
     *
     * @param player the active player
     * @param game   the active game state (unused here)
     */
    @Override
    public void applyPassive(Player player, Game game) {
        player.addShield(shieldPerTurn);
    }
}
