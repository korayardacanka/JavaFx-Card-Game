package com.koray;

/**
 * Interface for card visual design (Decorator pattern root).
 * Implementations and decorators provide background and border
 * colors that are read by the UI when rendering a card.
 *
 * Decorator chain:
 *   BaseDesign → CardTypeDecorator → LevelDesignDecorator
 */
public interface CardDesign {
    /** Returns the CSS-compatible background color string (e.g. "#FFCCCC"). */
    String getBackground();

    /** Returns the CSS-compatible border color string (e.g. "#DAA520"). */
    String getBorder();
}