package org.example.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private VBox loginPane;
    @FXML private VBox registerPane;
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;
    @FXML private TextField registerEmail;
    @FXML private PasswordField registerPassword;
    @FXML private Label statusLabel;
    @FXML private Button connectButton;

    private ClientService clientService;

    public void initialize() {
        clientService = new ClientService();
        showLoginPane();
        updateConnectionStatus();
    }

    @FXML
    private void handleConnect() {
        try {
            clientService.connect();
            statusLabel.setText("✓ Connesso al server");
            statusLabel.setStyle("-fx-text-fill: green;");
            connectButton.setDisable(true);
        } catch (Exception e) {
            showError("Errore di connessione: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        String email = loginEmail.getText().trim();
        String password = loginPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Compila tutti i campi");
            return;
        }

        try {
            String response = clientService.login(email, password);
            String[] parts = response.split("\\|");

            if (parts[0].equals("SUCCESS")) {
                String userId = parts[1];
                navigateToHome(userId);
            } else {
                showError(parts[1]);
            }
        } catch (Exception e) {
            showError("Errore: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        String email = registerEmail.getText().trim();
        String password = registerPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Compila tutti i campi");
            return;
        }

        try {
            String response = clientService.register(email, password);
            String[] parts = response.split("\\|");

            if (parts[0].equals("SUCCESS")) {
                String userId = parts[1];
                navigateToHome(userId);
            } else {
                showError(parts[1]);
            }
        } catch (Exception e) {
            showError("Errore: " + e.getMessage());
        }
    }

    private void navigateToHome(String userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("home-view.fxml"));
            Parent root = loader.load();

            HomeController homeController = loader.getController();
            homeController.setUserId(userId);
            homeController.setClientService(clientService);

            Stage stage = (Stage) loginEmail.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.setTitle("Spotify Recommender - Home");
        } catch (IOException e) {
            showError("Errore nel caricamento della home: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showLoginPane() {
        loginPane.setVisible(true);
        loginPane.setManaged(true);
        registerPane.setVisible(false);
        registerPane.setManaged(false);
        clearStatus();
    }

    @FXML
    private void showRegisterPane() {
        registerPane.setVisible(true);
        registerPane.setManaged(true);
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        clearStatus();
    }

    @FXML
    private void handleExit() {
        clientService.disconnect();
        Platform.exit();
    }

    private void showError(String message) {
        statusLabel.setText("✗ " + message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        statusLabel.setText("✓ " + message);
        statusLabel.setStyle("-fx-text-fill: green;");
    }

    private void clearStatus() {
        statusLabel.setText("");
    }

    private void updateConnectionStatus() {
        if (clientService.isConnected()) {
            statusLabel.setText("✓ Connesso");
            statusLabel.setStyle("-fx-text-fill: green;");
            connectButton.setDisable(true);
        } else {
            statusLabel.setText("Non connesso");
            statusLabel.setStyle("-fx-text-fill: gray;");
        }
    }
}
