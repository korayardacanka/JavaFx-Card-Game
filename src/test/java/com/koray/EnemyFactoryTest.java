package com.koray;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EnemyFactoryTest {

    @BeforeClass
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // Toolkit already started.
        }
    }

    @Test
    public void assignsFourFamiliesAndFivePhasesThroughLevelTwenty() {
        String[] families = {
            "Kanlı Şövalye", "Bataklık Cadısı", "Kül Ejderi", "Buz Revenantı"
        };
        String[] phases = {
            "I. Faz", "II. Faz", "III. Faz", "IV. Faz", "Boss Fazı"
        };

        for (int level = 1; level <= 20; level++) {
            Enemy enemy = EnemyFactory.createEnemy(level);
            int family = (level - 1) / 5;
            int phase = (level - 1) % 5;

            assertEquals(family, enemy.getFamily());
            assertEquals(phase + 1, enemy.getPhase());
            assertEquals(phase == 4, enemy.isBoss());
            assertEquals(families[family] + " - " + phases[phase], enemy.getName());
        }
    }

    @Test
    public void statsIncreaseAcrossEveryFamilyPhase() {
        for (int firstLevel = 1; firstLevel <= 16; firstLevel += 5) {
            Enemy previous = EnemyFactory.createEnemy(firstLevel);
            for (int level = firstLevel + 1; level < firstLevel + 5; level++) {
                Enemy current = EnemyFactory.createEnemy(level);
                assertTrue(current.getMaxHp() > previous.getMaxHp());
                assertTrue(current.getAttackDamage() > previous.getAttackDamage());
                previous = current;
            }
        }
    }

    @Test
    public void handleCardPlayIgnoresInputWhileTurnLocked() throws Exception {
        Game game = new Game();
        game.player.restoreEnergy(3);
        game.enemy = EnemyFactory.createEnemy(1);
        game.eventBus = new EventBus();

        DeckManager deckManager = new DeckManager(game);
        Card attack = CardFactory.make("Strike", 1, 10, 1, new DamageEffect(15));
        game.player.hand.add(attack);

        BattleController controller = new BattleController(
            game,
            deckManager,
            new AnimationPlayer(new ImageView(), this),
            new EnemyAnimationPlayer(new StackPane()),
            () -> {},
            () -> {},
            () -> {},
            s -> {}
        );

        Field battleState = BattleController.class.getDeclaredField("battleState");
        battleState.setAccessible(true);
        battleState.set(controller, BattleController.BattleState.ENEMY_TURN);

        controller.handleCardPlay(attack, new VBox());

        assertEquals(3, game.player.getEnergy());
        assertEquals(1, game.player.hand.size());
        assertTrue(game.player.hand.contains(attack));
    }

    @Test
    public void bloodPactRequiresLowHpAndLogsTheGainCorrectly() {
        Game game = new Game();
        game.maxEnergy = 3;

        BloodPactRelic relic = new BloodPactRelic();

        game.player.takeDamage(20);
        relic.applyOnBuy(game.player, game);
        assertEquals(80, game.player.getHp());
        assertEquals(3, game.maxEnergy);
        assertTrue(game.lastEvent.contains("HP ≤ 30"));

        game.player.takeDamage(50);
        relic.applyOnBuy(game.player, game);
        assertEquals(0, game.player.getHp());
        assertEquals(5, game.maxEnergy);
        assertTrue(game.lastEvent.contains("Max Enerji +2"));
        assertTrue(!game.lastEvent.contains("Max Enerji +5"));
    }

    @Test
    public void enemyDeathResetsCombatFlowBackToPlayerTurn() throws Exception {
        Game game = new Game();
        game.player.restoreEnergy(3);
        game.enemy = EnemyFactory.createEnemy(1);
        game.eventBus = new EventBus();

        DeckManager deckManager = new DeckManager(game);
        BattleController controller = new BattleController(
            game,
            deckManager,
            new AnimationPlayer(new ImageView(), this),
            new EnemyAnimationPlayer(new StackPane()),
            () -> {},
            () -> {},
            () -> {},
            s -> {}
        );

        Field battleState = BattleController.class.getDeclaredField("battleState");
        battleState.setAccessible(true);
        battleState.set(controller, BattleController.BattleState.ANIMATING);

        game.enemy.takeDamage(game.enemy.getMaxHp());
        java.lang.reflect.Method handleEnemyDeath = BattleController.class.getDeclaredMethod("handleEnemyDeath");
        handleEnemyDeath.setAccessible(true);
        handleEnemyDeath.invoke(controller);

        assertEquals(BattleController.BattleState.PLAYER_TURN, battleState.get(controller));
    }
}