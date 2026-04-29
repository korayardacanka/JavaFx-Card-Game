package com.koray;
 
/**
 * 🌵 Dikenli Zırh
 * Düşman saldırdığında aldığın hasarın bir kısmını geri yansıtır.
 */
class ThornRelic extends RelicItem {
    private int thornDamage;
 
    public ThornRelic(int thornDamage) {
        super("🌵 Dikenli Zırh",
              "Hasar alınca düşmana +" + thornDamage + " hasar yansıt",
              55);
        this.thornDamage = thornDamage;
    }
 
    @Override
    public void onDamageTaken(Player player, Enemy enemy, Game game, int damage) {
        if (damage > 0) {
            enemy.takeDamage(thornDamage);
            game.lastEvent += "  🌵 +" + thornDamage + " yansıma";
        }
    }
}
 