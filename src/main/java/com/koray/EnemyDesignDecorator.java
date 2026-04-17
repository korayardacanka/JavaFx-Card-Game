package com.koray;

public abstract class EnemyDesignDecorator implements EnemyDesign {
    protected EnemyDesign wrapped;
    public EnemyDesignDecorator(EnemyDesign wrapped) { this.wrapped = wrapped; }
}
