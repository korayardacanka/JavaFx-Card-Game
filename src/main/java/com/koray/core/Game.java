package com.koray.core;

import com.koray.cards.Card;
import com.koray.enemies.Enemy;
import com.koray.events.EventBus;
import com.koray.relics.RelicItem;
import com.koray.ui.UIConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central game state container.
 * Holds all mutable state shared between the game logic and the UI:
 * the player, the current enemy, the level counter, relics, shop contents,
 * and the EventBus used to decouple subsystems.
 *
 * State is exposed through getters and purpose-specific mutation methods.
 * Combat flow and turn rules live in CombatEngine; other rules are implemented
 * by the relevant domain classes.
 */
public class Game {

    /** The player character — stats, deck, hand, discard pile. */
    private final Player player = new Player();

    /** The current enemy the player is fighting. Replaced on enemy death. */
    private Enemy enemy = new Enemy();

    /** Current game level (increments each time an enemy is defeated). */
    private int level = 1;

    /** Event messages shown in the UI log. */
    private final EventLog eventLog = new EventLog();

    /** Event bus for decoupled communication between game subsystems. */
    private EventBus eventBus;

    /** Maximum energy the player restores at the start of each turn. */
    private int maxEnergy = 3;

    /** Number of permanent hand-size upgrades purchased (maximum 3). */
    private int handSizeUpgradeLevel;

    /** Cards currently available for purchase in the shop. */
    private List<Card> currentShopCards = new ArrayList<>();

    /** Boss-reward relics available after a boss kill (cleared after shop closes). */
    private final List<RelicItem> currentBossRelics = new ArrayList<>();

    /** All relics the player has purchased and owns. */
    private final List<RelicItem> ownedRelics = new ArrayList<>();

    public Player getPlayer() { return player; }
    public Enemy getEnemy() { return enemy; }
    public int getLevel() { return level; }
    public int getMaxEnergy() { return maxEnergy; }
    public EventBus getEventBus() { return eventBus; }
    public EventLog getEventLog() { return eventLog; }
    public List<Card> getCurrentShopCards() {
        return Collections.unmodifiableList(currentShopCards);
    }
    public List<RelicItem> getCurrentBossRelics() {
        return Collections.unmodifiableList(currentBossRelics);
    }
    public List<RelicItem> getOwnedRelics() {
        return Collections.unmodifiableList(ownedRelics);
    }

    public void setEnemy(Enemy enemy) { this.enemy = enemy; }
    public void setEventBus(EventBus eventBus) { this.eventBus = eventBus; }
    public void advanceLevel() { level++; }
    public void increaseMaxEnergy(int amount) { maxEnergy += amount; }
    public void setCurrentShopCards(List<Card> cards) {
        currentShopCards = new ArrayList<>(cards);
    }
    public void setCurrentBossRelics(List<RelicItem> relics) {
        currentBossRelics.clear();
        currentBossRelics.addAll(relics);
    }
    public void addCurrentBossRelic(RelicItem relic) { currentBossRelics.add(relic); }
    public void clearCurrentBossRelics() { currentBossRelics.clear(); }
    public void addOwnedRelic(RelicItem relic) { ownedRelics.add(relic); }

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
            player.getTotalCardCount()
        );
    }
}

