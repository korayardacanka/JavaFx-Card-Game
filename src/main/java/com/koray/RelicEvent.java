package com.koray;
 
/**
 * Event published to the EventBus when the player purchases a relic.
 * Carries the purchased RelicItem so subscribers can:
 *   - Show a toast notification with the relic's name/icon (UIObserver)
 *   - Filter already-owned relics from future shop pools (RelicFactory)
 */
public class RelicEvent extends GameEvent {
 
    /** The relic that was just purchased. Never null. */
    public final RelicItem relic;
 
    /**
     * @param relic the relic that was purchased
     */
    public RelicEvent(RelicItem relic) {
        this.relic = relic;
    }
}
 