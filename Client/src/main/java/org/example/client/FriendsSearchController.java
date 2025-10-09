package org.example.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;

public class FriendsSearchController {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<HBox> usersListView;

    @FXML
    private ListView<HBox> friendsListView;

    private ClientService clientService;
    private String userEmail;

    public void initialize() {
        clientService = new ClientService();
    }

    @FXML
    private void handleSearch() {
        String searchEmail = searchField.getText().trim();
        if (searchEmail.isEmpty()) {
            loadAllUsers();
        } else {
            searchSpecificUser(searchEmail);
        }
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public void loadInitialData() {
        loadFriends();
        loadAllUsers();
    }

    @FXML
    private void handleLoadAllUsers() {
        loadAllUsers();
    }

    @FXML
    private void handleRefreshFriends() {
        loadFriends();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-view.fxml"));
            Parent root = loader.load();
            HomeController controller = loader.getController();
            controller.setUserEmail(this.userEmail);

            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
        } catch (IOException e) {
            showError("Errore nel tornare alla home: " + e.getMessage());
        }
    }



    private void loadAllUsers() {
        new Thread(() -> {
            try {
                String usersResponse = clientService.getAllUsers();
                String friendsResponse = clientService.getFriends(userEmail);

                String[] usersParts = usersResponse.split("\\|");
                String[] friendsParts = friendsResponse.split("\\|");

                // Crea un set degli amici per velocizzare la ricerca
                java.util.Set<String> friendsSet = new java.util.HashSet<>();
                if (friendsParts[0].equals("FRIENDS")) {
                    for (int i = 1; i < friendsParts.length; i++) {
                        if (!friendsParts[i].isEmpty()) {
                            friendsSet.add(friendsParts[i]);
                        }
                    }
                }

                Platform.runLater(() -> {
                    usersListView.getItems().clear();
                    if (usersParts[0].equals("USERS")) {
                        for (int i = 1; i < usersParts.length; i++) {
                            String email = usersParts[i];
                            // Escludi: te stesso E gli amici che hai già
                            if (!email.isEmpty() && !email.equals(userEmail) && !friendsSet.contains(email)) {
                                usersListView.getItems().add(createUserItem(email));
                            }
                        }
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showError("Errore di connessione: " + e.getMessage()));
            }
        }).start();
    }

    private void searchSpecificUser(String email) {
        new Thread(() -> {
            try {
                String userResponse = clientService.searchUser(email);
                String friendsResponse = clientService.getFriends(userEmail);

                String[] friendsParts = friendsResponse.split("\\|");

                // Crea un set degli amici
                java.util.Set<String> friendsSet = new java.util.HashSet<>();
                if (friendsParts[0].equals("FRIENDS")) {
                    for (int i = 1; i < friendsParts.length; i++) {
                        if (!friendsParts[i].isEmpty()) {
                            friendsSet.add(friendsParts[i]);
                        }
                    }
                }

                Platform.runLater(() -> {
                    usersListView.getItems().clear();
                    if (userResponse.startsWith("SUCCESS")) {
                        // Escludi: te stesso E gli amici che hai già
                        if (!email.equals(userEmail) && !friendsSet.contains(email)) {
                            usersListView.getItems().add(createUserItem(email));
                        } else if (friendsSet.contains(email)) {
                            showError("Questo utente è già tuo amico");
                        }
                    } else {
                        showError("Utente non trovato");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showError("Errore di connessione: " + e.getMessage()));
            }
        }).start();
    }


    private void loadFriends() {
        if (userEmail == null) return;

        new Thread(() -> {
            try {
                String response = clientService.getFriends(userEmail);
                String[] parts = response.split("\\|");

                Platform.runLater(() -> {
                    friendsListView.getItems().clear();
                    if (parts[0].equals("FRIENDS")) {
                        if (parts.length > 1) {
                            for (int i = 1; i < parts.length; i++) {
                                if (!parts[i].isEmpty()) {
                                    friendsListView.getItems().add(createFriendItem(parts[i]));
                                }
                            }
                        }
                        if (friendsListView.getItems().isEmpty()) {
                            Label emptyLabel = new Label("👥 Nessun amico");
                            emptyLabel.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 14px;");
                            HBox emptyBox = new HBox(emptyLabel);
                            emptyBox.setStyle("-fx-alignment: center; -fx-padding: 20;");
                            friendsListView.getItems().add(emptyBox);
                        }
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showError("Errore di connessione: " + e.getMessage()));
            }
        }).start();
    }

    private HBox createUserItem(String email) {
        HBox item = new HBox(15);
        item.setStyle("-fx-background-color: #282828; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 15; " +
                "-fx-alignment: center-left;");

        Label emailLabel = new Label("👤 " + email);
        emailLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("➕ Aggiungi");
        addBtn.setStyle("-fx-background-color: #1DB954; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 8 20; " +
                "-fx-cursor: hand;");
        addBtn.setOnAction(e -> handleSendRequest(email));

        item.getChildren().addAll(emailLabel, spacer, addBtn);
        return item;
    }

    private HBox createFriendItem(String email) {
        HBox item = new HBox(15);
        item.setStyle("-fx-background-color: #282828; " +
                "-fx-background-radius: 10; " +
                "-fx-padding: 15; " +
                "-fx-alignment: center-left;");

        Label emailLabel = new Label("👤 " + email);
        emailLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button removeBtn = new Button("🗑 Rimuovi");
        removeBtn.setStyle("-fx-background-color: #e74c3c; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 8 20; " +
                "-fx-cursor: hand;");
        removeBtn.setOnAction(e -> handleRemoveFriend(email));

        item.getChildren().addAll(emailLabel, spacer, removeBtn);
        return item;
    }

    private void handleSendRequest(String toEmail) {
        new Thread(() -> {
            try {
                String response = clientService.sendFriendRequest(userEmail, toEmail);
                Platform.runLater(() -> {
                    if (response.startsWith("SUCCESS")) {
                        showSuccess("Richiesta inviata a " + toEmail);
                    } else {
                        showError("Errore nell'invio della richiesta");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showError("Errore di connessione: " + e.getMessage()));
            }
        }).start();
    }

    private void handleRemoveFriend(String friendEmail) {
        new Thread(() -> {
            try {
                String response = clientService.removeFriendship(userEmail, friendEmail);
                Platform.runLater(() -> {
                    if (response.startsWith("SUCCESS")) {
                        showSuccess("Amicizia rimossa");
                        loadFriends();
                    } else {
                        showError("Errore nella rimozione dell'amicizia");
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
