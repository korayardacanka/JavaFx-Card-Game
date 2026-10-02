package com.koray;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.layout.VBox;

/**
 * Controls all in-battle game logic.
 * Handles card playing, turn processing, enemy death, and player death.
 * Contains no JavaFX layout code — it only mutates game state and
 * triggers UI callbacks provided at construction time.
 *
 * Extracted from Main.java to separate game rules from UI code.
 */
public class BattleController {

    public static final int HAND_REROLL_COST = 10;

    public enum BattleState {
        PLAYER_TURN,
        ENEMY_TURN,
        ANIMATING,
        GAME_OVER
    }

    private final Game             game;
    private final DeckManager      deckManager;
    private final AnimationPlayer  animator;
    private final EnemyAnimationPlayer enemyAnimator;

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
     * Single source of truth for the combat flow.
     * PLAYER_TURN: player may act or end turn.
     * ENEMY_TURN: enemy is attacking / resolving status effects.
     * ANIMATING: UI input is blocked while an animation callback is in progress.
     * GAME_OVER: player death screen is active; no combat actions allowed.
     */
    private BattleState battleState = BattleState.PLAYER_TURN;

    /**
     * @param game          the active game state
     * @param deckManager   deck operations (draw, reset)
     * @param animator      sprite and card animations
     * @param enemyAnimator shape-based enemy animations
     * @param onUpdateUI    callback: refresh all UI labels and bars
     * @param onPlayerDeath callback: show the death screen
     * @param onEnemyDeath  callback: update enemy visuals and open shop
     * @param onLog         callback: write a string to the event log label
     */
    public BattleController(Game game,
                            DeckManager deckManager,
                            AnimationPlayer animator,
                            EnemyAnimationPlayer enemyAnimator,
                            Runnable onUpdateUI,
                            Runnable onPlayerDeath,
                            Runnable onEnemyDeath,
                            Consumer<String> onLog) {
        this.game          = game;
        this.deckManager   = deckManager;
        this.animator      = animator;
        this.enemyAnimator = enemyAnimator;
        this.onUpdateUI    = onUpdateUI;
        this.onPlayerDeath = onPlayerDeath;
        this.onEnemyDeath  = onEnemyDeath;
        this.onLog         = onLog;
    }

    /** Returns the current combat state. */
    public BattleState getState() { return battleState; }

    /** Returns true while input should be blocked for combat flow reasons. */
    public boolean isTurnLocked() { return battleState != BattleState.PLAYER_TURN; }

    /** Returns true when the player is allowed to act in the current turn. */
    public boolean canPlayerAct() {
        return battleState == BattleState.PLAYER_TURN && game.player.isAlive();
    }

    void setState(BattleState newState) {
        battleState = newState;
    }

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
        if (!canPlayerAct()) return;
        if (!game.player.spendEnergy(c.cost)) return;

        setState(BattleState.ANIMATING);

        int enemyHpBefore = game.enemy.getHp();
        c.use(game.player, game.enemy);
        game.player.hand.remove(c);
        game.player.discard.add(c);

