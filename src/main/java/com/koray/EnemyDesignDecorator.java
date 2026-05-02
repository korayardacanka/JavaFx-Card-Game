package com.koray;

/**
 * Abstract base class for enemy design decorators (Decorator pattern).
 * Holds a reference to the inner EnemyDesign so that concrete decorators
 * (e.g. BossBorderDecorator) can override specific visual properties
 * while delegating unchanged ones to the wrapped instance.
 */
public abstract class EnemyDesignDecorator implements EnemyDesign {

    /** The inner design being decorated. */
    protected EnemyDesign wrapped;

    /**
     * @param wrapped the EnemyDesign to wrap — must not be null
     */
    public EnemyDesignDecorator(EnemyDesign wrapped) {
        this.wrapped = wrapped;
    }
}