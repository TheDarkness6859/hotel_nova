package com.hotelNova.views;

import com.hotelNova.controllers.ReservationController;
import com.hotelNova.controllers.RoomController;
import com.hotelNova.models.Reservation;
import com.hotelNova.models.Room;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class ReceptionistDashboardController {
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private MenuItem checkInMenuItem;
    
    @FXML
    private MenuItem checkOutMenuItem;
    
    @FXML
    private MenuItem viewReservationsMenuItem;
    
    @FXML
    private MenuItem viewRoomsMenuItem;
    
    @FXML
    private Button quickCheckInButton;
    
    @FXML
    private Button quickCheckOutButton;
    
    @FXML
    private Button roomStatusButton;
    
    @FXML
    private Label todayCheckInsLabel;
    
    @FXML
    private Label todayCheckOutsLabel;
    
    @FXML
    private Label availableRoomsLabel;
    
    @FXML
    private Label currentOccupancyLabel;
    
    @FXML
    private Label statusLabel;
    
    private final RoomController roomController = new RoomController();
    private final ReservationController reservationController = new ReservationController();
    
    @FXML
    public void initialize() {
        setupEventHandlers();
        loadDashboardData();
    }
    
    private void setupEventHandlers() {
        logoutButton.setOnAction(e -> logout());
        checkInMenuItem.setOnAction(e -> openReservationManagement());
        checkOutMenuItem.setOnAction(e -> openReservationManagement());
        viewReservationsMenuItem.setOnAction(e -> openReservationManagement());
        viewRoomsMenuItem.setOnAction(e -> openRoomManagement());
        quickCheckInButton.setOnAction(e -> openReservationManagement());
        quickCheckOutButton.setOnAction(e -> openReservationManagement());
        roomStatusButton.setOnAction(e -> openRoomManagement());
    }
    
    private void loadDashboardData() {
        try {
            // Cargar estadísticas
            List<Room> allRooms = roomController.getAvailableRooms();
            List<Reservation> allReservations = reservationController.getAllReservations();
            
            // Habitaciones disponibles
            long availableCount = allRooms.stream()
                .filter(Room::isAvailable)
                .count();
            availableRoomsLabel.setText(String.valueOf(availableCount));
            
            // Ocupación actual
            long occupiedRooms = allRooms.size() - availableCount;
            double occupancyRate = allRooms.size() > 0 ? 
                (double) occupiedRooms / allRooms.size() * 100 : 0;
            currentOccupancyLabel.setText(String.format("%.1f%%", occupancyRate));
            
            // Check-ins y check-outs de hoy (simplificado)
            java.time.LocalDate today = java.time.LocalDate.now();
            long todayCheckIns = allReservations.stream()
                .filter(r -> r.getCheckIn().toLocalDate().equals(today))
                .filter(r -> "ACTIVE".equals(r.getStatus()) || "CHECKED_IN".equals(r.getStatus()))
                .count();
            todayCheckInsLabel.setText(String.valueOf(todayCheckIns));
            
            long todayCheckOuts = allReservations.stream()
                .filter(r -> r.getCheckOut().toLocalDate().equals(today))
                .filter(r -> !"CHECKED_OUT".equals(r.getStatus()))
                .count();
            todayCheckOutsLabel.setText(String.valueOf(todayCheckOuts));
            
            statusLabel.setText("Datos actualizados: " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
        } catch (Exception e) {
            showError("Error al cargar datos del dashboard: " + e.getMessage());
        }
    }
    
    private void openReservationManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reservation-management.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Gestión de Reservas - Hotel Nova");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Actualizar datos después de cerrar
            loadDashboardData();
            
        } catch (Exception e) {
            showError("Error al abrir gestión de reservas: " + e.getMessage());
        }
    }
    
    private void openRoomManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/room-management.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Estado de Habitaciones - Hotel Nova");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Actualizar datos después de cerrar
            loadDashboardData();
            
        } catch (Exception e) {
            showError("Error al abrir estado de habitaciones: " + e.getMessage());
        }
    }
    
    private void logout() {
        if (confirmAction("¿Está seguro de cerrar sesión?")) {
            // Cerrar ventana actual
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.close();
            
            // Abrir ventana de login
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                Scene scene = new Scene(loader.load());
                Stage loginStage = new Stage();
                loginStage.setTitle("Hotel Nova - Login");
                loginStage.setScene(scene);
                loginStage.show();
            } catch (Exception e) {
                showError("Error al abrir login: " + e.getMessage());
            }
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private boolean confirmAction(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().get() == ButtonType.OK;
    }
}
