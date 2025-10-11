package org.example.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.io.IOException;

public class FavoriteController {

    @FXML private TableView<Song> favoriteTable;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, Integer> popularityColumn;
    @FXML private TableColumn<Song, Void> favoriteColumn;
    @FXML private Label userEmailLabel;

    private final ObservableList<Song> favorites = FXCollections.observableArrayList();
    private ClientService clientService;
    private String currentUserEmail;

    // 🔹 Metodo richiamato dalla Home
    public void setUserData(String email, ClientService service) {
        this.currentUserEmail = email;
        this.clientService = service;
        userEmailLabel.setText(email);
        loadFavorites();
    }

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artists"));
        popularityColumn.setCellValueFactory(new PropertyValueFactory<>("popularity"));
        setupFavoriteColumn();
        favoriteTable.setItems(favorites);
    }

    private void setupFavoriteColumn() {
        favoriteColumn.setCellFactory(column -> new TableCell<>() {
            private final Label heartLabel = new Label("❤️");

            {
                heartLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red; -fx-cursor: hand;");
                heartLabel.setOnMouseEntered(e -> heartLabel.setScaleX(1.2));
                heartLabel.setOnMouseExited(e -> heartLabel.setScaleX(1.0));
                heartLabel.setOnMouseClicked(e -> {
                    Song song = getTableView().getItems().get(getIndex());
                    removeFavorite(song);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(heartLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void loadFavorites() {
        favorites.clear();
        try {
            String response = clientService.getFavorites(currentUserEmail);
            if (response != null && !response.equals("EMPTY")) {
                String[] entries = response.split("\\|");
                for (String entry : entries) {
                    String[] fields = entry.split(";");
                    if (fields.length >= 4) {
                        Song song = new Song(
                                fields[0],
                                fields[1],
                                fields[2],
                                Integer.parseInt(fields[3]),
                                0
                        );
                        song.setIsFavorite(true);
                        favorites.add(song);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeFavorite(Song song) {
        try {
            clientService.removeFavorite(currentUserEmail, song.getId());
            favorites.remove(song);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-view.fxml"));
            BorderPane homeRoot = loader.load();

            HomeController homeController = loader.getController();
            homeController.setUserEmail(currentUserEmail);
            homeController.setClientService(clientService);

            Stage stage = (Stage) favoriteTable.getScene().getWindow();
            stage.setScene(new Scene(homeRoot, 1000, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
