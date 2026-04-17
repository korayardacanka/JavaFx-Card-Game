package com.koray;

public class BossBorderDecorator extends EnemyDesignDecorator {
    private int level;

    public BossBorderDecorator(EnemyDesign wrapped, int level) {
        super(wrapped);
        this.level = level;
    }

    @Override
    public String getBorderStyle() {
        String color = (level % 10 == 0) ? "red" : "purple"; // 10'un katlarında kırmızı, 5'te mor
        return "-fx-border-color: " + color + "; -fx-border-width: 5; -fx-border-radius: 50%;";
    }

    @Override
    public String getEffect() {
        String color = (level % 10 == 0) ? "rgba(255,0,0,0.8)" : "rgba(128,0,128,0.8)";
        return "-fx-effect: dropshadow(gaussian, " + color + ", 20, 0.5, 0, 0);";
    }
}
