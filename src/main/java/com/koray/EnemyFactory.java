package com.koray;

/**
 * Factory class responsible for creating Enemy instances.
 *
 * Four enemy families each progress through four phases and a boss phase.
 * HP and attack scale with level and phase, with an additional multiplier
 * for each family's level-5 boss.
 *
 * All enemy creation must go through this factory — the Enemy class
 * exposes only package-private setters to enforce this constraint.
 */
public class EnemyFactory {

    private static final String[] FAMILIES = {
        "Blood Knight", "Swamp Witch", "Ash Dragon", "Frost Revenant"
    };

    private static final String[] PHASES = {
        "Phase I", "Phase II", "Phase III", "Phase IV", "Boss Phase"
    };

    /**
     * Creates and fully configures an enemy for the given level.
     *
    * Family changes every five levels; the fifth level in each family is its boss.
    * HP and attack increase by level and phase, then receive boss multipliers.
     *
     * @param level the current game level (1-based)
     * @return a ready-to-use Enemy instance
     */
    public static Enemy createEnemy(int level) {

        int family = Math.min((level - 1) / 5, FAMILIES.length - 1);
        int phase = (level - 1) % 5;
        boolean isBoss = phase == 4;
        int baseHp = 40 + level * 12 + phase * 8;
        int baseDamage = 8 + level * 2 + phase * 2;

        Enemy e = new Enemy();
        e.setFamily(family);
        e.setPhase(phase + 1);

        if (isBoss) {
            e.setHp((int)(baseHp * 1.45));
            e.setAttackDamage((int)(baseDamage * 1.3));
            e.setBoss(true);
            e.setName(FAMILIES[family] + " - " + PHASES[phase]);
        } else {
            e.setHp(baseHp);
            e.setAttackDamage(baseDamage);
            e.setBoss(false);
            e.setName(FAMILIES[family] + " - " + PHASES[phase]);
        }

        return e;
    }
}