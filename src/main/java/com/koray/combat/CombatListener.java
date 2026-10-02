package com.koray.combat;

import com.koray.cards.Card;
import com.koray.enemies.Enemy;

/** Receives synchronous combat events; callbacks resume the engine after animation. */
public interface CombatListener {

    default void onStateChanged(CombatEngine.BattleState state) {}
    default void onPlayerAttack(Card card, Runnable animationFinished) {
        animationFinished.run();
    }
    default void onEnemyHit() {}
    default void onEnemyStatusHit(Runnable animationFinished) {
        animationFinished.run();
    }
    default void onEnemyAttack(Runnable animationFinished) {
        animationFinished.run();
    }
    default void onEnemyDeath(Enemy enemy, Runnable animationFinished) {
        animationFinished.run();
    }
    default void onPlayerHurt(Runnable animationFinished) {
        animationFinished.run();
    }
    default void onPlayerDeath() {}
    default void onNewTurn() {}
    default void onEnemyDefeated() {}
    default void onTurnEnd() {}
    default void onUpdate() {}
    default void onLog(String message) {}
}


