package com.koray;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Collections;

/**
 * Main application class for the Card Game.
 * Manages UI rendering, animations, and game flow.
 */
public class Main extends Application {

    // =============== GAME STATE ===============
    private Game game = new Game();
    private Stage primaryStage;

    // =============== UI COMPONENTS ===============
    private ImageView playerView = new ImageView();
    private StackPane enemyVoid = new StackPane();
    private Timeline playerAnim = new Timeline();

    private Label goldLabel = new Label();
    private Label enemyHpLabel = new Label();
    private Label lvlLabel = new Label();
    private Label log = new Label();
    private Label energyLabel = new Label();

    private ProgressBar hpBar = new ProgressBar();
    private ProgressBar shieldBar = new ProgressBar();

    private HBox handBox = new HBox(UIConstants.HAND_BOX_SPACING);

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showStartScreen();
    }

    /**
     * Displays the main start screen.
     */
    private void showStartScreen() {
        VBox root = new VBox(20);
        root.setStyle(UIConstants.STYLE_CENTER_PADDING);

        Label title = new Label("CARD GAME");
        title.setStyle(UIConstants.STYLE_TITLE_LARGE);

        Button startBtn = new Button("Başlat");
        startBtn.setStyle(UIConstants.STYLE_BUTTON_LARGE);
        startBtn.setOnAction(e -> {
            initializeGame();
            startGame();
        });

        root.getChildren().addAll(title, startBtn);

        setScene(root);
        primaryStage.show();
    }

    /**
     * Displays the death screen when the player dies.
     */
    private void showDeathScreen() {
        VBox root = new VBox(20);
        root.setStyle(UIConstants.STYLE_CENTER_PADDING + UIConstants.STYLE_DARK_BG);

        Label title = new Label("ÖLDÜN!");
        title.setStyle(UIConstants.STYLE_TITLE_LARGE + UIConstants.STYLE_RED_TEXT);

        Button restartBtn = new Button("Tekrar Oyna");
        restartBtn.setStyle(UIConstants.STYLE_BUTTON_LARGE);
        restartBtn.setOnAction(e -> {
            initializeGame();
            startGame();
        });

        Button menuBtn = new Button("Ana Menü");
        menuBtn.setStyle(UIConstants.STYLE_BUTTON_LARGE);
        menuBtn.setOnAction(e -> showStartScreen());

        root.getChildren().addAll(title, restartBtn, menuBtn);

        setScene(root);
    }

    /**
     * Initializes the game state and event bus.
     */
    private void initializeGame() {
        game = new Game();
        game.eventBus = new EventBus();
        game.eventBus.subscribe(new RewardSystem(game));
        game.eventBus.subscribe(new UIObserver(game, this));
    }

    /**
     * Starts the game by initializing deck and displaying the game UI.
     */
    private void startGame() {
        resetPlayerDeck();
        drawHand();

        BorderPane root = new BorderPane();
        root.setTop(createTopPanel());
        root.setLeft(createLeftPanel());
        root.setRight(createRightPanel());
        root.setCenter(createCenterPanel());
        root.setBottom(createBottomPanel());

        setScene(root);
        updateUI();
    }

    /**
     * Sets the scene on the primary stage with fullscreen dimensions.
     */
    private void setScene(Pane root) {
        Scene scene = new Scene(root,
            Screen.getPrimary().getVisualBounds().getWidth(),
            Screen.getPrimary().getVisualBounds().getHeight()
        );
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
    }

    /**
     * Resets the player's deck to the starting configuration.
     */
    private void resetPlayerDeck() {
        game.player.deck.clear();
        game.player.hand.clear();
        game.player.discard.clear();

        for (int i = 0; i < UIConstants.INITIAL_DECK_COPIES; i++) {
            game.player.deck.add(CardFactory.make("Damage", 1, 10, 1, new DamageEffect(15)));
            game.player.deck.add(CardFactory.make("Shield", 1, 10, 1, new ShieldEffect(10)));
            game.player.deck.add(CardFactory.make("Heal", 2, 20, 1, new HealEffect(10)));
            game.player.deck.add(CardFactory.make("Damage", 1, 10, 1, new DamageEffect(15)));
            game.player.deck.add(CardFactory.make("Shield", 1, 10, 1, new ShieldEffect(10)));
            game.player.deck.add(CardFactory.make("Heal", 2, 20, 1, new HealEffect(10)));
            game.player.deck.add(CardFactory.make("Damage", 1, 10, 1, new DamageEffect(15)));
            game.player.deck.add(CardFactory.make("Shield", 1, 10, 1, new ShieldEffect(10)));
            game.player.deck.add(CardFactory.make("Heal", 2, 20, 1, new HealEffect(10)));
        }
        Collections.shuffle(game.player.deck);
    }

    /**
     * Creates the top panel showing the level.
     */
    private HBox createTopPanel() {
        lvlLabel = new Label();
        HBox top = new HBox(50, lvlLabel);
        top.setStyle(UIConstants.STYLE_TOP_PANEL);
        return top;
    }

    /**
     * Creates the left panel showing gold, energy, and health bars.
     */
    private VBox createLeftPanel() {
        goldLabel = new Label();
        energyLabel = new Label();
        
        hpBar = new ProgressBar();
        shieldBar = new ProgressBar();
        hpBar.setStyle(UIConstants.STYLE_HP_BAR);
        shieldBar.setStyle(UIConstants.STYLE_SHIELD_BAR);
        hpBar.setPrefWidth(UIConstants.PROGRESS_BAR_WIDTH);
        shieldBar.setPrefWidth(UIConstants.PROGRESS_BAR_WIDTH);
        
        VBox bars = new VBox(5, hpBar, shieldBar);
        VBox left = new VBox(10, goldLabel, energyLabel, bars);
        left.setStyle(UIConstants.STYLE_SIDE_PANEL);
        
        return left;
    }

    /**
     * Creates the right panel showing enemy information.
     */
    private VBox createRightPanel() {
        enemyHpLabel = new Label();
        VBox right = new VBox(10, enemyHpLabel);
        right.setStyle(UIConstants.STYLE_SIDE_PANEL);
        return right;
    }

    /**
     * Creates the center panel with the battlefield.
     */
    private HBox createCenterPanel() {
        playerView = new ImageView();
        playerAnim = new Timeline();
        playerView.setFitWidth(UIConstants.PLAYER_VIEW_WIDTH);
        playerView.setPreserveRatio(true);
        playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);

        enemyVoid.setPrefSize(UIConstants.ENEMY_SIZE, UIConstants.ENEMY_SIZE);
        updateEnemyVisuals();

        HBox battleField = new HBox(100, playerView, enemyVoid);
        battleField.setStyle(UIConstants.STYLE_CENTER_ALIGNMENT);
        return battleField;
    }

    /**
     * Creates the bottom panel with hand, end turn button, and log.
     */
    private VBox createBottomPanel() {
        handBox = new HBox(UIConstants.HAND_BOX_SPACING);
        handBox.setStyle(UIConstants.STYLE_CENTER_ALIGNMENT);
        log = new Label();

        Button endTurn = new Button("End Turn");
        endTurn.setOnAction(e -> handleEndTurn());

        VBox bottom = new VBox(10);
        bottom.getChildren().addAll(handBox, endTurn, log);
        return bottom;
    }

    /**
     * Handles the end turn button action.
     */
    private void handleEndTurn() {
        Shop.closeShop();
        game.enemy.attack(game.player);
        
        playAnimation("_HURT_", UIConstants.HURT_FRAME_COUNT, false, () -> {
            playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);
            
            if (game.player.hp <= 0) {
                checkPlayerDeath();
            } else {
                startNewTurn();
            }
        });
    }

    /**
     * Plays an animation for the player character.
     * 
     * @param prefix The animation prefix (e.g., "_IDLE_", "ATTACK_")
     * @param frameCount The number of frames in the animation
     * @param loop Whether the animation should loop
     * @param onFinish Callback to execute when the animation finishes (null for looping animations)
     */
    private void playAnimation(String prefix, int frameCount, boolean loop, Runnable onFinish) {
        playerAnim.stop();
        playerAnim.getKeyFrames().clear();

        for (int i = 0; i < frameCount; i++) {
            final int frame = i;
           String fileName = "assets/" + prefix + String.format("%03d", frame) + ".png";
            playerAnim.getKeyFrames().add(
                new KeyFrame(Duration.millis(UIConstants.ANIMATION_FRAME_MS * frame), e -> {
                    java.io.InputStream is = getClass().getClassLoader()
                        .getResourceAsStream(fileName);
                    if (is != null) {
                        playerView.setImage(new Image(is));
                    }
                })
            );
        }

        playerAnim.setCycleCount(loop ? Timeline.INDEFINITE : 1);
        if (onFinish != null && !loop) {
            playerAnim.setOnFinished(e -> onFinish.run());
        } else {
            playerAnim.setOnFinished(null);
        }
        playerAnim.play();
    }

    /**
     * Draws initial hand by clearing and filling with INITIAL_HAND_SIZE cards.
     */
    private void drawHand() {
        game.player.hand.clear();
        for (int i = 0; i < UIConstants.INITIAL_HAND_SIZE; i++) {
            drawSingleCard();
        }
    }

    /**
     * Draws a single card from the deck, reshuffling discard pile if needed.
     */
    private void drawSingleCard() {
        if (game.player.deck.isEmpty()) {
            game.player.deck.addAll(game.player.discard);
            game.player.discard.clear();
            Collections.shuffle(game.player.deck);
        }
        
        if (!game.player.deck.isEmpty()) {
            game.player.hand.add(game.player.deck.remove(0));
        }
    }

    /**
     * Creates a card UI component.
     * 
     * @param c The card to display
     * @param index The index in the hand
     * @return A VBox containing the card UI
     */
    private VBox createCard(Card c, int index) {
        VBox box = new VBox(5);
        box.setPrefSize(UIConstants.CARD_SIZE_WIDTH, UIConstants.CARD_SIZE_HEIGHT);

        String bg = "#ffffff";
        String border = "#000000";
        String borderWidth = "1";

        if (c.design != null) {
            bg = c.design.getBackground();
            border = c.design.getBorder();
            borderWidth = "3";
        }

        box.setStyle(
            "-fx-background-color:" + bg + ";" +
            "-fx-border-color:" + border + ";" +
            "-fx-border-width:" + borderWidth + ";" +
            "-fx-padding:10;"
        );

        Label name = new Label(c.name);
        Label cost = new Label("Cost: " + c.cost);
        Button play = new Button("Play");

        play.setOnAction(e -> handleCardPlay(c));

        box.getChildren().addAll(name, cost, play);
        return box;
    }

    /**
     * Handles card play action.
     * 
     * @param c The card being played
     */
    private void handleCardPlay(Card c) {
        if (game.player.energy >= c.cost) {
            game.player.energy -= c.cost;
            c.use(game.player, game.enemy);
            game.player.hand.remove(c);
            game.player.discard.add(c);

            if (c.effect instanceof DamageEffect) {
                playAnimation("ATTACK_", UIConstants.ATTACK_FRAME_COUNT, false,
                    () -> playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null));
            } else {
                playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);
            }

            checkEnemy();
            updateUI();
        }
    }

    /**
     * Updates the hand UI by clearing and redrawing all cards.
     */
    private void updateHandUI() {
        handBox.getChildren().clear();
        for (int i = 0; i < game.player.hand.size(); i++) {
            handBox.getChildren().add(createCard(game.player.hand.get(i), i));
        }
    }

    /**
     * Checks if enemy is dead and handles level progression.
     */
    private void checkEnemy() {
        if (game.enemy.hp <= 0) {
            Enemy dead = game.enemy;
            game.eventBus.publish(new EnemyDeathEvent(dead));
            game.level++;
            game.enemy = EnemyFactory.createEnemy(game.level);
            updateEnemyVisuals();
            
            if (game.level % 2 == 0) {
                game.maxEnergy++;
            }

            game.currentShopCards = CardFactory.shopCards(game.level, game.player);

            if (dead.isBoss) {
                game.currentBossRelics = RelicFactory.bossRelics(game.level);
            } else {
                game.currentBossRelics.clear();
            }

            Shop.open(game);
        }
    }

    /**
     * Checks if the player is dead and shows the death screen if so.
     */
    private void checkPlayerDeath() {
        if (game.player.hp <= 0) {
            playAnimation("_DIE_", UIConstants.DEATH_FRAME_COUNT, false, () -> {
                showDeathScreen();
            });
        }
    }

    /**
     * Updates all UI elements to reflect current game state.
     */
    public void updateUI() {
        goldLabel.setText("Gold: " + game.player.gold);

        String enemyLabel = game.enemy.name + " | HP: " + game.enemy.hp;
        if (game.enemy.isBoss) {
            enemyLabel = "⚠️ " + enemyLabel + " | ATK: " + game.enemy.attackDamage + " (x2)";
        } else {
            enemyLabel += " | ATK: " + game.enemy.attackDamage;
        }
        enemyHpLabel.setText(enemyLabel);

        lvlLabel.setText("LEVEL: " + game.level);

        hpBar.setProgress((double) game.player.hp / game.player.maxhp);

        if (game.player.shield > 0) {
            shieldBar.setVisible(true);
            shieldBar.setProgress(game.player.shield / UIConstants.SHIELD_MAX);
        } else {
            shieldBar.setVisible(false);
        }

        energyLabel.setText("Energy: " + game.player.energy + "/" + game.maxEnergy);

        if (!game.lastEvent.isEmpty()) {
            log.setText(game.lastEvent);
        }

        updateHandUI();
    }

    /**
     * Updates the visual representation of the enemy.
     */
    private void updateEnemyVisuals() {
        EnemyDesign design = new BaseEnemyDesign();

        if (game.enemy.isBoss) {
            design = new BossBorderDecorator(design, game.level);
        }

        enemyVoid.setStyle(
            "-fx-background-color: black; " +
            "-fx-background-radius: 50%; " +
            design.getBorderStyle() +
            design.getEffect()
        );
    }

    /**
     * Starts a new turn by restoring energy, applying passive relics, and drawing cards.
     */
    private void startNewTurn() {
        game.player.energy = game.maxEnergy;

        for (RelicItem relic : game.ownedRelics) {
            relic.applyPassive(game.player, game);
        }

        int cardsToDraw = UIConstants.INITIAL_HAND_SIZE - game.player.hand.size();
        for (int i = 0; i < cardsToDraw; i++) {
            drawSingleCard();
        }

        updateUI();
        log.setText("Yeni tur başladı. Yadigâr etkileri uygulandı!");
    }

    public static void main(String[] args) {
        launch();
    }
}
