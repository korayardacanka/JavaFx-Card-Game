package com.koray;

import java.util.function.Consumer;
import javafx.scene.layout.VBox;

/** Adapts the UI-free combat engine to JavaFX animations and views. */
public class BattleController {

    public static final int HAND_REROLL_COST = CombatEngine.HAND_REROLL_COST;

    public enum BattleState {
        PLAYER_TURN,
        ENEMY_TURN,
        ANIMATING,
        GAME_OVER
    }

    private final Game game;
    private final CombatEngine engine;
    private final AnimationPlayer animator;
    private final EnemyAnimationPlayer enemyAnimator;
    private final Runnable onUpdateUI;
    private final Runnable onPlayerDeath;
    private final Runnable onEnemyDeath;
    private final Runnable onTurnEnd;
    private final Consumer<String> onLog;
    private BattleState battleState = BattleState.PLAYER_TURN;
    private VBox activeCardBox;

    public BattleController(Game game,
                            DeckManager deckManager,
                            AnimationPlayer animator,
                            EnemyAnimationPlayer enemyAnimator,
                            Runnable onUpdateUI,
                            Runnable onPlayerDeath,
                            Runnable onEnemyDeath,
                            Consumer<String> onLog) {
        this(game, deckManager, animator, enemyAnimator, onUpdateUI, onPlayerDeath,
            onEnemyDeath, () -> {}, onLog);
    }

    public BattleController(Game game,
                            DeckManager deckManager,
                            AnimationPlayer animator,
                            EnemyAnimationPlayer enemyAnimator,
                            Runnable onUpdateUI,
                            Runnable onPlayerDeath,
                            Runnable onEnemyDeath,
                            Runnable onTurnEnd,
                            Consumer<String> onLog) {
        this.game = game;
        this.animator = animator;
        this.enemyAnimator = enemyAnimator;
        this.onUpdateUI = onUpdateUI;
        this.onPlayerDeath = onPlayerDeath;
        this.onEnemyDeath = onEnemyDeath;
        this.onTurnEnd = onTurnEnd;
        this.onLog = onLog;
        this.engine = new CombatEngine(game, deckManager, new CombatListener() {
            @Override
            public void onStateChanged(CombatEngine.BattleState state) {
                battleState = BattleState.valueOf(state.name());
            }

            @Override
            public void onPlayerAttack(Card card, Runnable animationFinished) {
                if (card.effect.isDirectDamage()) {
                    animator.playAnimation("ATTACK_", UIConstants.ATTACK_FRAME_COUNT, false,
                        () -> animator.playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT,
                            true, null));
                } else {
                    animator.playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);
                }
                if (activeCardBox == null) {
                    animationFinished.run();
                    return;
                }
                animator.playCardEffect(activeCardBox, () -> {
                    activeCardBox = null;
                    animationFinished.run();
                });
            }

            @Override
            public void onEnemyHit() {
                enemyAnimator.playHurt(null);
            }

            @Override
            public void onEnemyStatusHit(Runnable animationFinished) {
                enemyAnimator.playHurt(animationFinished);
            }

            @Override
            public void onEnemyAttack(Runnable animationFinished) {
                if (game.getEnemy().isAlive()) {
                    enemyAnimator.playAttack(null);
                    animationFinished.run();
                } else {
                    enemyAnimator.playAttack(animationFinished);
                }
            }

            @Override
            public void onEnemyDeath(Enemy enemy, Runnable animationFinished) {
                enemyAnimator.playDeath(animationFinished);
            }

            @Override
            public void onPlayerHurt(Runnable animationFinished) {
                animator.playAnimation("_HURT_", UIConstants.HURT_FRAME_COUNT, false, () -> {
                    animator.playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);
                    animationFinished.run();
                });
            }

            @Override
            public void onPlayerDeath() {
                animator.playAnimation("_DIE_", UIConstants.DEATH_FRAME_COUNT, false,
                    BattleController.this.onPlayerDeath);
            }

            @Override
            public void onNewTurn() {
                enemyAnimator.playIdle();
            }

            @Override
            public void onEnemyDefeated() {
                BattleController.this.onEnemyDeath.run();
            }

            @Override
            public void onTurnEnd() {
                BattleController.this.onTurnEnd.run();
            }

            @Override
            public void onUpdate() {
                BattleController.this.onUpdateUI.run();
            }

            @Override
            public void onLog(String message) {
                BattleController.this.onLog.accept(message);
            }
        });
    }

    public BattleState getState() { return battleState; }
    public boolean isTurnLocked() { return battleState != BattleState.PLAYER_TURN; }
    public boolean canPlayerAct() { return engine.canPlayerAct(); }

    void setState(BattleState newState) {
        battleState = newState;
        engine.setState(CombatEngine.BattleState.valueOf(newState.name()));
    }

    public void handleCardPlay(Card card, VBox cardBox) {
        if (!engine.canPlayerAct()) return;
        activeCardBox = cardBox;
        engine.playCard(card);
    }

    public void handleHandReroll() {
        engine.rerollHand();
    }

    void drawMissingHandCardsIfPlayerTurn() {
        engine.drawMissingHandCardsIfPlayerTurn();
    }

    public void handleEndTurn() {
        engine.endTurn();
    }

    void handleEnemyDeath() {
        engine.resolveCurrentEnemyDeath();
    }

    void startNewTurn() {
        engine.startNewTurn();
    }
}
