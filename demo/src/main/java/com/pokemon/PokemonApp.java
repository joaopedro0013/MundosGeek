package com.pokemon;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.google.gson.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class PokemonApp extends Application {

    private VBox infoBox;
    private VBox movesBox;
    private VBox informacaoBox;
    private int currentMovePage = 0;
    private JsonArray movesArray = new JsonArray();
    private JsonArray abilitiesArray = new JsonArray();
    private JsonArray statsArray = new JsonArray();
    private JsonArray tipoArray = new JsonArray();

    public Scene createScene(Stage mainStage, Scene mainMenuScene) {
        TextField inputNome = new TextField();
        inputNome.setPromptText("Digite o nome do Pokémon");

        Button btnBuscar = new Button("Buscar Pokémon");
        Button btnVoltar = new Button("Voltar ao Menu");

        infoBox = new VBox(10);
        infoBox.setStyle("-fx-background-color: transparent;");

        informacaoBox = new VBox(10);
        informacaoBox.setStyle("-fx-background-color: transparent;");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        btnBuscar.setOnAction(e -> {
            infoBox.getChildren().clear();
            informacaoBox.getChildren().clear();
            movesArray = new JsonArray();
            abilitiesArray = new JsonArray();
            statsArray = new JsonArray();
            tipoArray = new JsonArray();
            currentMovePage = 0;

            String nome = inputNome.getText().trim().toLowerCase();
            if (!nome.isEmpty()) {
                progressIndicator.setVisible(true);

                new Thread(() -> {
                    try {
                        String urlStr = "https://pokeapi.co/api/v2/pokemon/" + nome;
                        URL url = new URL(urlStr);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JsonObject root = JsonParser.parseString(response.toString()).getAsJsonObject();

                        String pokeName = root.get("name").getAsString();
                        int pokeId = root.get("id").getAsInt();
                        JsonObject sprites = root.getAsJsonObject("sprites");
                        String imgUrl = sprites.get("front_default").isJsonNull() ? null : sprites.get("front_default").getAsString();
                        // Extrair a URL do som com verificação
                        String cryUrl = null;
                        if (root.has("cries") && root.getAsJsonObject("cries").has("legacy")) {
                            cryUrl = root.getAsJsonObject("cries").get("legacy").getAsString();
                        }

                        final String finalCryUrl = cryUrl; // Para uso no Platform.runLater
                        javafx.application.Platform.runLater(() -> {
                            Label lblNome = new Label("Nome: " + pokeName);
                            lblNome.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #222;");
                            Label lblId = new Label("ID: " + pokeId);
                            lblId.setStyle("-fx-font-size: 18px; -fx-text-fill: #222;");

                            ImageView imgView = imgUrl != null ? new ImageView(new Image(imgUrl, 120, 120, true, true)) : new ImageView();
                            VBox vboxInfo = new VBox(10, lblNome, lblId, imgView);
                            vboxInfo.setStyle("-fx-background-color: transparent;");
                            infoBox.getChildren().add(vboxInfo);

                            // Reproduzir o som, se disponível
                            if (finalCryUrl != null) {
                                try {
                                    Media sound = new Media(finalCryUrl);
                                    MediaPlayer mediaPlayer = new MediaPlayer(sound);
                                    mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.dispose()); // Evitar vazamento de memória
                                    mediaPlayer.play();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    Label erroLabel = new Label("Erro ao reproduzir o som do Pokémon.");
                                    erroLabel.setStyle("-fx-text-fill: red;");
                                    infoBox.getChildren().add(erroLabel);
                                }
                            } else {
                                Label erroLabel = new Label("Som do Pokémon não disponível.");
                                erroLabel.setStyle("-fx-text-fill: red;");
                                infoBox.getChildren().add(erroLabel);
                            }
                        });

                        // Moves
                        movesArray = root.getAsJsonArray("moves");
                        abilitiesArray = root.getAsJsonArray("abilities");
                        statsArray = root.getAsJsonArray("stats");
                        tipoArray = root.getAsJsonArray("types");
                        javafx.application.Platform.runLater(this::dadosPokemon);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            Label erroLabel = new Label("Pokémon não encontrado ou erro na busca.");
                            erroLabel.setStyle("-fx-text-fill: red;");
                            infoBox.getChildren().add(erroLabel);
                        });
                    } finally {
                        javafx.application.Platform.runLater(() -> progressIndicator.setVisible(false));
                    }
                }).start();
            }
        });

        btnVoltar.setOnAction(e -> {
            mainStage.setScene(mainMenuScene);
        });

        VBox conteudo = new VBox(20, btnVoltar, inputNome, btnBuscar, progressIndicator, infoBox, informacaoBox);
        conteudo.setStyle("-fx-background-color: transparent; -fx-padding: 20;");

        ScrollPane scrollPane = new ScrollPane(conteudo);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        ObjectProperty<Color> color1 = new SimpleObjectProperty<>(Color.web("#53da53"));
        ObjectProperty<Color> color2 = new SimpleObjectProperty<>(Color.web("#53aada"));

        Rectangle background = new Rectangle();
        background.widthProperty().bind(mainStage.widthProperty());
        background.heightProperty().bind(mainStage.heightProperty());

        color1.addListener((obs, oldVal, newVal) -> updateGradient(background, color1.get(), color2.get()));
        color2.addListener((obs, oldVal, newVal) -> updateGradient(background, color1.get(), color2.get()));
        updateGradient(background, color1.get(), color2.get());

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(color1, Color.web("#53da53"), Interpolator.EASE_BOTH),
                        new KeyValue(color2, Color.web("#53aada"), Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.seconds(5),
                        new KeyValue(color1, Color.web("#aaffaa"), Interpolator.EASE_BOTH),
                        new KeyValue(color2, Color.web("#aaaaff"), Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.seconds(10),
                        new KeyValue(color1, Color.web("#53da53"), Interpolator.EASE_BOTH),
                        new KeyValue(color2, Color.web("#53aada"), Interpolator.EASE_BOTH)
                )
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.play();

        StackPane root = new StackPane();
        root.getChildren().addAll(background, scrollPane);

        Scene scene = new Scene(root, 800, 600);

        background.widthProperty().bind(scene.widthProperty());
        background.heightProperty().bind(scene.heightProperty());

        return scene;
    }

    private void dadosPokemon() {
        informacaoBox.getChildren().clear();
        int start = currentMovePage * 10;
        int end = Math.min(start + 10, abilitiesArray.size());
        int endMoves = Math.min(start + 10, movesArray.size());
        int endstats = Math.min(start + 10, statsArray.size());
        int endTipo = Math.min(start + 10, tipoArray.size());

        Label titulo = new Label("Informações do Pokemon:");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #222;");
        informacaoBox.getChildren().add(titulo);

        if (statsArray.size() > 0) {
            Label lblStats = new Label("Estatísticas:");
            lblStats.setStyle("-fx-font-size: 16px; -fx-text-fill: #222;-fx-font-weight: bold;");
            informacaoBox.getChildren().add(lblStats);
            for (int i = start; i < endstats; i++) {
                JsonObject statObj = statsArray.get(i).getAsJsonObject();
                JsonObject stat = statObj.getAsJsonObject("stat");
                String statName = stat.get("name").getAsString();
                int baseStat = statObj.get("base_stat").getAsInt();

                Label lblStatis = new Label(statName + ": " + baseStat);
                lblStatis.setStyle("-fx-font-size: 14px; -fx-text-fill: #222;");
                informacaoBox.getChildren().add(lblStatis);
            }
        }

        if (tipoArray.size() > 0) {
            Label lblStats = new Label("Tipo de Pokemon:");
            lblStats.setStyle("-fx-font-size: 16px; -fx-text-fill: #222;-fx-font-weight: bold;");
            informacaoBox.getChildren().add(lblStats);
            for (int i = start; i < endTipo; i++) {
                JsonObject tipoObj = tipoArray.get(i).getAsJsonObject();
                JsonObject tipo = tipoObj.getAsJsonObject("type");
                String tipoName = tipo.get("name").getAsString();

                Label lbltipo = new Label(tipoName);
                lbltipo.setStyle("-fx-font-size: 14px; -fx-text-fill: #222;");
                informacaoBox.getChildren().add(lbltipo);
            }
        }

        if (abilitiesArray.size() > 0) {
            Label lblAbilities = new Label("Habilidades:");
            lblAbilities.setStyle("-fx-font-size: 16px; -fx-text-fill: #222;-fx-font-weight: bold;");
            informacaoBox.getChildren().add(lblAbilities);
            for (int i = start; i < end; i++) {
                JsonObject abiObj = abilitiesArray.get(i).getAsJsonObject();
                JsonObject abi = abiObj.getAsJsonObject("ability");
                String abiName = abi.get("name").getAsString();

                Label lblabi = new Label(abiName);
                lblabi.setStyle("-fx-font-size: 14px; -fx-text-fill: #222;");
                informacaoBox.getChildren().add(lblabi);
            }
        }

        if (movesArray.size() > 0) {
            Label lblmovesLabel = new Label("Movimentos (Podem ser de ataque ou de defesa):");
            lblmovesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #222;-fx-font-weight: bold;");
            informacaoBox.getChildren().add(lblmovesLabel);
            for (int i = endMoves; i < movesArray.size(); i++) {
                JsonObject moveObj = movesArray.get(i).getAsJsonObject();
                JsonObject move = moveObj.getAsJsonObject("move");
                String moveName = move.get("name").getAsString();

                Label lblmove = new Label(moveName);
                lblmove.setStyle("-fx-font-size: 14px; -fx-text-fill: #222;");
                informacaoBox.getChildren().add(lblmove);
            }
        }
    }

    private void updateGradient(Rectangle background, Color c1, Color c2) {
        background.setFill(new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, c1), new Stop(1, c2)
        ));
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setScene(createScene(primaryStage, null));
        primaryStage.setTitle("Pokémon API");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}