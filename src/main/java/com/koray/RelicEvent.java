package com.koray;

/**
 * Event published to the EventBus when the player purchases a relic.
 * Currently carries no payload — subscribers react to the purchase
 * itself (e.g. triggering a UI refresh via UIObserver).
 */
public class RelicEvent extends GameEvent {
}