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
        if (currentStage != null && currentStage.isShowing()) {
            currentStage.close();
        }

        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        currentStage = stage;

        Label goldLabel = new Label();
        updateGoldLabel(goldLabel, game);
        goldLabel.setStyle("-fx-background-color:#382b1c; -fx-text-fill:#ffd36a;"
            + "-fx-font-size:17px; -fx-font-weight:bold; -fx-padding:10 16;"
            + "-fx-background-radius:18; -fx-border-color:#73552c;"
            + "-fx-border-radius:18;");
        Label info = new Label();
        info.setStyle("-fx-text-fill:#f6d98b; -fx-font-size:13px;");
        info.setWrapText(true);

        List<Button> shopCardButtons = new ArrayList<>();
        List<Card> availableShopCards = new ArrayList<>();
        Set<Button> purchasedCardButtons = new HashSet<>();
        List<RelicItem> availableBossRelics = new ArrayList<>();
        List<Button> bossRelicButtons = new ArrayList<>();

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleBlock = new VBox(3);
        Label title = new Label("THE WAYFARER'S MARKET");
        title.setStyle("-fx-text-fill:#f7e8c1; -fx-font-size:23px;"
            + "-fx-font-weight:bold;");
        Label subtitle = new Label("Rest, resupply, and prepare for the next battle");
        subtitle.setStyle("-fx-text-fill:#b5aa98; -fx-font-size:12px;");
        titleBlock.getChildren().addAll(title, subtitle);
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        header.getChildren().addAll(titleBlock, headerSpacer, goldLabel);

        VBox content = new VBox(18);
        content.setStyle("-fx-padding:22; -fx-background-color:#17191f;");
        content.getChildren().add(header);
        Separator headerSeparator = new Separator();
        headerSeparator.setStyle("-fx-opacity:0.35;");
        content.getChildren().add(headerSeparator);

        Button handUpgradeButton = new Button();
        updateHandUpgradeButton(handUpgradeButton, game);
        handUpgradeButton.setMaxWidth(Double.MAX_VALUE);
        handUpgradeButton.setStyle(primaryButtonStyle());
        handUpgradeButton.setOnAction(e -> {
            if (game.purchaseHandSizeUpgrade()) {
                info.setText("Hand capacity upgraded to " + game.getHandSizeLimit() + ".");
                updateGoldLabel(goldLabel, game);
                onHandSizeChanged.run();
                onGameStateChanged.run();
            } else {
                info.setText("Not enough gold for this upgrade.");
            }
            updateHandUpgradeButton(handUpgradeButton, game);
            updateShopCardButtons(game, availableShopCards, shopCardButtons, purchasedCardButtons);
            updateBossRelicButtons(game, availableBossRelics, bossRelicButtons);
        });

        VBox upgradePanel = new VBox(8);
        upgradePanel.setStyle(panelStyle());
        Label upgradeTitle = sectionTitle("PERMANENT UPGRADE");
        Label upgradeDescription = new Label(
            "Increase your hand size by one card for every future turn.");
        upgradeDescription.setStyle("-fx-text-fill:#b5aa98; -fx-font-size:12px;");
        upgradeDescription.setWrapText(true);
        upgradePanel.getChildren().addAll(upgradeTitle, upgradeDescription,
            handUpgradeButton);
        content.getChildren().add(upgradePanel);

        List<Card> shopCards = game.getCurrentShopCards().isEmpty()
            ? CardFactory.shopCards(game.getLevel(), game.getPlayer())
            : game.getCurrentShopCards();

        if (!shopCards.isEmpty()) {
            VBox cardsPanel = new VBox(12);
            cardsPanel.setStyle(panelStyle());
            cardsPanel.getChildren().add(sectionTitle("TRAVELER'S SUPPLIES"));
            TilePane cardGrid = new TilePane();
            cardGrid.setHgap(12);
            cardGrid.setVgap(12);
            cardGrid.setPrefColumns(2);
            cardGrid.setTileAlignment(Pos.TOP_LEFT);

            for (Card card : shopCards) {
                VBox cardTile = new VBox(9);
                cardTile.setPrefWidth(245);
                cardTile.setMinHeight(150);
                cardTile.setStyle("-fx-background-color:#252932; -fx-background-radius:10;"
                    + "-fx-border-color:#3c414d; -fx-border-radius:10;"
                    + "-fx-padding:13;");

                String accent = card.effect.color().isEmpty()
                    ? "#c49b55" : card.effect.color();
                HBox nameRow = new HBox(8);
                nameRow.setAlignment(Pos.CENTER_LEFT);
                Label icon = new Label(card.effect.icon());
                icon.setStyle("-fx-font-size:17px;");
                Label cardName = new Label(card.name);
                cardName.setStyle("-fx-text-fill:#f4ead5; -fx-font-size:15px;"
                    + "-fx-font-weight:bold;");
                Region nameSpacer = new Region();
                HBox.setHgrow(nameSpacer, Priority.ALWAYS);
                Label energy = new Label("⚡ " + card.cost);
                energy.setStyle("-fx-background-color:#37333a; -fx-text-fill:#f0c86d;"
                    + "-fx-padding:4 8; -fx-background-radius:12; -fx-font-weight:bold;");
                nameRow.getChildren().addAll(icon, cardName, nameSpacer, energy);

                Region accentLine = new Region();
                accentLine.setPrefHeight(3);
                accentLine.setStyle("-fx-background-color:" + accent
                    + "; -fx-background-radius:2;");
                Label description = new Label(card.effect.describe());
                description.setWrapText(true);
                description.setMinHeight(34);
                description.setStyle("-fx-text-fill:#c5c1b9; -fx-font-size:12px;");

                HBox purchaseRow = new HBox(8);
                purchaseRow.setAlignment(Pos.CENTER_LEFT);
                Label price = new Label(card.price + " gold");
                price.setStyle("-fx-text-fill:#e9bf66; -fx-font-weight:bold;");
                Region priceSpacer = new Region();
                HBox.setHgrow(priceSpacer, Priority.ALWAYS);
                Button btn = new Button("Buy");
                btn.setMinWidth(78);
                btn.setStyle(primaryButtonStyle());
                purchaseRow.getChildren().addAll(price, priceSpacer, btn);

                availableShopCards.add(card);
                shopCardButtons.add(btn);
                btn.setOnAction(e -> {
                    if (game.getPlayer().spendGold(card.price)) {
                        game.getPlayer().addToDeck(card);
                        info.setText(card.name + " added to your deck.");
                        updateGoldLabel(goldLabel, game);
                        onGameStateChanged.run();
                        purchasedCardButtons.add(btn);
                        updateHandUpgradeButton(handUpgradeButton, game);
                        updateShopCardButtons(game, availableShopCards, shopCardButtons,
                            purchasedCardButtons);
                        updateBossRelicButtons(game, availableBossRelics, bossRelicButtons);
                    } else {
                        info.setText("Not enough gold for " + card.name + ".");
                    }
                });
                cardTile.getChildren().addAll(nameRow, accentLine, description, purchaseRow);
                cardGrid.getChildren().add(cardTile);
            }
            cardsPanel.getChildren().add(cardGrid);
            content.getChildren().add(cardsPanel);
            updateShopCardButtons(game, availableShopCards, shopCardButtons, purchasedCardButtons);
        }

        if (!game.getCurrentBossRelics().isEmpty()) {
            VBox relicPanel = new VBox(12);
            relicPanel.setStyle(panelStyle());
            relicPanel.getChildren().add(sectionTitle("BOSS RELICS"));
            TilePane relicGrid = new TilePane();
            relicGrid.setHgap(12);
            relicGrid.setVgap(12);
            relicGrid.setPrefColumns(2);
            relicGrid.setTileAlignment(Pos.TOP_LEFT);

            for (RelicItem relic : game.getCurrentBossRelics()) {
                VBox relicTile = new VBox(9);
                relicTile.setPrefWidth(245);
                relicTile.setMinHeight(140);
                relicTile.setStyle("-fx-background-color:#30291f; -fx-background-radius:10;"
                    + "-fx-border-color:#755d36; -fx-border-radius:10;"
                    + "-fx-padding:13;");
                Label relicName = new Label("✦  " + relic.name);
                relicName.setStyle("-fx-text-fill:#f1d18a; -fx-font-size:14px;"
                    + "-fx-font-weight:bold;");
                Label relicDescription = new Label(relic.description);
                relicDescription.setWrapText(true);
                relicDescription.setMinHeight(34);
                relicDescription.setStyle("-fx-text-fill:#d0c5af; -fx-font-size:12px;");
                Button btn = new Button();
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setStyle(primaryButtonStyle());
                availableBossRelics.add(relic);
                bossRelicButtons.add(btn);

                btn.setOnAction(e -> {
                    if (!relic.canPurchase(game)) {
                        info.setText("Requirements are not met for " + relic.name + ".");
                        return;
                    }
                    if (game.getPlayer().spendGold(relic.price)) {
                        relic.applyOnBuy(game.getPlayer(), game);
                        game.addOwnedRelic(relic);
                        game.getEventBus().publish(new RelicEvent(relic));
                        info.setText(relic.name + " joined your collection.");
                        updateGoldLabel(goldLabel, game);
                        updateHandUpgradeButton(handUpgradeButton, game);
                        updateShopCardButtons(game, availableShopCards, shopCardButtons,
                            purchasedCardButtons);
                        updateBossRelicButtons(game, availableBossRelics, bossRelicButtons);
                    } else {
                        info.setText("Not enough gold for " + relic.name + ".");
                    }
                });
                relicTile.getChildren().addAll(relicName, relicDescription, btn);
                relicGrid.getChildren().add(relicTile);
            }
            relicPanel.getChildren().add(relicGrid);
            content.getChildren().add(relicPanel);
            updateBossRelicButtons(game, availableBossRelics, bossRelicButtons);
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background:#17191f; -fx-background-color:#17191f;"
            + "-fx-border-color:transparent;");

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color:#3b404b; -fx-text-fill:#eee7d8;"
            + "-fx-font-weight:bold; -fx-padding:9 24; -fx-background-radius:7;");
        closeBtn.setOnAction(e -> requestCloseWithConfirmation(stage, game));
        HBox closeRow = new HBox(closeBtn);
        closeRow.setAlignment(Pos.CENTER_RIGHT);
        VBox footer = new VBox(10, info, closeRow);
        footer.setStyle("-fx-padding:12 22 18 22; -fx-background-color:#20232a;"
            + "-fx-border-color:#353943 transparent transparent transparent;");
        BorderPane root = new BorderPane(scrollPane);
        root.setBottom(footer);
        root.setStyle("-fx-background-color:#17191f;");

        stage.setOnCloseRequest(e -> {
            e.consume();
            requestCloseWithConfirmation(stage, game);
        });

        stage.setScene(new Scene(root, 760, 720));
        stage.setMinWidth(620);
        stage.setMinHeight(500);
        stage.setTitle("Wayfarer's Market - Level " + game.getLevel());
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

            button.setText(alreadyOwned ? "Owned"
                : !canPurchase ? status.trim()
                : "Buy for " + relic.price + " gold");
            button.setDisable(alreadyOwned || !canPurchase
                || game.getPlayer().getGold() < relic.price);
        }
    }

    private static void updateHandUpgradeButton(Button button, Game game) {
        int cost = game.getNextHandSizeUpgradeCost();
        if (cost < 0) {
            button.setText("Hand size maxed  ·  " + game.getHandSizeLimit() + " cards");
            button.setDisable(true);
            return;
        }

        button.setText("Upgrade hand  ·  +1 card  ·  " + cost + " gold");
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
            boolean purchased = purchasedButtons.contains(button);
            button.setText(purchased ? "Added to deck"
                : "Buy for " + cards.get(i).price + " gold");
            button.setDisable(purchased || game.getPlayer().getGold() < cards.get(i).price);
        }
    }

    private static String panelStyle() {
        return "-fx-background-color:#20232a; -fx-background-radius:12;"
            + "-fx-border-color:#343943; -fx-border-radius:12; -fx-padding:16;";
    }

    private static Label sectionTitle(String text) {
        Label title = new Label(text);
        title.setStyle("-fx-text-fill:#e2bd70; -fx-font-size:12px;"
            + "-fx-font-weight:bold;");
        return title;
    }

    private static String primaryButtonStyle() {
        return "-fx-background-color:#72552d; -fx-text-fill:#fff0cf;"
            + "-fx-font-weight:bold; -fx-padding:8 12; -fx-background-radius:7;"
            + "-fx-cursor:hand;";
    }

    private static void updateGoldLabel(Label label, Game game) {
        label.setText("✦  " + game.getPlayer().getGold() + " GOLD");
    }

    public static void closeShop() {
        if (currentStage != null && currentStage.isShowing()) {
            currentStage.close();
            currentStage = null;
        }
    }
}