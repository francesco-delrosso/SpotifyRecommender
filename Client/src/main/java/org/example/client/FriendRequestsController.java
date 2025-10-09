package org.example.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;

public class FriendRequestsController {

    @FXML
    private ListView<HBox> requestsListView;

    private ClientService clientService;
    private String userEmail;

    public void initialize() {
        clientService = new ClientService();
    }

    @FXML
    private void handleRefresh() {
        loadFriendRequests();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-view.fxml"));
            Parent root = loader.load();
            HomeController controller = loader.getController();
            controller.setUserEmail(this.userEmail); // Passa l'email indietro

            Stage stage = (Stage) requestsListView.getScene().getWindow(); // o searchField per FriendsSearch
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            showError("Errore nel tornare alla home: " + e.getMessage());
        }
    }


    private void loadFriendRequests() {
        if (userEmail == null) return;

        new Thread(() -> {
            try {
                String response = clientService.getFriendRequests(userEmail);
                String[] parts = response.split("\\|");

                Platform.runLater(() -> {
                    requestsListView.getItems().clear();

                    if (parts[0].equals("REQUESTS")) {
                        if (parts.length > 1) {
                            for (int i = 1; i < parts.length; i++) {
                                if (!parts[i].isEmpty()) {
                                    requestsListView.getItems().add(createRequestItem(parts[i]));
                                }
                            }
                        }
                        if (requestsListView.getItems().isEmpty()) {
                            Label emptyLabel = new Label("📭 Nessuna richiesta di amicizia");
                            emptyLabel.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 16px;");
                            HBox emptyBox = new HBox(emptyLabel);
                            emptyBox.setStyle("-fx-alignment: center; -fx-padding: 30;");
                            requestsListView.getItems().add(emptyBox);
                        }
                    } else {
                        showError("Errore nel caricamento delle richieste");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showError("Errore di connessione: " + e.getMessage()));
            }
        }).start();
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public void loadInitialData() {
        loadFriendRequests();
    }


    private HBox createRequestItem(String requesterEmail) {
        HBox item = new HBox(15);
        item.setStyle("-fx-background-color: #282828; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 15; " +
                "-fx-alignment: center-left;");

        Label emailLabel = new Label("👤 " + requesterEmail);
        emailLabel.setStyle("-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button acceptBtn = new Button("✓ Accetta");
        acceptBtn.setStyle("-fx-background-color: #1DB954; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 8 20; " +
                "-fx-cursor: hand;");
        acceptBtn.setOnAction(e -> handleAcceptRequest(requesterEmail));

        Button rejectBtn = new Button("✗ Rifiuta");
        rejectBtn.setStyle("-fx-background-color: #e74c3c; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 8 20; " +
                "-fx-cursor: hand;");
        rejectBtn.setOnAction(e -> handleRejectRequest(requesterEmail));

        item.getChildren().addAll(emailLabel, spacer, acceptBtn, rejectBtn);
        return item;
    }

    private void handleAcceptRequest(String requesterEmail) {
        new Thread(() -> {
            try {
                String response = clientService.acceptFriendRequest(userEmail, requesterEmail);
                Platform.runLater(() -> {
                    if (response.startsWith("SUCCESS")) {
                        showSuccess("Richiesta accettata!");
                        loadFriendRequests();
                    } else {
                        showError("Errore nell'accettare la richiesta");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showError("Errore di connessione: " + e.getMessage()));
            }
        }).start();
    }

    private void handleRejectRequest(String requesterEmail) {
        new Thread(() -> {
            try {
                String response = clientService.rejectFriendRequest(userEmail, requesterEmail);
                Platform.runLater(() -> {
                    if (response.startsWith("SUCCESS")) {
                        showSuccess("Richiesta rifiutata");
                        loadFriendRequests();
                    } else {
                        showError("Errore nel rifiutare la richiesta");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showError("Errore di connessione: " + e.getMessage()));
            }
        }).start();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Successo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
