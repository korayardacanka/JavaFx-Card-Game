package com.koray;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

/**
 * JavaFX Application entry point.
 * Responsible for:
 *   - Screen management (start screen, game scene, death screen)
 *   - UI layout construction
 *   - UI state updates (labels, bars, hand rendering)
 *   - Wiring together DeckManager, AnimationPlayer, and BattleController
 *
 * Game logic, deck operations, and animations each live in their own class.
 */
public class Main extends Application {

    // ── Application state ────────────────────────────────────────────────────
    private int     currentBiome  = 1;
    private Game    game          = new Game();
    private Stage   primaryStage;
    private StackPane gameRoot;   // root StackPane kept for toast overlays

    // ── Extracted collaborators ───────────────────────────────────────────────
    private DeckManager       deckManager;
    private AnimationPlayer   animator;
    private BattleController  battleController;

    // ── UI components ─────────────────────────────────────────────────────────
    private ImageView  playerView  = new ImageView();
    private StackPane  enemyVoid   = new StackPane();
    private ImageView  background  = new ImageView();

    private Label goldLabel    = new Label();
    private Label enemyHpLabel = new Label();
    private Label lvlLabel     = new Label();
    private Label log          = new Label();
    private Label energyLabel  = new Label();
    private Label hpLabel      = new Label();
    private Label shieldLabel  = new Label();
    private Label statusLabel  = new Label();
    private Label relicsLabel  = new Label();  // shows owned relic icons in the HUD

    private ProgressBar hpBar      = new ProgressBar();
    private ProgressBar shieldBar  = new ProgressBar();
    private ProgressBar enemyHpBar = new ProgressBar();

    private HBox handBox = new HBox(UIConstants.HAND_BOX_SPACING);

