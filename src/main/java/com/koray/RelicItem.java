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
 
    // ── Mevcut hooklar ──────────────────────────────────────
    /** Satın alınınca bir kez çalışır */
    public void applyOnBuy(Player player, Game game) {}
 
    /** Her tur başında çalışır */
    public void applyPassive(Player player, Game game) {}
 
    // ── Yeni hooklar ────────────────────────────────────────
    /**
     * Oyuncu hasar aldığında çağrılır.
     * @param damage oyuncunun aldığı hasar miktarı
     */
    public void onDamageTaken(Player player, Enemy enemy, Game game, int damage) {}
 
    /**
     * Düşmana hasar verildiğinde çağrılır.
     * @param damage düşmanın aldığı hasar miktarı
     */
    public void onEnemyDamaged(Player player, Enemy enemy, Game game, int damage) {}
}