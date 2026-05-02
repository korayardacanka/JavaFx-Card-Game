package com.koray;

/**
 * Decorator that adds a glowing border to boss enemies.
 * Wraps a base EnemyDesign and overrides its border and shadow
 * based on the current level:
 *   - multiples of 10 → red border + red glow (major boss)
 *   - multiples of  5 → purple border + purple glow (mini-boss)
 */
public class BossBorderDecorator extends EnemyDesignDecorator {

    /** The game level used to decide the border color. */
    private int level;

    /**
     * @param wrapped the base design to decorate
     * @param level   the current game level
     */
    public BossBorderDecorator(EnemyDesign wrapped, int level) {
        super(wrapped);
        this.level = level;
    }

    /**
     * Returns a thick circular border:
     * red at level multiples of 10, purple otherwise.
     */
    @Override
    public String getBorderStyle() {
        String color = (level % 10 == 0) ? "red" : "purple";
        return "-fx-border-color: " + color + "; -fx-border-width: 5; -fx-border-radius: 50%;";
    }

    /**
     * Returns a Gaussian drop-shadow glow matching the border color.
     */
    @Override
    public String getEffect() {
        String color = (level % 10 == 0) ? "rgba(255,0,0,0.8)" : "rgba(128,0,128,0.8)";
        return "-fx-effect: dropshadow(gaussian, " + color + ", 20, 0.5, 0, 0);";
    }
}