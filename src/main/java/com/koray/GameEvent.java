package com.koray;

/**
 * Abstract base class for all game events (Observer pattern).
 * Concrete subclasses carry the payload relevant to a specific event
 * (e.g. EnemyDeathEvent holds the enemy that died).
 *
 * Events are published through EventBus and dispatched to all
 * registered Observer instances.
 */
public abstract class GameEvent {
}