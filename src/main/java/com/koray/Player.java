package com.koray;

import java.util.*;

public class Player {

    private int hp     = 100;
    private int shield = 0;
    private int gold   = 50;
    private int energy = 3;
    private int maxhp  = 100;

    // Koleksiyonlar aynı pakette erişilebilir (CardFactory, Shop vb.)
    List<Card> deck    = new ArrayList<>();
    List<Card> hand    = new ArrayList<>();
    List<Card> discard = new ArrayList<>();

    // ── Getters ─────────────────────────────────────────────
    public int  getHp()     { return hp; }
    public int  getShield() { return shield; }
    public int  getGold()   { return gold; }
    public int  getEnergy() { return energy; }
    public int  getMaxHp()  { return maxhp; }
    public boolean isAlive() { return hp > 0; }

    // ── HP ──────────────────────────────────────────────────
    /**
     * Hasarı önce shield'a, kalan kısmı hp'ye uygular.
     */
    public void takeDamage(int dmg) {
        if (dmg <= 0) return;
        if (shield > 0) {
            shield -= dmg;
            if (shield < 0) {
                hp += shield;   // kalan negatif miktarı hp'den düş
                shield = 0;
            }
        } else {
            hp -= dmg;
        }
    }

    /**
     * HP'yi maxhp'yi aşmadan artırır.
     */
    public void heal(int amount) {
        if (amount <= 0) return;
        hp = Math.min(hp + amount, maxhp);
    }

    /**
     * Max HP'yi artırır; mevcut HP de aynı miktarda yükselir.
     */
    public void increaseMaxHp(int amount) {
        maxhp += amount;
        hp = Math.min(hp + amount, maxhp);
    }

    // ── Shield ──────────────────────────────────────────────
    public void addShield(int amount) {
        if (amount > 0) shield += amount;
    }

    public void resetShield() {
        shield = 0;
    }

    // ── Energy ──────────────────────────────────────────────
    /**
     * Belirtilen miktarda enerji harcar.
     * @return Yeterli enerji varsa true, yoksa false (harcama yapılmaz).
     */
    public boolean spendEnergy(int cost) {
        if (energy < cost) return false;
        energy -= cost;
        return true;
    }

    public void restoreEnergy(int amount) {
        energy = amount;
    }

    // ── Gold ────────────────────────────────────────────────
    public void addGold(int amount) {
        if (amount > 0) gold += amount;
    }

    /**
     * Belirtilen miktarda gold harcar.
     * @return Yeterli gold varsa true, yoksa false (harcama yapılmaz).
     */
    public boolean spendGold(int amount) {
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }

    
   
}