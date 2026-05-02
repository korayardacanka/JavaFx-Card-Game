package com.koray;

/**
 * Central repository for all UI-related constants.
 * Keeping these values in one place makes it easy to tweak
 * layout, animation timing, and styling without hunting
 * through individual UI classes.
 *
 * Categories:
 *   - Animation  : frame timing and frame counts per animation state
 *   - Sizes      : fixed widths/heights for UI components
 *   - Game       : initial deck and hand configuration
 *   - Styles     : reusable JavaFX inline CSS strings
 */
public class UIConstants {

    // ── Animation ─────────────────────────────────────────────────────────
    /** Duration of each animation frame in milliseconds. */
    public static final int ANIMATION_FRAME_MS  = 100;

    /** Number of frames in the idle animation loop. */
    public static final int IDLE_FRAME_COUNT    = 7;

    /** Number of frames in the attack animation. */
    public static final int ATTACK_FRAME_COUNT  = 8;

    /** Number of frames in the hurt (take-damage) animation. */
    public static final int HURT_FRAME_COUNT    = 7;

    /** Number of frames in the death animation. */
    public static final int DEATH_FRAME_COUNT   = 7;

    // ── Sizes ─────────────────────────────────────────────────────────────
    /** Maximum shield value used to normalise the shield progress bar (0–1). */
    public static final double SHIELD_MAX         = 50.0;

    /** Preferred width of the player sprite ImageView. */
    public static final double PLAYER_VIEW_WIDTH  = 200;

    /** Preferred size (width and height) of the enemy container pane. */
    public static final double ENEMY_SIZE         = 180;

    /** Preferred width of the HP and shield progress bars. */
    public static final double PROGRESS_BAR_WIDTH = 200;

    // ── Game ──────────────────────────────────────────────────────────────
    /** Number of cards drawn into the player's hand at the start of each turn. */
    public static final int INITIAL_HAND_SIZE   = 4;

    /** How many copies of each starter card are added to the initial deck. */
    public static final int INITIAL_DECK_COPIES = 3;

    /** Horizontal spacing between cards in the hand HBox. */
    public static final int HAND_BOX_SPACING    = 10;

    /** Width of each card VBox in pixels. */
    public static final int CARD_SIZE_WIDTH     = 120;

    /** Height of each card VBox in pixels. */
    public static final int CARD_SIZE_HEIGHT    = 160;

    // ── Styles ────────────────────────────────────────────────────────────
    /** Centered layout with generous padding — used for start/death screens. */
    public static final String STYLE_CENTER_PADDING    = "-fx-alignment:center; -fx-padding:200;";

    /** Large bold title text — used for screen headings. */
    public static final String STYLE_TITLE_LARGE       = "-fx-font-size: 48px; -fx-font-weight: bold;";

    /** Large padded button — used for primary action buttons on menu screens. */
    public static final String STYLE_BUTTON_LARGE      = "-fx-font-size: 24px; -fx-padding: 10 30;";

    /** Horizontally centered alignment. */
    public static final String STYLE_CENTER_ALIGNMENT  = "-fx-alignment:center;";

    /** Very dark background color — used for the death screen. */
    public static final String STYLE_DARK_BG           = "-fx-background-color:#1a1a1a;";

    /** Red text fill — used for the "YOU DIED" heading. */
    public static final String STYLE_RED_TEXT          = "-fx-text-fill: red;";

    /** Red accent on the HP progress bar. */
    public static final String STYLE_HP_BAR            = "-fx-accent: red;";

    /** Blue accent on the shield progress bar. */
    public static final String STYLE_SHIELD_BAR        = "-fx-accent: blue;";

    /** Centered alignment with vertical padding — used for the top HUD panel. */
    public static final String STYLE_TOP_PANEL         = "-fx-alignment:center; -fx-padding:10;";

    /** Side padding — used for left/right info panels. */
    public static final String STYLE_SIDE_PANEL        = "-fx-padding:20;";
}