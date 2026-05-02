package com.koray;

import javafx.scene.layout.VBox;
import java.util.function.Consumer;

/**
 * Controls all in-battle game logic.
 * Handles card playing, turn processing, enemy death, and player death.
 * Contains no JavaFX layout code — it only mutates game state and
 * triggers UI callbacks provided at construction time.
 *
 * Extracted from Main.java to separate game rules from UI code.
 */
public class BattleController {

    private final Game             game;
    private final DeckManager      deckManager;
    private final AnimationPlayer  animator;

    // ── Callbacks into Main (UI layer) ────────────────────────────────────────
    /** Called whenever game state changes and the UI needs to refresh. */
    private final Runnable         onUpdateUI;

    /** Called when the player's HP reaches 0. */
    private final Runnable         onPlayerDeath;

    /** Called after an enemy dies — provides new enemy visuals and opens shop. */
    private final Runnable         onEnemyDeath;

    /** Writes a message into the UI event log. */
    private final Consumer<String> onLog;

    /**
     * Guards against double-turn-ending during animations.
     * true = a turn is currently in progress; input should be ignored.
     */
    private boolean turnLocked = false;

    /**
     * @param game          the active game state
     * @param deckManager   deck operations (draw, reset)
     * @param animator      sprite and card animations
     * @param onUpdateUI    callback: refresh all UI labels and bars
     * @param onPlayerDeath callback: show the death screen
     * @param onEnemyDeath  callback: update enemy visuals and open shop
     * @param onLog         callback: write a string to the event log label
     */
    public BattleController(Game game,
                            DeckManager deckManager,
                            AnimationPlayer animator,
                            Runnable onUpdateUI,
                            Runnable onPlayerDeath,
                            Runnable onEnemyDeath,
                            Consumer<String> onLog) {
        this.game          = game;
        this.deckManager   = deckManager;
        this.animator      = animator;
        this.onUpdateUI    = onUpdateUI;
        this.onPlayerDeath = onPlayerDeath;
        this.onEnemyDeath  = onEnemyDeath;
        this.onLog         = onLog;
    }

    /** Returns true while a turn animation is in progress (input should be blocked). */
    public boolean isTurnLocked() { return turnLocked; }

    // ── Card playing ──────────────────────────────────────────────────────────

