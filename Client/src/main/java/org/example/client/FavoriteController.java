package org.example.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;

public class FavoriteController {

    @FXML private TableView<Song> favoriteTable;
    @FXML private TableColumn<Song, String> titleColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, Integer> popularityColumn;
    @FXML private TableColumn<Song, Void> favoriteColumn;
    @FXML private Label userEmailLabel;
    @FXML private Label countLabel;

    private ClientService clientService;
    private String userEmail;
    private ObservableList<Song> favoriteSongs;

    public void initialize() {
        favoriteSongs = FXCollections.observableArrayList();

        titleColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        artistColumn.setCellValueFactory(data -> data.getValue().artistsProperty());
        popularityColumn.setCellValueFactory(data -> data.getValue().popularityProperty().asObject());

        setupFavoriteColumn();
        favoriteTable.setItems(favoriteSongs);
    }

    public void setUserData(String email, ClientService service) {
        this.userEmail = email;
        this.clientService = service;
        this.userEmailLabel.setText("👤 " + email);
        loadFavorites();
    }

    private void setupFavoriteColumn() {
        favoriteColumn.setCellFactory(column -> new TableCell<Song, Void>() {
            private final Label heartLabel = new Label("❤");

            {
                heartLabel.setStyle("-fx-font-size: 20px; -fx-cursor: hand; -fx-text-fill: #e74c3c; -fx-background-color: transparent; -fx-padding: 0;");
                heartLabel.setOnMouseClicked(event -> {
                    Song song = getTableView().getItems().get(getIndex());
                    removeFavorite(song);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                setGraphic(null);
                setText(null);

                if (!empty) {
                    setGraphic(heartLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }



    @FXML
    private void handleRefresh() {
        loadFavorites();
    }

    @FXML
    private void handleBackToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-view.fxml"));
            Parent root = loader.load();

            HomeController controller = loader.getController();
            controller.setUserEmail(this.userEmail);

            Stage stage = (Stage) favoriteTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            showError("Errore nel tornare alla home");
        }
    }

    private void loadFavorites() {
        new Thread(() -> {
            try {
                String response = clientService.getFavorites(userEmail);

                Platform.runLater(() -> {
                    favoriteSongs.clear();

                    if (response != null && !response.equals("EMPTY") && !response.startsWith("ERROR")) {
                        String[] favorites = response.split("\\|");

                        for (String fav : favorites) {
                            if (!fav.isEmpty()) {
                                String[] fields = fav.split(";");
                                if (fields.length >= 5) {
                                    try {
                                        Song song = new Song(
                                                fields[0],  // id
                                                fields[1],  // name
                                                fields[2],  // artists
                                                Integer.parseInt(fields[3]),  // popularity
                                                Integer.parseInt(fields[4])   // duration_ms
                                        );
                                        song.setIsFavorite(true);
                                        favoriteSongs.add(song);
                                    } catch (NumberFormatException e) {
                                        System.err.println("Errore parsing: " + fav);
                                    }
                                }
                            }
                        }
                    }

                    countLabel.setText(favoriteSongs.size() + " songs");

                    if (favoriteSongs.isEmpty()) {
                        System.out.println("Nessun preferito trovato per: " + userEmail);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Errore nel caricamento dei favoriti: " + e.getMessage()));
            }
        }).start();
    }



    private void removeFavorite(Song song) {
        new Thread(() -> {
            try {
                clientService.removeFavorite(userEmail, song.getId());
                Platform.runLater(() -> {
                    favoriteSongs.remove(song);
                    countLabel.setText(favoriteSongs.size() + " songs");
                });
            } catch (IOException e) {
                Platform.runLater(() -> showError("Errore nella rimozione"));
            }
        }).start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
