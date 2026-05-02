package com.koray;

/**
 * Interface for enemy visual design (Decorator pattern root).
 * Implementations return JavaFX inline CSS strings that are applied
 * directly to the enemy's container node in the UI.
 *
 * Decorator chain:
 *   BaseEnemyDesign → BossBorderDecorator (boss enemies only)
 */
public interface EnemyDesign {
    /** Returns the JavaFX CSS border style string (e.g. "-fx-border-color: red;"). */
    String getBorderStyle();

    /** Returns the JavaFX CSS effect string (e.g. a dropshadow glow). */
    String getEffect();
}