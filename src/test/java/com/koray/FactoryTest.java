package com.koray;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FactoryTest {

    @Test
    public void scaleValueTruncatesFractionalResult() {
        assertEquals(16, CardFactory.scaleValue(11, 1));
        assertEquals(19, CardFactory.scaleValue(13, 1));
    }

    @Test
    public void shopCardsExcludeOwnedCardsAndReturnAtMostFour() {
        Player player = new Player();
        player.addToDeck(CardFactory.make("Damage", 1, 10, 1, new DamageEffect(15)));

        List<Card> cards = CardFactory.shopCards(1, player, new Random(5));

        assertTrue(cards.size() <= 4);
        assertFalse(cards.stream().anyMatch(card -> card.name.equals("Damage")));
    }

    @Test
    public void relicFactoryExcludesOwnedRelicsAndReturnsAtMostThree() {
        List<RelicItem> owned = List.of(new MaxHpRelic(25));

        List<RelicItem> relics = RelicFactory.bossRelics(1, owned, new Random(5));

        assertTrue(relics.size() <= 3);
        assertFalse(relics.stream().anyMatch(relic -> relic.name.equals(owned.get(0).name)));
    }

    @Test
    public void enemyFactoryCapsFamilyForLevelsAboveTwentyAndPreservesBossScaling() {
        Enemy levelTwentyOne = EnemyFactory.createEnemy(21);
        assertEquals(3, levelTwentyOne.getFamily());
        assertEquals(1, levelTwentyOne.getPhase());
        assertEquals("Frost Revenant - Phase I", levelTwentyOne.getName());

        Enemy levelTwentyFour = EnemyFactory.createEnemy(24);
        Enemy boss = EnemyFactory.createEnemy(25);
        int bossBaseHp = 40 + 25 * 12 + 4 * 8;
        int bossBaseDamage = 8 + 25 * 2 + 4 * 2;
        assertEquals(3, boss.getFamily());
        assertEquals(5, boss.getPhase());
        assertTrue(boss.isBoss());
        assertEquals((int) (bossBaseHp * 1.45), boss.getMaxHp());
        assertEquals((int) (bossBaseDamage * 1.3), boss.getAttackDamage());
        assertEquals(3, levelTwentyFour.getFamily());
        assertFalse(levelTwentyFour.isBoss());
    }
}
