package com.koray.relics;

import com.koray.cards.Card;
import com.koray.cards.CardFactory;
import com.koray.cards.DamageEffect;
import com.koray.cards.ShieldEffect;
import com.koray.core.Player;
import com.koray.enemies.Enemy;
import com.koray.enemies.EnemyFactory;
import java.util.List;
import java.util.Random;
import org.junit.Test;
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
        player.addToHand(CardFactory.make("Damage", 1, 10, 1, new DamageEffect(15)));

        List<Card> cards = CardFactory.shopCards(1, player, new Random(5));

        assertTrue(cards.size() <= 4);
        assertFalse(cards.stream().anyMatch(card -> card.getName().equals("Damage")));
    }

    @Test
    public void shopAllowsASecondCopyAndCountsCopiesAcrossAllPiles() {
        Player player = new Player();
        player.addToDeck(CardFactory.make("Damage", 1, 10, 1, new DamageEffect(15)));
        for (String name : List.of("Shield", "Heal", "Poison", "Burn", "Freeze")) {
            Card first = CardFactory.make(name, 1, 10, 1, new ShieldEffect(1));
            Card second = CardFactory.make(name, 1, 10, 1, new ShieldEffect(1));
            player.addToHand(first);
            player.addToDiscard(second);
        }

        List<Card> shopCards = CardFactory.shopCards(1, player, new Random(5));

        assertEquals(1, shopCards.size());
        assertEquals("Damage", shopCards.get(0).getName());

        player.addToDiscard(CardFactory.make("Damage", 1, 10, 1, new DamageEffect(15)));
        assertFalse(CardFactory.shopCards(1, player, new Random(5)).stream()
            .anyMatch(card -> card.getName().equals("Damage")));
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


