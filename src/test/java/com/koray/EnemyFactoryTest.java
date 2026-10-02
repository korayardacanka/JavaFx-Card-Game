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
        BloodPactRelic relic = new BloodPactRelic();

        game.getPlayer().takeDamage(20);
        relic.applyOnBuy(game.getPlayer(), game);
        assertEquals(50, game.getPlayer().getHp());
        assertEquals(5, game.getMaxEnergy());
        assertTrue(game.getEventLog().toDisplayString().contains("+2 Max Energy"));

        game.getPlayer().takeDamage(20);
        relic.applyOnBuy(game.getPlayer(), game);
        assertEquals(30, game.getPlayer().getHp());
        assertEquals(5, game.getMaxEnergy());
        assertTrue(game.getEventLog().toDisplayString().contains("combined HP and Shield"));

        game.getPlayer().heal(5);
        assertEquals(35, game.getPlayer().getHp());
        relic.applyOnBuy(game.getPlayer(), game);
        assertEquals(5, game.getPlayer().getHp());
        assertEquals(7, game.getMaxEnergy());
        assertTrue(game.getEventLog().toDisplayString().contains("+2 Max Energy"));
    }

}