package com.koray;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Builds the start screen. */
public final class StartScreen {

    private StartScreen() {}

    public static VBox create(Runnable onStart) {
        VBox root = new VBox(20);
        root.setStyle(UIConstants.STYLE_CENTER_PADDING);
        Label title = new Label("CARD GAME");
        title.setStyle(UIConstants.STYLE_TITLE_LARGE);
        Button startButton = new Button("Start");
        startButton.setStyle(UIConstants.STYLE_BUTTON_LARGE);
        startButton.setOnAction(event -> onStart.run());
        root.getChildren().addAll(title, startButton);
        root.setAlignment(Pos.CENTER);
        return root;
    }
}
