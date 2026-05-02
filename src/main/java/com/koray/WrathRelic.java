package com.koray;
 
/**
 * 😡 Wrath Stone relic.
 * Grants bonus energy each turn when the player's HP falls below 50%.
 * Rewards high-risk play — the lower the HP, the more powerful the relic
 * (via extra energy to play more cards per turn).
 */
class WrathRelic extends RelicItem {

    /** Additional energy granted each turn when HP is below the threshold. */
    private int bonusEnergy;

    /**
     * @param bonusEnergy extra energy to add per turn when HP < 50%
     */
    public WrathRelic(int bonusEnergy) {
        super("😡 Öfke Taşı",
              "HP < %50 iken her tur +" + bonusEnergy + " enerji",
              50);
        this.bonusEnergy = bonusEnergy;
    }

    /**
     * Called at the start of every player turn.
     * If the player's current HP is below half their max HP,
     * raises the current energy by bonusEnergy.
     *
     * Note: uses restoreEnergy(current + bonus) because Player has
     * no addEnergy() method. Consider adding one for clarity.
     *
     * @param player the active player
     * @param game   the active game state (unused here)
     */
    @Override
    public void applyPassive(Player player, Game game) {
        if (player.getHp() < player.getMaxHp() / 2) {
            player.restoreEnergy(player.getEnergy() + bonusEnergy);
        }
    }
}