package com.koray;

/**
 * Observer interface for the EventBus (Observer pattern).
 * Any class that needs to react to game events must implement this
 * interface and register itself with EventBus.subscribe().
 *
 * Current implementations:
 *   - RewardSystem  : awards gold when an enemy dies
 *   - UIObserver    : triggers a full UI refresh on every event
 */
public interface Observer {
    /**
     * Called by the EventBus when a game event is published.
     *
     * @param event the event that occurred; cast to the concrete
     *              subtype (EnemyDeathEvent, RelicEvent, etc.) as needed
     */
    void onEvent(GameEvent event);
}