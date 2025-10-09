package org.example.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {
    @FXML private TableView<Song> songTable;
    @FXML private TableColumn<Song, String> nameColumn;
    @FXML private TableColumn<Song, String> artistColumn;
    @FXML private TableColumn<Song, Integer> popularityColumn;
    @FXML private TextField searchField;
    @FXML private Label pageLabel;
    @FXML private Button prevButton;
    @FXML private Button nextButton;


    private ClientService clientService;
    private ObservableList<Song> songs;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;
    private String userId;
    private static String userEmail;


    public void initialize() {
        clientService = new ClientService();
        songs = FXCollections.observableArrayList();

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        artistColumn.setCellValueFactory(data -> data.getValue().artistsProperty());
        popularityColumn.setCellValueFactory(data -> data.getValue().popularityProperty().asObject());
        songTable.setItems(songs);
        loadSongs();
    }

    private String currentUserEmail;

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

    private void loadSongs() {
        try {
            String response = clientService.getSongs(currentPage, PAGE_SIZE);
            parseSongsResponse(response);
        } catch (Exception e) {
            showError("Errore nel caricamento: " + e.getMessage());
        }
    }

    private void searchSongs(String searchTerm) {
        try {
            String response = clientService.searchSongs(searchTerm, currentPage, PAGE_SIZE);
            parseSongsResponse(response);
        } catch (Exception e) {
            showError("Errore nella ricerca: " + e.getMessage());
        }
    }

    private void parseSongsResponse(String response) {
        songs.clear();
        String[] parts = response.split("\\|");

        if (parts[0].equals("SONGS")) {
            for (int i = 1; i < parts.length; i++) {
                String[] songData = parts[i].split(";");
                if (songData.length == 5) {
                    songs.add(new Song(
                            songData[0],
                            songData[1],
                            songData[2],
                            Integer.parseInt(songData[3]),
                            Integer.parseInt(songData[4])
                    ));
                }
            }
            updatePageLabel();
            updateButtons();
        }
    }


    @FXML
    private Label userEmailLabel;

    @FXML
    private void handleGoToFriendsSearch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("friends-search-view.fxml"));
            Parent root = loader.load();

            FriendsSearchController controller = loader.getController();
            controller.setUserEmail(this.currentUserEmail); // Passa l'email
            controller.loadInitialData(); // Carica i dati

            Stage stage = (Stage) songTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            showError("Errore nel caricamento della pagina: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToFriendRequests() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("friend-requests-view.fxml"));
            Parent root = loader.load();

            FriendRequestsController controller = loader.getController();
            controller.setUserEmail(this.currentUserEmail); // Passa l'email
            controller.loadInitialData(); // Carica i dati

            Stage stage = (Stage) songTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            showError("Errore nel caricamento della pagina: " + e.getMessage());
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
            showError("Errore durante il logout: " + e.getMessage());
        }
    }

    private void updatePageLabel() {
        pageLabel.setText("Pagina " + (currentPage + 1));
    }

    private void updateButtons() {
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(songs.size() < PAGE_SIZE);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setClientService(ClientService service) {
        this.clientService = service;
    }

}