        // Fire onEnemyDamaged hooks for all owned relics
        int dealtDamage = enemyHpBefore - game.enemy.getHp();
        if (dealtDamage > 0) {
            enemyAnimator.playHurt(null);
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
            animator.playCardEffect(cardBox,
                () -> enemyAnimator.playDeath(this::handleEnemyDeath));
        } else {
            animator.playCardEffect(cardBox, () -> {
                setState(BattleState.PLAYER_TURN);
                onUpdateUI.run();
            });
        }
    }

    /**
     * Replaces the current hand with the same number of cards for
     * {@link #HAND_REROLL_COST} gold.
     */
    public void handleHandReroll() {
        if (!canPlayerAct()) return;
        if (game.player.hand.isEmpty()) {
            onLog.accept("There are no cards in hand to reroll.");
            return;
        }
        if (!game.player.spendGold(HAND_REROLL_COST)) {
            onLog.accept("Not enough gold to reroll your hand ("
                + HAND_REROLL_COST + " gold required).");
            return;
        }

        int cardsToDraw = game.player.hand.size();
        List<Card> oldHand = new java.util.ArrayList<>(game.player.hand);
        game.player.hand.clear();
        for (int i = 0; i < cardsToDraw; i++) {
            deckManager.drawSingleCard();
        }
        game.player.discard.addAll(oldHand);

        onUpdateUI.run();
        onLog.accept("Hand rerolled for " + HAND_REROLL_COST + " gold.");
    }

    /** Draws newly available hand slots immediately after a hand-size upgrade. */
    void drawMissingHandCardsIfPlayerTurn() {
        if (battleState != BattleState.PLAYER_TURN) return;

        int cardsToDraw = game.getHandSizeLimit() - game.player.hand.size();
        for (int i = 0; i < cardsToDraw; i++) {
            deckManager.drawSingleCard();
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
        if (!canPlayerAct()) return;
        setState(BattleState.ENEMY_TURN);
        Shop.closeShop();

        // 1. Apply status effects (poison/burn damage, freeze log)
        int enemyHpBeforeStatus = game.enemy.getHp();
        String statusLog = game.enemy.processStatusEffects();
        if (!statusLog.isEmpty()) {
            game.lastEvent = statusLog;
        }

        // 2. Did enemy die from status effects?
        if (!game.enemy.isAlive()) {
            onUpdateUI.run();
            setState(BattleState.ANIMATING);
            enemyAnimator.playDeath(this::finishEnemyDeathTurn);
            return;
        }

        if (game.enemy.getHp() < enemyHpBeforeStatus) {
            setState(BattleState.ANIMATING);
            enemyAnimator.playHurt(this::resolveEnemyTurnAttack);
        } else {
            resolveEnemyTurnAttack();
        }
    }

    private void resolveEnemyTurnAttack() {
        boolean enemyWasFrozen = game.enemy.isFrozen();

        // Enemy attacks
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
            setState(BattleState.ANIMATING);
            if (enemyWasFrozen) {
                enemyAnimator.playDeath(this::finishEnemyDeathTurn);
            } else {
                enemyAnimator.playAttack(
                    () -> enemyAnimator.playDeath(this::finishEnemyDeathTurn));
            }
            return;
        }

        if (enemyWasFrozen) {
            enemyAnimator.playIdle();
        } else {
            enemyAnimator.playAttack(null);
        }

        // Only react to a hit when the enemy actually removed player HP.
        Runnable finishTurn = () -> {
            if (!game.player.isAlive()) {
                checkPlayerDeath();
            } else {
                startNewTurn();
            }
        };
        if (damageTaken > 0) {
            setState(BattleState.ANIMATING);
            animator.playAnimation("_HURT_", UIConstants.HURT_FRAME_COUNT, false, () -> {
                animator.playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);
                finishTurn.run();
            });
        } else {
            finishTurn.run();
        }

        onUpdateUI.run();
    }

    private void finishEnemyDeathTurn() {
        handleEnemyDeath();
        startNewTurn();
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
        if (battleState == BattleState.GAME_OVER) return;
        if (!game.player.isAlive()) {
            checkPlayerDeath();
            return;
        }
        if (game.enemy.isAlive()) return; // double-call guard

        setState(BattleState.PLAYER_TURN);

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
        if (game.player.isAlive() || battleState == BattleState.GAME_OVER) return;
        setState(BattleState.GAME_OVER);
        animator.playAnimation("_DIE_", UIConstants.DEATH_FRAME_COUNT, false, onPlayerDeath);
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
        if (!game.player.isAlive()) {
            checkPlayerDeath();
            return;
        }
        setState(BattleState.PLAYER_TURN);
        game.player.restoreEnergy(game.maxEnergy);
        game.lastEvent = "";

        for (RelicItem relic : game.ownedRelics) {
            relic.applyPassive(game.player, game);
        }

        drawMissingHandCardsIfPlayerTurn();

        onUpdateUI.run();
        onLog.accept("New turn started.");
    }
}