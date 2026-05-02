package com.koray;

/**
 * Event published to the EventBus when an enemy dies.
 * Carries a reference to the defeated enemy so subscribers
 * (e.g. RewardSystem) can inspect its stats (boss flag, level, etc.)
 * when calculating gold rewards.
 */
public class EnemyDeathEvent extends GameEvent {

    /** The enemy that was defeated. */
    public Enemy enemy;

    /**
     * @param enemy the enemy that just died
     */
    public EnemyDeathEvent(Enemy enemy) {
        this.enemy = enemy;
    }
}