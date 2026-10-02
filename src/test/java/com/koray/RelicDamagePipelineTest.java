package com.koray;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RelicDamagePipelineTest {

    @Test
    public void executionerAddsHalfTheBaseDamageWhenEnemyIsBelowThirtyPercentHp() {
        Game game = createGameWithEnemyAtHp(15);
        game.enemy.takeDamage(5);
        game.ownedRelics.add(new ExecutionerRelic());

        DamageContext context = DamagePipeline.resolve(game, 5);

        assertEquals(5, context.baseDamage());
        assertEquals(2, context.bonusDamage());
        assertEquals(7, context.totalDamage());
        assertEquals(8, game.enemy.getHp());
        assertEquals("🪓 +2 execute", game.eventLog.toDisplayString());
    }

    @Test
    public void vampireHealsTwentyPercentOfTotalDamage() {
        Game game = createGameWithEnemyAtHp(100);
        game.enemy.takeDamage(10);
        game.player.takeDamage(50);
        game.ownedRelics.add(new VampireRelic(0.20));

        DamageContext context = DamagePipeline.resolve(game, 10);

        assertEquals(2, context.totalDamage() * 20 / 100);
        assertEquals(52, game.player.getHp());
        assertEquals("🧛 +2 HP", game.eventLog.toDisplayString());
    }

    @Test
    public void vampireDoesNotHealForDamageBelowFive() {
        Game game = createGameWithEnemyAtHp(100);
        game.enemy.takeDamage(4);
        game.player.takeDamage(20);
        game.ownedRelics.add(new VampireRelic(0.20));

        DamageContext context = DamagePipeline.resolve(game, 4);

        assertEquals(4, context.totalDamage());
        assertEquals(80, game.player.getHp());
        assertEquals(0, game.eventLog.messages().size());
    }

    @Test
    public void thornReflectsDamageTakenAndLogsTheReflection() {
        Game game = createGameWithEnemyAtHp(40);
        ThornRelic thornRelic = new ThornRelic(4);
        game.ownedRelics.add(thornRelic);

        thornRelic.onDamageTaken(game.player, game.enemy, game, 5);

        assertEquals(36, game.enemy.getHp());
        assertEquals("🌵 +4 reflected", game.eventLog.toDisplayString());
    }

    @Test
    public void executionerAndVampireResultsDoNotDependOnRelicOrder() {
        Game executionerFirst = createCombinedDamageGame(true);
        Game vampireFirst = createCombinedDamageGame(false);

        DamageContext firstContext = resolveBaseHit(executionerFirst, 5);
        DamageContext secondContext = resolveBaseHit(vampireFirst, 5);

        assertEquals(firstContext.totalDamage(), secondContext.totalDamage());
        assertEquals(executionerFirst.enemy.getHp(), vampireFirst.enemy.getHp());
        assertEquals(executionerFirst.player.getHp(), vampireFirst.player.getHp());
        assertEquals(executionerFirst.eventLog.toDisplayString(),
            vampireFirst.eventLog.toDisplayString());
    }

    private static Game createGameWithEnemyAtHp(int hp) {
        Game game = new Game();
        game.enemy = EnemyFactory.createEnemy(1);
        game.enemy.takeDamage(game.enemy.getHp() - hp);
        return game;
    }

    private static Game createCombinedDamageGame(boolean executionerFirst) {
        Game game = createGameWithEnemyAtHp(15);
        game.player.takeDamage(50);
        if (executionerFirst) {
            game.ownedRelics.add(new ExecutionerRelic());
            game.ownedRelics.add(new VampireRelic(0.20));
        } else {
            game.ownedRelics.add(new VampireRelic(0.20));
            game.ownedRelics.add(new ExecutionerRelic());
        }
        return game;
    }

    private static DamageContext resolveBaseHit(Game game, int damage) {
        game.enemy.takeDamage(damage);
        return DamagePipeline.resolve(game, damage);
    }
}
