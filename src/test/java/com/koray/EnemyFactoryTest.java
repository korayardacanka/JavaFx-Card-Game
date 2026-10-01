package com.koray;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EnemyFactoryTest {

    @Test
    public void assignsFourFamiliesAndFivePhasesThroughLevelTwenty() {
        String[] families = {
            "Kanlı Şövalye", "Bataklık Cadısı", "Kül Ejderi", "Buz Revenantı"
        };
        String[] phases = {
            "I. Faz", "II. Faz", "III. Faz", "IV. Faz", "Boss Fazı"
        };

        for (int level = 1; level <= 20; level++) {
            Enemy enemy = EnemyFactory.createEnemy(level);
            int family = (level - 1) / 5;
            int phase = (level - 1) % 5;

            assertEquals(family, enemy.getFamily());
            assertEquals(phase + 1, enemy.getPhase());
            assertEquals(phase == 4, enemy.isBoss());
            assertEquals(families[family] + " - " + phases[phase], enemy.getName());
        }
    }

    @Test
    public void statsIncreaseAcrossEveryFamilyPhase() {
        for (int firstLevel = 1; firstLevel <= 16; firstLevel += 5) {
            Enemy previous = EnemyFactory.createEnemy(firstLevel);
            for (int level = firstLevel + 1; level < firstLevel + 5; level++) {
                Enemy current = EnemyFactory.createEnemy(level);
                assertTrue(current.getMaxHp() > previous.getMaxHp());
                assertTrue(current.getAttackDamage() > previous.getAttackDamage());
                previous = current;
            }
        }
    }
}