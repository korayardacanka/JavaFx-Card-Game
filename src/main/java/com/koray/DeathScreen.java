package com.koray;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Builds the game-over screen. */
public final class DeathScreen {

    private DeathScreen() {}

    public static VBox create(RunSummary summary, Runnable onRestart, Runnable onMainMenu) {
        VBox root = new VBox(20);
        root.setStyle(UIConstants.STYLE_CENTER_PADDING + UIConstants.STYLE_DARK_BG);
        Label title = new Label("YOU DIED!");
        title.setStyle(UIConstants.STYLE_TITLE_LARGE + UIConstants.STYLE_RED_TEXT);
        String relics = summary.relics().isEmpty()
            ? "None" : String.join(", ", summary.relics());
        Label runSummary = new Label("Reached level: " + summary.level()
            + "\nGold collected: " + summary.gold()
            + "\nRelics: " + relics
            + "\nDeck size: " + summary.deckSize());
        runSummary.setStyle("-fx-text-fill:white; -fx-font-size:16px;");
        runSummary.setWrapText(true);
        Button restartButton = new Button("Play Again");
        restartButton.setStyle(UIConstants.STYLE_BUTTON_LARGE);
        restartButton.setOnAction(event -> onRestart.run());
        Button menuButton = new Button("Main Menu");
        menuButton.setStyle(UIConstants.STYLE_BUTTON_LARGE);
        menuButton.setOnAction(event -> onMainMenu.run());
        root.getChildren().addAll(title, runSummary, restartButton, menuButton);
        return root;
    }
}
