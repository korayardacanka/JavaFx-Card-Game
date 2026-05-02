package com.koray;
 
/**
 * Default (base) card design.
 * Provides white background and black border as the starting point
 * for the Decorator chain (CardTypeDecorator, LevelDesignDecorator).
 */
public class BaseDesign implements CardDesign {
    /** Returns the default white background color. */
    public String getBackground() { return "#ffffff"; }
 
    /** Returns the default black border color. */
    public String getBorder()     { return "#000000"; }
}
 