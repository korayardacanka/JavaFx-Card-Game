package com.koray;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ShopTest {

    @Test
    public void warnsOnlyWhenAnUnownedBossRelicIsPurchasable() {
        Game game = new Game();
        RelicItem relic = new RelicItem("Test Relic", "", 60) {};
        game.currentBossRelics.add(relic);

        assertFalse(Shop.hasPurchasableBossRelic(game));

        game.player.addGold(10);
        assertTrue(Shop.hasPurchasableBossRelic(game));

        game.ownedRelics.add(new RelicItem("Test Relic", "", 60) {});
        assertFalse(Shop.hasPurchasableBossRelic(game));
    }

    @Test
    public void bloodPactMustMeetItsHealthRequirementToBePurchasable() {
        Game game = new Game();
        game.currentBossRelics.add(new BloodPactRelic());

        game.player.takeDamage(70);
        assertFalse(Shop.hasPurchasableBossRelic(game));

        game.player.heal(1);
        assertTrue(Shop.hasPurchasableBossRelic(game));
    }

    @Test
    public void handSizeUpgradeCostsIncreaseAndRaiseThePersistentHandLimit() {
        Game game = new Game();
        assertEquals(4, game.getHandSizeLimit());
        assertEquals(100, game.getNextHandSizeUpgradeCost());

        game.player.addGold(50);
        assertTrue(game.purchaseHandSizeUpgrade());
        assertEquals(0, game.player.getGold());
        assertEquals(5, game.getHandSizeLimit());
        assertEquals(200, game.getNextHandSizeUpgradeCost());
    }

    @Test
    public void handSizeUpgradeCannotBePurchasedWithoutGoldOrBeyondTheMaximum() {
        Game game = new Game();
        assertFalse(game.purchaseHandSizeUpgrade());
        assertFalse(Shop.canPurchaseHandSizeUpgrade(game));
        assertEquals(4, game.getHandSizeLimit());

        game.player.addGold(49);
        assertFalse(Shop.canPurchaseHandSizeUpgrade(game));
        game.player.addGold(1);
        assertTrue(Shop.canPurchaseHandSizeUpgrade(game));
        assertTrue(game.purchaseHandSizeUpgrade());
        game.player.addGold(500);
        assertTrue(game.purchaseHandSizeUpgrade());
        assertTrue(game.purchaseHandSizeUpgrade());
        assertFalse(game.purchaseHandSizeUpgrade());
        assertFalse(Shop.canPurchaseHandSizeUpgrade(game));
        assertEquals(0, game.player.getGold());
        assertEquals(7, game.getHandSizeLimit());
        assertEquals(-1, game.getNextHandSizeUpgradeCost());
    }

    @Test
    public void deckManagerDrawsUpToTheUpgradedHandLimit() {
        Game game = new Game();
        game.player.addGold(100);
        assertTrue(game.purchaseHandSizeUpgrade());
        DeckManager deckManager = new DeckManager(game);
        deckManager.resetPlayerDeck();

        deckManager.drawHand();

        assertEquals(5, game.player.hand.size());
    }
}