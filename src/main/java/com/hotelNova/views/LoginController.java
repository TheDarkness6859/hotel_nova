package com.hotelNova.views;

import com.hotelNova.controllers.UserController;
import com.hotelNova.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label errorLabel;
    
    private final UserController userController = new UserController();
    
    @FXML
    public void initialize() {
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> openRegisterView());
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        errorLabel.setText("");
        
        try {
            Optional<User> userOpt = userController.login(username, password);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String role = user.getRole();
                
                if (!"ADMIN".equals(role) && !"RECEPTIONIST".equals(role) && !"GUEST".equals(role)) {
                    errorLabel.setText("Rol de usuario no válido");
                    return;
                }
                
                showSuccess("Login exitoso como " + role);
                
                if ("ADMIN".equals(role)) {
                    openAdminView();
                } else if ("RECEPTIONIST".equals(role)) {
                    openReceptionistView();
                } else if ("GUEST".equals(role)) {
                    openGuestView();
                }
                ((Stage) loginButton.getScene().getWindow()).close();
                
            } else {
                errorLabel.setText("Usuario o contraseña incorrectos");
            }
        } catch (Exception e) {
            errorLabel.setText("Error en el login: " + e.getMessage());
        }
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void openAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Panel Administrador - Hotel Nova");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Error al abrir vista de administrador: " + e.getMessage());
        }
    }
    
    private void openReceptionistView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/receptionist-dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Panel Recepcionista - Hotel Nova");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Error al abrir vista de recepcionista: " + e.getMessage());
        }
    }

    private void openGuestView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/guest-dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Panel Huésped - Hotel Nova");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Error al abrir vista de huésped: " + e.getMessage());
        }
    }
    
    private void openRegisterView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Hotel Nova - Registro");
        } catch (IOException e) {
            showError("Error al abrir registro: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
