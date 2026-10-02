package com.koray;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CardEffectTest {

    @Test
    public void describesEachEffectsMagnitude() {
        assertEquals("Deal 15 damage", new DamageEffect(15).describe());
        assertEquals("Heal 10 HP", new HealEffect(10).describe());
        assertEquals("Gain 10 shield", new ShieldEffect(10).describe());
        assertEquals("Apply 3 poison", new PoisonEffect(3).describe());
        assertEquals("Apply 8 burn", new BurnEffect(8).describe());
        assertEquals("Freeze for 1 turn", new FreezeEffect(1).describe());
        assertEquals("Freeze for 2 turns", new FreezeEffect(2).describe());
    }
}
