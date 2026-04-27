package com.hotelNova.views;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class GuestDashboardController {

    @FXML
    private Button logoutButton;

    @FXML
    public void initialize() {
        logoutButton.setOnAction(event -> logout());
    }

    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Hotel Nova - Login");
        } catch (IOException e) {
            System.err.println("Error al abrir login: " + e.getMessage());
        }
    }
}
