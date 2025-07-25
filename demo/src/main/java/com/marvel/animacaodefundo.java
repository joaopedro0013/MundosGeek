package com.marvel;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class animacaodefundo extends Application {

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 800, 600);

        // Propriedades para as cores do gradiente
        ObjectProperty<Color> color1 = new SimpleObjectProperty<>(Color.web("#f12345"));
        ObjectProperty<Color> color2 = new SimpleObjectProperty<>(Color.web("#123456"));

        // Retângulo de fundo
        Rectangle background = new Rectangle(0, 0, scene.getWidth(), scene.getHeight());
        background.widthProperty().bind(scene.widthProperty());
        background.heightProperty().bind(scene.heightProperty());
        root.getChildren().add(background);

        // Atualiza o gradiente conforme as cores mudam
        color1.addListener((obs, oldVal, newVal) -> updateGradient(background, color1.get(), color2.get()));
        color2.addListener((obs, oldVal, newVal) -> updateGradient(background, color1.get(), color2.get()));
        updateGradient(background, color1.get(), color2.get());

        // Animação suave entre duas combinações de cores
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(color1, Color.web("#f12345"), Interpolator.EASE_BOTH),
                new KeyValue(color2, Color.web("#123456"), Interpolator.EASE_BOTH)
            ),
            new KeyFrame(Duration.seconds(5),
                new KeyValue(color1, Color.web("#654321"), Interpolator.EASE_BOTH),
                new KeyValue(color2, Color.web("#abcdef"), Interpolator.EASE_BOTH)
            ),
            new KeyFrame(Duration.seconds(10),
                new KeyValue(color1, Color.web("#f12345"), Interpolator.EASE_BOTH),
                new KeyValue(color2, Color.web("#123456"), Interpolator.EASE_BOTH)
            )
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.play();

        primaryStage.setTitle("Teste de Background Animado");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateGradient(Rectangle background, Color c1, Color c2) {
        background.setFill(new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, c1), new Stop(1, c2)
        ));
    }

    public static void main(String[] args) {
        launch(args);
    }
}