package com.principal;

import javafx.application.Application;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.marvel.MarvelApp;
import com.pokemon.PokemonApp;

import java.util.ArrayList;
import java.util.List;

public class MenuApp extends Application {

    private Stage mainStage;
    private static final int NUM_STARS = 400; // Edite aqui para ajustar a quantidade de estrelas

    // Classe interna para representar uma estrela
    private static class Star {
        double x;
        double y;
        double z;

        public Star(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;

        // estilizar esses botões para ficarem mais bonitos
        

        Button btnMarvel = new Button("Mundo Marvel");
        Button btnPokemon = new Button("Mundo Pokémon");
        btnMarvel.setStyle("-fx-font-size: 20px; -fx-background-color: #dd4646ff; -fx-text-fill: white; -fx-padding: 10px;");
        btnPokemon.setStyle("-fx-font-size: 20px; -fx-background-color: #5353daff; -fx-text-fill: white; -fx-padding: 10px;");


        btnMarvel.setOnAction(e -> {
            MarvelApp marvelApp = new MarvelApp();
            Scene marvelScene = marvelApp.createScene(mainStage, mainStage.getScene());
            mainStage.setScene(marvelScene);
        });
        btnPokemon.setOnAction(e -> {
            PokemonApp pokemonApp = new PokemonApp();
            Scene pokemonScene = pokemonApp.createScene(mainStage, mainStage.getScene());
            mainStage.setScene(pokemonScene);
        });

        StackPane stackPane = new StackPane();
        Canvas canvas = new Canvas(800, 600); // Tamanho inicial maior
        stackPane.getChildren().add(canvas);

        VBox vbox = new VBox(20, btnMarvel, btnPokemon);
        vbox.setStyle("-fx-padding: 40; -fx-alignment: center; -fx-background-color: transparent;");
        stackPane.getChildren().add(vbox);

        Scene scene = new Scene(stackPane, 800, 600); // Tamanho inicial maior

        // Ajusta o tamanho do canvas quando a janela for redimensionada
        scene.widthProperty().addListener((obs, oldVal, newVal) -> canvas.setWidth(newVal.doubleValue()));
        scene.heightProperty().addListener((obs, oldVal, newVal) -> canvas.setHeight(newVal.doubleValue()));

        // Animação do campo estelar
        double max_z = 1000;
        double scale = 0.5;
        List<Star> stars = new ArrayList<>();
        for (int i = 0; i < NUM_STARS; i++) {
            double x = (Math.random() - 0.5) * canvas.getWidth() * 2;
            double y = (Math.random() - 0.5) * canvas.getHeight() * 2;
            double z = Math.random() * max_z + 1;
            stars.add(new Star(x, y, z));
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double w = canvas.getWidth();
                double h = canvas.getHeight();
                double cx = w / 2;
                double cy = h / 2;
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, w, h);
                gc.setFill(Color.WHITE);
                for (Star star : stars) {
                    double screen_x = (star.x / star.z) * scale + cx;
                    double screen_y = (star.y / star.z) * scale + cy;
                    if (screen_x >= 0 && screen_x < w && screen_y >= 0 && screen_y < h) {
                        gc.fillOval(screen_x - 1, screen_y - 1, 2, 2);
                    }
                    star.z -= 0.2; // Velocidade suave
                    if (star.z <= 1) {
                        star.z = max_z;
                        // Gere nova posição usando tamanho atual do canvas
                        star.x = (Math.random() - 0.5) * w * 2;
                        star.y = (Math.random() - 0.5) * h * 2;
                    }
                }
            }
        };
        timer.start();

        primaryStage.setTitle("Menu Principal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}