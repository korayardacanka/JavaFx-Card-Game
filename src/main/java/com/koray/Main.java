package com.koray;

import javafx.animation.*;
import javafx.application.Application;
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
    private Stage primaryStage;
    private StackPane gameRoot;   // root StackPane kept for toast overlays

    // ── Extracted collaborators ───────────────────────────────────────────────
    private DeckManager       deckManager;
    private AnimationPlayer   animator;
    private EnemyAnimationPlayer enemyAnimator;
    private BattleController  battleController;
    private HudView hudView;
    private HandView handView;

    // ── UI components ─────────────────────────────────────────────────────────
    private ImageView  playerView  = new ImageView();
    private StackPane  enemyVoid   = new StackPane();
    private ImageView  background  = new ImageView();

    // =========================================================================
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showStartScreen();
    }

    // ── Screen management ─────────────────────────────────────────────────────

    /** Shows the main menu screen with a Start button. */
    private void showStartScreen() {
        setScene(StartScreen.create(() -> {
            initializeGame();
            startGame();
        }));
        primaryStage.show();
    }

    /** Shows the game-over screen with restart and menu buttons. */
    private void showDeathScreen() {
        RunSummary summary = game.createRunSummary();
        if (animator != null) animator.stop();
        if (enemyAnimator != null) enemyAnimator.stop();
        Shop.closeShop();

        setScene(DeathScreen.create(summary,
            () -> {
                Shop.closeShop();
                initializeGame();
                startGame();
            },
            this::showStartScreen
        ));
    }

    /**
     * Initialises a fresh game and wires all collaborators together.
     * Called both at first start and on restart.
     *
     * Clears the old EventBus observers first to prevent stale UIObserver/
     * RewardSystem references keeping the previous game graph alive (memory leak).
     */
    private void initializeGame() {
        // Clear old bus subscribers so the previous game graph can be collected
        // without the Main reference held by UIObserver remaining reachable.
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

        hudView = new HudView();
        handView = new HandView(game, () -> battleController);

        // Wire BattleController with all callbacks
        battleController = new BattleController(
            game,
            deckManager,
            animator,
            enemyAnimator,
            this::updateUI,
            this::showDeathScreen,
            this::onEnemyDeath,
            msg -> handView.setLog(msg)
        );

        VBox playerSlot = new VBox(playerView);
        playerSlot.setAlignment(Pos.BOTTOM_CENTER);
        VBox enemySlot = new VBox(enemyVoid);
        enemySlot.setAlignment(Pos.BOTTOM_CENTER);

        AnchorPane ui = new AnchorPane();
        AnchorPane.setTopAnchor(hudView.getTopPanel(), 0.0);
        AnchorPane.setLeftAnchor(hudView.getTopPanel(), 0.0);
        AnchorPane.setRightAnchor(hudView.getTopPanel(), 0.0);
        AnchorPane.setLeftAnchor(hudView.getLeftPanel(), 0.0);
        AnchorPane.setTopAnchor(hudView.getLeftPanel(), 50.0);
        AnchorPane.setRightAnchor(hudView.getRightPanel(), 0.0);
        AnchorPane.setTopAnchor(hudView.getRightPanel(), 50.0);
        AnchorPane.setLeftAnchor(playerSlot, W * 0.20);
        AnchorPane.setBottomAnchor(playerSlot, 230.0);
        AnchorPane.setRightAnchor(enemySlot, W * 0.20);
        AnchorPane.setBottomAnchor(enemySlot, 230.0);
        AnchorPane.setBottomAnchor(handView.getView(), 0.0);
        AnchorPane.setLeftAnchor(handView.getView(), 0.0);
        AnchorPane.setRightAnchor(handView.getView(), 0.0);
        ui.getChildren().addAll(playerSlot, enemySlot, hudView.getTopPanel(),
            hudView.getLeftPanel(), hudView.getRightPanel(), handView.getView());

        StackPane root = new StackPane(background, ui);
        gameRoot = root;
        setScene(root);
        updateUI();
    }

    /**
     * Creates and sets the scene, binding the E key to handleEndTurn.
     *
     * Guard conditions:
     *   - battleController != null : may not be set on the start screen
     *   - game.player.isAlive()    : ignore E on the death screen to prevent
     *                                invalid background state mutations
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
            } else if (battleController != null && handView != null) {
                int handIndex = CardHotkeys.handIndexFor(e.getCode());
                if (handIndex >= 0) {
                    handView.playCardAt(handIndex, battleController);
                }
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
        hudView.update(game);
        if (!game.lastEvent.isEmpty()) {
            handView.setLog(game.lastEvent);
        }
        handView.update(battleController);
    }

    /** Applies the EnemyDesign decorator and updates the enemy container style. */
    private void updateEnemyVisuals() {
        EnemyDesign design = new BaseEnemyDesign();
        if (game.enemy.isBoss()) {
            design = new BossBorderDecorator(design, game.level);
        }
        enemyVoid.setStyle("-fx-background-color: rgba(8, 8, 12, 0.82); "
            + "-fx-background-radius: 50%; " + design.getBorderStyle() + design.getEffect());
        enemyVoid.getChildren().setAll(EnemyPortraitFactory.create(game.enemy));
        if (enemyAnimator != null) enemyAnimator.playIdle();
    }
    /** Updates background, enemy visuals, refreshes the UI, and opens the shop. */
    private void onEnemyDeath() {
        updateBackground();
        updateEnemyVisuals();
        updateUI();
        Shop.open(game, primaryStage, this::updateUI,
            () -> battleController.drawMissingHandCardsIfPlayerTurn());
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

        Label toast = new Label("✨ " + relic.name + " acquired!");
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
        // The root may be a VBox on the start or death screen.
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