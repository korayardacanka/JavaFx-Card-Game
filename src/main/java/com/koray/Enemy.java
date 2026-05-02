package com.koray;
 
/**
 * Represents an enemy combatant in a battle.
 *
 * Each enemy has HP, an attack value, and three stackable status effects:
 *   - Poison  : deals fixed damage each turn, does not decay
 *   - Burn    : deals damage each turn, decreases by 1 per turn
 *   - Freeze  : prevents the enemy from attacking for N turns
 *
 * Enemies are created and configured exclusively through EnemyFactory.
 * The package-private setters enforce this — external code cannot modify
 * core stats directly.
 */
public class Enemy {

    private int     hp           = 50;
    private int     maxHp        = 50;
    private int     attackDamage = 10;
    private boolean boss         = false;
    private String  name         = "Enemy";

    // ── Status effect counters ─────────────────────────────────────────────
    private int poisonStacks = 0; // damage per turn; accumulates (does not decay)
    private int burnStacks   = 0; // damage per turn; decreases by 1 each turn
    private int freezeTurns  = 0; // turns remaining where the enemy cannot attack

    // ── Getters ───────────────────────────────────────────────────────────
    public int     getHp()           { return hp; }
    public int     getMaxHp()        { return maxHp; }
    public int     getAttackDamage() { return attackDamage; }
    public boolean isBoss()          { return boss; }
    public String  getName()         { return name; }
    public boolean isAlive()         { return hp > 0; }
    public int     getPoisonStacks() { return poisonStacks; }
    public int     getBurnStacks()   { return burnStacks; }
    public int     getFreezeTurns()  { return freezeTurns; }

    /** Returns true if the enemy is currently unable to attack. */
    public boolean isFrozen()        { return freezeTurns > 0; }

    // ── Package-private setters (EnemyFactory only) ───────────────────────
    void setHp(int hp)            { this.hp = hp; this.maxHp = hp; }
    void setAttackDamage(int dmg) { this.attackDamage = dmg; }
    void setBoss(boolean boss)    { this.boss = boss; }
    void setName(String name)     { this.name = name; }

    // ── Status effect application ─────────────────────────────────────────

    /** Adds poison stacks. Stacks accumulate and do not decay. */
    public void addPoison(int stacks) { poisonStacks += stacks; }

    /** Adds burn stacks. Stacks accumulate but decrease by 1 each turn. */
    public void addBurn(int stacks)   { burnStacks   += stacks; }

    /** Adds freeze turns. Stacks accumulate — multiple freezes extend the duration. */
    public void addFreeze(int turns)  { freezeTurns  += turns; }

    /**
     * Processes all active status effects at the start of a turn.
     * Applies damage from poison and burn, ticks down burn stacks,
     * and logs the freeze status.
     *
     * @return a human-readable log string for the UI (may be empty)
     */
    public String processStatusEffects() {
        StringBuilder log = new StringBuilder();

        if (poisonStacks > 0) {
            takeDamage(poisonStacks);
            log.append("☠ Zehir: -").append(poisonStacks).append(" HP  ");
            // Poison does not decay — a separate mechanic is needed to cleanse it
        }

        if (burnStacks > 0) {
            takeDamage(burnStacks);
            log.append("🔥 Yanma: -").append(burnStacks).append(" HP  ");
            burnStacks = Math.max(0, burnStacks - 1); // burn decreases by 1 each turn
        }

        if (freezeTurns > 0) {
            log.append("❄ Donuk (").append(freezeTurns).append(" tur)  ");
        }

        return log.toString().trim();
    }

    // ── Combat ────────────────────────────────────────────────────────────

    /**
     * Reduces the enemy's HP by the given amount.
     * Negative or zero values are ignored.
     *
     * @param dmg damage to deal
     */
    public void takeDamage(int dmg) {
        if (dmg > 0) hp -= dmg;
    }

    /**
     * Performs the enemy's attack against the player.
     * If the enemy is frozen, the freeze counter is decremented
     * and the attack is skipped entirely.
     *
     * @param player the player to attack
     */
    public void attack(Player player) {
        if (isFrozen()) {
            freezeTurns--;
            return;
        }
        player.takeDamage(attackDamage);
    }
}