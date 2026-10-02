package com.koray.combat;

import com.koray.cards.Card;
import com.koray.cards.CardFactory;
import com.koray.core.DeckManager;
import com.koray.core.Game;
import com.koray.enemies.Enemy;
import com.koray.enemies.EnemyFactory;
import com.koray.events.EnemyDeathEvent;
import com.koray.relics.RelicFactory;
import com.koray.relics.RelicItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Owns combat rules and state without depending on JavaFX. */
public final class CombatEngine {

    public static final int HAND_REROLL_COST = 10;

    public enum BattleState {
        PLAYER_TURN,
        ENEMY_TURN,
        ANIMATING,
        GAME_OVER
    }

    private final Game game;
    private final DeckManager deckManager;
    private final CombatListener listener;
    private final Random random;
    private BattleState state = BattleState.PLAYER_TURN;

    public CombatEngine(Game game, DeckManager deckManager, CombatListener listener) {
        this(game, deckManager, listener, new Random());
    }

    public CombatEngine(Game game, DeckManager deckManager, CombatListener listener,
                        Random random) {
        this.game = game;
        this.deckManager = deckManager;
        this.listener = listener == null ? new CombatListener() {} : listener;
        this.random = random;
    }

    public BattleState getState() { return state; }

    public boolean canPlayerAct() {
        return state == BattleState.PLAYER_TURN && game.getPlayer().isAlive();
    }

    public void playCard(Card card) {
        if (!canPlayerAct() || !game.getPlayer().spendEnergy(card.getCost())) return;

        setState(BattleState.ANIMATING);
        int enemyHpBefore = game.getEnemy().getHp();
        card.use(game.getPlayer(), game.getEnemy());
        game.getPlayer().moveHandCardToDiscard(card);

        int dealtDamage = enemyHpBefore - game.getEnemy().getHp();
        if (dealtDamage > 0) {
            listener.onEnemyHit();
            DamagePipeline.resolve(game, dealtDamage);
        }

        listener.onPlayerAttack(card, this::finishCardPlay);
    }

    private void finishCardPlay() {
        if (!game.getEnemy().isAlive()) {
            notifyEnemyDeath(false, false);
        } else {
            setState(BattleState.PLAYER_TURN);
            listener.onUpdate();
        }
    }

    public void endTurn() {
        if (!canPlayerAct()) return;

        setState(BattleState.ENEMY_TURN);
        listener.onTurnEnd();
        int enemyHpBeforeStatus = game.getEnemy().getHp();
        String statusLog = game.getEnemy().processStatusEffects();
        if (!statusLog.isEmpty()) {
            game.getEventLog().set(statusLog);
        }

        if (!game.getEnemy().isAlive()) {
            listener.onUpdate();
            notifyEnemyDeath(false, true);
            return;
        }

        if (game.getEnemy().getHp() < enemyHpBeforeStatus) {
            setState(BattleState.ANIMATING);
            listener.onEnemyStatusHit(this::resolveEnemyAttack);
        } else {
            resolveEnemyAttack();
        }
    }

    private void resolveEnemyAttack() {
        boolean enemyWasFrozen = game.getEnemy().isFrozen();
        int playerHpBefore = game.getPlayer().getHp();
        game.getEnemy().attack(game.getPlayer());
        int damageTaken = playerHpBefore - game.getPlayer().getHp();

        if (damageTaken > 0) {
            for (RelicItem relic : game.getOwnedRelics()) {
                relic.onDamageTaken(game.getPlayer(), game.getEnemy(), game, damageTaken);
            }
        }

        if (!game.getEnemy().isAlive()) {
            listener.onUpdate();
            if (enemyWasFrozen) {
                notifyEnemyDeath(true, true);
            } else {
                listener.onEnemyAttack(() -> notifyEnemyDeath(true, true));
            }
            return;
        }

        Runnable finishTurn = () -> {
            if (damageTaken > 0) {
                listener.onPlayerHurt(this::finishTurnAfterHurt);
            } else {
                finishTurnAfterHurt();
            }
        };
        if (enemyWasFrozen) {
            finishTurn.run();
        } else {
            listener.onEnemyAttack(finishTurn);
        }
        listener.onUpdate();
    }