    /**
     * Plays a card: spends energy, applies the effect, fires relic hooks,
     * moves the card to the discard pile, and starts the appropriate animation.
     * Does nothing if the player lacks sufficient energy.
     *
     * @param c       the card being played
     * @param cardBox the card's VBox (used for the play animation)
     */
    public void handleCardPlay(Card c, VBox cardBox) {
        if (!game.player.spendEnergy(c.cost)) return;

        int enemyHpBefore = game.enemy.getHp();
        c.use(game.player, game.enemy);
        game.player.hand.remove(c);
        game.player.discard.add(c);

        // Fire onEnemyDamaged hooks for all owned relics
        int dealtDamage = enemyHpBefore - game.enemy.getHp();
        if (dealtDamage > 0) {
            for (RelicItem r : game.ownedRelics) {
                r.onEnemyDamaged(game.player, game.enemy, game, dealtDamage);
            }
        }

        // Play attack animation for damage cards, idle for others
        if (c.effect instanceof DamageEffect) {
            animator.playAnimation("ATTACK_", UIConstants.ATTACK_FRAME_COUNT, false,
                () -> animator.playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null));
        } else {
            animator.playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);
        }

        // Animate the card flying off, then update UI or handle enemy death
        if (!game.enemy.isAlive()) {
            animator.playCardEffect(cardBox, this::handleEnemyDeath);
        } else {
            animator.playCardEffect(cardBox, onUpdateUI);
        }
    }

    // ── Turn processing ───────────────────────────────────────────────────────

    /**
     * Processes the end of the player's turn:
     *   1. Apply status effects (poison, burn, freeze)
     *   2. Check if enemy died from status damage
     *   3. Enemy attacks the player
     *   4. Fire onDamageTaken relic hooks
     *   5. Check if relic retaliation killed the enemy
     *   6. Play hurt animation, then start the next turn
     *
     * Ignores calls while turnLocked is true (prevents double-ending).
     */
    public void handleEndTurn() {
        if (turnLocked) return;
        turnLocked = true;
        Shop.closeShop();

        // 1. Apply status effects (poison/burn damage, freeze log)
        String statusLog = game.enemy.processStatusEffects();
        if (!statusLog.isEmpty()) {
            game.lastEvent = statusLog;
        }

        // 2. Did enemy die from status effects?
        if (!game.enemy.isAlive()) {
            onUpdateUI.run();
            handleEnemyDeath();
            startNewTurn();
            turnLocked = false;
            return;
        }

        // 3. Enemy attacks
        int playerHpBefore = game.player.getHp();
        game.enemy.attack(game.player);
        int damageTaken = playerHpBefore - game.player.getHp();

        // 4. Fire onDamageTaken hooks (e.g. ThornRelic reflects damage)
        if (damageTaken > 0) {
            for (RelicItem r : game.ownedRelics) {
                r.onDamageTaken(game.player, game.enemy, game, damageTaken);
            }
        }

        // 5. Did relic retaliation kill the enemy?
        if (!game.enemy.isAlive()) {
            onUpdateUI.run();
            handleEnemyDeath();
            startNewTurn();
            turnLocked = false;
            return;
        }

        // 6. Play hurt animation; start next turn or show death screen in callback
        animator.playAnimation("_HURT_", UIConstants.HURT_FRAME_COUNT, false, () -> {
            animator.playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);
            if (!game.player.isAlive()) {
                checkPlayerDeath();
            } else {
                startNewTurn();
            }
            turnLocked = false;
        });

        onUpdateUI.run();
    }

    // ── Enemy / player death ──────────────────────────────────────────────────

    /**
     * Handles enemy death: publishes the death event (triggering gold reward),
     * increments the level, scales max energy on even levels, prepares shop
     * contents, and calls back into Main to update visuals and open the shop.
     *
     * Safe to call multiple times — exits immediately if the enemy is still alive.
     */
    private void handleEnemyDeath() {
        if (game.enemy.isAlive()) return; // double-call guard

        Enemy dead = game.enemy;
        game.eventBus.publish(new EnemyDeathEvent(dead));
        game.level++;
        game.enemy = EnemyFactory.createEnemy(game.level);

        // Every 2 levels, max energy grows by 1
        if (game.level % 2 == 0) game.maxEnergy++;

        // Prepare shop inventory
        game.currentShopCards = CardFactory.shopCards(game.level, game.player);
        if (dead.isBoss()) {
            game.currentBossRelics = RelicFactory.bossRelics(game.level, game.ownedRelics);
        } else {
            game.currentBossRelics.clear();
        }

        // Delegate visual/shop update to Main
        onEnemyDeath.run();
    }

    /**
     * Triggers the death animation followed by the death screen.
     * Only executes if the player is actually dead.
     */
    private void checkPlayerDeath() {
        if (!game.player.isAlive()) {
            animator.playAnimation("_DIE_", UIConstants.DEATH_FRAME_COUNT, false, onPlayerDeath);
        }
    }

    // ── New turn ──────────────────────────────────────────────────────────────

    /**
     * Starts a new player turn:
     *   - Restores energy to max
     *   - Applies passive relic effects
     *   - Draws cards up to hand size
     *   - Clears the last event log
     */
    private void startNewTurn() {
        game.player.restoreEnergy(game.maxEnergy);
        game.lastEvent = "";

        for (RelicItem relic : game.ownedRelics) {
            relic.applyPassive(game.player, game);
        }

        int cardsToDraw = UIConstants.INITIAL_HAND_SIZE - game.player.hand.size();
        for (int i = 0; i < cardsToDraw; i++) {
            deckManager.drawSingleCard();
        }

        onUpdateUI.run();
        onLog.accept("Yeni tur başladı.");
    }
}