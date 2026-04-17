package com.koray;
public class EnemyFactory {

    public static Enemy createEnemy(int level) {
    	 
        int baseHp     = 40 + (level * 12);
        int baseDamage = 8  + (level * 2);
 
        boolean isBoss = (level % 5 == 0);
 
        if (isBoss) {
            int bossHp     = (int)(baseHp * 1.5);
            int bossDamage = (int)(baseDamage * 1.5);
 
            Enemy boss = new Enemy();
            boss.hp           = bossHp;
            boss.attackDamage = bossDamage;
            boss.isBoss       = true;
            boss.name         = "BOSS (Lv " + level + ")";
            return boss;
        }
 
        // Normal düşman
        Enemy e = new Enemy();
        e.hp           = baseHp;
        e.attackDamage = baseDamage;
        e.isBoss       = false;
        e.name         = "Enemy (Lv " + level + ")";
        return e;
    }
}
