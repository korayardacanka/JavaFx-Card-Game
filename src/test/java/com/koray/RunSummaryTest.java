package com.koray;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RunSummaryTest {

    @Test
    public void gameCreatesRunSummaryFromCurrentProgressAndEveryCardPile() {
        Game game = new Game();
        game.level = 8;
        game.player.spendGold(20);
        game.player.deck.add(CardFactory.make("Deck", 1, 1, 1, new DamageEffect(1)));
        game.player.hand.add(CardFactory.make("Hand", 1, 1, 1, new DamageEffect(1)));
        game.player.discard.add(CardFactory.make("Discard", 1, 1, 1, new DamageEffect(1)));
        game.ownedRelics.add(new RelicItem("Relic A", "", 0) {});
        game.ownedRelics.add(new RelicItem("Relic B", "", 0) {});

        RunSummary summary = game.createRunSummary();

        assertEquals(8, summary.level());
        assertEquals(30, summary.gold());
        assertEquals(java.util.List.of("Relic A", "Relic B"), summary.relics());
        assertEquals(3, summary.deckSize());
    }
}
