package com.koray;

/**
 * Default (base) enemy visual design.
 * Applies no border and no shadow effect — used as the wrappee
 * at the bottom of the EnemyDesign Decorator chain.
 * Boss enemies replace this with BossBorderDecorator.
 */
public class BaseEnemyDesign implements EnemyDesign {

    /** Returns a transparent border style (no visible border). */
    public String getBorderStyle() { return "-fx-border-color: transparent;"; }

    /** Returns a zero-radius drop shadow (no visible glow). */
    public String getEffect() { return "-fx-effect: dropshadow(three-pass-box, black, 0, 0, 0, 0);"; }
}