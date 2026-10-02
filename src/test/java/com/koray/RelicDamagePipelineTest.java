package com.koray;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RelicDamagePipelineTest {

    @Test
    public void executionerAddsHalfTheBaseDamageWhenEnemyIsBelowThirtyPercentHp() {
        Game game = createGameWithEnemyAtHp(15);
        game.getEnemy().takeDamage(5);
        game.addOwnedRelic(new ExecutionerRelic());

        DamageContext context = DamagePipeline.resolve(game, 5);

        assertEquals(5, context.baseDamage());
        assertEquals(2, context.bonusDamage());
        assertEquals(7, context.totalDamage());
        assertEquals(8, game.getEnemy().getHp());
        assertEquals("🪓 +2 execute", game.getEventLog().toDisplayString());
    }

    @Test
    public void vampireHealsTwentyPercentOfTotalDamage() {
        Game game = createGameWithEnemyAtHp(100);
        game.getEnemy().takeDamage(10);
        game.getPlayer().takeDamage(50);
        game.addOwnedRelic(new VampireRelic(0.20));

        DamageContext context = DamagePipeline.resolve(game, 10);

        assertEquals(2, context.totalDamage() * 20 / 100);
        assertEquals(52, game.getPlayer().getHp());
        assertEquals("🧛 +2 HP", game.getEventLog().toDisplayString());
    }

    @Test
    public void vampireDoesNotHealForDamageBelowFive() {
        Game game = createGameWithEnemyAtHp(100);
        game.getEnemy().takeDamage(4);
        game.getPlayer().takeDamage(20);
        game.addOwnedRelic(new VampireRelic(0.20));

        DamageContext context = DamagePipeline.resolve(game, 4);

        assertEquals(4, context.totalDamage());
        assertEquals(80, game.getPlayer().getHp());
        assertEquals(0, game.getEventLog().messages().size());
    }

    @Test
    public void thornReflectsDamageTakenAndLogsTheReflection() {
        Game game = createGameWithEnemyAtHp(40);
        ThornRelic thornRelic = new ThornRelic(4);
        game.addOwnedRelic(thornRelic);

        thornRelic.onDamageTaken(game.getPlayer(), game.getEnemy(), game, 5);

        assertEquals(36, game.getEnemy().getHp());
        assertEquals("🌵 +4 reflected", game.getEventLog().toDisplayString());
    }

    @Test
    public void executionerAndVampireResultsDoNotDependOnRelicOrder() {
        Game executionerFirst = createCombinedDamageGame(true);
        Game vampireFirst = createCombinedDamageGame(false);

        DamageContext firstContext = resolveBaseHit(executionerFirst, 5);
        DamageContext secondContext = resolveBaseHit(vampireFirst, 5);

        assertEquals(firstContext.totalDamage(), secondContext.totalDamage());
        assertEquals(executionerFirst.getEnemy().getHp(), vampireFirst.getEnemy().getHp());
        assertEquals(executionerFirst.getPlayer().getHp(), vampireFirst.getPlayer().getHp());
        assertEquals(executionerFirst.getEventLog().toDisplayString(),
            vampireFirst.getEventLog().toDisplayString());
    }

    private static Game createGameWithEnemyAtHp(int hp) {
        Game game = new Game();
        game.setEnemy(EnemyFactory.createEnemy(1));
        game.getEnemy().takeDamage(game.getEnemy().getHp() - hp);
        return game;
    }

    private static Game createCombinedDamageGame(boolean executionerFirst) {
        Game game = createGameWithEnemyAtHp(15);
        game.getPlayer().takeDamage(50);
        if (executionerFirst) {
            game.addOwnedRelic(new ExecutionerRelic());
            game.addOwnedRelic(new VampireRelic(0.20));
        } else {
            game.addOwnedRelic(new VampireRelic(0.20));
            game.addOwnedRelic(new ExecutionerRelic());
        }
        return game;
    }

    private static DamageContext resolveBaseHit(Game game, int damage) {
        game.getEnemy().takeDamage(damage);
        return DamagePipeline.resolve(game, damage);
    }
}
