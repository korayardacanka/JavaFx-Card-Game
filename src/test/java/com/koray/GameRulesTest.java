package com.koray;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GameRulesTest {

    @Test
    public void tierChangesEveryFiveLevels() {
        assertEquals(0, GameRules.tierForLevel(1));
        assertEquals(0, GameRules.tierForLevel(5));
        assertEquals(1, GameRules.tierForLevel(6));
        assertEquals(1, GameRules.tierForLevel(10));
        assertEquals(2, GameRules.tierForLevel(11));
        assertEquals(GameRules.tierForLevel(6), CardFactory.tierForLevel(6));
    }

    @Test
    public void starterDeckMatchesOriginalThreeCopiesOfEachStarterCard() {
        java.util.List<Card> deck = CardFactory.starterDeck();

        assertEquals(UIConstants.INITIAL_DECK_COPIES * 3, deck.size());
        int damage = 0;
        int shield = 0;
        int heal = 0;
        for (Card card : deck) {
            switch (card.name) {
                case "Damage" -> {
                    damage++;
                    assertEquals(15, ((DamageEffect) card.effect).damage);
                    assertEquals(1, card.cost);
                    assertEquals(10, card.price);
                }
                case "Shield" -> {
                    shield++;
                    assertEquals(10, ((ShieldEffect) card.effect).shield);
                    assertEquals(1, card.cost);
                    assertEquals(10, card.price);
                }
                case "Heal" -> {
                    heal++;
                    assertEquals(10, ((HealEffect) card.effect).heal);
                    assertEquals(2, card.cost);
                    assertEquals(20, card.price);
                }
                default -> throw new AssertionError("Unexpected starter card: " + card.name);
            }
        }
        assertEquals(UIConstants.INITIAL_DECK_COPIES, damage);
        assertEquals(UIConstants.INITIAL_DECK_COPIES, shield);
        assertEquals(UIConstants.INITIAL_DECK_COPIES, heal);
    }
}
