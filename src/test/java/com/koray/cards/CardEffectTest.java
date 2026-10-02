package com.koray.cards;

import com.koray.ui.BaseDesign;
import com.koray.ui.CardTypeDecorator;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void exposesTheExistingIconAndCardBackgroundForEveryEffect() {
        assertVisual(new DamageEffect(15), "⚔ ", "#FFCCCC");
        assertVisual(new HealEffect(10), "💚 ", "#CCFFCC");
        assertVisual(new ShieldEffect(10), "🛡 ", "#CCE5FF");
        assertVisual(new PoisonEffect(3), "☠ ", "#D8FFD8");
        assertVisual(new BurnEffect(8), "🔥 ", "#FFE0B2");
        assertVisual(new FreezeEffect(1), "❄ ", "#E0F4FF");

        assertTrue(new DamageEffect(1).isDirectDamage());
        assertFalse(new HealEffect(1).isDirectDamage());
        assertFalse(new ShieldEffect(1).isDirectDamage());
        assertFalse(new PoisonEffect(1).isDirectDamage());
        assertFalse(new BurnEffect(1).isDirectDamage());
        assertFalse(new FreezeEffect(1).isDirectDamage());
    }

    private static void assertVisual(CardEffect effect, String icon, String color) {
        assertFalse(effect.icon().isEmpty());
        assertFalse(effect.color().isEmpty());
        assertEquals(icon, effect.icon());
        assertEquals(color, effect.color());
        assertEquals(color, new CardTypeDecorator(new BaseDesign(), effect).getBackground());
    }
}


