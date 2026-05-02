package com.koray;

import java.util.Collections;

/**
 * Manages all deck operations for the player.
 * Responsible for building the starting deck, drawing cards into hand,
 * and recycling the discard pile when the deck runs out.
 *
 * Extracted from Main.java to separate deck logic from UI code.
 */
public class DeckManager {

    private final Game game;

    /**
     * @param game the active game state (provides access to player piles)
     */
    public DeckManager(Game game) {
        this.game = game;
    }

    /**
     * Clears all card piles and rebuilds the starting deck from scratch.
     * Each starter card (Damage, Shield, Heal) is added INITIAL_DECK_COPIES times,
     * then the deck is shuffled.
     */
    public void resetPlayerDeck() {
        game.player.deck.clear();
        game.player.hand.clear();
        game.player.discard.clear();

        for (int i = 0; i < UIConstants.INITIAL_DECK_COPIES; i++) {
            game.player.deck.add(CardFactory.make("Damage", 1, 10, 1, new DamageEffect(15)));
            game.player.deck.add(CardFactory.make("Shield", 1, 10, 1, new ShieldEffect(10)));
            game.player.deck.add(CardFactory.make("Heal",   2, 20, 1, new HealEffect(10)));
        }

        Collections.shuffle(game.player.deck);
    }

    /**
     * Draws a full hand of INITIAL_HAND_SIZE cards.
     * Clears any existing hand before drawing.
     */
    public void drawHand() {
        game.player.hand.clear();
        for (int i = 0; i < UIConstants.INITIAL_HAND_SIZE; i++) {
            drawSingleCard();
        }
    }

    /**
     * Draws one card from the top of the deck into the player's hand.
     * If the deck is empty, the discard pile is shuffled back into the deck first.
     * If both piles are empty, no card is drawn.
     */
    public void drawSingleCard() {
        if (game.player.deck.isEmpty()) {
            game.player.deck.addAll(game.player.discard);
            game.player.discard.clear();
            Collections.shuffle(game.player.deck);
        }
        if (!game.player.deck.isEmpty()) {
            game.player.hand.add(game.player.deck.remove(0));
        }
    }
}