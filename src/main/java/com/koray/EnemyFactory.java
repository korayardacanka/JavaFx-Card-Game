package com.koray;

public class EnemyFactory {

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