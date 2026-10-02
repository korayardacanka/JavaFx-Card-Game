package com.koray.cards;

import com.koray.core.Player;
import com.koray.enemies.Enemy;
import com.koray.ui.CardDesign;

/**
 * Represents a single playable card in the player's deck.
 * Each card has a mana cost, a shop price,
 * a CardEffect (Strategy pattern) that defines what it does,
 * and an optional CardDesign (Decorator pattern) for visual styling.
 */
public class Card {

    String name;
    int cost;           // energy cost to play this card
    int price;          // gold cost to buy in the shop
    CardEffect effect;  // what the card does when played (Strategy)
    CardDesign design;  // visual appearance (Decorator chain), nullable

    /**
     * Full constructor — creates a card with a design.
     * Use this path when building cards via CardFactory.
     */
    public Card(String name, int cost, int price, CardEffect effect, CardDesign design) {
        this.name          = name;
        this.cost          = cost;
        this.price         = price;
        this.effect        = effect;
        this.design        = design;
    }

    public String getName() { return name; }
    public int getCost() { return cost; }
    public int getPrice() { return price; }
    public CardEffect getEffect() { return effect; }
    public CardDesign getDesign() { return design; }

    /**
     * Plays the card: delegates to the card's effect.
     * Energy spending is handled by the caller before invoking this.
     *
     * @param player the active player
     * @param enemy  the current enemy target
     */
    public void use(Player player, Enemy enemy) {
        effect.apply(player, enemy);
    }
}


