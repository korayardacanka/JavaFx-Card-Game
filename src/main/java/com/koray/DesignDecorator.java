package com.koray;

/**
 * Abstract base class for card design decorators (Decorator pattern).
 * Holds a reference to the inner (wrapped) CardDesign so that
 * concrete decorators can override only the properties they care about
 * and delegate the rest to the wrapped instance.
 *
 * Decorator chain:
 *   BaseDesign → CardTypeDecorator → LevelDesignDecorator
 */
public abstract class DesignDecorator implements CardDesign {

    /** The inner design being decorated. */
    protected CardDesign wrapped;

    /**
     * @param wrapped the CardDesign to wrap — must not be null
     */
    public DesignDecorator(CardDesign wrapped) {
        this.wrapped = wrapped;
    }
}