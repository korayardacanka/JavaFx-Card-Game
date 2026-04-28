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

import java.util.Collections;
import java.util.Objects;

public class Main extends Application {

    private Game game = new Game();
    private Stage primaryStage;

    private ImageView   playerView  = new ImageView();
    private StackPane   enemyVoid   = new StackPane();
    private Timeline    playerAnim  = new Timeline();

    private Label goldLabel    = new Label();
    private Label enemyHpLabel = new Label();
    private Label lvlLabel     = new Label();
    private Label log          = new Label();
    private Label energyLabel  = new Label();
    private Label hpLabel      = new Label();
    private Label shieldLabel  = new Label();
    private Label statusLabel  = new Label();

    private ProgressBar hpBar      = new ProgressBar();
    private ProgressBar shieldBar  = new ProgressBar();
    private ProgressBar enemyHpBar = new ProgressBar();

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
        startBtn.setOnAction(e -> { initializeGame(); startGame(); });
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
        restartBtn.setOnAction(e -> { initializeGame(); startGame(); });
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

        double W = Screen.getPrimary().getVisualBounds().getWidth();

        ImageView background = new ImageView(
            new Image(Objects.requireNonNull(
                getClass().getResource("/assets/game_background_4.png")
            ).toExternalForm())
        );
        background.setFitWidth(W);
        background.setFitHeight(Screen.getPrimary().getVisualBounds().getHeight());
        background.setPreserveRatio(false);

