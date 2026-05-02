package com.koray;
/**
 * Observer that keeps the JavaFX UI in sync with game state.
 * Subscribes to the EventBus and calls Main.updateUI() on every event,
 * regardless of the event type.
 *
 * This "refresh everything on any event" approach is simple and
 * correct, but rebuilds the entire UI on each call. For a larger
 * project, per-event targeted updates would be more efficient.
 */
public class UIObserver implements Observer {

    /** The game state to read when refreshing the UI. */
    Game game;

    /** The main application class that owns the UI components. */
    Main main;

    /**
     * @param game the active game state
     * @param main the Main instance whose updateUI() will be called
     */
    public UIObserver(Game game, Main main) {
        this.game = game;
        this.main = main;
    }

    /**
     * Called by the EventBus whenever any game event is published.
     * Triggers a full UI refresh to reflect the latest game state.
     *
     * @param event the event that occurred (not inspected here)
     */
    @Override
    public void onEvent(GameEvent event) {
        main.updateUI();
    }
}