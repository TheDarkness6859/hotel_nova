package com.hotelNova.views;

import com.hotelNova.controllers.UserController;
import com.hotelNova.models.Guest;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backToLoginButton;

    @FXML
    private Label messageLabel;

    private final UserController userController = new UserController();

    @FXML
    public void initialize() {
        registerButton.setOnAction(event -> handleRegister());
        backToLoginButton.setOnAction(event -> openLoginView());
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        messageLabel.setText("");

        // Validaciones
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            messageLabel.setText("Todos los campos son obligatorios excepto teléfono");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Las contraseñas no coinciden");
            return;
        }

        if (password.length() < 6) {
            messageLabel.setText("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        try {
            Guest guest = new Guest(username, password, firstName, lastName, email, phone);

            boolean success = userController.saveUser(guest);

            if (success) {
                showSuccess("Registro exitoso. Ahora puedes iniciar sesión.");
                openLoginView();
            } else {
                messageLabel.setText("Error al registrar usuario. El usuario puede ya existir.");
            }
        } catch (IllegalArgumentException e) {
            messageLabel.setText("Error: " + e.getMessage());
        } catch (Exception e) {
            messageLabel.setText("Error inesperado: " + e.getMessage());
        }
    }

    private void openLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backToLoginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Hotel Nova - Login");
        } catch (IOException e) {
            showError("Error al abrir login: " + e.getMessage());
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
