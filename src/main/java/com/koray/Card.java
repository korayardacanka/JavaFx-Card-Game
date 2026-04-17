package com.koray;

public class Card {

    String name;
    int cost;
    int price;
    int requiredLevel;
    CardEffect effect;
    CardDesign design;  // ← yeni field

    // eski constructor — design olmadan (geriye dönük uyumluluk için)
    public Card(String name, int cost, int price, int requiredLevel, CardEffect effect) {
        this.name = name;
        this.cost = cost;
        this.price = price;
        this.requiredLevel = requiredLevel;
        this.effect = effect;
        this.design = null;
    }

    // yeni constructor — design ile
    public Card(String name, int cost, int price, int requiredLevel, CardEffect effect, CardDesign design) {
        this.name = name;
        this.cost = cost;
        this.price = price;
        this.requiredLevel = requiredLevel;
        this.effect = effect;
        this.design = design;
    }

    public void use(Player player, Enemy enemy) {
        effect.apply(player, enemy);
    }
}
