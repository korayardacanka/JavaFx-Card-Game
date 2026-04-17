package com.koray;

public abstract class RelicItem {
    public String name;
    public String description;
    public int price;
 
    public RelicItem(String name, String description, int price) {
        this.name        = name;
        this.description = description;
        this.price       = price;
    }
 
    // Satın alınınca bir kez çalışır
    public void applyOnBuy(Player player, Game game) {}
 
    // Her tur başında çalışır (pasif efekt)
    public void applyPassive(Player player, Game game) {}
}
