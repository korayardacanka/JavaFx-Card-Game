package com.koray.ui;

import com.koray.cards.Card;
import com.koray.combat.BattleController;
import com.koray.core.Game;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Owns the card hand, card rendering, and turn controls. */
public class HandView {

    private final Game game;
    private final Supplier<BattleController> controllerSupplier;
    private final HBox cardsBox = new HBox(UIConstants.HAND_BOX_SPACING);
    private final List<VBox> cardBoxes = new ArrayList<>();
    private final Label log = new Label();
    private final Button endTurnButton = new Button("End Turn (E)");
    private final Button rerollButton = new Button("Reroll Hand (10 Gold)");
    private final VBox root;

    public HandView(Game game, Supplier<BattleController> controllerSupplier) {
        this.game = game;
        this.controllerSupplier = controllerSupplier;
        cardsBox.setAlignment(Pos.CENTER);

        log.setStyle("-fx-text-fill: #ffdd88; -fx-font-size:13px;");
        endTurnButton.setStyle("-fx-font-size:14px; -fx-padding: 6 20;");
        endTurnButton.setOnAction(event -> {
            BattleController controller = this.controllerSupplier.get();
            if (controller != null && controller.canPlayerAct()) {
                controller.handleEndTurn();
            }
        });
        rerollButton.setStyle("-fx-font-size:14px; -fx-padding: 6 20;");
        rerollButton.setOnAction(event -> {
            BattleController controller = this.controllerSupplier.get();
            if (controller != null) {
                controller.handleHandReroll();
            }
        });
        HBox turnControls = new HBox(10, endTurnButton, rerollButton);
        turnControls.setAlignment(Pos.CENTER);

        root = new VBox(6, cardsBox, turnControls, log);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: rgba(0,0,0,0.55); -fx-padding:8;");
    }

    public VBox getView() {
        return root;
    }

    public void update(BattleController controller) {
        cardsBox.getChildren().clear();
        cardBoxes.clear();
        for (Card card : game.getPlayer().getHand()) {
            VBox cardBox = createCard(card, controller);
            cardBoxes.add(cardBox);
            cardsBox.getChildren().add(cardBox);
        }
        endTurnButton.setDisable(controller == null || !controller.canPlayerAct());
        rerollButton.setDisable(controller == null || !controller.canPlayerAct()
            || game.getPlayer().getHand().isEmpty()
            || game.getPlayer().getGold() < BattleController.HAND_REROLL_COST);
    }

    public void setLog(String message) {
        log.setText(message);
    }

    public void playCardAt(int index, BattleController controller) {
        if (controller == null || !controller.canPlayerAct()
                || index < 0 || index >= game.getPlayer().getHand().size()
                || index >= cardBoxes.size()) {
            return;
        }

        Card card = game.getPlayer().getHand().get(index);
        if (game.getPlayer().getEnergy() < card.getCost()) return;
        playCard(card, cardBoxes.get(index), controller);
    }

    private VBox createCard(Card card, BattleController controller) {
        VBox box = new VBox(5);
        box.setPrefSize(UIConstants.CARD_SIZE_WIDTH, UIConstants.CARD_SIZE_HEIGHT);

        String bg = "#ffffff", border = "#000000", borderWidth = "1";
        if (card.getDesign() != null) {
            bg = card.getDesign().getBackground();
            border = card.getDesign().getBorder();
            borderWidth = "3";
        }
        box.setStyle("-fx-background-color:" + bg + "; -fx-border-color:" + border
            + "; -fx-border-width:" + borderWidth
            + "; -fx-background-radius:8; -fx-border-radius:8; -fx-padding:10;");

        Label name = new Label(card.getEffect().icon() + card.getName());
        name.setStyle("-fx-font-weight:bold; -fx-font-size:13px;");
        Label effectDescription = new Label(card.getEffect().describe());
        effectDescription.setWrapText(true);
        effectDescription.setMaxWidth(UIConstants.CARD_SIZE_WIDTH - 20);
        effectDescription.setStyle("-fx-font-size:11px;");
        Label cost = new Label("Cost: " + card.getCost());
        Button play = new Button("Play");
        play.setDisable(controller == null || controller.isTurnLocked()
            || game.getPlayer().getEnergy() < card.getCost());
        play.setOnAction(event -> playCard(card, box, controller));
        box.getChildren().addAll(name, effectDescription, cost, play);
        return box;
    }

    private void playCard(Card card, VBox box, BattleController controller) {
        if (controller != null && controller.canPlayerAct()
                && game.getPlayer().getEnergy() >= card.getCost()) {
            controller.handleCardPlay(card, box);
        }
    }
}


