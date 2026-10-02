package com.koray;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Owns the battle HUD layout and refreshes its displayed game state. */
public class HudView {

    private final Label goldLabel = new Label();
    private final Label deckCountsLabel = new Label();
    private final Label energyLabel = new Label();
    private final Label hpLabel = new Label();
    private final Label shieldLabel = new Label();
    private final Label levelLabel = new Label();
    private final Label enemyHpLabel = new Label();
    private final Label statusLabel = new Label();
    private final ProgressBar hpBar = new ProgressBar(1.0);
    private final ProgressBar shieldBar = new ProgressBar(0.0);
    private final ProgressBar enemyHpBar = new ProgressBar(1.0);
    private final FlowPane relicsBox = new FlowPane(5, 3);
    private final HBox topPanel;
    private final VBox leftPanel;
    private final VBox rightPanel;

    public HudView() {
        levelLabel.setStyle("-fx-text-fill:#f5e5bd; -fx-font-size:15px;"
            + "-fx-font-weight:bold; -fx-background-color:rgba(17,20,26,0.84);"
            + "-fx-background-radius:18; -fx-border-color:rgba(222,185,111,0.65);"
            + "-fx-border-radius:18; -fx-padding:7 20;");
        topPanel = new HBox(levelLabel);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(12));

        styleHUDLabel(goldLabel);
        styleHUDLabel(deckCountsLabel);
        styleHUDLabel(energyLabel);
        styleHUDLabel(hpLabel);
        styleHUDLabel(shieldLabel);
        hpBar.setPrefWidth(UIConstants.HUD_PANEL_WIDTH);
        hpBar.setPrefHeight(10);
        hpBar.setStyle("-fx-accent:#d66b67;");
        shieldBar.setStyle("-fx-accent: #3388cc;");
        shieldBar.setPrefWidth(UIConstants.HUD_PANEL_WIDTH);
        shieldBar.setPrefHeight(8);
        relicsBox.setMaxWidth(UIConstants.HUD_PANEL_WIDTH);
        relicsBox.setPadding(new Insets(2, 0, 0, 0));

        Label heroHeading = sectionHeading("HERO STATUS");
        Label resourcesHeading = sectionHeading("RESOURCES");
        Label relicsHeading = sectionHeading("RELICS");
        leftPanel = new VBox(8, heroHeading, hpLabel, hpBar, shieldLabel, shieldBar,
            separator(), resourcesHeading, energyLabel, goldLabel, deckCountsLabel,
            separator(), relicsHeading, relicsBox);
        leftPanel.setPrefWidth(UIConstants.HUD_PANEL_WIDTH + 36);
        leftPanel.setPadding(new Insets(16));
        leftPanel.setStyle(panelStyle(true));

