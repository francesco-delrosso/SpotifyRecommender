package org.example.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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
    @FXML private Circle statusIndicator;

    private ClientService clientService;

    public void initialize() {
        clientService = new ClientService();
        showLoginPane();



    }



    @FXML
    private void handleLogin() {
        String email = loginEmail.getText().trim();
        String password = loginPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Invalid Input", "Please fill in all fields");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Invalid Email", "Please enter a valid email address");
            return;
        }

        new Thread(() -> {
            try {
                String response = clientService.login(email, password);
                String[] parts = response.split("\\|");

                Platform.runLater(() -> {
                    if (parts[0].equals("SUCCESS")) {
                        String userId = parts[1];
                        navigateToHome(userId, email);
                    } else {
                        showError("Login Failed", parts[1]);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error", "Connection error: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleRegister() {
        String email = registerEmail.getText().trim();
        String password = registerPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Invalid Input", "Please fill in all fields");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Invalid Email", "Please enter a valid email address");
            return;
        }

        if (password.length() < 6) {
            showError("Weak Password", "Password must be at least 6 characters long");
            return;
        }

        new Thread(() -> {
            try {
                String response = clientService.register(email, password);
                String[] parts = response.split("\\|");

                Platform.runLater(() -> {
                    if (parts[0].equals("SUCCESS")) {
                        String userId = parts[1];
                        showSuccess("Welcome! Account created successfully");
                        navigateToHome(userId, email);
                    } else {
                        showError("Registration Failed", parts[1]);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error", "Connection error: " + e.getMessage()));
            }
        }).start();
    }

    private void navigateToHome(String userId, String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/client/home-view.fxml"));
            Parent root = loader.load();

            HomeController homeController = loader.getController();
            homeController.setUserEmail(email);

            Stage stage = (Stage) loginEmail.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setTitle("Spotify Recommender - Home");
        } catch (IOException e) {
            showError("Error", "Unable to load home screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showLoginPane() {
        loginPane.setVisible(true);
        loginPane.setManaged(true);
        registerPane.setVisible(false);
        registerPane.setManaged(false);
        clearFields();
    }

    @FXML
    private void showRegisterPane() {
        registerPane.setVisible(true);
        registerPane.setManaged(true);
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        clearFields();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #282828;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #282828;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        alert.showAndWait();
    }

    private void clearFields() {
        loginEmail.clear();
        loginPassword.clear();
        registerEmail.clear();
        registerPassword.clear();
    }

    private void updateConnectionStatus() {
        if (clientService.isConnected()) {
            statusLabel.setText("Connected");
            statusLabel.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 12px; -fx-font-weight: bold;");
            statusIndicator.setFill(Color.web("#1DB954"));
            connectButton.setVisible(false);
        } else {
            statusLabel.setText("Not Connected");
            statusLabel.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 12px;");
            statusIndicator.setFill(Color.GRAY);
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void addButtonHoverEffect(Button button) {
        String originalStyle = button.getStyle();
        button.setOnMouseEntered(e -> button.setStyle(originalStyle + "-fx-opacity: 0.9;"));
        button.setOnMouseExited(e -> button.setStyle(originalStyle));
    }
}