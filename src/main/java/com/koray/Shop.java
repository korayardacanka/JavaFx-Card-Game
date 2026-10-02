package com.koray;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.*;

public class Shop {

    private static Stage currentStage;

    public static void open(Game game, Stage owner, Runnable onGameStateChanged) {

        // Close the previous shop if it is still open.
        if (currentStage != null && currentStage.isShowing()) {
            currentStage.close();
        }

        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        currentStage = stage;

        VBox content = new VBox(12);
        content.setStyle("-fx-padding:20;");

        Label goldLabel = new Label("Gold: " + game.player.getGold());
        goldLabel.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");
        content.getChildren().add(goldLabel);

        Label info = new Label();
        List<Button> shopCardButtons = new ArrayList<>();
        List<Card> availableShopCards = new ArrayList<>();
        Set<Button> purchasedCardButtons = new HashSet<>();

        Button handUpgradeButton = new Button();
        updateHandUpgradeButton(handUpgradeButton, game);
        handUpgradeButton.setMaxWidth(Double.MAX_VALUE);
        handUpgradeButton.setOnAction(e -> {
            if (game.purchaseHandSizeUpgrade()) {
                info.setText("Hand size increased to " + game.getHandSizeLimit() + " cards.");
                goldLabel.setText("Gold: " + game.player.getGold());
                onGameStateChanged.run();
            } else {
                info.setText("❌ Not enough gold!");
            }
            updateHandUpgradeButton(handUpgradeButton, game);
            updateShopCardButtons(game, availableShopCards, shopCardButtons, purchasedCardButtons);
        });
        Label upgradeTitle = new Label("── Permanent Upgrade ──");
        upgradeTitle.setStyle("-fx-font-weight:bold;");
        content.getChildren().addAll(upgradeTitle, handUpgradeButton);

        // ── CARDS ────────────────────────────────────
        List<Card> shopCards = game.currentShopCards.isEmpty()
            ? CardFactory.shopCards(game.level, game.player)
            : game.currentShopCards;

        if (!shopCards.isEmpty()) {
            Label cardTitle = new Label("── Cards ──");
            cardTitle.setStyle("-fx-font-weight:bold;");
            content.getChildren().add(cardTitle);

            for (Card card : shopCards) {
                Button btn = new Button(
                    card.name + "  |  Cost: " + card.cost +
                    "  |  Price: " + card.price + " gold"
                );
                btn.setMaxWidth(Double.MAX_VALUE);
                availableShopCards.add(card);
                shopCardButtons.add(btn);
                btn.setOnAction(e -> {
                    if (game.player.spendGold(card.price)) {
                        game.player.deck.add(card);
                        info.setText("✅ Purchased: " + card.name);
                        goldLabel.setText("Gold: " + game.player.getGold());
                        onGameStateChanged.run();
                        purchasedCardButtons.add(btn);
                        updateHandUpgradeButton(handUpgradeButton, game);
                        updateShopCardButtons(game, availableShopCards, shopCardButtons,
                            purchasedCardButtons);
                    } else {
                        info.setText("❌ Not enough gold!");
                    }
                });
                content.getChildren().add(btn);
            }
            updateShopCardButtons(game, availableShopCards, shopCardButtons, purchasedCardButtons);
        }

        // ── BOSS RELICS (only after defeating a boss) ─
        if (!game.currentBossRelics.isEmpty()) {
            Label sep = new Label("── Boss Rewards ──");
            sep.setStyle("-fx-font-weight:bold; -fx-text-fill:#cc7700;");
            content.getChildren().add(sep);

            for (RelicItem relic : game.currentBossRelics) {
                boolean alreadyOwned = game.ownedRelics.stream()
                    .anyMatch(r -> r.name.equals(relic.name));
                boolean bloodPactLocked = relic instanceof BloodPactRelic
                    && game.player.getHp() <= 30;

                Button btn = new Button(
                    relic.name + "  |  " + relic.description +
                    "  |  " + relic.price + " gold"
                );
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setStyle("-fx-background-color:#fff3cd;");
                if (alreadyOwned) {
                    btn.setText(btn.getText() + "  [Owned]");
                    btn.setDisable(true);
                }
                if (bloodPactLocked) {
                    btn.setText(btn.getText() + "  [Requires HP > 30]");
                    btn.setDisable(true);
                }

                btn.setOnAction(e -> {
                    if (bloodPactLocked) {
                        info.setText("❌ You need more than 30 HP to buy Blood Pact.");
                        return;
                    }
                    if (game.player.spendGold(relic.price)) {
                        relic.applyOnBuy(game.player, game);
                        game.ownedRelics.add(relic);
                        game.eventBus.publish(new RelicEvent(relic));
                        info.setText("✨ Purchased: " + relic.name);
                        goldLabel.setText("Gold: " + game.player.getGold());
                        btn.setDisable(true);
                        updateHandUpgradeButton(handUpgradeButton, game);
                        updateShopCardButtons(game, availableShopCards, shopCardButtons,
                            purchasedCardButtons);
                    } else {
                        info.setText("❌ Not enough gold!");
                    }
                });
                content.getChildren().add(btn);
            }

        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> requestCloseWithConfirmation(stage, game));
        HBox closeRow = new HBox(closeBtn);
        closeRow.setAlignment(Pos.CENTER_RIGHT);
        VBox footer = new VBox(8, info, closeRow);
        footer.setStyle("-fx-padding:10 16 16 16;");
        BorderPane root = new BorderPane(scrollPane);
        root.setBottom(footer);

        stage.setOnCloseRequest(e -> {
            e.consume();
            requestCloseWithConfirmation(stage, game);
        });

        stage.setScene(new Scene(root, 620, 620));
        stage.setMinWidth(460);
        stage.setMinHeight(360);
        stage.setTitle("SHOP - Level " + game.level);
        stage.show();
        stage.centerOnScreen();
    }

