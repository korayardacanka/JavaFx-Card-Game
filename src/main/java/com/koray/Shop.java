package com.koray;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.*;
 
public class Shop {
	private static Stage currentStage;
    public static void open(Game game) {
    	 
        // Önceki shop açıksa kapat
        if (game.shopStage != null && game.shopStage.isShowing()) {
            game.shopStage.close();
        }
 
        Stage stage = new Stage();
        currentStage = stage;
        game.shopStage = stage;
        
        
        VBox root = new VBox(12);
        root.setStyle("-fx-padding:20;");
 
        Label goldLabel = new Label("Gold: " + game.player.gold);
        goldLabel.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");
        root.getChildren().add(goldLabel);
 
        Label info = new Label();
 
        // ── NORMAL KARTLAR ──────────────────────────
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
                    if (game.player.gold >= card.price) {
                        game.player.gold -= card.price;
                        game.player.deck.add(card);
                        info.setText("✅ Alındı: " + card.name);
                        goldLabel.setText("Gold: " + game.player.gold);
                        btn.setDisable(true);
                    } else {
                        info.setText("❌ Yeterli gold yok!");
                    }
                });
                root.getChildren().add(btn);
            }
        }
 
        // ── BOSS RELICLERİ (sadece boss sonrası) ────
        if (!game.currentBossRelics.isEmpty()) {
            Label sep = new Label("── Boss Ödülleri ──");
            sep.setStyle("-fx-font-weight:bold; -fx-text-fill:#cc7700;");
            root.getChildren().add(sep);
 
            for (RelicItem relic : game.currentBossRelics) {
                // Zaten sahipse gösterme
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
                    if (game.player.gold >= relic.price) {
                        game.player.gold -= relic.price;
                        relic.applyOnBuy(game.player, game);
                        game.ownedRelics.add(relic);
                        game.eventBus.publish(new RelicEvent());
                        info.setText("✨ Alındı: " + relic.name);
                        goldLabel.setText("Gold: " + game.player.gold);
                        btn.setDisable(true);
                    } else {
                        info.setText("❌ Yeterli gold yok!");
                    }
                });
                root.getChildren().add(btn);
            }
 
            // Shop kapandıktan sonra boss relic listesini temizle
            stage.setOnHidden(e -> game.currentBossRelics.clear());
            
        }
 
        Button closeBtn = new Button("Kapat");
        closeBtn.setOnAction(e -> stage.close());
        root.getChildren().addAll(info, closeBtn);
 
        stage.setScene(new Scene(root, 420, 380));
        stage.setTitle("SHOP - Level " + game.level);
        stage.show();
    }
    public static void closeShop() {
        // currentStage static olduğu için bu metot sorunsuz çalışır
        if (currentStage != null && currentStage.isShowing()) {
            currentStage.close();
            currentStage = null;
        }
    }
}
