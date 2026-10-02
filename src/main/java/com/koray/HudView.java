package com.koray;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Owns the battle HUD layout and refreshes its displayed game state. */
public class HudView {

    private final Label goldLabel = new Label();
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
        levelLabel.setStyle("-fx-text-fill:white; -fx-font-size:18px; -fx-font-weight:bold;");
        topPanel = new HBox(levelLabel);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(10));
        topPanel.setStyle("-fx-background-color: rgba(0,0,0,0.45);");

        styleHUDLabel(goldLabel);
        styleHUDLabel(energyLabel);
        styleHUDLabel(hpLabel);
        styleHUDLabel(shieldLabel);
        hpBar.setPrefWidth(UIConstants.PROGRESS_BAR_WIDTH);
        shieldBar.setStyle("-fx-accent: #3388cc;");
        shieldBar.setPrefWidth(UIConstants.PROGRESS_BAR_WIDTH);
        relicsBox.setMaxWidth(UIConstants.HUD_PANEL_WIDTH);
        leftPanel = new VBox(5, hpLabel, hpBar, shieldLabel, shieldBar,
            energyLabel, goldLabel, relicsBox);
        leftPanel.setPadding(new Insets(14));
        leftPanel.setStyle("-fx-background-color: rgba(0,0,0,0.52); -fx-background-radius: 0 10 10 0;");

        styleHUDLabel(enemyHpLabel);
        enemyHpBar.setStyle("-fx-accent: #cc3333;");
        enemyHpBar.setPrefWidth(UIConstants.PROGRESS_BAR_WIDTH);
        statusLabel.setStyle("-fx-text-fill: #aaffaa; -fx-font-size:12px;");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(UIConstants.HUD_PANEL_WIDTH);
        rightPanel = new VBox(5, enemyHpLabel, enemyHpBar, statusLabel);
        rightPanel.setPadding(new Insets(14));
        rightPanel.setStyle("-fx-background-color: rgba(0,0,0,0.52); -fx-background-radius: 10 0 0 10;");
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
        goldLabel.setText("Gold: " + game.player.getGold());
        energyLabel.setText("Energy: " + game.player.getEnergy() + " / " + game.maxEnergy);
        levelLabel.setText("LEVEL " + game.level);
        hpLabel.setText("❤  " + game.player.getHp() + " / " + game.player.getMaxHp());

        double playerHealthRatio = (double) game.player.getHp() / game.player.getMaxHp();
        hpBar.setProgress(Math.max(0, playerHealthRatio));
        hpBar.setStyle("-fx-accent: " + healthColor(playerHealthRatio) + ";");

        if (game.player.getShield() > 0) {
            shieldLabel.setText("🛡  " + game.player.getShield());
            shieldBar.setProgress(Math.min(1.0, game.player.getShield() / UIConstants.SHIELD_MAX));
            shieldLabel.setVisible(true);
            shieldBar.setVisible(true);
        } else {
            shieldLabel.setVisible(false);
            shieldBar.setVisible(false);
        }

        Enemy enemy = game.enemy;
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
        statusLabel.setText(statuses.toString().trim());

        relicsBox.getChildren().clear();
        for (RelicItem relic : game.ownedRelics) {
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

    private static void styleHUDLabel(Label label) {
        label.setStyle("-fx-text-fill: white; -fx-font-size:13px;");
    }
}
