package org.example.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

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
                showSuccess("Login riuscito! Benvenuto, " + parts[1] + "!");
                loginEmail.clear();
                loginPassword.clear();
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
                showSuccess("Registrazione completata! Benvenuto, " + parts[1] + "!");
                registerEmail.clear();
                registerPassword.clear();
            } else {
                showError(parts[1]);
            }
        } catch (Exception e) {
            showError("Errore: " + e.getMessage());
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

