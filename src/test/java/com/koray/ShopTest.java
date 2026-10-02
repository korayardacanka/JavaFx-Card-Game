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
        game.addCurrentBossRelic(relic);

        assertFalse(Shop.hasPurchasableBossRelic(game));

        game.getPlayer().addGold(10);
        assertTrue(Shop.hasPurchasableBossRelic(game));

        game.addOwnedRelic(new RelicItem("Test Relic", "", 60) {});
        assertFalse(Shop.hasPurchasableBossRelic(game));
    }

    @Test
    public void bloodPactMustMeetItsHealthRequirementToBePurchasable() {
        Game game = new Game();
        game.addCurrentBossRelic(new BloodPactRelic());

        game.getPlayer().takeDamage(70);
        assertFalse(Shop.hasPurchasableBossRelic(game));

        game.getPlayer().heal(1);
        assertTrue(Shop.hasPurchasableBossRelic(game));
    }

    @Test
    public void bloodPactCountsShieldTowardItsPurchaseRequirement() {
        Game game = new Game();
        BloodPactRelic relic = new BloodPactRelic();
        game.addCurrentBossRelic(relic);
        game.getPlayer().takeDamage(80);
        game.getPlayer().addShield(10);

        assertEquals(30, game.getPlayer().getHp() + game.getPlayer().getShield());
        assertFalse(relic.canPurchase(game));
        assertFalse(Shop.hasPurchasableBossRelic(game));

        game.getPlayer().addShield(1);

        assertTrue(relic.canPurchase(game));
        assertTrue(Shop.hasPurchasableBossRelic(game));
    }

    @Test
    public void handSizeUpgradeCostsIncreaseAndRaiseThePersistentHandLimit() {
        Game game = new Game();
        assertEquals(4, game.getHandSizeLimit());
        assertEquals(100, game.getNextHandSizeUpgradeCost());

        game.getPlayer().addGold(50);
        assertTrue(game.purchaseHandSizeUpgrade());
        assertEquals(0, game.getPlayer().getGold());
        assertEquals(5, game.getHandSizeLimit());
        assertEquals(200, game.getNextHandSizeUpgradeCost());
    }

    @Test
    public void handSizeUpgradeCannotBePurchasedWithoutGoldOrBeyondTheMaximum() {
        Game game = new Game();
        assertFalse(game.purchaseHandSizeUpgrade());
        assertFalse(Shop.canPurchaseHandSizeUpgrade(game));
        assertEquals(4, game.getHandSizeLimit());

        game.getPlayer().addGold(49);
        assertFalse(Shop.canPurchaseHandSizeUpgrade(game));
        game.getPlayer().addGold(1);
        assertTrue(Shop.canPurchaseHandSizeUpgrade(game));
        assertTrue(game.purchaseHandSizeUpgrade());
        game.getPlayer().addGold(500);
        assertTrue(game.purchaseHandSizeUpgrade());
        assertTrue(game.purchaseHandSizeUpgrade());
        assertFalse(game.purchaseHandSizeUpgrade());
        assertFalse(Shop.canPurchaseHandSizeUpgrade(game));
        assertEquals(0, game.getPlayer().getGold());
        assertEquals(7, game.getHandSizeLimit());
        assertEquals(-1, game.getNextHandSizeUpgradeCost());
    }

    @Test
    public void deckManagerDrawsUpToTheUpgradedHandLimit() {
        Game game = new Game();
        game.getPlayer().addGold(100);
        assertTrue(game.purchaseHandSizeUpgrade());
        DeckManager deckManager = new DeckManager(game);
        deckManager.resetPlayerDeck();

        deckManager.drawHand();

        assertEquals(5, game.getPlayer().getHand().size());
    }

}