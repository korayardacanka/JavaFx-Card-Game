package com.koray;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PlayerTest {

    @Test
    public void shieldOverflowReducesHp() {
        Player player = new Player();
        player.addShield(10);

        player.takeDamage(15);

        assertEquals(95, player.getHp());
        assertEquals(0, player.getShield());
    }

    @Test
    public void healingDoesNotExceedMaximumHp() {
        Player player = new Player();
        player.takeDamage(20);

        player.heal(100);

        assertEquals(player.getMaxHp(), player.getHp());
    }

    @Test
    public void insufficientEnergyAndGoldDoNotChangeBalances() {
        Player player = new Player();
        player.restoreEnergy(1);
        int goldBefore = player.getGold();

        assertFalse(player.spendEnergy(2));
        assertFalse(player.spendGold(goldBefore + 1));

        assertEquals(1, player.getEnergy());
        assertEquals(goldBefore, player.getGold());
    }

    @Test
    public void increasingMaxHpAlsoRaisesCurrentHpWithinNewMaximum() {
        Player player = new Player();
        player.takeDamage(40);

        player.increaseMaxHp(25);

        assertEquals(125, player.getMaxHp());
        assertEquals(85, player.getHp());
    }

    @Test
    public void cardPileGettersCannotBeUsedToMutatePlayerState() {
        Player player = new Player();
        Card card = CardFactory.make("Test", 1, 1, 1, new DamageEffect(1));
        player.addToHand(card);
        player.addToDiscard(card);

        try {
            player.getDeck().add(card);
            fail("Deck view should be read-only.");
        } catch (UnsupportedOperationException expected) {
            assertTrue(player.getDeck().isEmpty());
        }
        try {
            player.getHand().clear();
            fail("Hand view should be read-only.");
        } catch (UnsupportedOperationException expected) {
            assertEquals(1, player.getHand().size());
        }
        try {
            player.getDiscard().clear();
            fail("Discard view should be read-only.");
        } catch (UnsupportedOperationException expected) {
            assertEquals(1, player.getDiscard().size());
        }
    }
}
