package com.koray;

/** Tracks base damage and relic-added damage for one outgoing hit. */
public final class DamageContext {

    private final int baseDamage;
    private int bonusDamage;

    public DamageContext(int baseDamage) {
        if (baseDamage < 0) {
            throw new IllegalArgumentException("Base damage cannot be negative.");
        }
        this.baseDamage = baseDamage;
    }

    public int baseDamage() {
        return baseDamage;
    }

    public int bonusDamage() {
        return bonusDamage;
    }

    public int totalDamage() {
        return baseDamage + bonusDamage;
    }

    public void addBonus(int amount) {
        if (amount > 0) {
            bonusDamage += amount;
        }
    }

    public void applyBonusDamage(Enemy enemy) {
        if (bonusDamage > 0) {
            enemy.takeDamage(bonusDamage);
        }
    }
}
