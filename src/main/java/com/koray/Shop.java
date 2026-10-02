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

    public static void open(Game game, Stage owner, Runnable onGameStateChanged,
                            Runnable onHandSizeChanged) {

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

        Label goldLabel = new Label("Gold: " + game.getPlayer().getGold());
        goldLabel.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");
        content.getChildren().add(goldLabel);

        Label info = new Label();
        List<Button> shopCardButtons = new ArrayList<>();
        List<Card> availableShopCards = new ArrayList<>();
        Set<Button> purchasedCardButtons = new HashSet<>();
        List<RelicItem> availableBossRelics = new ArrayList<>();
        List<Button> bossRelicButtons = new ArrayList<>();

        Button handUpgradeButton = new Button();
        updateHandUpgradeButton(handUpgradeButton, game);
        handUpgradeButton.setMaxWidth(Double.MAX_VALUE);
        handUpgradeButton.setOnAction(e -> {
            if (game.purchaseHandSizeUpgrade()) {
                info.setText("Hand size increased to " + game.getHandSizeLimit() + " cards.");
                goldLabel.setText("Gold: " + game.getPlayer().getGold());
                onHandSizeChanged.run();
                onGameStateChanged.run();
            } else {
                info.setText("❌ Not enough gold!");
            }
            updateHandUpgradeButton(handUpgradeButton, game);
            updateShopCardButtons(game, availableShopCards, shopCardButtons, purchasedCardButtons);
            updateBossRelicButtons(game, availableBossRelics, bossRelicButtons);
        });
        Label upgradeTitle = new Label("── Permanent Upgrade ──");
        upgradeTitle.setStyle("-fx-font-weight:bold;");
        content.getChildren().addAll(upgradeTitle, handUpgradeButton);

        // ── CARDS ────────────────────────────────────
        List<Card> shopCards = game.getCurrentShopCards().isEmpty()
            ? CardFactory.shopCards(game.getLevel(), game.getPlayer())
            : game.getCurrentShopCards();

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
                    if (game.getPlayer().spendGold(card.price)) {
                        game.getPlayer().addToDeck(card);
                        info.setText("✅ Purchased: " + card.name);
                        goldLabel.setText("Gold: " + game.getPlayer().getGold());
                        onGameStateChanged.run();
                        purchasedCardButtons.add(btn);
                        updateHandUpgradeButton(handUpgradeButton, game);
                        updateShopCardButtons(game, availableShopCards, shopCardButtons,
                            purchasedCardButtons);
                        updateBossRelicButtons(game, availableBossRelics, bossRelicButtons);
                    } else {
                        info.setText("❌ Not enough gold!");
                    }
                });
                content.getChildren().add(btn);
            }
            updateShopCardButtons(game, availableShopCards, shopCardButtons, purchasedCardButtons);
        }

        // ── BOSS RELICS (only after defeating a boss) ─
        if (!game.getCurrentBossRelics().isEmpty()) {
            Label sep = new Label("── Boss Rewards ──");
            sep.setStyle("-fx-font-weight:bold; -fx-text-fill:#cc7700;");
            content.getChildren().add(sep);

            for (RelicItem relic : game.getCurrentBossRelics()) {
                Button btn = new Button(
                    relic.name + "  |  " + relic.description +
                    "  |  " + relic.price + " gold"
                );
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setStyle("-fx-background-color:#fff3cd;");
                availableBossRelics.add(relic);
                bossRelicButtons.add(btn);

                btn.setOnAction(e -> {
                    if (!relic.canPurchase(game)) {
                        info.setText("❌ Requirements not met for " + relic.name + ".");
                        return;
                    }
                    if (game.getPlayer().spendGold(relic.price)) {
                        relic.applyOnBuy(game.getPlayer(), game);
                        game.addOwnedRelic(relic);
                        game.getEventBus().publish(new RelicEvent(relic));
                        info.setText("✨ Purchased: " + relic.name);
                        goldLabel.setText("Gold: " + game.getPlayer().getGold());
                        updateHandUpgradeButton(handUpgradeButton, game);
                        updateShopCardButtons(game, availableShopCards, shopCardButtons,
                            purchasedCardButtons);
                        updateBossRelicButtons(game, availableBossRelics, bossRelicButtons);
                    } else {
                        info.setText("❌ Not enough gold!");
                    }
                });
                content.getChildren().add(btn);
            }
            updateBossRelicButtons(game, availableBossRelics, bossRelicButtons);

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
        stage.setTitle("SHOP - Level " + game.getLevel());
        stage.show();
        stage.centerOnScreen();
    }

    private static void requestCloseWithConfirmation(Stage stage, Game game) {
        if (!hasPurchasableBossRelic(game)) {
            game.clearCurrentBossRelics();
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
            game.clearCurrentBossRelics();
            stage.close();
        }
    }

    static boolean hasPurchasableBossRelic(Game game) {
        return game.getCurrentBossRelics().stream().anyMatch(relic -> {
            boolean alreadyOwned = game.getOwnedRelics().stream()
                .anyMatch(owned -> owned.name.equals(relic.name));
            return !alreadyOwned
                && relic.canPurchase(game)
                && game.getPlayer().getGold() >= relic.price;
        });
    }

    private static void updateBossRelicButtons(Game game, List<RelicItem> relics,
                                                List<Button> buttons) {
        for (int i = 0; i < relics.size(); i++) {
            RelicItem relic = relics.get(i);
            Button button = buttons.get(i);
            boolean alreadyOwned = game.getOwnedRelics().stream()
                .anyMatch(owned -> owned.name.equals(relic.name));
            boolean canPurchase = relic.canPurchase(game);
            String status = alreadyOwned ? "  [Owned]"
                : !canPurchase && relic instanceof BloodPactRelic
                    ? "  [Requires HP + Shield > 30]" : "";

            button.setText(relic.name + "  |  " + relic.description
                + "  |  " + relic.price + " gold" + status);
            button.setDisable(alreadyOwned || !canPurchase
                || game.getPlayer().getGold() < relic.price);
        }
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
        return cost >= 0 && game.getPlayer().getGold() >= cost;
    }

    private static void updateShopCardButtons(Game game, List<Card> cards, List<Button> buttons,
                                               Set<Button> purchasedButtons) {
        for (int i = 0; i < cards.size(); i++) {
            Button button = buttons.get(i);
            button.setDisable(purchasedButtons.contains(button)
                || game.getPlayer().getGold() < cards.get(i).price);
        }
    }

    public static void closeShop() {
        if (currentStage != null && currentStage.isShowing()) {
            currentStage.close();
            currentStage = null;
        }
    }
}