    private void finishTurnAfterHurt() {
        if (!game.getPlayer().isAlive()) {
            finishPlayerDeath();
        } else {
            startNewTurn();
        }
    }

    public void rerollHand() {
        if (!canPlayerAct()) return;
        if (game.getPlayer().getHand().isEmpty()) {
            listener.onLog("There are no cards in hand to reroll.");
            return;
        }
        if (!game.getPlayer().spendGold(HAND_REROLL_COST)) {
            listener.onLog("Not enough gold to reroll your hand ("
                + HAND_REROLL_COST + " gold required).");
            return;
        }

        int cardsToDraw = game.getPlayer().getHand().size();
        List<Card> oldHand = new ArrayList<>(game.getPlayer().getHand());
        game.getPlayer().clearHand();
        for (int i = 0; i < cardsToDraw; i++) {
            deckManager.drawSingleCard();
        }
        game.getPlayer().moveHandToDiscard(oldHand);
        listener.onUpdate();
        listener.onLog("Hand rerolled for " + HAND_REROLL_COST + " gold.");
    }

    public void drawMissingHandCardsIfPlayerTurn() {
        if (state != BattleState.PLAYER_TURN) return;
        int cardsToDraw = game.getHandSizeLimit() - game.getPlayer().getHand().size();
        for (int i = 0; i < cardsToDraw; i++) {
            deckManager.drawSingleCard();
        }
    }

    public void startNewTurn() {
        if (!game.getPlayer().isAlive()) {
            finishPlayerDeath();
            return;
        }
        setState(BattleState.PLAYER_TURN);
        game.getPlayer().restoreEnergy(game.getMaxEnergy());
        game.getEventLog().clear();
        for (RelicItem relic : game.getOwnedRelics()) {
            relic.applyPassive(game.getPlayer(), game);
        }
        drawMissingHandCardsIfPlayerTurn();
        listener.onUpdate();
        listener.onNewTurn();
        listener.onLog("New turn started.");
    }

    private void notifyEnemyDeath(boolean enemyWasAttacking, boolean startNewTurn) {
        if (state == BattleState.GAME_OVER) return;
        if (!game.getPlayer().isAlive()) {
            finishPlayerDeath();
            return;
        }
        if (game.getEnemy().isAlive()) return;

        setState(BattleState.ANIMATING);
        Enemy dead = game.getEnemy();
        listener.onEnemyDeath(dead, () -> finishEnemyDeath(dead, startNewTurn));
    }

    void resolveCurrentEnemyDeath() {
        notifyEnemyDeath(false, false);
    }

    private void finishEnemyDeath(Enemy dead, boolean startTurn) {
        if (!game.getPlayer().isAlive()) {
            finishPlayerDeath();
            return;
        }
        game.getEventBus().publish(new EnemyDeathEvent(dead));
        game.advanceLevel();
        game.setEnemy(EnemyFactory.createEnemy(game.getLevel()));
        if (game.getLevel() % 2 == 0) {
            game.increaseMaxEnergy(1);
        }
        game.setCurrentShopCards(
            CardFactory.shopCards(game.getLevel(), game.getPlayer(), random));
        if (dead.isBoss()) {
            game.setCurrentBossRelics(
                RelicFactory.bossRelics(game.getLevel(), game.getOwnedRelics(), random));
        } else {
            game.clearCurrentBossRelics();
        }
        setState(BattleState.PLAYER_TURN);
        listener.onEnemyDefeated();
        if (startTurn) {
            startNewTurn();
        }
    }

    private void finishPlayerDeath() {
        if (game.getPlayer().isAlive() || state == BattleState.GAME_OVER) return;
        setState(BattleState.GAME_OVER);
        listener.onPlayerDeath();
    }

    void setState(BattleState newState) {
        state = newState;
        listener.onStateChanged(newState);
    }
}


