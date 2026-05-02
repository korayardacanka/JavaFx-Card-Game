package com.koray;

/**
 * Factory class responsible for creating Enemy instances.
 *
 * Enemy stats (HP and attack damage) scale linearly with the game level.
 * Every 5th level spawns a boss: 1.5× base HP, 1.5× base attack,
 * and a distinct name/flag so the UI and reward system can distinguish it.
 *
 * All enemy creation must go through this factory — the Enemy class
 * exposes only package-private setters to enforce this constraint.
 */
public class EnemyFactory {

    /**
     * Creates and fully configures an enemy for the given level.
     *
     * Scaling formulas:
     *   base HP     = 40 + (level × 12)
     *   base attack = 8  + (level × 2)
     *   boss multiplier = ×1.5 on both stats
     *
     * @param level the current game level (1-based)
     * @return a ready-to-use Enemy instance
     */
    public static Enemy createEnemy(int level) {

        int baseHp     = 40 + (level * 12);
        int baseDamage = 8  + (level * 2);

        boolean isBoss = (level % 5 == 0);

        Enemy e = new Enemy();

        if (isBoss) {
            e.setHp((int)(baseHp * 1.5));
            e.setAttackDamage((int)(baseDamage * 1.5));
            e.setBoss(true);
            e.setName("BOSS (Lv " + level + ")");
        } else {
            e.setHp(baseHp);
            e.setAttackDamage(baseDamage);
            e.setBoss(false);
            e.setName("Enemy (Lv " + level + ")");
        }

        return e;
    }
}