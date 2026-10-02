package com.koray;
 
/**
 * 🪓 Executioner's Axe relic.
 * Deals 50% bonus damage whenever the enemy's HP drops below 30%.
 * Encourages aggressive finishing moves — the bonus only activates
 * when the enemy is already near death.
 */
class ExecutionerRelic extends RelicItem {

    public ExecutionerRelic() {
        super("🪓 Executioner's Axe",
              "Deal +50% damage when enemy HP is below 30%",
              70);
    }

    @Override
    public void addDamageBonus(Player player, Enemy enemy, Game game,
                               DamageContext context) {
        double hpRatio = (double) enemy.getHp() / enemy.getMaxHp();
        if (hpRatio < 0.30 && context.baseDamage() > 0) {
            int bonus = (int)(context.baseDamage() * 0.5);
            if (bonus > 0) {
                context.addBonus(bonus);
                game.eventLog.append("🪓 +" + bonus + " execute");
            }
        }
    }
}
 