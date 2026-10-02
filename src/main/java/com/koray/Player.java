package com.koray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Represents the player character.
 *
 * Manages four resource pools:
 *   - HP     : health points; reaches 0 → game over
 *   - Shield : absorbs incoming damage before HP; 
 *   - Energy : spent when playing cards; restored at turn start
 *   - Gold   : spent in the shop; earned from enemy kills
 *
 * Also owns the three card piles used for deck-building gameplay. Their
 * public views are read-only; mutation goes through purpose-specific methods.
 */
public class Player {

    private int hp     = 100;
    private int shield = 0;
    private int gold   = 50;
    private int energy = 3;
    private int maxhp  = 100;

    private final List<Card> deck    = new ArrayList<>();
    private final List<Card> hand    = new ArrayList<>();
    private final List<Card> discard = new ArrayList<>();

    // ── Getters ───────────────────────────────────────────────────────────
    public int     getHp()     { return hp; }
    public int     getShield() { return shield; }
    public int     getGold()   { return gold; }
    public int     getEnergy() { return energy; }
    public int     getMaxHp()  { return maxhp; }
    public List<Card> getDeck() { return Collections.unmodifiableList(deck); }
    public List<Card> getHand() { return Collections.unmodifiableList(hand); }
    public List<Card> getDiscard() { return Collections.unmodifiableList(discard); }

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
            hp += shield; 
            shield = 0;
        }
    } else {
        hp -= dmg;
    }
    
    hp = Math.max(0, hp);
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

    public void addEnergy(int amount) {
        if (amount > 0) energy += amount;
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

    public void addToDeck(Card card) { deck.add(card); }
    public void addToHand(Card card) { hand.add(card); }
    public void addToDiscard(Card card) { discard.add(card); }
    public void moveHandToDiscard(List<Card> cards) { discard.addAll(cards); }
    public boolean removeFromHand(Card card) { return hand.remove(card); }

    public void moveHandCardToDiscard(Card card) {
        if (hand.remove(card)) discard.add(card);
    }

    public void moveHandToDiscard() {
        discard.addAll(hand);
        hand.clear();
    }

    public void clearCardPiles() {
        deck.clear();
        hand.clear();
        discard.clear();
    }

    public void clearHand() { hand.clear(); }

    public void resetDeck(List<Card> cards) {
        deck.clear();
        deck.addAll(cards);
    }

    public boolean drawCard(Random random) {
        if (deck.isEmpty() && !discard.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.isEmpty()) return false;
        hand.add(deck.remove(0));
        return true;
    }
}