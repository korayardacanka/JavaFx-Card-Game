package com.koray;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void cardRemovalCostIncreasesAndOnlyOneCardCanBeRemovedPerShopVisit() {
        Game game = gameWithCards(7);
        game.beginShopVisit();
        Card first = game.getPlayer().getDeck().get(0);

        assertEquals(50, game.getCardRemovalCost());
        assertTrue(game.canRemoveCard());
        assertTrue(game.removeCardFromRun(first));
        assertEquals(0, game.getPlayer().getGold());
        assertEquals(1, game.getCardRemovalCount());
        assertEquals(75, game.getCardRemovalCost());
        assertFalse(game.canRemoveCard());

        game.beginShopVisit();
        game.getPlayer().addGold(75);
        assertTrue(game.canRemoveCard());
        assertTrue(game.removeCardFromRun(game.getPlayer().getDeck().get(0)));
        assertEquals(2, game.getCardRemovalCount());
        assertEquals(100, game.getCardRemovalCost());
        assertEquals(0, game.getPlayer().getGold());
    }

    @Test
    public void cardRemovalCannotReduceTheCombinedPilesBelowFiveCards() {
        Game game = gameWithCards(6);
        game.beginShopVisit();

        assertTrue(game.removeCardFromRun(game.getPlayer().getDeck().get(0)));
        assertEquals(5, game.getPlayer().getTotalCardCount());

        game.beginShopVisit();
        game.getPlayer().addGold(100);
        assertFalse(game.canRemoveCard());
        assertFalse(game.removeCardFromRun(game.getPlayer().getDeck().get(0)));
        assertEquals(5, game.getPlayer().getTotalCardCount());
        assertEquals(1, game.getCardRemovalCount());
    }

    @Test
    public void removingCardRemovesItsReferenceFromEveryPile() {
        Game game = gameWithCards(6);
        Card selected = game.getPlayer().getDeck().get(0);
        game.getPlayer().addToHand(selected);
        game.getPlayer().addToDiscard(selected);
        game.beginShopVisit();
        int countBefore = game.getPlayer().getTotalCardCount();

        assertTrue(game.removeCardFromRun(selected));

        assertFalse(game.getPlayer().getDeck().contains(selected));
        assertFalse(game.getPlayer().getHand().contains(selected));
        assertFalse(game.getPlayer().getDiscard().contains(selected));
        assertEquals(countBefore - 3, game.getPlayer().getTotalCardCount());
        assertEquals(0, game.getPlayer().getGold());
    }

    private static Game gameWithCards(int count) {
        Game game = new Game();
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(CardFactory.make("Test " + i, 1, 1, 1, new DamageEffect(1)));
        }
        game.getPlayer().resetDeck(cards);
        return game;
    }
}