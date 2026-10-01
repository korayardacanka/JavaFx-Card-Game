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
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
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
    private static Stage primaryStage;
    private StackPane gameRoot;   // root StackPane kept for toast overlays

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    // ── Extracted collaborators ───────────────────────────────────────────────
    private DeckManager       deckManager;
    private AnimationPlayer   animator;
    private EnemyAnimationPlayer enemyAnimator;
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
    private Button endTurnButton;

    // =========================================================================
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
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
        if (animator != null) animator.stop();
        if (enemyAnimator != null) enemyAnimator.stop();
        Shop.closeShop();

        VBox root = new VBox(20);
        root.setStyle(UIConstants.STYLE_CENTER_PADDING + UIConstants.STYLE_DARK_BG);
        Label title = new Label("ÖLDÜN!");
        title.setStyle(UIConstants.STYLE_TITLE_LARGE + UIConstants.STYLE_RED_TEXT);
        Button restartBtn = new Button("Tekrar Oyna");
        restartBtn.setStyle(UIConstants.STYLE_BUTTON_LARGE);
        restartBtn.setOnAction(e -> { Shop.closeShop(); initializeGame(); startGame(); });
        Button menuBtn = new Button("Ana Menü");
        menuBtn.setStyle(UIConstants.STYLE_BUTTON_LARGE);
        menuBtn.setOnAction(e -> showStartScreen());
        root.getChildren().addAll(title, restartBtn, menuBtn);
        setScene(root);
    }

    /**
     * Initialises a fresh game and wires all collaborators together.
     * Called both at first start and on restart.
     *
     * Clears the old EventBus observers first to prevent stale UIObserver/
     * RewardSystem references keeping the previous game graph alive (memory leak).
     */
    private void initializeGame() {
        // Eski bus'ın abonelerini temizle — UIObserver içindeki Main referansı
        // GC root'a bağlı kalmadan eski game graph'ının collect edilmesini sağlar.
        if (game != null && game.eventBus != null) {
            game.eventBus.clearObservers();
        }

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
        Shop.closeShop();
        if (animator != null) animator.stop();
        if (enemyAnimator != null) enemyAnimator.stop();
        enemyAnimator = null;
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

        // Enemy display
        enemyVoid = new StackPane();
        enemyVoid.setPrefSize(UIConstants.ENEMY_SIZE, UIConstants.ENEMY_SIZE);
        updateEnemyVisuals();
        enemyAnimator = new EnemyAnimationPlayer(enemyVoid);
        enemyAnimator.playIdle();

        // Wire BattleController with all callbacks
        battleController = new BattleController(
            game,
            deckManager,
            animator,
            enemyAnimator,
            this::updateUI,
            this::showDeathScreen,
            this::onEnemyDeath,
            msg -> log.setText(msg)
        );

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
        endTurnButton = new Button("End Turn (E)");
        endTurnButton.setStyle("-fx-font-size:14px; -fx-padding: 6 20;");
        endTurnButton.setOnAction(e -> {
            if (battleController != null && battleController.canPlayerAct()) {
                battleController.handleEndTurn();
            }
        });

        VBox bottomPanel = new VBox(6, handBox, endTurnButton, log);
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

    /**
     * Creates and sets the scene, binding the E key to handleEndTurn.
     *
     * Guard conditions:
     *   - battleController != null : start ekranında henüz set edilmemiş olabilir
     *   - game.player.isAlive()    : death ekranında E sessize alınır,
     *                                arka planda bozuk state mutasyonu önlenir
     */
    private void setScene(Pane root) {
        Scene scene = new Scene(root,
            Screen.getPrimary().getVisualBounds().getWidth(),
            Screen.getPrimary().getVisualBounds().getHeight());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.E
                    && battleController != null
                    && battleController.canPlayerAct()) {
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
        if (endTurnButton != null && battleController != null) {
            endTurnButton.setDisable(!battleController.canPlayerAct());
        }

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
        play.setDisable(battleController.isTurnLocked());
        play.setOnAction(e -> {
            if (battleController.isTurnLocked()) return;
            battleController.handleCardPlay(c, box);
        });
        box.getChildren().addAll(name, cost, play);
        return box;
    }

    /** Applies the EnemyDesign decorator and updates the enemy container style. */
    private void updateEnemyVisuals() {
        EnemyDesign design = new BaseEnemyDesign();
        if (game.enemy.isBoss()) {
            design = new BossBorderDecorator(design, game.level);
        }
        enemyVoid.setStyle("-fx-background-color: rgba(8, 8, 12, 0.82); "
            + "-fx-background-radius: 50%; " + design.getBorderStyle() + design.getEffect());
        enemyVoid.getChildren().setAll(createEnemyPortrait(game.enemy));
        if (enemyAnimator != null) enemyAnimator.playIdle();
    }

    /** Builds a family- and phase-specific dark-fantasy enemy portrait. */
    private Pane createEnemyPortrait(Enemy enemy) {
        int family = enemy.getFamily();
        int phase = enemy.getPhase();
        boolean boss = enemy.isBoss();
        Color[] auraColors = {
            Color.rgb(155, 27, 34), Color.rgb(58, 151, 66),
            Color.rgb(219, 88, 35), Color.rgb(65, 157, 219)
        };
        Color[] trimColors = {
            Color.rgb(137, 74, 62), Color.rgb(104, 142, 76),
            Color.rgb(163, 91, 52), Color.rgb(105, 159, 190)
        };
        Color[] energyColors = {
            Color.rgb(255, 69, 43), Color.rgb(157, 255, 81),
            Color.rgb(255, 154, 48), Color.rgb(117, 226, 255)
        };
        Color[] cloakColors = {
            Color.rgb(20, 21, 27), Color.rgb(18, 29, 22),
            Color.rgb(34, 22, 19), Color.rgb(19, 28, 38)
        };
        Color auraColor = auraColors[family];
        Color trimColor = trimColors[family];
        Color energyColor = energyColors[family];

        Pane portrait = new Pane();
        portrait.setPrefSize(UIConstants.ENEMY_SIZE, UIConstants.ENEMY_SIZE);
        portrait.setMinSize(UIConstants.ENEMY_SIZE, UIConstants.ENEMY_SIZE);
        portrait.setMaxSize(UIConstants.ENEMY_SIZE, UIConstants.ENEMY_SIZE);

        Circle aura = new Circle(90, 88, 60 + phase * 3,
            Color.color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 0.12 + phase * 0.025));
        aura.setStroke(Color.color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 0.5 + phase * 0.08));
        aura.setStrokeWidth(1.2 + phase * 0.45);
        aura.setEffect(new DropShadow(14 + phase * 3,
            Color.color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 0.42 + phase * 0.08)));

        Line spear = new Line(43, 144, 65, 52);
        spear.setStroke(family == 3 ? Color.rgb(148, 213, 238) : Color.rgb(103, 110, 119));
        spear.setStrokeWidth(3 + phase * 0.25);
        spear.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.8)));
        Polygon spearHead = polygon(61, 40, 69, 55, 63, 68, 56, 53);
        spearHead.setFill(family == 3 ? Color.rgb(169, 239, 255) : Color.rgb(174, 180, 183));

        Polygon cloak = polygon(88, 48, 69, 59, 54, 83, 45, 124, 55, 151,
            63, 140, 70, 157, 80, 144, 90, 163, 100, 145, 112, 157,
            119, 139, 129, 149, 137, 118, 124, 82, 109, 58);
        cloak.setFill(cloakColors[family]);
        cloak.setStroke(trimColor);
        cloak.setStrokeWidth(2);

        Polygon torso = polygon(72, 75, 109, 75, 119, 119, 91, 143, 63, 119);
        torso.setFill(Color.rgb(46, 48, 55));
        torso.setStroke(trimColor);
        torso.setStrokeWidth(2);

        Polygon leftShoulder = polygon(72, 69, 53, 76, 44, 94, 69, 100, 82, 82);
        leftShoulder.setFill(Color.rgb(61, 56, 57));
        leftShoulder.setStroke(trimColor);
        leftShoulder.setStrokeWidth(2);
        Polygon rightShoulder = polygon(109, 68, 128, 76, 138, 93, 114, 101, 100, 81);
        rightShoulder.setFill(Color.rgb(56, 52, 55));
        rightShoulder.setStroke(trimColor);
        rightShoulder.setStrokeWidth(2);

        Polygon hood = switch (family) {
            case 1 -> polygon(66, 67, 61, 42, 72, 25, 94, 17, 119, 28,
                130, 53, 116, 76, 79, 80);
            case 2 -> polygon(67, 63, 70, 39, 84, 20, 97, 25, 113, 17,
                126, 43, 122, 67, 108, 79, 78, 77);
            case 3 -> polygon(67, 64, 68, 40, 82, 19, 96, 9, 112, 22,
                128, 43, 122, 66, 109, 79, 78, 77);
            default -> polygon(68, 63, 68, 42, 81, 21, 96, 13, 113, 23,
                126, 44, 122, 65, 109, 79, 78, 77);
        };
        hood.setFill(family == 1 ? Color.rgb(27, 39, 25) : cloakColors[family].brighter());
        hood.setStroke(trimColor);
        hood.setStrokeWidth(2.5);
        Polygon face = polygon(79, 47, 91, 37, 104, 39, 116, 49, 109, 68, 97, 75, 84, 66);
        face.setFill(family == 2 ? Color.rgb(47, 25, 17) : Color.rgb(9, 10, 14));

        Polygon leftHorn = switch (family) {
            case 1 -> polygon(77, 33, 58, 22, 70, 39, 82, 42);
            case 2 -> polygon(77, 31, 67, 8 - phase * 2, 72, 32, 82, 41);
            case 3 -> polygon(77, 31, 72, 4 - phase * 2, 78, 30, 84, 39);
            default -> polygon(77, 31, 69, 12 - phase * 2, 72, 31, 82, 41);
        };
        leftHorn.setFill(family == 3 ? Color.rgb(145, 216, 238) : Color.rgb(181, 160, 129));
        Polygon rightHorn = switch (family) {
            case 1 -> polygon(111, 32, 134, 21, 119, 40, 108, 42);
            case 2 -> polygon(111, 31, 125, 8 - phase * 2, 120, 32, 108, 42);
            case 3 -> polygon(111, 31, 119, 4 - phase * 2, 116, 31, 106, 40);
            default -> boss
                ? polygon(111, 31, 126, 5 - phase * 2, 121, 32, 108, 42)
                : polygon(110, 31, 119, 13 - phase * 2, 116, 34, 106, 42);
        };
        rightHorn.setFill(family == 3 ? Color.rgb(145, 216, 238) : Color.rgb(181, 160, 129));

        Polygon leftEye = polygon(84, 51, 93, 54, 86, 57);
        Polygon rightEye = polygon(101, 54, 111, 51, 107, 58);
        leftEye.setFill(energyColor);
        rightEye.setFill(energyColor);
        DropShadow eyeGlow = new DropShadow(8 + phase * 2, energyColor);
        leftEye.setEffect(eyeGlow);
        rightEye.setEffect(eyeGlow);

        Polygon chestSigil = polygon(91, 88, 98, 98, 91, 108, 84, 98);
        chestSigil.setFill(auraColor);
        chestSigil.setStroke(energyColor);
        chestSigil.setStrokeWidth(1 + phase * 0.3);

        portrait.getChildren().add(aura);
        for (int rank = 0; rank < phase - 1; rank++) {
            Circle phaseRing = new Circle(90, 88, 43 + rank * 5);
            phaseRing.setFill(Color.TRANSPARENT);
            phaseRing.setStroke(Color.color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 0.28));
            phaseRing.setStrokeWidth(1.2);
            portrait.getChildren().add(phaseRing);
        }
        if (family == 2) {
            Polygon leftWing = polygon(67, 78, 43, 59, 27, 79, 44, 78, 31, 101, 63, 94);
            Polygon rightWing = polygon(115, 78, 139, 59, 155, 79, 138, 78, 151, 101, 119, 94);
            leftWing.setFill(Color.rgb(67, 34, 26));
            rightWing.setFill(Color.rgb(67, 34, 26));
            leftWing.setStroke(trimColor);
            rightWing.setStroke(trimColor);
            portrait.getChildren().addAll(leftWing, rightWing);
        } else if (family == 3) {
            Polygon leftCrystal = polygon(67, 77, 51, 51 - phase, 72, 63, 79, 83);
            Polygon rightCrystal = polygon(115, 77, 131, 51 - phase, 110, 63, 103, 83);
            leftCrystal.setFill(Color.rgb(135, 220, 244, 0.88));
            rightCrystal.setFill(Color.rgb(135, 220, 244, 0.88));
            leftCrystal.setStroke(Color.WHITE);
            rightCrystal.setStroke(Color.WHITE);
            portrait.getChildren().addAll(leftCrystal, rightCrystal);
        }
        portrait.getChildren().addAll(spear, spearHead, cloak, torso,
            leftShoulder, rightShoulder, hood, face, leftHorn, rightHorn,
            leftEye, rightEye, chestSigil);
        if (family == 1) {
            Circle fungusCrown = new Circle(96, 27, 13 + phase, Color.rgb(75, 119, 52));
            fungusCrown.setStroke(Color.rgb(171, 195, 91));
            fungusCrown.setStrokeWidth(2);
            portrait.getChildren().add(fungusCrown);
        }
        if (boss) {
            Polygon crownSpike = polygon(89, 20, 91, 2, 97, 19);
            crownSpike.setFill(energyColor);
            portrait.getChildren().add(crownSpike);
        }
        return portrait;
    }

    private Polygon polygon(double... points) {
        Polygon polygon = new Polygon();
        for (double point : points) {
            polygon.getPoints().add(point);
        }
        return polygon;
    }

    /** Updates background, enemy visuals, refreshes the UI, and opens the shop. */
    private void onEnemyDeath() {
        updateBackground();
        updateEnemyVisuals();
        updateUI();
        Shop.open(game);
    }

    // ── Background ────────────────────────────────────────────────────────────

    /** Loads the correct biome background, cross-fading when it changes. */
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
     *
     * Safe cast: returns early if the scene root is not a StackPane
     * (e.g. start or death screen), preventing a ClassCastException.
     */
    private void crossFadeBackground(Image newImage) {
        // Güvenli cast — start/death ekranında root VBox olabilir
        if (!(primaryStage.getScene().getRoot() instanceof StackPane root)) return;

        ImageView overlay = new ImageView(newImage);
        overlay.setFitWidth(background.getFitWidth());
        overlay.setFitHeight(background.getFitHeight());
        overlay.setOpacity(0);

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