    // =========================================================================
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showStartScreen();
    }

    // ── Screen management ─────────────────────────────────────────────────────

    /** Shows the main menu screen with a Start button. */
    private void showStartScreen() {
        VBox root = new VBox(20);
        root.setStyle(UIConstants.STYLE_CENTER_PADDING);
        Label title = new Label("CARD GAME");
        title.setStyle(UIConstants.STYLE_TITLE_LARGE);
        Button startBtn = new Button("Başlat");
        startBtn.setStyle(UIConstants.STYLE_BUTTON_LARGE);
        startBtn.setOnAction(e -> { initializeGame(); startGame(); });
        root.getChildren().addAll(title, startBtn);
        setScene(root);
        primaryStage.show();
    }

    /** Shows the game-over screen with restart and menu buttons. */
    private void showDeathScreen() {
        VBox root = new VBox(20);
        root.setStyle(UIConstants.STYLE_CENTER_PADDING + UIConstants.STYLE_DARK_BG);
        Label title = new Label("ÖLDÜN!");
        title.setStyle(UIConstants.STYLE_TITLE_LARGE + UIConstants.STYLE_RED_TEXT);
        Button restartBtn = new Button("Tekrar Oyna");
        restartBtn.setStyle(UIConstants.STYLE_BUTTON_LARGE);
        restartBtn.setOnAction(e -> { initializeGame(); startGame(); });
        Button menuBtn = new Button("Ana Menü");
        menuBtn.setStyle(UIConstants.STYLE_BUTTON_LARGE);
        menuBtn.setOnAction(e -> showStartScreen());
        root.getChildren().addAll(title, restartBtn, menuBtn);
        setScene(root);
    }

    /**
     * Initialises a fresh game and wires all collaborators together.
     * Called both at first start and on restart.
     */
    private void initializeGame() {
        game = new Game();
        game.eventBus = new EventBus();
        game.eventBus.subscribe(new RewardSystem(game));
        game.eventBus.subscribe(new UIObserver(game, this));

        deckManager = new DeckManager(game);

        // AnimationPlayer and BattleController are created in startGame()
        // after playerView is (re)instantiated.
    }

    // ── Game scene setup ──────────────────────────────────────────────────────

    /**
     * Builds the full game scene, instantiates collaborators, and shows it.
     * Called after initializeGame().
     */
    private void startGame() {
        deckManager.resetPlayerDeck();
        deckManager.drawHand();

        double W = Screen.getPrimary().getVisualBounds().getWidth();
        double H = Screen.getPrimary().getVisualBounds().getHeight();

        // Background
        updateBackground();
        background.setFitWidth(W);
        background.setFitHeight(H);
        background.setPreserveRatio(false);

        // Player sprite
        playerView = new ImageView();
        playerView.setFitWidth(UIConstants.PLAYER_VIEW_WIDTH);
        playerView.setPreserveRatio(true);

        animator = new AnimationPlayer(playerView, this);
        animator.playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);

        // Wire BattleController with all callbacks
        battleController = new BattleController(
            game,
            deckManager,
            animator,
            this::updateUI,
            this::showDeathScreen,
            this::onEnemyDeath,
            msg -> log.setText(msg)
        );

        // Enemy display
        enemyVoid = new StackPane();
        enemyVoid.setPrefSize(UIConstants.ENEMY_SIZE, UIConstants.ENEMY_SIZE);
        updateEnemyVisuals();

        // Layout
        VBox playerSlot = new VBox(playerView);
        playerSlot.setAlignment(Pos.BOTTOM_CENTER);
        VBox enemySlot = new VBox(enemyVoid);
        enemySlot.setAlignment(Pos.BOTTOM_CENTER);

        lvlLabel = new Label();
        lvlLabel.setStyle("-fx-text-fill:white; -fx-font-size:18px; -fx-font-weight:bold;");
        HBox topHUD = new HBox(lvlLabel);
        topHUD.setAlignment(Pos.CENTER);
        topHUD.setPadding(new Insets(10));
        topHUD.setStyle("-fx-background-color: rgba(0,0,0,0.45);");

        goldLabel = new Label(); energyLabel = new Label();
        hpLabel   = new Label(); shieldLabel = new Label();
        styleHUDLabel(goldLabel); styleHUDLabel(energyLabel);
        styleHUDLabel(hpLabel);   styleHUDLabel(shieldLabel);

        hpBar = new ProgressBar(1.0); hpBar.setPrefWidth(160);
        shieldBar = new ProgressBar(0.0);
        shieldBar.setStyle("-fx-accent: #3388cc;"); shieldBar.setPrefWidth(160);

        relicsLabel = new Label();
        relicsLabel.setStyle("-fx-text-fill: #ffe866; -fx-font-size:16px;");
        relicsLabel.setWrapText(true);
        relicsLabel.setMaxWidth(170);

        VBox leftPanel = new VBox(5, hpLabel, hpBar, shieldLabel, shieldBar, energyLabel, goldLabel, relicsLabel);
        leftPanel.setPadding(new Insets(14));
        leftPanel.setStyle("-fx-background-color: rgba(0,0,0,0.52); -fx-background-radius: 0 10 10 0;");

        enemyHpLabel = new Label(); styleHUDLabel(enemyHpLabel);
        enemyHpBar   = new ProgressBar(1.0);
        enemyHpBar.setStyle("-fx-accent: #cc3333;"); enemyHpBar.setPrefWidth(160);
        statusLabel  = new Label();
        statusLabel.setStyle("-fx-text-fill: #aaffaa; -fx-font-size:12px;");
        statusLabel.setWrapText(true); statusLabel.setMaxWidth(170);

        VBox rightPanel = new VBox(5, enemyHpLabel, enemyHpBar, statusLabel);
        rightPanel.setPadding(new Insets(14));
        rightPanel.setStyle("-fx-background-color: rgba(0,0,0,0.52); -fx-background-radius: 10 0 0 10;");

        handBox = new HBox(UIConstants.HAND_BOX_SPACING);
        handBox.setAlignment(Pos.CENTER);
        log = new Label();
        log.setStyle("-fx-text-fill: #ffdd88; -fx-font-size:13px;");
        Button endTurn = new Button("End Turn (E)");
        endTurn.setStyle("-fx-font-size:14px; -fx-padding: 6 20;");
        endTurn.setOnAction(e -> battleController.handleEndTurn());

        VBox bottomPanel = new VBox(6, handBox, endTurn, log);
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setPadding(new Insets(8));
        bottomPanel.setStyle("-fx-background-color: rgba(0,0,0,0.55);");

        AnchorPane ui = new AnchorPane();
        AnchorPane.setTopAnchor(topHUD, 0.0);
        AnchorPane.setLeftAnchor(topHUD, 0.0);
        AnchorPane.setRightAnchor(topHUD, 0.0);
        AnchorPane.setLeftAnchor(leftPanel, 0.0);
        AnchorPane.setTopAnchor(leftPanel, 50.0);
        AnchorPane.setRightAnchor(rightPanel, 0.0);
        AnchorPane.setTopAnchor(rightPanel, 50.0);
        AnchorPane.setLeftAnchor(playerSlot, W * 0.20);
        AnchorPane.setBottomAnchor(playerSlot, 230.0);
        AnchorPane.setRightAnchor(enemySlot, W * 0.20);
        AnchorPane.setBottomAnchor(enemySlot, 230.0);
        AnchorPane.setBottomAnchor(bottomPanel, 0.0);
        AnchorPane.setLeftAnchor(bottomPanel, 0.0);
        AnchorPane.setRightAnchor(bottomPanel, 0.0);
        ui.getChildren().addAll(playerSlot, enemySlot, topHUD, leftPanel, rightPanel, bottomPanel);

        StackPane root = new StackPane(background, ui);
        gameRoot = root;
        setScene(root);
        updateUI();
    }

    /** Sets a styled label for HUD use (white text, 13px). */
    private void styleHUDLabel(Label lbl) {
        lbl.setStyle("-fx-text-fill: white; -fx-font-size:13px;");
    }

    /** Creates and sets the scene, binding the E key to handleEndTurn. */
    private void setScene(Pane root) {
        Scene scene = new Scene(root,
            Screen.getPrimary().getVisualBounds().getWidth(),
            Screen.getPrimary().getVisualBounds().getHeight());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.E) {
                battleController.handleEndTurn();
            }
        });
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
    }

    // ── UI updates ────────────────────────────────────────────────────────────

    /**
     * Refreshes all HUD labels, progress bars, and the hand display.
     * Called by UIObserver on every game event.
     */
    public void updateUI() {
        goldLabel.setText("Gold: "   + game.player.getGold());
        energyLabel.setText("Energy: " + game.player.getEnergy() + " / " + game.maxEnergy);
        lvlLabel.setText("LEVEL "    + game.level);
        hpLabel.setText("❤  "       + game.player.getHp() + " / " + game.player.getMaxHp());

        double pr = (double) game.player.getHp() / game.player.getMaxHp();
        hpBar.setProgress(Math.max(0, pr));
        hpBar.setStyle("-fx-accent: " + (pr > 0.5 ? "#cc3333" : pr > 0.25 ? "#cc8800" : "#880000") + ";");

        if (game.player.getShield() > 0) {
            shieldLabel.setText("🛡  " + game.player.getShield());
            shieldBar.setProgress(Math.min(1.0, game.player.getShield() / UIConstants.SHIELD_MAX));
            shieldLabel.setVisible(true);
            shieldBar.setVisible(true);
        } else {
            shieldLabel.setVisible(false);
            shieldBar.setVisible(false);
        }

        enemyHpLabel.setText(
            (game.enemy.isBoss() ? "⚠️  " : "") + game.enemy.getName()
            + "\nHP: "  + game.enemy.getHp()  + " / " + game.enemy.getMaxHp()
            + "\nATK: " + game.enemy.getAttackDamage()
            + (game.enemy.isFrozen() ? "  ❄ DONMUŞ" : "")
        );

        double er = (double) game.enemy.getHp() / game.enemy.getMaxHp();
        enemyHpBar.setProgress(Math.max(0, er));
        enemyHpBar.setStyle("-fx-accent: " + (er > 0.5 ? "#cc3333" : er > 0.25 ? "#cc8800" : "#880000") + ";");

        StringBuilder sb = new StringBuilder();
        if (game.enemy.getPoisonStacks() > 0) sb.append("☠ x").append(game.enemy.getPoisonStacks()).append("  ");
        if (game.enemy.getBurnStacks()   > 0) sb.append("🔥 x").append(game.enemy.getBurnStacks()).append("  ");
        if (game.enemy.getFreezeTurns()  > 0) sb.append("❄ x").append(game.enemy.getFreezeTurns()).append("  ");
        statusLabel.setText(sb.toString().trim());

        if (!game.lastEvent.isEmpty()) log.setText(game.lastEvent);

        // Show owned relic icons in the left HUD panel
        if (game.ownedRelics.isEmpty()) {
            relicsLabel.setText("");
        } else {
            StringBuilder relicIcons = new StringBuilder();
            for (RelicItem r : game.ownedRelics) {
                // Extract the leading emoji from the relic name (first "word")
                String[] parts = r.name.split(" ", 2);
                relicIcons.append(parts[0]).append(" ");
            }
            relicsLabel.setText(relicIcons.toString().trim());
        }

        updateHandUI();
    }

    /** Rebuilds the hand display from the current hand list. */
    private void updateHandUI() {
        handBox.getChildren().clear();
        for (Card c : game.player.hand) {
            handBox.getChildren().add(createCard(c));
        }
    }

    /**
     * Creates the visual VBox for a single card.
     * Reads color from the card's design decorator chain.
     * Wires the Play button to BattleController.handleCardPlay().
     */
    private VBox createCard(Card c) {
        VBox box = new VBox(5);
        box.setPrefSize(UIConstants.CARD_SIZE_WIDTH, UIConstants.CARD_SIZE_HEIGHT);

        String bg = "#ffffff", border = "#000000", bw = "1";
        if (c.design != null) {
            bg = c.design.getBackground();
            border = c.design.getBorder();
            bw = "3";
        }
        box.setStyle("-fx-background-color:" + bg + "; -fx-border-color:" + border
            + "; -fx-border-width:" + bw
            + "; -fx-background-radius:8; -fx-border-radius:8; -fx-padding:10;");

        String emoji = "";
        if      (c.effect instanceof DamageEffect) emoji = "⚔ ";
        else if (c.effect instanceof HealEffect)   emoji = "💚 ";
        else if (c.effect instanceof ShieldEffect) emoji = "🛡 ";
        else if (c.effect instanceof PoisonEffect) emoji = "☠ ";
        else if (c.effect instanceof BurnEffect)   emoji = "🔥 ";
        else if (c.effect instanceof FreezeEffect) emoji = "❄ ";

        Label name = new Label(emoji + c.name);
        name.setStyle("-fx-font-weight:bold; -fx-font-size:13px;");
        Label cost = new Label("Cost: " + c.cost);
        Button play = new Button("Play");
        play.setOnAction(e -> battleController.handleCardPlay(c, box));
        box.getChildren().addAll(name, cost, play);
        return box;
    }

    /** Applies the EnemyDesign decorator and updates the enemy container style. */
    private void updateEnemyVisuals() {
        EnemyDesign design = new BaseEnemyDesign();
        if (game.enemy.isBoss()) {
            design = new BossBorderDecorator(design, game.level);
        }
        enemyVoid.setStyle("-fx-background-color: black; -fx-background-radius: 50%; "
            + design.getBorderStyle() + design.getEffect());
    }

    /**
     * Callback triggered by BattleController after an enemy dies.
     * Updates background, enemy visuals, refreshes the UI, and opens the shop.
     */
    private void onEnemyDeath() {
        updateBackground();
        updateEnemyVisuals();
        updateUI();
        Shop.open(game);
    }

    // ── Background ────────────────────────────────────────────────────────────

    /**
     * Loads the correct biome background for the current level.
     * Biome cycles every 5 levels across 4 variants.
     * On the very first load sets the image directly; subsequent changes cross-fade.
     */
    private void updateBackground() {
        int biome = ((game.level - 1) / 5) % 4 + 1;

        if (biome == currentBiome && background.getImage() != null) return;
        currentBiome = biome;

        String path = switch (biome) {
            case 1  -> "/assets/game_background_1.png";
            case 2  -> "/assets/game_background_2.png";
            case 3  -> "/assets/game_background_3.png";
            default -> "/assets/game_background_4.png";
        };

        Image newImage = new Image(
            Objects.requireNonNull(getClass().getResourceAsStream(path))
        );

        if (background.getImage() == null) {
            background.setImage(newImage);
            return;
        }

        crossFadeBackground(newImage);
    }

    /**
     * Displays a temporary toast notification when a relic is purchased.
     * A label with the relic's name floats in the top-center of the screen,
     * fades in, stays for 1.5 seconds, then fades out and is removed.
     *
     * @param relic the relic that was just purchased
     */
    public void showRelicToast(RelicItem relic) {
        if (gameRoot == null) return;

        Label toast = new Label("✨ " + relic.name + " alındı!");
        toast.setStyle(
            "-fx-background-color: rgba(30,20,0,0.82);" +
            "-fx-text-fill: #ffe866;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 24;" +
            "-fx-background-radius: 12;"
        );
        toast.setOpacity(0);

        // Position at top-center
        javafx.scene.layout.StackPane.setAlignment(toast, javafx.geometry.Pos.TOP_CENTER);
        javafx.scene.layout.StackPane.setMargin(toast, new javafx.geometry.Insets(80, 0, 0, 0));
        gameRoot.getChildren().add(toast);

        // Fade in → pause → fade out → remove
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setToValue(1.0);

        PauseTransition pause = new PauseTransition(Duration.millis(1500));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> gameRoot.getChildren().remove(toast));

        new SequentialTransition(fadeIn, pause, fadeOut).play();
    }

    /**
     * Smoothly transitions to a new background image over 1.5 seconds.
     * An overlay ImageView fades in on top of the existing background,
     * then the base image is swapped and the overlay removed.
     */
    private void crossFadeBackground(Image newImage) {
        ImageView overlay = new ImageView(newImage);
        overlay.setFitWidth(background.getFitWidth());
        overlay.setFitHeight(background.getFitHeight());
        overlay.setOpacity(0);

        StackPane root = (StackPane) primaryStage.getScene().getRoot();
        root.getChildren().add(1, overlay);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), overlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setOnFinished(e -> {
            background.setImage(newImage);
            root.getChildren().remove(overlay);
        });
        fadeIn.play();
    }

    public static void main(String[] args) { launch(); }
}