package com.koray;

/**
 * Observer that keeps the JavaFX UI in sync with game state.
 * Subscribes to the EventBus and reacts to game events:
 *   - RelicEvent  → shows a toast notification with the purchased relic's name,
 *                   then does a full UI refresh (so the relic bar updates too)
 *   - All others  → full UI refresh only
 */
public class UIObserver implements Observer {

    /** The game state to read when refreshing the UI. */
    Game game;

    /** The main application class that owns the UI components. */
    Main main;

    /**
     * @param game the active game state
     * @param main the Main instance whose UI methods will be called
     */
    public UIObserver(Game game, Main main) {
        this.game = game;
        this.main = main;
    }

    /**
     * Called by the EventBus whenever a game event is published.
     * Distinguishes RelicEvent to trigger the toast notification,
     * then always calls updateUI() to keep labels and bars current.
     *
     * @param event the event that occurred
     */
    @Override
    public void onEvent(GameEvent event) {
        if (event instanceof RelicEvent re) {
            main.showRelicToast(re.relic);
        }
        main.updateUI();
    }
}