        styleHUDLabel(enemyHpLabel);
        enemyHpLabel.setStyle("-fx-text-fill:#f1e9df; -fx-font-size:13px;"
            + "-fx-font-weight:bold; -fx-line-spacing:4px;");
        enemyHpBar.setStyle("-fx-accent:#c95c59;");
        enemyHpBar.setPrefWidth(UIConstants.HUD_PANEL_WIDTH);
        enemyHpBar.setPrefHeight(10);
        statusLabel.setStyle("-fx-text-fill:#c6d9bd; -fx-font-size:12px;"
            + "-fx-background-color:rgba(99,130,90,0.18);"
            + "-fx-background-radius:7; -fx-padding:7 9;");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(UIConstants.HUD_PANEL_WIDTH);
        Label enemyHeading = sectionHeading("OPPONENT");
        Label statusHeading = sectionHeading("CONDITIONS");
        rightPanel = new VBox(9, enemyHeading, enemyHpLabel, enemyHpBar,
            separator(), statusHeading, statusLabel);
        rightPanel.setPrefWidth(UIConstants.HUD_PANEL_WIDTH + 36);
        rightPanel.setPadding(new Insets(16));
        rightPanel.setStyle(panelStyle(false));
    }

    public HBox getTopPanel() {
        return topPanel;
    }

    public VBox getLeftPanel() {
        return leftPanel;
    }

    public VBox getRightPanel() {
        return rightPanel;
    }

    public void update(Game game) {
        goldLabel.setText("✦  Gold: " + game.getPlayer().getGold());
        deckCountsLabel.setText("▤  Draw " + game.getPlayer().getDeck().size()
            + "   ·   Discard " + game.getPlayer().getDiscard().size()
            + "\nTotal cards " + game.getPlayer().getTotalCardCount());
        energyLabel.setText("⚡  Energy: " + game.getPlayer().getEnergy()
            + " / " + game.getMaxEnergy());
        levelLabel.setText("LEVEL " + game.getLevel());
        hpLabel.setText("❤  " + game.getPlayer().getHp() + " / " + game.getPlayer().getMaxHp());

        double playerHealthRatio = (double) game.getPlayer().getHp() / game.getPlayer().getMaxHp();
        hpBar.setProgress(Math.max(0, playerHealthRatio));
        hpBar.setStyle("-fx-accent: " + healthColor(playerHealthRatio) + ";");

        if (game.getPlayer().getShield() > 0) {
            shieldLabel.setText("🛡  " + game.getPlayer().getShield());
            shieldBar.setProgress(Math.min(1.0, game.getPlayer().getShield() / UIConstants.SHIELD_MAX));
            shieldLabel.setVisible(true);
            shieldBar.setVisible(true);
        } else {
            shieldLabel.setVisible(false);
            shieldBar.setVisible(false);
        }

        Enemy enemy = game.getEnemy();
        enemyHpLabel.setText(
            (enemy.isBoss() ? "⚠️  " : "") + enemy.getName()
            + "\nHP: " + enemy.getHp() + " / " + enemy.getMaxHp()
            + "\nATK: " + enemy.getAttackDamage()
            + (enemy.isFrozen() ? "  ❄ FROZEN" : "")
        );
        double enemyHealthRatio = (double) enemy.getHp() / enemy.getMaxHp();
        enemyHpBar.setProgress(Math.max(0, enemyHealthRatio));
        enemyHpBar.setStyle("-fx-accent: " + healthColor(enemyHealthRatio) + ";");

        StringBuilder statuses = new StringBuilder();
        if (enemy.getPoisonStacks() > 0) statuses.append("☠ x").append(enemy.getPoisonStacks()).append("  ");
        if (enemy.getBurnStacks() > 0) statuses.append("🔥 x").append(enemy.getBurnStacks()).append("  ");
        if (enemy.getFreezeTurns() > 0) statuses.append("❄ x").append(enemy.getFreezeTurns()).append("  ");
        statusLabel.setText(statuses.isEmpty() ? "No active effects" : statuses.toString().trim());

        relicsBox.getChildren().clear();
        for (RelicItem relic : game.getOwnedRelics()) {
            String[] nameParts = relic.name.split(" ", 2);
            Label icon = new Label(nameParts[0]);
            icon.setStyle("-fx-text-fill: #ffe866; -fx-font-size:16px;");
            Tooltip.install(icon, new Tooltip(relic.name + "\n" + relic.description));
            relicsBox.getChildren().add(icon);
        }
    }

    private static String healthColor(double ratio) {
        return ratio > 0.5 ? "#cc3333" : ratio > 0.25 ? "#cc8800" : "#880000";
    }

    private static Label sectionHeading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill:#d7b974; -fx-font-size:10px;"
            + "-fx-font-weight:bold;");
        return label;
    }

    private static Region separator() {
        Region line = new Region();
        line.setPrefHeight(1);
        line.setMaxWidth(Double.MAX_VALUE);
        line.setStyle("-fx-background-color:rgba(218,207,185,0.18);");
        return line;
    }

    private static String panelStyle(boolean left) {
        String edge = left ? "0 12 12 0" : "12 0 0 12";
        String border = left
            ? "-fx-border-color:rgba(225,190,117,0.35) transparent rgba(225,190,117,0.35) transparent;"
            : "-fx-border-color:rgba(225,190,117,0.35) transparent rgba(225,190,117,0.35) rgba(225,190,117,0.35);";
        return "-fx-background-color:linear-gradient(to bottom, rgba(17,20,26,0.94),"
            + " rgba(25,27,31,0.88)); -fx-background-radius:" + edge + ";"
            + "-fx-border-radius:" + edge + "; -fx-border-width:1; " + border;
    }

    private static void styleHUDLabel(Label label) {
        label.setStyle("-fx-text-fill:#e5e1d9; -fx-font-size:12px;");
    }
}
