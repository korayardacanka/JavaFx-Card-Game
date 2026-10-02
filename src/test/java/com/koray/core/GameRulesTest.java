package com.koray.core;

import com.koray.cards.Card;
import com.koray.cards.CardFactory;
import com.koray.cards.DamageEffect;
import com.koray.cards.HealEffect;
import com.koray.cards.ShieldEffect;
import com.koray.ui.UIConstants;
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
            switch (card.getName()) {
                case "Damage" -> {
                    damage++;
                    assertEquals(15, ((DamageEffect) card.getEffect()).getDamage());
                    assertEquals(1, card.getCost());
                    assertEquals(10, card.getPrice());
                }
                case "Shield" -> {
                    shield++;
                    assertEquals(10, ((ShieldEffect) card.getEffect()).getShield());
                    assertEquals(1, card.getCost());
                    assertEquals(10, card.getPrice());
                }
                case "Heal" -> {
                    heal++;
                    assertEquals(10, ((HealEffect) card.getEffect()).getHeal());
                    assertEquals(2, card.getCost());
                    assertEquals(20, card.getPrice());
                }
                default -> throw new AssertionError("Unexpected starter card: " + card.getName());
            }
        }
        assertEquals(UIConstants.INITIAL_DECK_COPIES, damage);
        assertEquals(UIConstants.INITIAL_DECK_COPIES, shield);
        assertEquals(UIConstants.INITIAL_DECK_COPIES, heal);
    }
}


