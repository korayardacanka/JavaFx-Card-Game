package com.koray;

public class Enemy {

    private int     hp           = 50;
    private int     maxHp        = 50;
    private int     attackDamage = 10;
    private boolean boss         = false;
    private String  name         = "Enemy";

    // ── Getters ─────────────────────────────────────────────
    public int     getHp()           { return hp; }
    public int     getMaxHp()        { return maxHp; }
    public int     getAttackDamage() { return attackDamage; }
    public boolean isBoss()          { return boss; }
    public String  getName()         { return name; }
    public boolean isAlive()         { return hp > 0; }

    // ── Package-private setters (EnemyFactory kullanır) ─────
    void setHp(int hp)                   { this.hp = hp; this.maxHp = hp; }
    void setAttackDamage(int dmg)        { this.attackDamage = dmg; }
    void setBoss(boolean boss)           { this.boss = boss; }
    void setName(String name)            { this.name = name; }

    // ── Savaş ───────────────────────────────────────────────
    public void takeDamage(int dmg) {
        if (dmg > 0) hp -= dmg;
    }

    public void attack(Player player) {
        player.takeDamage(attackDamage);
    }
}