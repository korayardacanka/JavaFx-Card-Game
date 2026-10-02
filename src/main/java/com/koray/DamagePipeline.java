package com.koray;

/** Resolves outgoing damage bonuses before notifying damage-response relics. */
public final class DamagePipeline {

    private DamagePipeline() {}

    public static DamageContext resolve(Game game, int baseDamage) {
        DamageContext context = new DamageContext(baseDamage);
        for (RelicItem relic : game.ownedRelics) {
            relic.addDamageBonus(game.player, game.enemy, game, context);
        }

        context.applyBonusDamage(game.enemy);
        for (RelicItem relic : game.ownedRelics) {
            relic.onEnemyDamaged(game.player, game.enemy, game, context);
        }
        return context;
    }
}
