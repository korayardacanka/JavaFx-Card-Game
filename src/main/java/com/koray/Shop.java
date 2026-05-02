package com.koray;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.*;

/**
 * JavaFX shop window that opens after each enemy kill.
 * Displays two sections:
 *   1. Normal cards  — available for purchase at any time
 *   2. Boss relics   — only shown after a boss kill; cleared when the window closes
 *
 * The shop is a modal-style secondary Stage. Only one instance can be
 * open at a time — opening a new one closes the previous one.
 *
 * Note: this class uses a static Stage reference (currentStage) to
 * enforce the single-window constraint. This is a known limitation
 * and would benefit from being moved to an instance-based approach.
 */
public class Shop {

    /** Reference to the currently open shop window, if any. */
    private static Stage currentStage;

    /**
     * Opens the shop window for the given game state.
     * If a shop is already open, it is closed first.
     * Card and relic lists are read from game.currentShopCards
     * and game.currentBossRelics respectively.
     *
     * @param game the active game state
     */
    public static void open(Game game) {

        // Close any previously open shop window
        if (currentStage != null && currentStage.isShowing()) {
            currentStage.close();
        }

        Stage stage = new Stage();
        currentStage = stage;

        VBox root = new VBox(12);
        root.setStyle("-fx-padding:20;");

        Label goldLabel = new Label("Gold: " + game.player.getGold());
        goldLabel.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");
        root.getChildren().add(goldLabel);

        Label info = new Label();

        // ── Normal cards section ──────────────────────────────────────────
        List<Card> shopCards = game.currentShopCards.isEmpty()
            ? CardFactory.shopCards(game.level, game.player)
            : game.currentShopCards;

        if (!shopCards.isEmpty()) {
            Label cardTitle = new Label("── Kartlar ──");
            cardTitle.setStyle("-fx-font-weight:bold;");
            root.getChildren().add(cardTitle);

            for (Card card : shopCards) {
                Button btn = new Button(
                    card.name + "  |  Maliyet: " + card.cost +
                    "  |  Fiyat: " + card.price + " gold"
                );
                btn.setOnAction(e -> {
                    if (game.player.spendGold(card.price)) {
                        game.player.deck.add(card);
                        info.setText("✅ Alındı: " + card.name);
                        goldLabel.setText("Gold: " + game.player.getGold());
                        btn.setDisable(true);
                    } else {
                        info.setText("❌ Yeterli gold yok!");
                    }
                });
                root.getChildren().add(btn);
            }
        }

        // ── Boss relic section (only shown after a boss kill) ─────────────
        if (!game.currentBossRelics.isEmpty()) {
            Label sep = new Label("── Boss Ödülleri ──");
            sep.setStyle("-fx-font-weight:bold; -fx-text-fill:#cc7700;");
            root.getChildren().add(sep);

            for (RelicItem relic : game.currentBossRelics) {
                boolean alreadyOwned = game.ownedRelics.stream()
                    .anyMatch(r -> r.name.equals(relic.name));

                Button btn = new Button(
                    relic.name + "  |  " + relic.description +
                    "  |  " + relic.price + " gold"
                );
                btn.setStyle("-fx-background-color:#fff3cd;");
                if (alreadyOwned) {
                    btn.setText(btn.getText() + "  [Sahipsin]");
                    btn.setDisable(true);
                }

                btn.setOnAction(e -> {
                    if (game.player.spendGold(relic.price)) {
                        relic.applyOnBuy(game.player, game);
                        game.ownedRelics.add(relic);
                        game.eventBus.publish(new RelicEvent());
                        info.setText("✨ Alındı: " + relic.name);
                        goldLabel.setText("Gold: " + game.player.getGold());
                        btn.setDisable(true);
                    } else {
                        info.setText("❌ Yeterli gold yok!");
                    }
                });
                root.getChildren().add(btn);
            }

            // Clear boss relics when the shop window is closed
            stage.setOnHidden(e -> game.currentBossRelics.clear());
        }

        Button closeBtn = new Button("Kapat");
        closeBtn.setOnAction(e -> stage.close());
        root.getChildren().addAll(info, closeBtn);

        stage.setScene(new Scene(root, 420, 380));
        stage.setTitle("SHOP - Level " + game.level);
        stage.show();
    }

    /**
     * Closes the shop window if one is currently open.
     * Called at the start of each End Turn to prevent the shop
     * from remaining open during combat.
     */
    public static void closeShop() {
        if (currentStage != null && currentStage.isShowing()) {
            currentStage.close();
            currentStage = null;
        }
    }
}