package com.koray;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.BeforeClass;
import org.junit.Test;

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
        game.player.restoreEnergy(3);
        game.enemy = EnemyFactory.createEnemy(1);
        game.eventBus = new EventBus();

        Card attack = CardFactory.make("Strike", 1, 10, 1, new DamageEffect(15));
        game.player.hand.add(attack);
        BattleController controller = createController(game, () -> {});
        controller.setState(BattleController.BattleState.ENEMY_TURN);

        controller.handleCardPlay(attack, new VBox());

        assertEquals(3, game.player.getEnergy());
        assertEquals(1, game.player.hand.size());
        assertTrue(game.player.hand.contains(attack));
    }

    @Test
    public void handRerollCostsTenGoldAndReplacesHandWithSameNumberOfCards() {
        Game game = new Game();
        game.enemy = EnemyFactory.createEnemy(1);
        game.eventBus = new EventBus();
        for (int i = 0; i < 4; i++) {
            game.player.hand.add(CardFactory.make("Old " + i, 1, 1, 1, new DamageEffect(1)));
            game.player.deck.add(CardFactory.make("New " + i, 1, 1, 1, new DamageEffect(1)));
        }
        java.util.List<Card> oldHand = new java.util.ArrayList<>(game.player.hand);
        BattleController controller = createController(game, () -> {});

        controller.handleHandReroll();

        assertEquals(40, game.player.getGold());
        assertEquals(4, game.player.hand.size());
        assertEquals(4, game.player.discard.size());
        assertTrue(game.player.discard.containsAll(oldHand));
        for (Card card : oldHand) {
            assertTrue(!game.player.hand.contains(card));
        }
    }

    @Test
    public void handRerollDoesNothingWithoutEnoughGold() {
        Game game = new Game();
        game.enemy = EnemyFactory.createEnemy(1);
        game.eventBus = new EventBus();
        while (game.player.spendGold(1)) {}
        Card card = CardFactory.make("Strike", 1, 1, 1, new DamageEffect(1));
        game.player.hand.add(card);
        BattleController controller = createController(game, () -> {});

        controller.handleHandReroll();

        assertEquals(0, game.player.getGold());
        assertEquals(1, game.player.hand.size());
        assertTrue(game.player.hand.contains(card));
        assertTrue(game.player.discard.isEmpty());
    }

    @Test
    public void handRerollDoesNotShuffleTheOldHandBackIntoASmallDeck() {
        Game game = new Game();
        game.enemy = EnemyFactory.createEnemy(1);
        game.eventBus = new EventBus();
        Card oldCard = CardFactory.make("Old", 1, 1, 1, new DamageEffect(1));
        Card newCard = CardFactory.make("New", 1, 1, 1, new DamageEffect(1));
        game.player.hand.add(oldCard);
        game.player.hand.add(CardFactory.make("Old 2", 1, 1, 1, new DamageEffect(1)));
        game.player.deck.add(newCard);
        BattleController controller = createController(game, () -> {});

        controller.handleHandReroll();

        assertEquals(1, game.player.hand.size());
        assertTrue(game.player.hand.contains(newCard));
        assertTrue(!game.player.hand.contains(oldCard));
        assertEquals(2, game.player.discard.size());
        assertTrue(game.player.discard.contains(oldCard));
    }

    @Test
    public void handUpgradeDrawsMissingCardsImmediatelyOnlyDuringPlayerTurn() {
        Game game = new Game();
        game.enemy = EnemyFactory.createEnemy(1);
        game.eventBus = new EventBus();
        game.player.addGold(100);
        for (int i = 0; i < game.getHandSizeLimit(); i++) {
            game.player.hand.add(CardFactory.make("Hand " + i, 1, 1, 1,
                new DamageEffect(1)));
        }
        Card extraCard = CardFactory.make("Extra", 1, 1, 1, new DamageEffect(1));
        game.player.deck.add(extraCard);
        BattleController controller = createController(game, () -> {});

        assertTrue(game.purchaseHandSizeUpgrade());
        controller.setState(BattleController.BattleState.ENEMY_TURN);
        controller.drawMissingHandCardsIfPlayerTurn();
        assertEquals(4, game.player.hand.size());

        controller.setState(BattleController.BattleState.PLAYER_TURN);
        controller.drawMissingHandCardsIfPlayerTurn();

        assertEquals(5, game.player.hand.size());
        assertTrue(game.player.hand.contains(extraCard));
    }

    @Test
    public void enemyDeathResetsCombatFlowBackToPlayerTurn() throws Exception {
        Game game = new Game();
        game.player.restoreEnergy(3);
        game.enemy = EnemyFactory.createEnemy(1);
        game.eventBus = new EventBus();
        BattleController controller = createController(game, () -> {});
        controller.setState(BattleController.BattleState.ANIMATING);

        game.enemy.takeDamage(game.enemy.getMaxHp());
        java.lang.reflect.Method handleEnemyDeath = BattleController.class.getDeclaredMethod("handleEnemyDeath");
        handleEnemyDeath.setAccessible(true);
        handleEnemyDeath.invoke(controller);

        assertEquals(BattleController.BattleState.PLAYER_TURN, controller.getState());
    }

    @Test
    public void deadPlayerCannotStartANewTurn() throws Exception {
        Game game = new Game();
        game.player.takeDamage(100);
        game.player.restoreEnergy(0);
        game.enemy = EnemyFactory.createEnemy(1);
        BattleController controller = createController(game, () -> {});

        java.lang.reflect.Method startNewTurn = BattleController.class.getDeclaredMethod("startNewTurn");
        startNewTurn.setAccessible(true);
        startNewTurn.invoke(controller);

        assertEquals(BattleController.BattleState.GAME_OVER, controller.getState());
        assertEquals(0, game.player.getEnergy());
    }

    @Test
    public void deadPlayerDoesNotProcessEnemyRewards() throws Exception {
        Game game = new Game();
        game.player.takeDamage(100);
        game.enemy = EnemyFactory.createEnemy(1);
        game.enemy.takeDamage(game.enemy.getMaxHp());
        int[] enemyDeathCallbacks = {0};
        BattleController controller = createController(game, () -> enemyDeathCallbacks[0]++);

        java.lang.reflect.Method handleEnemyDeath = BattleController.class.getDeclaredMethod("handleEnemyDeath");
        handleEnemyDeath.setAccessible(true);
        handleEnemyDeath.invoke(controller);

        assertEquals(BattleController.BattleState.GAME_OVER, controller.getState());
        assertEquals(1, game.level);
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
}