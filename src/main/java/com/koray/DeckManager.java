package com.koray;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Manages all deck operations for the player.
 * Responsible for building the starting deck, drawing cards into hand,
 * and recycling the discard pile when the deck runs out.
 *
 * Extracted from Main.java to separate deck logic from UI code.
 */
public class DeckManager {

    private final Game game;
    private final Random random;

    /**
     * @param game the active game state (provides access to player piles)
     */
    public DeckManager(Game game) {
        this(game, new Random());
    }

    public DeckManager(Game game, Random random) {
        this.game = game;
        this.random = random;
    }

    /**
     * Clears all card piles and rebuilds the starting deck from scratch.
     * Each starter card is added INITIAL_DECK_COPIES times, then shuffled.
     */
    public void resetPlayerDeck() {
        game.getPlayer().clearCardPiles();
        List<Card> starterDeck = CardFactory.starterDeck();
        Collections.shuffle(starterDeck, random);
        game.getPlayer().resetDeck(starterDeck);
    }

    /**
     * Draws a full hand up to the current hand-size limit.
     * Clears any existing hand before drawing.
     */
    public void drawHand() {
        game.getPlayer().clearHand();
        for (int i = 0; i < game.getHandSizeLimit(); i++) {
            drawSingleCard();
        }
    }

    /**
     * Draws one card from the top of the deck into the player's hand.
     * If the deck is empty, the discard pile is shuffled back into the deck first.
     * If both piles are empty, no card is drawn.
     */
    public void drawSingleCard() {
        game.getPlayer().drawCard(random);
    }
}