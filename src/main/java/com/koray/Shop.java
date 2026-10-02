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

    public static void open(Game game, Stage owner) {

        // Önceki shop açıksa kapat
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

        // ── NORMAL KARTLAR ──────────────────────────
        List<Card> shopCards = game.currentShopCards.isEmpty()
            ? CardFactory.shopCards(game.level, game.player)
            : game.currentShopCards;

        if (!shopCards.isEmpty()) {
            Label cardTitle = new Label("── Kartlar ──");
            cardTitle.setStyle("-fx-font-weight:bold;");
            content.getChildren().add(cardTitle);

            for (Card card : shopCards) {
                Button btn = new Button(
                    card.name + "  |  Maliyet: " + card.cost +
                    "  |  Fiyat: " + card.price + " gold"
                );
                btn.setMaxWidth(Double.MAX_VALUE);
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
                content.getChildren().add(btn);
            }
        }

        // ── BOSS RELICLERİ (sadece boss sonrası) ────
        if (!game.currentBossRelics.isEmpty()) {
            Label sep = new Label("── Boss Ödülleri ──");
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
                    btn.setText(btn.getText() + "  [Sahipsin]");
                    btn.setDisable(true);
                }
                if (bloodPactLocked) {
                    btn.setText(btn.getText() + "  [HP > 30 gerekli]");
                    btn.setDisable(true);
                }

                btn.setOnAction(e -> {
                    if (bloodPactLocked) {
                        info.setText("❌ Kan Antlaşması için HP > 30 olmalı.");
                        return;
                    }
                    if (game.player.spendGold(relic.price)) {
                        relic.applyOnBuy(game.player, game);
                        game.ownedRelics.add(relic);
                        game.eventBus.publish(new RelicEvent(relic));
                        info.setText("✨ Alındı: " + relic.name);
                        goldLabel.setText("Gold: " + game.player.getGold());
                        btn.setDisable(true);
                    } else {
                        info.setText("❌ Yeterli gold yok!");
                    }
                });
                content.getChildren().add(btn);
            }

        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        Button closeBtn = new Button("Kapat");
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
        confirm.setTitle("Boss ödülü kaybolacak");
        confirm.setHeaderText("Kapatmak istediğinize emin misiniz?");
        confirm.setContentText("Bu işlem, mevcut boss relic'leri siler. Devam etmek istiyor musunuz?");

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

    public static void closeShop() {
        if (currentStage != null && currentStage.isShowing()) {
            currentStage.close();
            currentStage = null;
        }
    }
}