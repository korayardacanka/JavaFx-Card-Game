package com.koray;

import java.util.*;

/**
 * Central game state container.
 * Holds all mutable state shared between the game logic and the UI:
 * the player, the current enemy, the level counter, relics, shop contents,
 * and the EventBus used to decouple subsystems.
 *
 * This class intentionally contains no logic — it is a plain data object.
 * Combat flow and turn rules live in BattleController; other rules are
 * implemented by the relevant domain classes.
 */
public class Game {

    /** The player character — stats, deck, hand, discard pile. */
    Player player    = new Player();

    /** The current enemy the player is fighting. Replaced on enemy death. */
    Enemy  enemy     = new Enemy();

    /** Current game level (increments each time an enemy is defeated). */
    int    level     = 1;

    /** Event messages shown in the UI log. */
    final EventLog eventLog = new EventLog();

    /** Event bus for decoupled communication between game subsystems. */
    public EventBus eventBus;

    /** Maximum energy the player restores at the start of each turn. */
    int maxEnergy = 3;

    /** Number of permanent hand-size upgrades purchased (maximum 3). */
    private int handSizeUpgradeLevel;

    /** Cards currently available for purchase in the shop. */
    List<Card>      currentShopCards  = new ArrayList<>();

    /** Boss-reward relics available after a boss kill (cleared after shop closes). */
    List<RelicItem> currentBossRelics = new ArrayList<>();

    /** All relics the player has purchased and owns. */
    List<RelicItem> ownedRelics       = new ArrayList<>();

    public int getHandSizeLimit() {
        return UIConstants.INITIAL_HAND_SIZE + handSizeUpgradeLevel;
    }

    public int getHandSizeUpgradeLevel() {
        return handSizeUpgradeLevel;
    }

    public int getNextHandSizeUpgradeCost() {
        if (handSizeUpgradeLevel >= 3) {
            return -1;
        }
        return (handSizeUpgradeLevel + 1) * 100;
    }

    public boolean purchaseHandSizeUpgrade() {
        int cost = getNextHandSizeUpgradeCost();
        if (cost < 0 || !player.spendGold(cost)) {
            return false;
        }
        handSizeUpgradeLevel++;
        return true;
    }

    public RunSummary createRunSummary() {
        return new RunSummary(
            level,
            player.getGold(),
            ownedRelics.stream().map(relic -> relic.name).toList(),
            player.deck.size() + player.hand.size() + player.discard.size()
        );
    }
}