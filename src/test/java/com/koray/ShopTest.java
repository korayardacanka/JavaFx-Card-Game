package com.koray;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
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
}