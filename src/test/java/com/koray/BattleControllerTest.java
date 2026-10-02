package com.koray;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BattleControllerTest {

    @BeforeClass
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit already started.
        }
    }

    @Test
    public void handleCardPlayIgnoresInputWhileTurnLocked() {
        Game game = new Game();
        game.getPlayer().restoreEnergy(3);
        game.setEnemy(EnemyFactory.createEnemy(1));
        game.setEventBus(new EventBus());

        Card attack = CardFactory.make("Strike", 1, 10, 1, new DamageEffect(15));
        game.getPlayer().addToHand(attack);
        BattleController controller = createController(game, () -> {});
        controller.setState(BattleController.BattleState.ENEMY_TURN);

        controller.handleCardPlay(attack, new VBox());

        assertEquals(3, game.getPlayer().getEnergy());
        assertEquals(1, game.getPlayer().getHand().size());
        assertTrue(game.getPlayer().getHand().contains(attack));
    }

    @Test
    public void handRerollCostsTenGoldAndReplacesHandWithSameNumberOfCards() {
        Game game = new Game();
        game.setEnemy(EnemyFactory.createEnemy(1));
        game.setEventBus(new EventBus());
        for (int i = 0; i < 4; i++) {
            game.getPlayer().addToHand(CardFactory.make("Old " + i, 1, 1, 1, new DamageEffect(1)));
            game.getPlayer().addToDeck(CardFactory.make("New " + i, 1, 1, 1, new DamageEffect(1)));
        }
        java.util.List<Card> oldHand = new java.util.ArrayList<>(game.getPlayer().getHand());
        BattleController controller = createController(game, () -> {});

        controller.handleHandReroll();

        assertEquals(40, game.getPlayer().getGold());
        assertEquals(4, game.getPlayer().getHand().size());
        assertEquals(4, game.getPlayer().getDiscard().size());
        assertTrue(game.getPlayer().getDiscard().containsAll(oldHand));
        for (Card card : oldHand) {
            assertTrue(!game.getPlayer().getHand().contains(card));
        }
    }

    @Test
    public void handRerollDoesNothingWithoutEnoughGold() {
        Game game = new Game();
        game.setEnemy(EnemyFactory.createEnemy(1));
        game.setEventBus(new EventBus());
        while (game.getPlayer().spendGold(1)) {}
        Card card = CardFactory.make("Strike", 1, 1, 1, new DamageEffect(1));
        game.getPlayer().addToHand(card);
        BattleController controller = createController(game, () -> {});

        controller.handleHandReroll();

        assertEquals(0, game.getPlayer().getGold());
        assertEquals(1, game.getPlayer().getHand().size());
        assertTrue(game.getPlayer().getHand().contains(card));
        assertTrue(game.getPlayer().getDiscard().isEmpty());
    }

    @Test
    public void handRerollDoesNotShuffleTheOldHandBackIntoASmallDeck() {
        Game game = new Game();
        game.setEnemy(EnemyFactory.createEnemy(1));
        game.setEventBus(new EventBus());
        Card oldCard = CardFactory.make("Old", 1, 1, 1, new DamageEffect(1));
        Card newCard = CardFactory.make("New", 1, 1, 1, new DamageEffect(1));
        game.getPlayer().addToHand(oldCard);
        game.getPlayer().addToHand(CardFactory.make("Old 2", 1, 1, 1, new DamageEffect(1)));
        game.getPlayer().addToDeck(newCard);
        BattleController controller = createController(game, () -> {});

        controller.handleHandReroll();

        assertEquals(1, game.getPlayer().getHand().size());
        assertTrue(game.getPlayer().getHand().contains(newCard));
        assertTrue(!game.getPlayer().getHand().contains(oldCard));
        assertEquals(2, game.getPlayer().getDiscard().size());
        assertTrue(game.getPlayer().getDiscard().contains(oldCard));
    }

    @Test
    public void handUpgradeDrawsMissingCardsImmediatelyOnlyDuringPlayerTurn() {
        Game game = new Game();
        game.setEnemy(EnemyFactory.createEnemy(1));
        game.setEventBus(new EventBus());
        game.getPlayer().addGold(100);
        for (int i = 0; i < game.getHandSizeLimit(); i++) {
            game.getPlayer().addToHand(CardFactory.make("Hand " + i, 1, 1, 1,
                new DamageEffect(1)));
        }
        Card extraCard = CardFactory.make("Extra", 1, 1, 1, new DamageEffect(1));
        game.getPlayer().addToDeck(extraCard);
        BattleController controller = createController(game, () -> {});

        assertTrue(game.purchaseHandSizeUpgrade());
        controller.setState(BattleController.BattleState.ENEMY_TURN);
        controller.drawMissingHandCardsIfPlayerTurn();
        assertEquals(4, game.getPlayer().getHand().size());

        controller.setState(BattleController.BattleState.PLAYER_TURN);
        controller.drawMissingHandCardsIfPlayerTurn();

        assertEquals(5, game.getPlayer().getHand().size());
        assertTrue(game.getPlayer().getHand().contains(extraCard));
    }

    @Test
    public void enemyDeathResetsCombatFlowBackToPlayerTurn() {
        Game game = new Game();
        game.getPlayer().restoreEnergy(3);
        game.setEnemy(EnemyFactory.createEnemy(1));
        game.setEventBus(new EventBus());
        CountDownLatch enemyDeathFinished = new CountDownLatch(1);
        BattleController controller = createController(game, enemyDeathFinished::countDown);
        controller.setState(BattleController.BattleState.ANIMATING);

        game.getEnemy().takeDamage(game.getEnemy().getMaxHp());
        controller.handleEnemyDeath();

        assertTrue(await(enemyDeathFinished));
        assertEquals(BattleController.BattleState.PLAYER_TURN, controller.getState());
    }

    @Test
    public void deadPlayerCannotStartANewTurn() {
        Game game = new Game();
        game.getPlayer().takeDamage(100);
        game.getPlayer().restoreEnergy(0);
        game.setEnemy(EnemyFactory.createEnemy(1));
        BattleController controller = createController(game, () -> {});

        controller.startNewTurn();

        assertEquals(BattleController.BattleState.GAME_OVER, controller.getState());
        assertEquals(0, game.getPlayer().getEnergy());
    }

    @Test
    public void deadPlayerDoesNotProcessEnemyRewards() {
        Game game = new Game();
        game.getPlayer().takeDamage(100);
        game.setEnemy(EnemyFactory.createEnemy(1));
        game.getEnemy().takeDamage(game.getEnemy().getMaxHp());
        int[] enemyDeathCallbacks = {0};
        BattleController controller = createController(game, () -> enemyDeathCallbacks[0]++);

        controller.handleEnemyDeath();

        assertEquals(BattleController.BattleState.GAME_OVER, controller.getState());
        assertEquals(1, game.getLevel());
        assertEquals(0, enemyDeathCallbacks[0]);
    }

    private BattleController createController(Game game, Runnable onEnemyDeath) {
        return new BattleController(
            game,
            new DeckManager(game),
            new AnimationPlayer(new ImageView(), this),
            new EnemyAnimationPlayer(new StackPane()),
            () -> {},
            () -> {},
            onEnemyDeath,
            s -> {}
        );
    }

    private boolean await(CountDownLatch latch) {
        try {
            return latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}