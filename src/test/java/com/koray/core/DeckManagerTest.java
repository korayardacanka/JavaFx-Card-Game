package com.koray.core;

import com.koray.cards.Card;
import com.koray.cards.CardFactory;
import com.koray.cards.DamageEffect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeckManagerTest {

    @Test
    public void recyclesAndShufflesDiscardWhenDeckRunsOut() {
        Game game = new Game();
        DeckManager deckManager = new DeckManager(game, new Random(7));
        Card discardOne = testCard("Discard one");
        Card discardTwo = testCard("Discard two");
        Card discardThree = testCard("Discard three");
        List<Card> discardedCards = List.of(discardOne, discardTwo, discardThree);
        game.getPlayer().addToDiscard(discardOne);
        game.getPlayer().addToDiscard(discardTwo);
        game.getPlayer().addToDiscard(discardThree);

        deckManager.drawSingleCard();
        deckManager.drawSingleCard();
        deckManager.drawSingleCard();

        assertEquals(0, game.getPlayer().getDeck().size());
        assertEquals(0, game.getPlayer().getDiscard().size());
        assertEquals(3, game.getPlayer().getHand().size());
        List<Card> expectedOrder = new ArrayList<>(discardedCards);
        Collections.shuffle(expectedOrder, new Random(7));
        assertEquals(expectedOrder, game.getPlayer().getHand());
    }

    @Test
    public void drawingWithEmptyDeckAndDiscardDoesNotThrow() {
        Game game = new Game();
        DeckManager deckManager = new DeckManager(game, new Random(7));

        deckManager.drawSingleCard();

        assertTrue(game.getPlayer().getHand().isEmpty());
    }

    private static Card testCard(String name) {
        return CardFactory.make(name, 1, 1, 1, new DamageEffect(1));
    }
}


