package com.koray;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GameTest {

    @Test
    public void collectionGettersAreReadOnly() {
        Game game = new Game();
        Card card = CardFactory.make("Test", 1, 1, 1, new DamageEffect(1));
        RelicItem relic = new RelicItem("Test Relic", "", 1) {};
        game.setCurrentShopCards(java.util.List.of(card));
        game.addCurrentBossRelic(relic);
        game.addOwnedRelic(relic);

        assertUnmodifiable(() -> game.getCurrentShopCards().clear());
        assertUnmodifiable(() -> game.getCurrentBossRelics().clear());
        assertUnmodifiable(() -> game.getOwnedRelics().clear());
        assertTrue(game.getCurrentShopCards().contains(card));
        assertTrue(game.getCurrentBossRelics().contains(relic));
        assertTrue(game.getOwnedRelics().contains(relic));
    }

    private static void assertUnmodifiable(Runnable mutation) {
        try {
            mutation.run();
            fail("Game collection views should be read-only.");
        } catch (UnsupportedOperationException expected) {
            // Expected for collection views returned by Game.
        }
    }
}
