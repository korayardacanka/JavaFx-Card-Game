package com.koray.core;

import com.koray.cards.CardFactory;
import com.koray.cards.DamageEffect;
import com.koray.relics.RelicItem;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class RunSummaryTest {

    @Test
    public void gameCreatesRunSummaryFromCurrentProgressAndEveryCardPile() {
        Game game = new Game();
        for (int i = 1; i < 8; i++) {
            game.advanceLevel();
        }
        game.getPlayer().spendGold(20);
        game.getPlayer().addToDeck(CardFactory.make("Deck", 1, 1, 1, new DamageEffect(1)));
        game.getPlayer().addToHand(CardFactory.make("Hand", 1, 1, 1, new DamageEffect(1)));
        game.getPlayer().addToDiscard(CardFactory.make("Discard", 1, 1, 1, new DamageEffect(1)));
        game.addOwnedRelic(new RelicItem("Relic A", "", 0) {});
        game.addOwnedRelic(new RelicItem("Relic B", "", 0) {});

        RunSummary summary = game.createRunSummary();

        assertEquals(8, summary.level());
        assertEquals(30, summary.gold());
        assertEquals(java.util.List.of("Relic A", "Relic B"), summary.relics());
        assertEquals(3, summary.deckSize());
    }
}


