package com.koray;

import java.util.*;

/**
 * Represents the player character.
 *
 * Manages four resource pools:
 *   - HP     : health points; reaches 0 → game over
 *   - Shield : absorbs incoming damage before HP; resets each turn
 *   - Energy : spent when playing cards; restored at turn start
 *   - Gold   : spent in the shop; earned from enemy kills
 *
 * Also owns the three card piles (deck, hand, discard) used for
 * deck-building gameplay. These are package-private so CardFactory
 * and Main can manage them directly.
 */
public class Player {

    private int hp     = 100;
    private int shield = 0;
    private int gold   = 50;
    private int energy = 3;
    private int maxhp  = 100;

    // Card piles — accessible within the package (CardFactory, Shop, Main)
    List<Card> deck    = new ArrayList<>();
    List<Card> hand    = new ArrayList<>();
    List<Card> discard = new ArrayList<>();

    // ── Getters ───────────────────────────────────────────────────────────
    public int     getHp()     { return hp; }
    public int     getShield() { return shield; }
    public int     getGold()   { return gold; }
    public int     getEnergy() { return energy; }
    public int     getMaxHp()  { return maxhp; }

    /** Returns true while the player has at least 1 HP. */
    public boolean isAlive()   { return hp > 0; }

    // ── HP ────────────────────────────────────────────────────────────────

    /**
     * Applies incoming damage, consuming shield first.
     * Any damage that exceeds the shield bleeds through to HP.
     * Negative or zero values are ignored.
     *
     * @param dmg damage to deal
     */
    public void takeDamage(int dmg) {
        if (dmg <= 0) return;
        if (shield > 0) {
            shield -= dmg;
            if (shield < 0) {
                hp += shield; // shield went negative → subtract the overflow from HP
                shield = 0;
            }
        } else {
            hp -= dmg;
        }
    }

    /**
     * Restores HP, capped at max HP.
     * Negative or zero values are ignored.
     *
     * @param amount HP to restore
     */
    public void heal(int amount) {
        if (amount <= 0) return;
        hp = Math.min(hp + amount, maxhp);
    }

    /**
     * Permanently increases max HP and heals the player by the same amount.
     *
     * @param amount the HP increase (applied to both maxhp and current hp)
     */
    public void increaseMaxHp(int amount) {
        maxhp += amount;
        hp = Math.min(hp + amount, maxhp);
    }

    // ── Shield ────────────────────────────────────────────────────────────

    /**
     * Adds shield. Shield absorbs damage before HP is affected.
     * Negative or zero values are ignored.
     *
     * @param amount shield to add
     */
    public void addShield(int amount) {
        if (amount > 0) shield += amount;
    }

    /** Removes all shield. Typically called at the start of each turn. */
    public void resetShield() {
        shield = 0;
    }

    // ── Energy ────────────────────────────────────────────────────────────

    /**
     * Attempts to spend the given amount of energy.
     *
     * @param cost energy to spend
     * @return true if spending succeeded (energy was deducted),
     *         false if insufficient energy (no change)
     */
    public boolean spendEnergy(int cost) {
        if (energy < cost) return false;
        energy -= cost;
        return true;
    }

    /**
     * Sets the player's energy to the given value.
     * Used at the start of each turn to restore energy to maxEnergy.
     *
     * @param amount the new energy value
     */
    public void restoreEnergy(int amount) {
        energy = amount;
    }

    // ── Gold ──────────────────────────────────────────────────────────────

    /**
     * Adds gold to the player's wallet.
     * Negative or zero values are ignored.
     *
     * @param amount gold to add
     */
    public void addGold(int amount) {
        if (amount > 0) gold += amount;
    }

    /**
     * Attempts to spend the given amount of gold.
     *
     * @param amount gold to spend
     * @return true if spending succeeded (gold was deducted),
     *         false if insufficient gold (no change)
     */
    public boolean spendGold(int amount) {
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }
}