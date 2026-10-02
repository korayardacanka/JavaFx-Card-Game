package com.koray;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Builds the game-over screen. */
public final class DeathScreen {

    private DeathScreen() {}

    public static VBox create(RunSummary summary, Runnable onRestart, Runnable onMainMenu) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(32));
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,"
            + "#171317 0%, #241b20 55%, #151920 100%);");

        VBox panel = new VBox(18);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(700);
        panel.setPadding(new Insets(38, 54, 38, 54));
        panel.setStyle("-fx-background-color:rgba(15,16,20,0.88);"
            + "-fx-background-radius:22; -fx-border-color:#71434a;"
            + "-fx-border-width:1.5; -fx-border-radius:22;");

        Label emblem = new Label("☠");
        emblem.setStyle("-fx-text-fill:#bd7378; -fx-font-size:30px;");
        Label title = new Label("YOUR RUN ENDS");
        title.setStyle("-fx-text-fill:#f0dede; -fx-font-size:35px;"
            + "-fx-font-weight:bold;");
        Label subtitle = new Label("The road was hard. Your legend continues.");
        subtitle.setStyle("-fx-text-fill:#aaa3a4; -fx-font-size:14px;");

        HBox highlights = new HBox(12,
            statCard("LEVEL REACHED", Integer.toString(summary.level()), "#e1bd72"),
            statCard("GOLD KEPT", Integer.toString(summary.gold()), "#edc76c"),
            statCard("TOTAL CARDS", Integer.toString(summary.deckSize()), "#9fc7d5"));
        highlights.setAlignment(Pos.CENTER);

        Label relicTitle = new Label("RELICS COLLECTED");
        relicTitle.setStyle("-fx-text-fill:#d7b974; -fx-font-size:11px;"
            + "-fx-font-weight:bold;");
        String relicText = summary.relics().isEmpty()
            ? "No relics collected this run"
            : String.join("   ·   ", summary.relics());
        Label relics = new Label(relicText);
        relics.setWrapText(true);
        relics.setMaxWidth(520);
        relics.setAlignment(Pos.CENTER);
        relics.setStyle("-fx-text-fill:#e4dfd5; -fx-font-size:14px;"
            + "-fx-background-color:#25262b; -fx-background-radius:10;"
            + "-fx-padding:14 18;");

        Button restartButton = new Button("PLAY AGAIN");
        restartButton.setMinWidth(210);
        restartButton.setMinHeight(44);
        restartButton.setStyle("-fx-background-color:linear-gradient(to bottom,#d7ad59,#a87530);"
            + "-fx-text-fill:#1c1710; -fx-font-weight:bold; -fx-font-size:14px;"
            + "-fx-background-radius:8; -fx-cursor:hand;");
        restartButton.setOnAction(event -> onRestart.run());
        Button menuButton = new Button("MAIN MENU");
        menuButton.setMinWidth(150);
        menuButton.setMinHeight(44);
        menuButton.setStyle("-fx-background-color:#34363c; -fx-text-fill:#e9e4da;"
            + "-fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand;");
        menuButton.setOnAction(event -> onMainMenu.run());
        HBox actions = new HBox(12, restartButton, menuButton);
        actions.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(emblem, title, subtitle, highlights, relicTitle,
            relics, actions);
        root.getChildren().add(panel);
        return root;
    }

    private static VBox statCard(String caption, String value, String valueColor) {
        Label title = new Label(caption);
        title.setStyle("-fx-text-fill:#99989a; -fx-font-size:10px;"
            + "-fx-font-weight:bold;");
        Label stat = new Label(value);
        stat.setStyle("-fx-text-fill:" + valueColor + "; -fx-font-size:25px;"
            + "-fx-font-weight:bold;");
        VBox card = new VBox(7, title, stat);
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(145);
        card.setPadding(new Insets(16, 12, 16, 12));
        card.setStyle("-fx-background-color:#25262b; -fx-background-radius:11;"
            + "-fx-border-color:#393a41; -fx-border-radius:11;");
        return card;
    }
}
