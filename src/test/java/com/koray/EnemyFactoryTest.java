package com.koray;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EnemyFactoryTest {

    @Test
    public void assignsFourFamiliesAndFivePhasesThroughLevelTwenty() {
        String[] families = {
            "Blood Knight", "Swamp Witch", "Ash Dragon", "Frost Revenant"
        };
        String[] phases = {
            "Phase I", "Phase II", "Phase III", "Phase IV", "Boss Phase"
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

    @Test
    public void bloodPactRequiresMoreThanThirtyCombinedHpAndShieldAndLogsTheGainCorrectly() {
        Game game = new Game();
        game.maxEnergy = 3;

        BloodPactRelic relic = new BloodPactRelic();

        game.player.takeDamage(20);
        relic.applyOnBuy(game.player, game);
        assertEquals(50, game.player.getHp());
        assertEquals(5, game.maxEnergy);
        assertTrue(game.lastEvent.contains("+2 Max Energy"));

        game.player.takeDamage(20);
        relic.applyOnBuy(game.player, game);
        assertEquals(30, game.player.getHp());
        assertEquals(5, game.maxEnergy);
        assertTrue(game.lastEvent.contains("combined HP and Shield"));

        game.player.heal(5);
        assertEquals(35, game.player.getHp());
        relic.applyOnBuy(game.player, game);
        assertEquals(5, game.player.getHp());
        assertEquals(7, game.maxEnergy);
        assertTrue(game.lastEvent.contains("+2 Max Energy"));
    }

}