package com.koray;
 
public class Enemy {
 
    private int     hp           = 50;
    private int     maxHp        = 50;
    private int     attackDamage = 10;
    private boolean boss         = false;
    private String  name         = "Enemy";
 
    // ── Durum efektleri ─────────────────────────────────────
    private int poisonStacks = 0;  // her tur bu kadar hasar, birikerek artar
    private int burnStacks   = 0;  // her tur bu kadar hasar, 1 azalarak söner
    private int freezeTurns  = 0;  // bu kadar tur saldıramaz
 
    // ── Getters ─────────────────────────────────────────────
    public int     getHp()           { return hp; }
    public int     getMaxHp()        { return maxHp; }
    public int     getAttackDamage() { return attackDamage; }
    public boolean isBoss()          { return boss; }
    public String  getName()         { return name; }
    public boolean isAlive()         { return hp > 0; }
    public int     getPoisonStacks() { return poisonStacks; }
    public int     getBurnStacks()   { return burnStacks; }
    public int     getFreezeTurns()  { return freezeTurns; }
    public boolean isFrozen()        { return freezeTurns > 0; }
 
    // ── Package-private setters (EnemyFactory kullanır) ─────
    void setHp(int hp)            { this.hp = hp; this.maxHp = hp; }
    void setAttackDamage(int dmg) { this.attackDamage = dmg; }
    void setBoss(boolean boss)    { this.boss = boss; }
    void setName(String name)     { this.name = name; }
 
    // ── Durum efekti uygulama ────────────────────────────────
    public void addPoison(int stacks) { poisonStacks += stacks; }
    public void addBurn(int stacks)   { burnStacks   += stacks; }
    public void addFreeze(int turns)  { freezeTurns  += turns; }
 
    /**
     * Her tur başında çağrılır.
     * Aktif durum efektlerini uygular ve sürelerini günceller.
     * @return efektlerden kaynaklanan toplam hasarı açıklayan string (UI için)
     */
    public String processStatusEffects() {
        StringBuilder log = new StringBuilder();
 
        if (poisonStacks > 0) {
            takeDamage(poisonStacks);
            log.append("☠ Zehir: -").append(poisonStacks).append(" HP  ");
            // Zehir azalmaz — temizlemek için başka kart/mekanik gerekir
        }
 
        if (burnStacks > 0) {
            takeDamage(burnStacks);
            log.append("🔥 Yanma: -").append(burnStacks).append(" HP  ");
            burnStacks = Math.max(0, burnStacks - 1); // her tur 1 azalır
        }
 
        if (freezeTurns > 0) {
            
            log.append("❄ Donuk (").append(freezeTurns).append(" tur)  ");
            
        }
 
        return log.toString().trim();
    }
 
    // ── Savaş ───────────────────────────────────────────────
    public void takeDamage(int dmg) {
        if (dmg > 0) hp -= dmg;
    }
 
    /**
     * Donmuşsa saldırmaz.
     */
    public void attack(Player player) {
        if (isFrozen()) {
            freezeTurns--;
            return;}
        player.takeDamage(attackDamage);
    }
}