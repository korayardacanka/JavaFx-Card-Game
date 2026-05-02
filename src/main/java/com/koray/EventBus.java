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
     * Dispatches an event synchronously to all registered observers.
     *
     * @param event the event to broadcast
     */
    public void publish(GameEvent event) {
        for (Observer o : observers) {
            o.onEvent(event);
        }
    }
}