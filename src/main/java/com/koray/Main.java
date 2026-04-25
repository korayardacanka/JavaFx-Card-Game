package com.koray;

import javafx.animation.*;
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

    private void initializeGame() {
        game = new Game();
        game.eventBus = new EventBus();
        game.eventBus.subscribe(new RewardSystem(game));
        game.eventBus.subscribe(new UIObserver(game, this));
    }

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

    private void setScene(Pane root) {
        Scene scene = new Scene(root,
            Screen.getPrimary().getVisualBounds().getWidth(),
            Screen.getPrimary().getVisualBounds().getHeight()
        );
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
    }

    /**
     * FIX: Her tipten INITIAL_DECK_COPIES (3) adet kart ekler → 9 kart toplam.
     * Önceki kodda döngü içinde 9 kart/iterasyon × 3 iterasyon = 27 kart oluyordu.
     */
    private void resetPlayerDeck() {
        game.player.deck.clear();
        game.player.hand.clear();
        game.player.discard.clear();

        for (int i = 0; i < UIConstants.INITIAL_DECK_COPIES; i++) {
            game.player.deck.add(CardFactory.make("Damage", 1, 10, 1, new DamageEffect(15)));
            game.player.deck.add(CardFactory.make("Shield", 1, 10, 1, new ShieldEffect(10)));
            game.player.deck.add(CardFactory.make("Heal", 2, 20, 1, new HealEffect(10)));
        }
        Collections.shuffle(game.player.deck);
    }

    private HBox createTopPanel() {
        lvlLabel = new Label();
        HBox top = new HBox(50, lvlLabel);
        top.setStyle(UIConstants.STYLE_TOP_PANEL);
        return top;
    }

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

    private VBox createRightPanel() {
        enemyHpLabel = new Label();
        VBox right = new VBox(10, enemyHpLabel);
        right.setStyle(UIConstants.STYLE_SIDE_PANEL);
        return right;
    }

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

    private void handleEndTurn() {
        Shop.closeShop();
        game.enemy.attack(game.player);

        playAnimation("_HURT_", UIConstants.HURT_FRAME_COUNT, false, () -> {
            playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);

            if (!game.player.isAlive()) {
                checkPlayerDeath();
            } else {
                startNewTurn();
            }
        });
    }

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
                    if (is != null) playerView.setImage(new Image(is));
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

    private void drawHand() {
        game.player.hand.clear();
        for (int i = 0; i < UIConstants.INITIAL_HAND_SIZE; i++) {
            drawSingleCard();
        }
    }

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
        play.setOnAction(e -> handleCardPlay(c, box));

        box.getChildren().addAll(name, cost, play);
        return box;
    }

    private void handleCardPlay(Card c, VBox cardBox) {
        if (game.player.spendEnergy(c.cost)) {
            // Oyun mantığını işle
            c.use(game.player, game.enemy);
            game.player.hand.remove(c);
            game.player.discard.add(c);

            if (c.effect instanceof DamageEffect) {
                playAnimation("ATTACK_", UIConstants.ATTACK_FRAME_COUNT, false,
                    () -> playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null));
            } else {
                playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);
            }

            // Kart efekti oynat ve animasyon bitince UI güncelle
            playCardEffect(cardBox, () -> {
                checkEnemy();
                updateUI();
            });
        }
    }

    /**
     * Kartın ortaya doğru gidip kaybolması efektini oynat.
     * Animasyon bitince onFinish callback'i çalıştır.
     */
    private void playCardEffect(VBox cardBox, Runnable onFinish) {
        // Yukarı hareket
        TranslateTransition translate = new TranslateTransition(Duration.millis(600), cardBox);
        translate.setToY(-200);

        // Solma
        FadeTransition fade = new FadeTransition(Duration.millis(600), cardBox);
        fade.setToValue(0.0);

        // Döndürme
        RotateTransition rotate = new RotateTransition(Duration.millis(600), cardBox);
        rotate.setByAngle(360);

        // Ölçeklendirme
        ScaleTransition scale = new ScaleTransition(Duration.millis(600), cardBox);
        scale.setToX(0.3);
        scale.setToY(0.3);

        // Tüm animasyonları paralel çalıştır
        ParallelTransition parallel = new ParallelTransition(translate, fade, rotate, scale);
        parallel.setOnFinished(e -> {
            // Animasyon bittikten sonra callback'i çalıştır
            if (onFinish != null) {
                onFinish.run();
            }
        });
        parallel.play();
    }

    private void updateHandUI() {
        handBox.getChildren().clear();
        for (int i = 0; i < game.player.hand.size(); i++) {
            handBox.getChildren().add(createCard(game.player.hand.get(i), i));
        }
    }

    private void checkEnemy() {
        if (!game.enemy.isAlive()) {
            Enemy dead = game.enemy;
            game.eventBus.publish(new EnemyDeathEvent(dead));
            game.level++;
            game.enemy = EnemyFactory.createEnemy(game.level);
            updateEnemyVisuals();

            if (game.level % 2 == 0) {
                game.maxEnergy++;
            }

            game.currentShopCards = CardFactory.shopCards(game.level, game.player);

            if (dead.isBoss()) {
                game.currentBossRelics = RelicFactory.bossRelics(game.level);
            } else {
                game.currentBossRelics.clear();
            }

            Shop.open(game);
        }
    }

    private void checkPlayerDeath() {
        if (!game.player.isAlive()) {
            playAnimation("_DIE_", UIConstants.DEATH_FRAME_COUNT, false, this::showDeathScreen);
        }
    }

    /**
     * Tüm UI bileşenlerini güncel oyun durumuna göre yeniler.
     */
    public void updateUI() {
        goldLabel.setText("Gold: " + game.player.getGold());

        String enemyLabel = game.enemy.getName() + " | HP: " + game.enemy.getHp();
        if (game.enemy.isBoss()) {
            enemyLabel = "⚠️ " + enemyLabel + " | ATK: " + game.enemy.getAttackDamage() + " (x2)";
        } else {
            enemyLabel += " | ATK: " + game.enemy.getAttackDamage();
        }
        enemyHpLabel.setText(enemyLabel);

        lvlLabel.setText("LEVEL: " + game.level);

        hpBar.setProgress((double) game.player.getHp() / game.player.getMaxHp());

        if (game.player.getShield() > 0) {
            shieldBar.setVisible(true);
            shieldBar.setProgress(game.player.getShield() / UIConstants.SHIELD_MAX);
        } else {
            shieldBar.setVisible(false);
        }

        energyLabel.setText("Energy: " + game.player.getEnergy() + "/" + game.maxEnergy);

        if (!game.lastEvent.isEmpty()) {
            log.setText(game.lastEvent);
        }

        updateHandUI();
    }

    private void updateEnemyVisuals() {
        EnemyDesign design = new BaseEnemyDesign();

        if (game.enemy.isBoss()) {
            design = new BossBorderDecorator(design, game.level);
        }

        enemyVoid.setStyle(
            "-fx-background-color: black; " +
            "-fx-background-radius: 50%; " +
            design.getBorderStyle() +
            design.getEffect()
        );
    }

    private void startNewTurn() {
        game.player.restoreEnergy(game.maxEnergy);
        game.lastEvent = "";

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
