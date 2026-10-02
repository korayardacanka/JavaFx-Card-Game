package com.koray;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Builds the start screen. */
public final class StartScreen {

    private StartScreen() {}

    public static VBox create(Runnable onStart) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(36));
        root.setStyle("-fx-background-color:linear-gradient(to bottom right,"
            + "#10151d 0%, #1c2430 52%, #30251c 100%);");

        VBox titleCard = new VBox(18);
        titleCard.setAlignment(Pos.CENTER);
        titleCard.setMaxWidth(760);
        titleCard.setPadding(new Insets(54, 68, 48, 68));
        titleCard.setStyle("-fx-background-color:rgba(13,17,23,0.78);"
            + "-fx-background-radius:24; -fx-border-color:rgba(218,177,98,0.55);"
            + "-fx-border-width:1.5; -fx-border-radius:24;");

        Label emblem = new Label("✦     ⚔     ✦");
        emblem.setStyle("-fx-text-fill:#e1b966; -fx-font-size:20px;");
        Label eyebrow = new Label("A DECK-BUILDING ADVENTURE");
        eyebrow.setStyle("-fx-text-fill:#d4b674; -fx-font-size:12px;"
            + "-fx-font-weight:bold;");
        Label title = new Label("CARD GAME");
        title.setStyle("-fx-text-fill:#fff2d5; -fx-font-size:54px;"
            + "-fx-font-weight:bold; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.65), 12, 0, 0, 3);");
        Label subtitle = new Label("Build your deck. Face the unknown. Survive the run.");
        subtitle.setStyle("-fx-text-fill:#c2c6cc; -fx-font-size:16px;");
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);

        HBox features = new HBox(10,
            feature("⚔", "TACTICAL COMBAT"),
            feature("✦", "RARE RELICS"),
            feature("♧", "BUILD YOUR DECK"));
        features.setAlignment(Pos.CENTER);
        features.setPadding(new Insets(12, 0, 8, 0));

        Button startButton = new Button("BEGIN YOUR RUN");
        startButton.setMinWidth(230);
        startButton.setMinHeight(48);
        startButton.setStyle("-fx-background-color:linear-gradient(to bottom,#d7ad59,#a87530);"
            + "-fx-text-fill:#1c1710; -fx-font-size:15px; -fx-font-weight:bold;"
            + "-fx-padding:12 30; -fx-background-radius:9; -fx-cursor:hand;"
            + "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 3);");
        startButton.setOnAction(event -> onStart.run());
        Label hint = new Label("Press 1–7 to play cards  ·  E to end your turn");
        hint.setStyle("-fx-text-fill:#8f969f; -fx-font-size:11px;");

        titleCard.getChildren().addAll(emblem, eyebrow, title, subtitle, features,
            startButton, hint);
        root.getChildren().add(titleCard);
        return root;
    }

    private static VBox feature(String iconText, String text) {
        Label icon = new Label(iconText);
        icon.setStyle("-fx-text-fill:#e2bd70; -fx-font-size:18px;");
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:#d5d0c6; -fx-font-size:10px;"
            + "-fx-font-weight:bold;");
        VBox feature = new VBox(6, icon, label);
        feature.setAlignment(Pos.CENTER);
        feature.setPadding(new Insets(12, 15, 12, 15));
        feature.setStyle("-fx-background-color:#242a33; -fx-background-radius:10;"
            + "-fx-border-color:#3b414a; -fx-border-radius:10;");
        return feature;
    }
}
