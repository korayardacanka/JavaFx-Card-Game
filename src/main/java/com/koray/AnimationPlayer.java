package com.koray;

import javafx.animation.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Handles all sprite and card animations.
 * Wraps the player's ImageView and its Timeline so that animation
 * logic is fully isolated from game logic and UI layout.
 *
 * Extracted from Main.java to separate animation from turn handling.
 */
public class AnimationPlayer {

    private final ImageView playerView;
    private Timeline playerAnim = new Timeline();

    /** Reference to the owner so we can load resources from the classpath. */
    private final Object resourceOwner;

    /**
     * @param playerView    the sprite ImageView to animate
     * @param resourceOwner any object from the application classpath (used for getResourceAsStream)
     */
    public AnimationPlayer(ImageView playerView, Object resourceOwner) {
        this.playerView    = playerView;
        this.resourceOwner = resourceOwner;
    }

    /**
     * Plays a sprite animation from a numbered frame sequence.
     * Stops any currently running animation before starting the new one.
     *
     * @param prefix     filename prefix (e.g. "_IDLE_", "ATTACK_", "_HURT_", "_DIE_")
     * @param frameCount total number of frames in the sequence
     * @param loop       if true, loops indefinitely; if false, plays once
     * @param onFinish   optional callback run after a non-looping animation completes
     */
    public void playAnimation(String prefix, int frameCount, boolean loop, Runnable onFinish) {
        playerAnim.stop();
        playerAnim.getKeyFrames().clear();

        for (int i = 0; i < frameCount; i++) {
            final int frame = i;
            String fn = "assets/" + prefix + String.format("%03d", frame) + ".png";
            playerAnim.getKeyFrames().add(new KeyFrame(
                Duration.millis(UIConstants.ANIMATION_FRAME_MS * frame), e -> {
                    java.io.InputStream is =
                        resourceOwner.getClass().getClassLoader().getResourceAsStream(fn);
                    if (is != null) playerView.setImage(new Image(is));
                }
            ));
        }

        playerAnim.setCycleCount(loop ? Timeline.INDEFINITE : 1);
        playerAnim.setOnFinished((!loop && onFinish != null) ? e -> onFinish.run() : null);
        playerAnim.play();
    }

    /**
     * Plays the card play animation: the card flies up, rotates, fades, and shrinks.
     * Calls onFinish when the animation completes.
     *
     * @param cardBox  the card VBox to animate
     * @param onFinish callback run after the animation ends
     */
    public void playCardEffect(VBox cardBox, Runnable onFinish) {
        TranslateTransition t = new TranslateTransition(Duration.millis(500), cardBox);
        t.setToY(-200);
        FadeTransition f = new FadeTransition(Duration.millis(500), cardBox);
        f.setToValue(0.0);
        RotateTransition r = new RotateTransition(Duration.millis(500), cardBox);
        r.setByAngle(360);
        ScaleTransition s = new ScaleTransition(Duration.millis(500), cardBox);
        s.setToX(0.3);
        s.setToY(0.3);

        ParallelTransition p = new ParallelTransition(t, f, r, s);
        p.setOnFinished(e -> { if (onFinish != null) onFinish.run(); });
        p.play();
    }
}