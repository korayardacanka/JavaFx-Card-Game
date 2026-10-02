package com.koray.simulation;

import com.koray.cards.Card;
import com.koray.cards.CardFactory;
import com.koray.cards.DamageEffect;
import com.koray.combat.CombatEngine;
import com.koray.combat.CombatListener;
import com.koray.core.DeckManager;
import com.koray.core.Game;
import com.koray.core.RewardSystem;
import com.koray.enemies.Enemy;
import com.koray.enemies.EnemyFactory;
import com.koray.events.EventBus;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HeadlessSimulatorTest {

    @Test
    public void simulationIsReproducibleAndReportsEveryGameExactlyOnceAsADeath() {
        HeadlessSimulator.Result first = HeadlessSimulator.simulate(25, 1234L);
        HeadlessSimulator.Result second = HeadlessSimulator.simulate(25, 1234L);

        assertEquals(first, second);
        assertEquals(25, first.levels().values().stream().mapToInt(HeadlessSimulator.LevelStats::deaths).sum());
        assertEquals(25, first.levels().get(1).entrants());
        assertTrue(first.highestLevelReached() >= 1);
        assertTrue(first.levels().containsKey(first.highestLevelReached()));
    }

    @Test
    public void playerTurnUsesCombatEngineToResolveCardAndEnemyDeath() {
        Game game = new Game();
        game.setEventBus(new EventBus());
        game.getEventBus().subscribe(new RewardSystem(game));
        Enemy enemy = EnemyFactory.createEnemy(1);
        enemy.takeDamage(enemy.getHp() - 1);
        game.setEnemy(enemy);
        Card finisher = CardFactory.make("Finisher", 1, 1, 1, new DamageEffect(1));
        game.getPlayer().addToHand(finisher);
        CombatEngine engine = new CombatEngine(game, new DeckManager(game, new Random(1)),
            new CombatListener() {}, new Random(1));

        HeadlessSimulator.playAvailableCards(game, engine);

        assertEquals(2, game.getLevel());
        assertTrue(game.getPlayer().getDiscard().contains(finisher));
        assertTrue(engine.canPlayerAct());
    }
}


