package com.marvel;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class MarvelApp extends Application {

    private int currentPage = 0;
    private List<JsonObject> comicList = new ArrayList<>();
    private VBox historiasBox;
    private Button btnAnterior;
    private Button btnProxima;
    private String personagemImageUrl = null;

    public Scene createScene(Stage mainStage, Scene menuScene) {
        TextField inputNome = new TextField();
        inputNome.setPromptText("Digite o nome do personagem");

        Button btnBuscar = new Button("Buscar Quadrinhos");
        Button btnVoltar = new Button("Voltar ao Menu");
        Button btnListaPersonagens = new Button("Ver nomes válidos");

        VBox personagemBox = new VBox(10);
        personagemBox.setStyle("-fx-background-color: transparent;");

        historiasBox = new VBox(10);
        historiasBox.setStyle("-fx-background-color: transparent;");

        btnAnterior = new Button("Anterior");
        btnProxima = new Button("Próxima");
        btnAnterior.setDisable(true);
        btnProxima.setDisable(true);

        HBox paginacaoBox = new HBox(10, btnAnterior, btnProxima);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        btnListaPersonagens.setOnAction(e -> {
    try {
        String caminho = "c:\\desenvolvimento\\Java\\MarvelProject\\demo\\src\\main\\java\\com\\marvel\\marvel-names.json";
        String json = Files.readString(Paths.get(caminho));
        JsonArray nomes = JsonParser.parseString(json).getAsJsonArray();

        StringBuilder sb = new StringBuilder();
        for (JsonElement el : nomes) {
            sb.append(el.getAsString()).append("\n");
        }

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setPrefHeight(400);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nomes válidos para busca");
        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    } catch (Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao carregar nomes: " + ex.getMessage());
        alert.showAndWait();
    }
});

        btnBuscar.setOnAction(e -> {
            personagemBox.getChildren().clear();
            historiasBox.getChildren().clear();
            comicList.clear();
            currentPage = 0;
            btnAnterior.setDisable(true);
            btnProxima.setDisable(true);

            String nome = inputNome.getText().trim();
            if (!nome.isEmpty()) {
                progressIndicator.setVisible(true);

                new Thread(() -> {
                    String jsonChar = Marvel.buscarPersonagemPorNome(nome);
                    System.out.println("Resposta da API (personagem): " + jsonChar);
                    String nomePersonagem = "";
                    String descricao = "";
                    personagemImageUrl = null;
                    try {
                        JsonObject rootChar = JsonParser.parseString(jsonChar).getAsJsonObject();
                        if (!rootChar.has("data") || rootChar.get("data").isJsonNull()) {
                            String status = rootChar.has("message") ? rootChar.get("message").getAsString() : "Resposta inesperada da API.";
                            throw new Exception("Erro da API: " + status);
                        }
                        JsonArray resultsChar = rootChar.getAsJsonObject("data").getAsJsonArray("results");
                        if (resultsChar.size() > 0) {
                            JsonObject personagem = resultsChar.get(0).getAsJsonObject();
                            nomePersonagem = personagem.get("name").getAsString();
                            descricao = personagem.get("description").getAsString();
                            JsonObject thumbnail = personagem.getAsJsonObject("thumbnail");
                            personagemImageUrl = thumbnail.get("path").getAsString() + "/portrait_fantastic." + thumbnail.get("extension").getAsString();

                            ImageView imgView = new ImageView(new Image(personagemImageUrl, 250, 250, true, true));

                            Label lblNome = new Label(nomePersonagem);
                            lblNome.setStyle(
                                    "-fx-font-size: 42px;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-text-fill: white;" +
                                            "-fx-effect: dropshadow(one-pass-box, black, 4, 0.8, 0, 0);"
                            );

                            Label lblDescricao = new Label(descricao.isEmpty() ? "Sem descrição disponível." : descricao);
                            lblDescricao.setWrapText(true);
                            lblDescricao.setStyle("-fx-font-size: 14px; -fx-text-fill: #fff;");
                            lblDescricao.setMaxWidth(400);

                            VBox vboxInfo = new VBox(10, lblNome, lblDescricao);
                            vboxInfo.setStyle("-fx-background-color: transparent;");
                            vboxInfo.setMaxWidth(500);

                            HBox topo = new HBox(15, imgView, vboxInfo);
                            topo.setStyle("-fx-alignment: top-left;");

                            javafx.application.Platform.runLater(() -> {
                                personagemBox.getChildren().add(topo);
                            });
                        } else {
                            javafx.application.Platform.runLater(() -> {
                                personagemBox.getChildren().add(new Label("Personagem não encontrado."));
                            });
                            return;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            Label erroLabel = new Label("Erro ao buscar dados do personagem.\n" + ex.getMessage());
                            erroLabel.setStyle("-fx-text-fill: red;");
                            personagemBox.getChildren().add(erroLabel);
                        });
                        return;
                    }

                    String json = Marvel.buscarComicsPorPersonagem(nome);
                    System.out.println("Resposta da API (comics): " + json);
                    try {
                        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                        if (!root.has("data") || root.get("data").isJsonNull()) {
                            String status = root.has("message") ? root.get("message").getAsString() : "Resposta inesperada da API.";
                            throw new Exception("Erro da API: " + status);
                        }
                        JsonArray results = root.getAsJsonObject("data").getAsJsonArray("results");
                        if (results.size() > 0) {
                            for (int i = 0; i < results.size(); i++) {
                                JsonObject comic = results.get(i).getAsJsonObject();
                                comicList.add(comic);
                            }
                            javafx.application.Platform.runLater(this::atualizarPaginaComics);
                        } else {
                            javafx.application.Platform.runLater(() -> {
                                historiasBox.getChildren().add(new Label("Nenhum quadrinho encontrado para esse personagem."));
                            });
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        javafx.application.Platform.runLater(() -> {
                            Label erroLabel = new Label("Erro ao buscar quadrinhos.\n" + ex.getMessage());
                            erroLabel.setStyle("-fx-text-fill: red;");
                            historiasBox.getChildren().add(erroLabel);
                        });
                    } finally {
                        javafx.application.Platform.runLater(() -> progressIndicator.setVisible(false));
                    }
                }).start();
            }
        });

        btnAnterior.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                atualizarPaginaComics();
            }
        });

        btnProxima.setOnAction(e -> {
            if ((currentPage + 1) * 10 < comicList.size()) {
                currentPage++;
                atualizarPaginaComics();
            }
        });

        btnVoltar.setOnAction(e -> {
            mainStage.setScene(menuScene);
        });

        VBox conteudo = new VBox(20, btnVoltar, inputNome, btnBuscar, btnListaPersonagens, progressIndicator, personagemBox, historiasBox, paginacaoBox);
        conteudo.setStyle("-fx-background-color: transparent; -fx-padding: 20;");

        ScrollPane scrollPane = new ScrollPane(conteudo);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        ObjectProperty<Color> color1 = new SimpleObjectProperty<>(Color.web("#f12345"));
        ObjectProperty<Color> color2 = new SimpleObjectProperty<>(Color.web("#123456"));

        Rectangle background = new Rectangle();
        background.widthProperty().bind(mainStage.widthProperty());
        background.heightProperty().bind(mainStage.heightProperty());

        color1.addListener((obs, oldVal, newVal) -> updateGradient(background, color1.get(), color2.get()));
        color2.addListener((obs, oldVal, newVal) -> updateGradient(background, color1.get(), color2.get()));
        updateGradient(background, color1.get(), color2.get());

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

        StackPane root = new StackPane();
        root.getChildren().addAll(background, scrollPane);

        Scene scene = new Scene(root, 800, 600);

        background.widthProperty().bind(scene.widthProperty());
        background.heightProperty().bind(scene.heightProperty());

        return scene;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setScene(createScene(primaryStage, new Scene(new VBox(new Label("Menu")))));
        primaryStage.setTitle("Marvel Characters");
        primaryStage.show();
    }

    private void atualizarPaginaComics() {
        historiasBox.getChildren().clear();
        int start = currentPage * 10;
        int end = Math.min(start + 10, comicList.size());

        if (comicList.size() > 0) {
            Label titulo = new Label("Quadrinhos (página " + (currentPage + 1) + "):");
            titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #fff;");
            historiasBox.getChildren().add(titulo);

            for (int i = start; i < end; i++) {
                JsonObject comic = comicList.get(i);
                String title = comic.get("title").getAsString();
                JsonObject thumb = comic.getAsJsonObject("thumbnail");
                String path = thumb.get("path").getAsString();
                String ext = thumb.get("extension").getAsString();
                String comicImageUrl = path + "/portrait_fantastic." + ext;

                HBox comicBox = new HBox(10);
                ImageView comicImg = new ImageView(new Image(comicImageUrl, 80, 120, true, true));
                comicBox.getChildren().add(comicImg);

                Label lblTitle = new Label(title);
                lblTitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #fff;");
                lblTitle.setWrapText(true);
                lblTitle.setMaxWidth(400);
                comicBox.getChildren().add(lblTitle);

                // Adiciona ação para abrir o navegador ao clicar
                comicBox.setOnMouseClicked(event -> {
                    try {
                        String personagem = "";
                        if (comic.has("characters")) {
                            JsonObject characters = comic.getAsJsonObject("characters");
                            if (characters.has("items") && characters.get("items").isJsonArray()) {
                                JsonArray items = characters.getAsJsonArray("items");
                                if (items.size() > 0) {
                                    personagem = items.get(0).getAsJsonObject().get("name").getAsString();
                                }
                            }
                        }
                        if (personagem.isEmpty()) {
                            personagem = lblTitle.getText();
                        }
                        String urlBusca = "https://www.google.com/search?q=" +
                                java.net.URLEncoder.encode(title + " " + personagem, "UTF-8");
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(urlBusca));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                historiasBox.getChildren().add(comicBox);
            }
        }

        btnAnterior.setDisable(currentPage == 0);
        btnProxima.setDisable((currentPage + 1) * 10 >= comicList.size());
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