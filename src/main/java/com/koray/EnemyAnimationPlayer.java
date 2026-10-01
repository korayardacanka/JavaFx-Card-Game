package com.koray;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/** Animates the shape-based enemy portrait through its combat states. */
public class EnemyAnimationPlayer {

    private final StackPane enemyView;
    private Animation activeAnimation;

    public EnemyAnimationPlayer(StackPane enemyView) {
        this.enemyView = enemyView;
    }

    public void playIdle() {
        stopAndReset();

        TranslateTransition floatMotion = new TranslateTransition(Duration.millis(900), enemyView);
        floatMotion.setByY(-4);
        floatMotion.setAutoReverse(true);
        floatMotion.setCycleCount(Animation.INDEFINITE);
        floatMotion.setInterpolator(Interpolator.EASE_BOTH);

        RotateTransition sway = new RotateTransition(Duration.millis(1400), enemyView);
        sway.setByAngle(1.2);
        sway.setAutoReverse(true);
        sway.setCycleCount(Animation.INDEFINITE);
        sway.setInterpolator(Interpolator.EASE_BOTH);

        activeAnimation = new ParallelTransition(floatMotion, sway);
        activeAnimation.play();
    }

    public void playAttack(Runnable onFinished) {
        stopAndReset();
        Timeline attack = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(enemyView.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(150),
                new KeyValue(enemyView.translateXProperty(), -28, Interpolator.EASE_IN),
                new KeyValue(enemyView.rotateProperty(), -7, Interpolator.EASE_IN)),
            new KeyFrame(Duration.millis(250),
                new KeyValue(enemyView.translateXProperty(), -24),
                new KeyValue(enemyView.rotateProperty(), -5)),
            new KeyFrame(Duration.millis(420),
                new KeyValue(enemyView.translateXProperty(), 0, Interpolator.EASE_OUT),
                new KeyValue(enemyView.rotateProperty(), 0, Interpolator.EASE_OUT))
        );
        playFinite(attack, onFinished);
    }

    public void playHurt(Runnable onFinished) {
        stopAndReset();
        Timeline hurt = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(enemyView.translateXProperty(), 0),
                new KeyValue(enemyView.opacityProperty(), 1)),
            new KeyFrame(Duration.millis(55),
                new KeyValue(enemyView.translateXProperty(), 8),
                new KeyValue(enemyView.opacityProperty(), 0.45)),
            new KeyFrame(Duration.millis(110),
                new KeyValue(enemyView.translateXProperty(), -8),
                new KeyValue(enemyView.opacityProperty(), 1)),
            new KeyFrame(Duration.millis(165),
                new KeyValue(enemyView.translateXProperty(), 6),
                new KeyValue(enemyView.opacityProperty(), 0.5)),
            new KeyFrame(Duration.millis(230),
                new KeyValue(enemyView.translateXProperty(), 0),
                new KeyValue(enemyView.opacityProperty(), 1))
        );
        playFinite(hurt, onFinished);
    }

    public void playDeath(Runnable onFinished) {
        stopAndReset();

        FadeTransition fade = new FadeTransition(Duration.millis(620), enemyView);
        fade.setToValue(0);
        TranslateTransition fall = new TranslateTransition(Duration.millis(620), enemyView);
        fall.setByY(42);
        RotateTransition collapse = new RotateTransition(Duration.millis(620), enemyView);
        collapse.setByAngle(28);
        ScaleTransition shrink = new ScaleTransition(Duration.millis(620), enemyView);
        shrink.setToX(0.55);
        shrink.setToY(0.55);

        ParallelTransition death = new ParallelTransition(fade, fall, collapse, shrink);
        activeAnimation = death;
        death.setOnFinished(event -> {
            activeAnimation = null;
            if (onFinished != null) onFinished.run();
        });
        death.play();
    }

    public void stop() {
        stopAndReset();
    }

    private void playFinite(Animation animation, Runnable onFinished) {
        activeAnimation = animation;
        animation.setOnFinished(event -> {
            activeAnimation = null;
            resetView();
            if (onFinished != null) {
                onFinished.run();
            } else {
                playIdle();
            }
        });
        animation.play();
    }

    private void stopAndReset() {
        if (activeAnimation != null) {
            activeAnimation.stop();
            activeAnimation = null;
        }
        resetView();
    }

    private void resetView() {
        enemyView.setTranslateX(0);
        enemyView.setTranslateY(0);
        enemyView.setRotate(0);
        enemyView.setScaleX(1);
        enemyView.setScaleY(1);
        enemyView.setOpacity(1);
    }
}