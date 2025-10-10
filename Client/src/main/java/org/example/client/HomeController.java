package org.example.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeController {
    @FXML
    private TableView<Song> songTable;
    @FXML
    private TableColumn<Song, String> nameColumn;
    @FXML
    private TableColumn<Song, String> artistColumn;
    @FXML
    private TableColumn<Song, Integer> popularityColumn;
    @FXML
    private TableColumn<Song, Void> favoriteColumn;
    @FXML
    private TextField searchField;
    @FXML
    private Label pageLabel;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private Label userEmailLabel;

    private ClientService clientService;
    private ObservableList<Song> songs;
    private List<Song> favoriteSongs;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private String currentUserEmail;

    public void initialize() {
        clientService = new ClientService();
        songs = FXCollections.observableArrayList();
        favoriteSongs = new ArrayList<>();

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        artistColumn.setCellValueFactory(data -> data.getValue().artistsProperty());
        popularityColumn.setCellValueFactory(data -> data.getValue().popularityProperty().asObject());

        // Setup favorite column with heart button
        setupFavoriteColumn();

        songTable.setItems(songs);
        loadSongs();
    }

    private void setupFavoriteColumn() {
        favoriteColumn.setCellFactory(column -> new TableCell<Song, Void>() {
            private final Button favoriteButton = new Button();

            {
                favoriteButton.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-cursor: hand; " +
                                "-fx-font-size: 18px; " +
                                "-fx-padding: 5;"
                );

                // Hover: cambia colore e ingrandisce leggermente
                favoriteButton.setOnMouseEntered(event -> {
                    favoriteButton.setScaleX(1.2);
                    favoriteButton.setScaleY(1.2);
                    favoriteButton.setTextFill(javafx.scene.paint.Color.RED);
                });

                favoriteButton.setOnMouseExited(event -> {
                    favoriteButton.setScaleX(1.0);
                    favoriteButton.setScaleY(1.0);
                    favoriteButton.setTextFill(javafx.scene.paint.Color.WHITE);
                });

                // Click → aggiunge o rimuove dai preferiti
                favoriteButton.setOnAction(event -> {
                    Song song = getTableView().getItems().get(getIndex());
                    toggleFavorite(song);
                    updateButtonStyle(song);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Song song = getTableView().getItems().get(getIndex());
                    updateButtonStyle(song);
                    setGraphic(favoriteButton);
                    setAlignment(Pos.CENTER);
                }
            }

            private void updateButtonStyle(Song song) {
                if (song.IsFavorite()) {
                    favoriteButton.setText("\u2764"); // ❤️
                    favoriteButton.setStyle("-fx-text-fill: red; -fx-background-color: transparent; -fx-font-size: 18;");
                } else {
                    favoriteButton.setText("\u2661"); // ♡
                    favoriteButton.setStyle("-fx-text-fill: white; -fx-background-color: transparent; -fx-font-size: 18;");
                }
            }

        });
    }


    private void toggleFavorite(Song song) {
        song.setIsFavorite(!song.IsFavorite());

        if (song.IsFavorite()) {
            if (favoriteSongs.stream().noneMatch(s -> s.getId().equals(song.getId()))) {
                favoriteSongs.add(song);
            }
        } else {
            favoriteSongs.removeIf(s -> s.getId().equals(song.getId()));
        }

        songTable.refresh(); // 🔥 forza aggiornamento della tabella
    }



    public void setUserEmail(String email) {
        this.currentUserEmail = email;
        userEmailLabel.setText(email);
    }

    @FXML
    private void handleSearch() {
        currentPage = 0;
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            loadSongs();
        } else {
            searchSongs(searchTerm);
        }
    }

    @FXML
    private void handlePrevious() {
        if (currentPage > 0) {
            currentPage--;
            loadSongs();
        }
    }

    @FXML
    private void handleNext() {
        currentPage++;
        loadSongs();
    }

    @FXML
    private void handleGoToFavorites() {
        try {
            // Create a simple dialog to show favorites
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Favorite Songs");
            alert.setHeaderText("Your Favorite Songs (" + favoriteSongs.size() + ")");

            if (favoriteSongs.isEmpty()) {
                alert.setContentText("No favorite songs yet! Add some by clicking the heart icon.");
            } else {
                StringBuilder content = new StringBuilder();
                for (Song song : favoriteSongs) {
                    content.append("❤️ ")
                            .append(song.nameProperty().get())
                            .append(" - ")
                            .append(song.artistsProperty().get())
                            .append("\n");
                }
                alert.setContentText(content.toString());
            }

            alert.showAndWait();
        } catch (Exception e) {
            showError("Error loading favorites: " + e.getMessage());
        }
    }

    private void loadSongs() {
        try {
            String response = clientService.getSongs(currentPage, PAGE_SIZE);
            parseSongsResponse(response);
        } catch (Exception e) {
            showError("Loading error: " + e.getMessage());
        }
    }

    private void searchSongs(String searchTerm) {
        try {
            String response = clientService.searchSongs(searchTerm, currentPage, PAGE_SIZE);
            parseSongsResponse(response);
        } catch (Exception e) {
            showError("Search error: " + e.getMessage());
        }
    }

    private void parseSongsResponse(String response) {
        songs.clear();
        String[] parts = response.split("\\|");

        if (parts[0].equals("SONGS")) {
            for (int i = 1; i < parts.length; i++) {
                String[] songData = parts[i].split(";");
                if (songData.length == 5) {
                    Song song = new Song(
                            songData[0],
                            songData[1],
                            songData[2],
                            Integer.parseInt(songData[3]),
                            Integer.parseInt(songData[4])
                    );

                    // Restore favorite status if song was favorited before
                    for (Song favSong : favoriteSongs) {
                        if (favSong.getId().equals(song.getId())) {
                            song.setIsFavorite(true);
                            break;
                        }
                    }

                    songs.add(song);
                }
            }
            updatePageLabel();
            updateButtons();
        }
    }

    @FXML
    private void handleGoToFriendsSearch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("friends-search-view.fxml"));
            Parent root = loader.load();

            FriendsSearchController controller = loader.getController();
            controller.setUserEmail(this.currentUserEmail);
            controller.loadInitialData();

            Stage stage = (Stage) songTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            showError("Error loading page: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToFriendRequests() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("friend-requests-view.fxml"));
            Parent root = loader.load();

            FriendRequestsController controller = loader.getController();
            controller.setUserEmail(this.currentUserEmail);
            controller.loadInitialData();

            Stage stage = (Stage) songTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            showError("Error loading page: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            clientService.disconnect();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) songTable.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 500));
            stage.setTitle("Login - Spotify Recommender");
        } catch (IOException e) {
            showError("Error during logout: " + e.getMessage());
        }
    }

    private void updatePageLabel() {
        pageLabel.setText("Page " + (currentPage + 1));
    }

    private void updateButtons() {
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(songs.size() < PAGE_SIZE);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setClientService(ClientService service) {
        this.clientService = service;
    }

}