package com.koray;

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
}
