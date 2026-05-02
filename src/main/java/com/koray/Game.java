package com.koray;

import java.util.*;

/**
 * Central game state container.
 * Holds all mutable state shared between the game logic and the UI:
 * the player, the current enemy, the level counter, relics, shop contents,
 * and the EventBus used to decouple subsystems.
 *
 * This class intentionally contains no logic — it is a plain data object.
 * All game rules live in Main (turn handling) and the relevant domain classes.
 */
public class Game {

    /** The player character — stats, deck, hand, discard pile. */
    Player player    = new Player();

    /** The current enemy the player is fighting. Replaced on enemy death. */
    Enemy  enemy     = new Enemy();

    /** Current game level (increments each time an enemy is defeated). */
    int    level     = 1;

    /** Short message describing the last notable game event, shown in the UI log. */
    String lastEvent = "";

    /** Event bus for decoupled communication between game subsystems. */
    public EventBus eventBus;

    /** Maximum energy the player restores at the start of each turn. */
    int maxEnergy = 3;

    /** Cards currently available for purchase in the shop. */
    List<Card>      currentShopCards  = new ArrayList<>();

    /** Boss-reward relics available after a boss kill (cleared after shop closes). */
    List<RelicItem> currentBossRelics = new ArrayList<>();

    /** All relics the player has purchased and owns. */
    List<RelicItem> ownedRelics       = new ArrayList<>();
}