package com.koray;

/**
 * Represents a single playable card in the player's deck.
 * Each card has a mana cost, a shop price, a minimum required level,
 * a CardEffect (Strategy pattern) that defines what it does,
 * and an optional CardDesign (Decorator pattern) for visual styling.
 */
public class Card {

    String name;
    int cost;           // energy cost to play this card
    int price;          // gold cost to buy in the shop
    int requiredLevel;  // minimum game level needed to obtain
    CardEffect effect;  // what the card does when played (Strategy)
    CardDesign design;  // visual appearance (Decorator chain), nullable

    /**
     * Legacy constructor — creates a card without a design.
     * Design will be null; the UI falls back to default styling.
     */
    public Card(String name, int cost, int price, int requiredLevel, CardEffect effect) {
        this.name          = name;
        this.cost          = cost;
        this.price         = price;
        this.requiredLevel = requiredLevel;
        this.effect        = effect;
        this.design        = null;
    }

    /**
     * Full constructor — creates a card with a design.
     * Use this path when building cards via CardFactory.
     */
    public Card(String name, int cost, int price, int requiredLevel,
                CardEffect effect, CardDesign design) {
        this.name          = name;
        this.cost          = cost;
        this.price         = price;
        this.requiredLevel = requiredLevel;
        this.effect        = effect;
        this.design        = design;
    }

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
