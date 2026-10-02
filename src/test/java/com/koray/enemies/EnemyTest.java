package com.koray.enemies;

import com.koray.core.Player;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EnemyTest {

    @Test
    public void poisonPersistsWhileBurnLosesOneStackPerTurn() {
        Enemy enemy = EnemyFactory.createEnemy(1);
        enemy.addPoison(3);
        enemy.addBurn(3);

        enemy.processStatusEffects();
        assertEquals(3, enemy.getPoisonStacks());
        assertEquals(2, enemy.getBurnStacks());

        enemy.processStatusEffects();
        assertEquals(3, enemy.getPoisonStacks());
        assertEquals(1, enemy.getBurnStacks());
    }

    @Test
    public void freezeDecreasesOnceForEachSkippedAttack() {
        Enemy enemy = EnemyFactory.createEnemy(1);
        Player player = new Player();
        int hpBefore = player.getHp();
        enemy.addFreeze(2);

        enemy.attack(player);
        assertEquals(1, enemy.getFreezeTurns());
        assertEquals(hpBefore, player.getHp());

        enemy.attack(player);
        assertEquals(0, enemy.getFreezeTurns());
        assertEquals(hpBefore, player.getHp());

        enemy.attack(player);
        assertTrue(player.getHp() < hpBefore);
    }

    @Test
    public void damageCannotReduceEnemyHpBelowZero() {
        Enemy enemy = EnemyFactory.createEnemy(1);

        enemy.takeDamage(enemy.getMaxHp() + 100);

        assertEquals(0, enemy.getHp());
        assertFalse(enemy.isAlive());
    }
}


