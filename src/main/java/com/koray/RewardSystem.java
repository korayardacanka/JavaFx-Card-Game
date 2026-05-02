package com.koray;
 
/**
 * Observer that handles gold rewards when an enemy dies.
 * Subscribes to the EventBus and reacts to EnemyDeathEvent:
 *   - Regular enemy → 10 + (level × 3) gold
 *   - Boss enemy    → flat 50 gold bonus
 *
 * This class contains no UI code — it only modifies game state.
 * The UIObserver handles the subsequent UI refresh.
 */
public class RewardSystem implements Observer {

    /** Reference to the game state used to award gold and log events. */
    Game game;

    /**
     * @param game the active game state
     */
    public RewardSystem(Game game) {
        this.game = game;
    }

    /**
     * Handles incoming game events.
     * Only EnemyDeathEvent is acted upon; all other event types are ignored.
     *
     * @param event the event dispatched by the EventBus
     */
    @Override
    public void onEvent(GameEvent event) {
        if (event instanceof EnemyDeathEvent e) {
            if (e.enemy.isBoss()) {
                game.player.addGold(50);
                game.lastEvent = "💀 BOSS öldürüldü! +50 gold";
            } else {
                int goldGain = 10 + (game.level * 3);
                game.player.addGold(goldGain);
                game.lastEvent = "Enemy öldürüldü! +" + goldGain + " gold";
            }
        }
    }
}