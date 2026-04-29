package com.koray;
 
/**
 * 🩸 Kan Antlaşması
 * Alınca -30 HP kaybettirir ama kalıcı olarak maxEnergy +2 artırır.
 * Yüksek riskli, yüksek kazançlı bir relic.
 */
class BloodPactRelic extends RelicItem {
 
    public BloodPactRelic() {
        super("🩸 Kan Antlaşması",
              "Al: -30 HP  |  Kazanç: Max Enerji +2 (kalıcı)",
              0); // ücretsiz ama HP bedeli var
    }
 
    @Override
    public void applyOnBuy(Player player, Game game) {
        player.takeDamage(30);
        game.maxEnergy += 2;
        game.lastEvent = "🩸 Kan Antlaşması! -30 HP, Max Enerji +" + game.maxEnergy;
    }
}