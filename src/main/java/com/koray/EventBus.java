package com.koray;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple synchronous event bus (Observer pattern).
 * Observers register themselves via subscribe() and are notified
 * in registration order whenever publish() is called.
 *
 * Currently used to decouple game logic from UI updates and reward
 * calculations: Main publishes events (EnemyDeathEvent, RelicEvent)
 * and RewardSystem / UIObserver react independently.
 *
 * Note: all notifications happen on the calling thread — no threading
 * or queuing is involved.
 */
public class EventBus {

    /** All currently registered observers. */
    private List<Observer> observers = new ArrayList<>();

    /**
     * Registers an observer to receive future events.
     *
     * @param o the observer to add
     */
    public void subscribe(Observer o) {
        observers.add(o);
    }

    /**
     * Removes a previously registered observer.
     * No-op if the observer was never subscribed.
     *
     * @param o the observer to remove
     */
    public void unsubscribe(Observer o) {
        observers.remove(o);
    }

    /**
     * Removes all registered observers at once.
     * Call this before discarding an EventBus instance (e.g. on game restart)
     * to prevent stale observer references from keeping the old game graph alive.
     */
    public void clearObservers() {
        observers.clear();
    }

    /**
     * Dispatches an event synchronously to all registered observers.
     *
     * @param event the event to broadcast
     */
    public void publish(GameEvent event) {
        for (Observer o : new ArrayList<>(observers)) { // snapshot → safe if a handler unsubscribes
            o.onEvent(event);
        }
    }
}