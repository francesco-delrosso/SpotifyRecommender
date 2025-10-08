package org.example.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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


    public void initialize() {
        clientService = new ClientService();
        songs = FXCollections.observableArrayList();

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        artistColumn.setCellValueFactory(data -> data.getValue().artistsProperty());
        popularityColumn.setCellValueFactory(data -> data.getValue().popularityProperty().asObject());
        songTable.setItems(songs);
        loadSongs();
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
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setClientService(ClientService service) {
        this.clientService = service;
    }

}