    private static void requestCloseWithConfirmation(Stage stage, Game game) {
        if (!hasPurchasableBossRelic(game)) {
            game.currentBossRelics.clear();
            stage.close();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.initModality(Modality.WINDOW_MODAL);
        confirm.setTitle("Boss rewards will be lost");
        confirm.setHeaderText("Are you sure you want to close?");
        confirm.setContentText("This will discard the current boss relics. Continue?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            game.currentBossRelics.clear();
            stage.close();
        }
    }

    static boolean hasPurchasableBossRelic(Game game) {
        return game.currentBossRelics.stream().anyMatch(relic -> {
            boolean alreadyOwned = game.ownedRelics.stream()
                .anyMatch(owned -> owned.name.equals(relic.name));
            boolean bloodPactLocked = relic instanceof BloodPactRelic
                && game.player.getHp() <= 30;
            return !alreadyOwned
                && !bloodPactLocked
                && game.player.getGold() >= relic.price;
        });
    }

    private static void updateHandUpgradeButton(Button button, Game game) {
        int cost = game.getNextHandSizeUpgradeCost();
        if (cost < 0) {
            button.setText("Hand Size: MAX (" + game.getHandSizeLimit() + " cards)");
            button.setDisable(true);
            return;
        }
        button.setText("Hand Size Lv. " + (game.getHandSizeUpgradeLevel() + 1)
            + " (+1 card) | " + cost + " gold");
        button.setDisable(!canPurchaseHandSizeUpgrade(game));
    }

    static boolean canPurchaseHandSizeUpgrade(Game game) {
        int cost = game.getNextHandSizeUpgradeCost();
        return cost >= 0 && game.player.getGold() >= cost;
    }

    private static void updateShopCardButtons(Game game, List<Card> cards, List<Button> buttons,
                                               Set<Button> purchasedButtons) {
        for (int i = 0; i < cards.size(); i++) {
            Button button = buttons.get(i);
            button.setDisable(purchasedButtons.contains(button)
                || game.player.getGold() < cards.get(i).price);
        }
    }

    public static void closeShop() {
        if (currentStage != null && currentStage.isShowing()) {
            currentStage.close();
            currentStage = null;
        }
    }
}