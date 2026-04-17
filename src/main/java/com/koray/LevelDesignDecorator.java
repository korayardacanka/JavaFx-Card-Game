package com.koray;

public class LevelDesignDecorator extends DesignDecorator {

    private int level;

    public LevelDesignDecorator(CardDesign wrapped, int level) {
        super(wrapped);  // ← DesignDecorator'ın constructor'ı
        this.level = level;
    }

    public String getBackground() { return wrapped.getBackground(); }

    public String getBorder() {
        return switch (level) {
            case 1 -> "#AAAAAA";
            case 2 -> "#DAA520";
            default -> "#9B30FF";
        };
    }
}
