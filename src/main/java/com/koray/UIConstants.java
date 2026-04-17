package com.koray;

/**
 * Constants for UI styling and layout.
 */
public class UIConstants {
    // Animation constants
    public static final int ANIMATION_FRAME_MS = 100;
    public static final int IDLE_FRAME_COUNT = 7;
    public static final int ATTACK_FRAME_COUNT = 8;
    public static final int HURT_FRAME_COUNT = 7;
    public static final int DEATH_FRAME_COUNT = 7;
    
    // Size constants
    public static final double SHIELD_MAX = 50.0;
    public static final double PLAYER_VIEW_WIDTH = 200;
    public static final double ENEMY_SIZE = 180;
    public static final double PROGRESS_BAR_WIDTH = 200;
    
    // Game constants
    public static final int INITIAL_HAND_SIZE = 4;
    public static final int INITIAL_DECK_COPIES = 3;
    public static final int HAND_BOX_SPACING = 10;
    public static final int CARD_SIZE_WIDTH = 120;
    public static final int CARD_SIZE_HEIGHT = 160;
    
    // Style constants
    public static final String STYLE_CENTER_PADDING = "-fx-alignment:center; -fx-padding:200;";
    public static final String STYLE_TITLE_LARGE = "-fx-font-size: 48px; -fx-font-weight: bold;";
    public static final String STYLE_BUTTON_LARGE = "-fx-font-size: 24px; -fx-padding: 10 30;";
    public static final String STYLE_CENTER_ALIGNMENT = "-fx-alignment:center;";
    public static final String STYLE_DARK_BG = "-fx-background-color:#1a1a1a;";
    public static final String STYLE_RED_TEXT = "-fx-text-fill: red;";
    public static final String STYLE_HP_BAR = "-fx-accent: red;";
    public static final String STYLE_SHIELD_BAR = "-fx-accent: blue;";
    public static final String STYLE_TOP_PANEL = "-fx-alignment:center; -fx-padding:10;";
    public static final String STYLE_SIDE_PANEL = "-fx-padding:20;";
}
