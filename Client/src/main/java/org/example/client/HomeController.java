package org.example.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Pos;

import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeController {

    @FXML private TableView<Song> songTable;
    @FXML private TableColumn<Song, String> nameColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, Integer> popularityColumn;
    @FXML private TableColumn<Song, Void> favoriteColumn;
    @FXML private TextField searchField;
    @FXML private Label pageLabel;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label userEmailLabel;

    private ClientService clientService;
    private ObservableList<Song> songs;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private String currentUserEmail;



    public void setClientService(ClientService service) {
        this.clientService = service;
    }


    public void initialize() {
        clientService = new ClientService();
        songs = FXCollections.observableArrayList();

        // Imposta colonne
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        artistColumn.setCellValueFactory(data -> data.getValue().artistsProperty());
        popularityColumn.setCellValueFactory(data -> data.getValue().popularityProperty().asObject());

        // Colonna cuori
        setupFavoriteColumn();

        songTable.setItems(songs);

        // Carica prima pagina
        loadSongs();
    }

    public void setUserEmail(String email) {
        this.currentUserEmail = email;
        userEmailLabel.setText(email);
    }

    /** Gestione colonna dei favoriti */
    private void setupFavoriteColumn() {
        favoriteColumn.setCellFactory(column -> new TableCell<Song, Void>() {
            private final Label heartLabel = new Label();

            {
                heartLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-cursor: hand;");
                heartLabel.setOnMouseClicked(event -> {
                    Song song = getTableView().getItems().get(getIndex());
                    toggleFavorite(song);
                    updateHeart(song);
                });

                heartLabel.setOnMouseEntered(event -> heartLabel.setScaleX(1.2));
                heartLabel.setOnMouseEntered(event -> heartLabel.setScaleY(1.2));
                heartLabel.setOnMouseExited(event -> {
                    heartLabel.setScaleX(1.0);
                    heartLabel.setScaleY(1.0);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Song song = getTableView().getItems().get(getIndex());
                    updateHeart(song);
                    setGraphic(heartLabel);
                    setAlignment(Pos.CENTER);
                }
            }

            private void updateHeart(Song song) {
                if (song.IsFavorite()) {
                    heartLabel.setText("\u2764"); // ❤️ pieno
                    heartLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red;");
                } else {
                    heartLabel.setText("\u2661"); // ♡ vuoto
                    heartLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
                }
            }
        });
    }

    /** Toggle preferito */
    private void toggleFavorite(Song song) {
        song.setIsFavorite(!song.IsFavorite());

        try {
            if (song.IsFavorite()) {
                clientService.addFavorite(currentUserEmail, song.getId());
            } else {
                clientService.removeFavorite(currentUserEmail, song.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Caricamento canzoni (paginato e robusto) */
    private void loadSongs() {
        try {
            String response = clientService.getSongs(currentPage, PAGE_SIZE);
            songs.clear();

            if (response != null && response.startsWith("SONGS|")) {
                // Rimuovi il prefisso "SONGS|"
                String songsData = response.substring(6);

                if (!songsData.isEmpty()) {
                    String[] entries = songsData.split("\\|");

                    // Carica i preferiti una sola volta
                    List<String> favoriteIds = loadFavoriteIds();

                    for (String entry : entries) {
                        String[] fields = entry.split(";");
                        if (fields.length >= 5) {
                            Song s = new Song(
                                    fields[0],
                                    fields[1],
                                    fields[2],
                                    Integer.parseInt(fields[3]),
                                    Integer.parseInt(fields[4])
                            );

                            // Imposta se è preferito
                            s.setIsFavorite(favoriteIds.contains(s.getId()));
                            songs.add(s);
                        }
                    }
                }
            }

            songTable.setItems(songs);
            updateButtons();
            updatePageLabel();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore nel caricamento delle canzoni");
        }
    }
    private List<String> loadFavoriteIds() {
        List<String> favoriteIds = new ArrayList<>();
        try {
            String favResponse = clientService.getFavorites(currentUserEmail);
            if (favResponse != null && !favResponse.isEmpty()) {
                String[] favorites = favResponse.split("\\|");
                for (String fav : favorites) {
                    String[] fields = fav.split(";");
                    if (fields.length > 0) {
                        favoriteIds.add(fields[0]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return favoriteIds;
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }




    /** Pulsanti Next/Previous */
    private void updateButtons() {
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(songs.size() < PAGE_SIZE);
    }

    private void updatePageLabel() {
        pageLabel.setText("Page " + (currentPage + 1));
    }

    /** Navigazione */
    @FXML
    private void handleNext() {
        currentPage++;
        loadSongs();
    }

    @FXML
    private void handlePrevious() {
        if (currentPage > 0) currentPage--;
        loadSongs();
    }

    @FXML
    private void handleSearch() {
        currentPage = 0;
        String term = searchField.getText().trim();

        if (term.isEmpty()) {
            loadSongs();
        } else {
            searchSongs(term);
        }
    }

    private void searchSongs(String term) {
        try {
            String response = clientService.searchSongs(term, currentPage, PAGE_SIZE);
            songs.clear();

            if (response != null && response.startsWith("SONGS|")) {
                String songsData = response.substring(6);

                if (!songsData.isEmpty()) {
                    String[] entries = songsData.split("\\|");
                    List<String> favoriteIds = loadFavoriteIds();

                    for (String entry : entries) {
                        String[] fields = entry.split(";");
                        if (fields.length >= 5) {
                            Song s = new Song(
                                    fields[0],
                                    fields[1],
                                    fields[2],
                                    Integer.parseInt(fields[3]),
                                    Integer.parseInt(fields[4])
                            );

                            s.setIsFavorite(favoriteIds.contains(s.getId()));
                            songs.add(s);
                        }
                    }
                }
            }

            songTable.setItems(songs);
            updateButtons();
            updatePageLabel();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore nella ricerca");
        }
    }


    /** Vai alla pagina dei preferiti */
    @FXML
    private void handleGoToFavorites() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("favorite-view.fxml"));
            BorderPane root = loader.load();

            FavoriteController controller = loader.getController();
            controller.setUserData(currentUserEmail, clientService);

            Stage stage = (Stage) songTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleGoToFriendsSearch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/client/friends-search-view.fxml"));
            Parent root = loader.load();

            FriendsSearchController controller = loader.getController();
            controller.setUserEmail(this.currentUserEmail);
            controller.loadInitialData();

            Stage stage = (Stage) songTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleGoToFriendRequests() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/client/friend-requests-view.fxml"));
            Parent root = loader.load();

            FriendsSearchController controller = loader.getController();
            controller.setUserEmail(this.currentUserEmail);
            controller.loadInitialData();

            Stage stage = (Stage) songTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleLogout() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/client/friend-requests-view.fxml"));
            Parent root = loader.load();

            FriendsSearchController controller = loader.getController();
            controller.setUserEmail(this.currentUserEmail);
            controller.loadInitialData();

            Stage stage = (Stage) songTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
