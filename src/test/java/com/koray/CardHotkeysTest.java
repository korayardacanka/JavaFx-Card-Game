package com.koray;

import javafx.scene.input.KeyCode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CardHotkeysTest {

    @Test
    public void mapsNumberKeysToZeroBasedHandIndexes() {
        assertEquals(0, CardHotkeys.handIndexFor(KeyCode.DIGIT1));
        assertEquals(6, CardHotkeys.handIndexFor(KeyCode.DIGIT7));
        assertEquals(2, CardHotkeys.handIndexFor(KeyCode.NUMPAD3));
        assertEquals(-1, CardHotkeys.handIndexFor(KeyCode.DIGIT8));
        assertEquals(-1, CardHotkeys.handIndexFor(KeyCode.E));
    }
}
