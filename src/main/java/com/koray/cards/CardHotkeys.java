package com.koray.cards;

import javafx.scene.input.KeyCode;

/** Maps number-row and numpad keys to zero-based hand indexes. */
public final class CardHotkeys {

    private CardHotkeys() {}

    public static int handIndexFor(KeyCode keyCode) {
        return switch (keyCode) {
            case DIGIT1, NUMPAD1 -> 0;
            case DIGIT2, NUMPAD2 -> 1;
            case DIGIT3, NUMPAD3 -> 2;
            case DIGIT4, NUMPAD4 -> 3;
            case DIGIT5, NUMPAD5 -> 4;
            case DIGIT6, NUMPAD6 -> 5;
            case DIGIT7, NUMPAD7 -> 6;
            default -> -1;
        };
    }
}


