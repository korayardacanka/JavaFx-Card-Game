package com.koray;

public class LevelDesignDecorator extends DesignDecorator {

    
private int tier;
    public LevelDesignDecorator(CardDesign wrapped, int level) {
        super(wrapped);  // ← DesignDecorator'ın constructor'ı
        this.tier = CardFactory.tierForLevel(level);
    }

    public String getBackground() { return wrapped.getBackground(); }

    public String getBorder() {
        return switch (tier) {
            case 0 -> "#AAAAAA";
            case 1 -> "#DAA520";
            default -> "#9B30FF";
        };
    }
}
