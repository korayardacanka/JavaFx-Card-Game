package com.koray.combat;

import com.koray.cards.Card;
import com.koray.cards.CardFactory;
import com.koray.cards.DamageEffect;
import com.koray.core.DeckManager;
import com.koray.core.Game;
import com.koray.core.RewardSystem;
import com.koray.enemies.Enemy;
import com.koray.enemies.EnemyFactory;
import com.koray.events.EventBus;
import com.koray.relics.ThornRelic;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CombatEngineTest {

    @Test
    public void playsCardAndMovesItToDiscard() {
        Game game = newGame();
        Card card = card("Strike", 10);
        game.getPlayer().addToHand(card);
        CombatEngine engine = new CombatEngine(game, new DeckManager(game), new CombatListener() {});

        engine.playCard(card);

        assertFalse(game.getPlayer().getHand().contains(card));
        assertTrue(game.getPlayer().getDiscard().contains(card));
        assertEquals(2, game.getPlayer().getEnergy());
        assertEquals(EnemyFactory.createEnemy(1).getMaxHp() - 10,
            game.getEnemy().getHp());
        assertEquals(CombatEngine.BattleState.PLAYER_TURN, engine.getState());
    }

    @Test
    public void killingEnemyAwardsGoldAndAdvancesLevelAfterDeathCallback() {
        Game game = newGame();
        game.setEnemy(enemyWithHp(5));
        game.getPlayer().addToHand(card("Finisher", 5));
        int initialGold = game.getPlayer().getGold();
        CombatEngine engine = new CombatEngine(game, new DeckManager(game), new CombatListener() {});

        engine.playCard(game.getPlayer().getHand().get(0));

        assertEquals(2, game.getLevel());
        assertEquals(initialGold + 13, game.getPlayer().getGold());
        assertEquals(CombatEngine.BattleState.PLAYER_TURN, engine.getState());
    }

    @Test
    public void statusEffectCanKillEnemyAndAwardReward() {
        Game game = newGame();
        game.setEnemy(enemyWithHp(2));
        game.getEnemy().addPoison(3);
        int initialGold = game.getPlayer().getGold();
        CombatEngine engine = new CombatEngine(game, new DeckManager(game), new CombatListener() {});

        engine.endTurn();

        assertEquals(2, game.getLevel());
        assertEquals(initialGold + 13, game.getPlayer().getGold());
        assertEquals(CombatEngine.BattleState.PLAYER_TURN, engine.getState());
    }

    @Test
    public void enemyAttackCanKillPlayer() {
        Game game = newGame();
        game.getPlayer().takeDamage(95);
        game.setEnemy(enemyWithAttack(10));
        int[] deaths = {0};
        CombatEngine engine = new CombatEngine(game, new DeckManager(game),
            new CombatListener() {
                @Override
                public void onPlayerDeath() { deaths[0]++; }
            });

        engine.endTurn();

        assertEquals(0, game.getPlayer().getHp());
        assertEquals(CombatEngine.BattleState.GAME_OVER, engine.getState());
        assertEquals(1, deaths[0]);
    }

    @Test
    public void thornCausingBothDeathsResultsInGameOverWithoutEnemyReward() {
        Game game = newGame();
        game.getPlayer().takeDamage(99);
        game.setEnemy(enemyWithAttack(10));
        game.addOwnedRelic(new ThornRelic(game.getEnemy().getMaxHp()));
        int[] deaths = {0};
        CombatEngine engine = new CombatEngine(game, new DeckManager(game),
            new CombatListener() {
                @Override
                public void onPlayerDeath() { deaths[0]++; }
            });

        engine.endTurn();

        assertEquals(0, game.getPlayer().getHp());
        assertEquals(0, game.getEnemy().getHp());
        assertEquals(1, game.getLevel());
        assertEquals(50, game.getPlayer().getGold());
        assertEquals(CombatEngine.BattleState.GAME_OVER, engine.getState());
        assertEquals(1, deaths[0]);
    }

    @Test
    public void deadPlayerCannotStartNewTurn() {
        Game game = newGame();
        game.getPlayer().takeDamage(100);
        game.getPlayer().restoreEnergy(0);
        int[] deaths = {0};
        CombatEngine engine = new CombatEngine(game, new DeckManager(game),
            new CombatListener() {
                @Override
                public void onPlayerDeath() { deaths[0]++; }
            });

        engine.startNewTurn();

        assertEquals(0, game.getPlayer().getEnergy());
        assertEquals(CombatEngine.BattleState.GAME_OVER, engine.getState());
        assertEquals(1, deaths[0]);
    }

    private static Game newGame() {
        Game game = new Game();
        game.setEventBus(new EventBus());
        game.getEventBus().subscribe(new RewardSystem(game));
        game.setEnemy(EnemyFactory.createEnemy(1));
        return game;
    }

    private static Enemy enemyWithHp(int hp) {
        Enemy enemy = EnemyFactory.createEnemy(1);
        enemy.takeDamage(enemy.getHp() - hp);
        return enemy;
    }

    private static Enemy enemyWithAttack(int attack) {
        Enemy enemy = EnemyFactory.createEnemy(1);
        enemy.setAttackDamage(attack);
        return enemy;
    }

    private static Card card(String name, int damage) {
        return CardFactory.make(name, 1, 1, 1, new DamageEffect(damage));
    }
}


