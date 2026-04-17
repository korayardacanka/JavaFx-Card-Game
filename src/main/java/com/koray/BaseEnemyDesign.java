package com.koray;

public class BaseEnemyDesign implements EnemyDesign {
    public String getBorderStyle() { return "-fx-border-color: transparent;"; }
    public String getEffect() { return "-fx-effect: dropshadow(three-pass-box, black, 0, 0, 0, 0);"; }
}
