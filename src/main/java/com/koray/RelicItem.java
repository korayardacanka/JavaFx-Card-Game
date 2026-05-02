package com.koray;
 
/**
 * Abstract base class for all relic items.
 * Relics are persistent upgrades the player acquires in the shop
 * (regular) or after defeating a boss (boss relics).
 *
 * Each relic can hook into four distinct game moments via override:
 *   applyOnBuy()      — one-time effect when purchased
 *   applyPassive()    — passive effect called at the start of each turn
 *   onDamageTaken()   — triggered when the player takes damage
 *   onEnemyDamaged()  — triggered when the player deals damage to an enemy
 *
 * Concrete subclasses override only the hooks they need; unused hooks
 * default to no-ops so no boilerplate is required.
 */
public abstract class RelicItem {

    /** Display name shown in the shop and relic list. */
    public String name;

    /** Short description of the relic's effect, shown in the shop. */
    public String description;

    /** Gold cost to purchase this relic in the shop. */
    public int price;

    /**
     * @param name        display name
     * @param description short effect description
     * @param price       gold cost (0 = free, but may have an HP cost)
     */
    public RelicItem(String name, String description, int price) {
        this.name        = name;
        this.description = description;
        this.price       = price;
    }

    // ── Lifecycle hooks (override as needed) ──────────────────────────────

    /**
     * Called once, immediately after the player buys this relic.
     * Use for one-time stat changes (max HP, max energy, etc.).
     */
    public void applyOnBuy(Player player, Game game) {}

    /**
     * Called at the start of every player turn.
     * Use for passive effects (HP regen, shield gain, gold income, etc.).
     */
    public void applyPassive(Player player, Game game) {}

    /**
     * Called whenever the player takes damage (after shield reduction).
     * Use for reactive effects like damage reflection (ThornRelic).
     *
     * @param damage the final damage amount the player received
     */
    public void onDamageTaken(Player player, Enemy enemy, Game game, int damage) {}

    /**
     * Called whenever the player deals damage to the enemy.
     * Use for effects that scale with outgoing damage (VampireRelic, ExecutionerRelic).
     *
     * @param damage the damage amount just dealt to the enemy
     */
    public void onEnemyDamaged(Player player, Enemy enemy, Game game, int damage) {}
}