        playerView = new ImageView();
        playerAnim = new Timeline();
        playerView.setFitWidth(UIConstants.PLAYER_VIEW_WIDTH);
        playerView.setPreserveRatio(true);
        playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);

        enemyVoid = new StackPane();
        enemyVoid.setPrefSize(UIConstants.ENEMY_SIZE, UIConstants.ENEMY_SIZE);
        updateEnemyVisuals();

        VBox playerSlot = new VBox(playerView);
        playerSlot.setAlignment(Pos.BOTTOM_CENTER);
        VBox enemySlot = new VBox(enemyVoid);
        enemySlot.setAlignment(Pos.BOTTOM_CENTER);

        // Üst HUD
        lvlLabel = new Label();
        lvlLabel.setStyle("-fx-text-fill:white; -fx-font-size:18px; -fx-font-weight:bold;");
        HBox topHUD = new HBox(lvlLabel);
        topHUD.setAlignment(Pos.CENTER);
        topHUD.setPadding(new Insets(10));
        topHUD.setStyle("-fx-background-color: rgba(0,0,0,0.45);");

        // Sol panel
        goldLabel = new Label(); energyLabel = new Label();
        hpLabel = new Label(); shieldLabel = new Label();
        styleHUDLabel(goldLabel); styleHUDLabel(energyLabel);
        styleHUDLabel(hpLabel); styleHUDLabel(shieldLabel);

        hpBar = new ProgressBar(1.0); hpBar.setPrefWidth(160);
        shieldBar = new ProgressBar(0.0);
        shieldBar.setStyle("-fx-accent: #3388cc;"); shieldBar.setPrefWidth(160);

        VBox leftPanel = new VBox(5, hpLabel, hpBar, shieldLabel, shieldBar, energyLabel, goldLabel);
        leftPanel.setPadding(new Insets(14));
        leftPanel.setStyle("-fx-background-color: rgba(0,0,0,0.52); -fx-background-radius: 0 10 10 0;");

        // Sağ panel
        enemyHpLabel = new Label(); styleHUDLabel(enemyHpLabel);
        enemyHpBar = new ProgressBar(1.0);
        enemyHpBar.setStyle("-fx-accent: #cc3333;"); enemyHpBar.setPrefWidth(160);
        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #aaffaa; -fx-font-size:12px;");
        statusLabel.setWrapText(true); statusLabel.setMaxWidth(170);

        VBox rightPanel = new VBox(5, enemyHpLabel, enemyHpBar, statusLabel);
        rightPanel.setPadding(new Insets(14));
        rightPanel.setStyle("-fx-background-color: rgba(0,0,0,0.52); -fx-background-radius: 10 0 0 10;");

        // Alt panel
        handBox = new HBox(UIConstants.HAND_BOX_SPACING);
        handBox.setAlignment(Pos.CENTER);
        log = new Label();
        log.setStyle("-fx-text-fill: #ffdd88; -fx-font-size:13px;");
        Button endTurn = new Button("End Turn(E)");
        endTurn.setStyle("-fx-font-size:14px; -fx-padding: 6 20;");
        endTurn.setOnAction(e -> handleEndTurn());
        VBox bottomPanel = new VBox(6, handBox, endTurn, log);
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setPadding(new Insets(8));
        bottomPanel.setStyle("-fx-background-color: rgba(0,0,0,0.55);");

        AnchorPane ui = new AnchorPane();
        AnchorPane.setTopAnchor(topHUD,      0.0); AnchorPane.setLeftAnchor(topHUD, 0.0); AnchorPane.setRightAnchor(topHUD, 0.0);
        AnchorPane.setLeftAnchor(leftPanel,  0.0); AnchorPane.setTopAnchor(leftPanel, 50.0);
        AnchorPane.setRightAnchor(rightPanel,0.0); AnchorPane.setTopAnchor(rightPanel, 50.0);
        AnchorPane.setLeftAnchor(playerSlot, W * 0.20); AnchorPane.setBottomAnchor(playerSlot, 230.0);
        AnchorPane.setRightAnchor(enemySlot, W * 0.20); AnchorPane.setBottomAnchor(enemySlot, 230.0);
        AnchorPane.setBottomAnchor(bottomPanel, 0.0); AnchorPane.setLeftAnchor(bottomPanel, 0.0); AnchorPane.setRightAnchor(bottomPanel, 0.0);
        ui.getChildren().addAll(playerSlot, enemySlot, topHUD, leftPanel, rightPanel, bottomPanel);

        setScene(new StackPane(background, ui));
        updateUI();
    }

    private void styleHUDLabel(Label lbl) {
        lbl.setStyle("-fx-text-fill: white; -fx-font-size:13px;");
    }

    private void setScene(Pane root) {
    Scene scene = new Scene(root,
        Screen.getPrimary().getVisualBounds().getWidth(),
        Screen.getPrimary().getVisualBounds().getHeight()
    );

    scene.setOnKeyPressed(e -> {
        if (e.getCode() == javafx.scene.input.KeyCode.E) {
            handleEndTurn();
        }
    });

    primaryStage.setScene(scene);
    primaryStage.setMaximized(true);
}

    private void resetPlayerDeck() {
        game.player.deck.clear(); game.player.hand.clear(); game.player.discard.clear();
        for (int i = 0; i < UIConstants.INITIAL_DECK_COPIES; i++) {
            game.player.deck.add(CardFactory.make("Damage", 1, 10, 1, new DamageEffect(15)));
            game.player.deck.add(CardFactory.make("Shield", 1, 10, 1, new ShieldEffect(10)));
            game.player.deck.add(CardFactory.make("Heal",   2, 20, 1, new HealEffect(10)));
        }
        Collections.shuffle(game.player.deck);
    }

    private void drawHand() {
        game.player.hand.clear();
        for (int i = 0; i < UIConstants.INITIAL_HAND_SIZE; i++) drawSingleCard();
    }

    private void drawSingleCard() {
        if (game.player.deck.isEmpty()) {
            game.player.deck.addAll(game.player.discard);
            game.player.discard.clear();
            Collections.shuffle(game.player.deck);
        }
        if (!game.player.deck.isEmpty())
            game.player.hand.add(game.player.deck.remove(0));
    }

    private VBox createCard(Card c) {
        VBox box = new VBox(5);
        box.setPrefSize(UIConstants.CARD_SIZE_WIDTH, UIConstants.CARD_SIZE_HEIGHT);

        String bg = "#ffffff", border = "#000000", bw = "1";
        if (c.design != null) { bg = c.design.getBackground(); border = c.design.getBorder(); bw = "3"; }

        box.setStyle("-fx-background-color:" + bg + "; -fx-border-color:" + border + ";" +
            "-fx-border-width:" + bw + "; -fx-background-radius:8; -fx-border-radius:8; -fx-padding:10;");

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
        play.setOnAction(e -> handleCardPlay(c, box));
        box.getChildren().addAll(name, cost, play);
        return box;
    }

    private void updateHandUI() {
        handBox.getChildren().clear();
        for (Card c : game.player.hand) handBox.getChildren().add(createCard(c));
    }

    // ─── Kart oynama: önce oyun mantığı, sonra animasyon ──────────────────
    private void handleCardPlay(Card c, VBox cardBox) {
        if (!game.player.spendEnergy(c.cost)) return;

        // 1) Kartı uygula
        c.use(game.player, game.enemy);
        game.player.hand.remove(c);
        game.player.discard.add(c);

        // 2) Düşman HEMEN öldü mü? (direkt hasar kartı)
        if (!game.enemy.isAlive()) {
            playCardEffect(cardBox, () -> { checkEnemy(); updateUI(); });
            return;
        }

        // 3) Animasyon — oyun mantığı zaten işlendi
        if (c.effect instanceof DamageEffect) {
            playAnimation("ATTACK_", UIConstants.ATTACK_FRAME_COUNT, false,
                () -> playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null));
        } else {
            playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);
        }

        playCardEffect(cardBox, () -> updateUI());
    }

    private void playCardEffect(VBox cardBox, Runnable onFinish) {
        TranslateTransition t = new TranslateTransition(Duration.millis(500), cardBox);
        t.setToY(-200);
        FadeTransition f = new FadeTransition(Duration.millis(500), cardBox);
        f.setToValue(0.0);
        RotateTransition r = new RotateTransition(Duration.millis(500), cardBox);
        r.setByAngle(360);
        ScaleTransition s = new ScaleTransition(Duration.millis(500), cardBox);
        s.setToX(0.3); s.setToY(0.3);
        ParallelTransition p = new ParallelTransition(t, f, r, s);
        p.setOnFinished(e -> { if (onFinish != null) onFinish.run(); });
        p.play();
    }

    // ─── Tur sonu ────────────────────────────────────────────────────────────
    private void handleEndTurn() {
        Shop.closeShop();

        // 1) Durum efektleri (zehir, yanma, donma süre azaltma)
        String statusLog = game.enemy.processStatusEffects();
        if (!statusLog.isEmpty()) {
            game.lastEvent = statusLog;
            updateUI();
        }

        // 2) Durum efektleri düşmanı öldürdü mü?
        if (!game.enemy.isAlive()) {
            checkEnemy();
            return;
        }

        // 3) Düşman saldırısı (dondurulmuşsa saldırmaz)
        game.enemy.attack(game.player);

        playAnimation("_HURT_", UIConstants.HURT_FRAME_COUNT, false, () -> {
            playAnimation("_IDLE_", UIConstants.IDLE_FRAME_COUNT, true, null);
            if (!game.player.isAlive()) checkPlayerDeath();
            else                        startNewTurn();
        });
    }

    private void checkEnemy() {
        if (!game.enemy.isAlive()) {
            Enemy dead = game.enemy;
            game.eventBus.publish(new EnemyDeathEvent(dead));
            game.level++;
            game.enemy = EnemyFactory.createEnemy(game.level);
            updateEnemyVisuals();
            if (game.level % 2 == 0) game.maxEnergy++;
            game.currentShopCards = CardFactory.shopCards(game.level, game.player);
            if (dead.isBoss()) game.currentBossRelics = RelicFactory.bossRelics(game.level);
            else               game.currentBossRelics.clear();
            Shop.open(game);
        }
    }

    private void checkPlayerDeath() {
        if (!game.player.isAlive())
            playAnimation("_DIE_", UIConstants.DEATH_FRAME_COUNT, false, this::showDeathScreen);
    }

    private void startNewTurn() {
        game.player.restoreEnergy(game.maxEnergy);
        game.lastEvent = "";
        for (RelicItem relic : game.ownedRelics) relic.applyPassive(game.player, game);
        int cardsToDraw = UIConstants.INITIAL_HAND_SIZE - game.player.hand.size();
        for (int i = 0; i < cardsToDraw; i++) drawSingleCard();
        updateUI();
        log.setText("Yeni tur başladı.");
    }

    private void playAnimation(String prefix, int frameCount, boolean loop, Runnable onFinish) {
        playerAnim.stop();
        playerAnim.getKeyFrames().clear();
        for (int i = 0; i < frameCount; i++) {
            final int frame = i;
            String fn = "assets/" + prefix + String.format("%03d", frame) + ".png";
            playerAnim.getKeyFrames().add(
                new KeyFrame(Duration.millis(UIConstants.ANIMATION_FRAME_MS * frame), e -> {
                    java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(fn);
                    if (is != null) playerView.setImage(new Image(is));
                })
            );
        }
        playerAnim.setCycleCount(loop ? Timeline.INDEFINITE : 1);
        playerAnim.setOnFinished((!loop && onFinish != null) ? e -> onFinish.run() : null);
        playerAnim.play();
    }

    // ─── UI ──────────────────────────────────────────────────────────────────
    public void updateUI() {
        goldLabel.setText("Gold: " + game.player.getGold());
        energyLabel.setText("Energy: " + game.player.getEnergy() + " / " + game.maxEnergy);
        lvlLabel.setText("LEVEL " + game.level);
        hpLabel.setText("❤  " + game.player.getHp() + " / " + game.player.getMaxHp());

        double pr = (double) game.player.getHp() / game.player.getMaxHp();
        hpBar.setProgress(Math.max(0, pr));
        hpBar.setStyle("-fx-accent: " + (pr > 0.5 ? "#cc3333" : pr > 0.25 ? "#cc8800" : "#880000") + ";");

        if (game.player.getShield() > 0) {
            shieldLabel.setText("🛡  " + game.player.getShield());
            shieldBar.setProgress(Math.min(1.0, game.player.getShield() / UIConstants.SHIELD_MAX));
            shieldLabel.setVisible(true); shieldBar.setVisible(true);
        } else {
            shieldLabel.setVisible(false); shieldBar.setVisible(false);
        }

        enemyHpLabel.setText(
            (game.enemy.isBoss() ? "⚠️  " : "") + game.enemy.getName() +
            "\nHP: " + game.enemy.getHp() + " / " + game.enemy.getMaxHp() +
            "\nATK: " + game.enemy.getAttackDamage() +
            (game.enemy.isFrozen() ? "  ❄ DONMUŞ" : "")
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
        updateHandUI();
    }

    private void updateEnemyVisuals() {
        EnemyDesign design = new BaseEnemyDesign();
        if (game.enemy.isBoss()) design = new BossBorderDecorator(design, game.level);
        enemyVoid.setStyle("-fx-background-color: black; -fx-background-radius: 50%; " +
            design.getBorderStyle() + design.getEffect());
    }

    public static void main(String[] args) { launch(); }
}