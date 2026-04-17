package com.koray;

public abstract class DesignDecorator implements CardDesign {
    protected CardDesign wrapped;
    
    public DesignDecorator(CardDesign wrapped) {
        this.wrapped = wrapped;
    }
}
