package com.koray.enemies;

import com.koray.ui.UIConstants;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

/** Builds the family- and phase-specific enemy portrait. */
public final class EnemyPortraitFactory {

    private EnemyPortraitFactory() {}

    public static Pane create(Enemy enemy) {
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

    private static Polygon polygon(double... points) {
        Polygon polygon = new Polygon();
        for (double point : points) {
            polygon.getPoints().add(point);
        }
        return polygon;
    }
}


