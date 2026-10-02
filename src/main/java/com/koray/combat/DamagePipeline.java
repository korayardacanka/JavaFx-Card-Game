package com.koray.combat;

import com.koray.core.Game;
import com.koray.relics.RelicItem;

/** Resolves outgoing damage bonuses before notifying damage-response relics. */
public final class DamagePipeline {

    private DamagePipeline() {}

    public static DamageContext resolve(Game game, int baseDamage) {
        DamageContext context = new DamageContext(baseDamage);
        for (RelicItem relic : game.getOwnedRelics()) {
            relic.addDamageBonus(game.getPlayer(), game.getEnemy(), game, context);
        }

        context.applyBonusDamage(game.getEnemy());
        for (RelicItem relic : game.getOwnedRelics()) {
            relic.onEnemyDamaged(game.getPlayer(), game.getEnemy(), game, context);
        }
        return context;
    }
}


