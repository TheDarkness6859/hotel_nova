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
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class AdminDashboardController {
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private MenuItem manageRoomsMenuItem;
    
    @FXML
    private MenuItem manageReservationsMenuItem;
    
    @FXML
    private MenuItem manageUsersMenuItem;
    
    @FXML
    private MenuItem occupancyReportMenuItem;
    
    @FXML
    private MenuItem revenueReportMenuItem;
    
    @FXML
    private Label totalRoomsLabel;
    
    @FXML
    private Label activeReservationsLabel;
    
    @FXML
    private Label occupancyLabel;
    
    @FXML
    private Label monthlyRevenueLabel;
    
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
        manageRoomsMenuItem.setOnAction(e -> openRoomManagement());
        manageReservationsMenuItem.setOnAction(e -> openReservationManagement());
        manageUsersMenuItem.setOnAction(e -> showInfo("Gestión de usuarios"));
        occupancyReportMenuItem.setOnAction(e -> showInfo("Reporte de ocupación"));
        revenueReportMenuItem.setOnAction(e -> showInfo("Reporte de ingresos"));
    }
    
    private void loadDashboardData() {
        try {
            // Cargar estadísticas
            List<Room> allRooms = roomController.getAvailableRooms();
            List<Reservation> allReservations = reservationController.getAllReservations();
            
            // Total de habitaciones
            totalRoomsLabel.setText(String.valueOf(allRooms.size()));
            
            // Reservas activas
            long activeCount = allReservations.stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()) || "CHECKED_IN".equals(r.getStatus()))
                .count();
            activeReservationsLabel.setText(String.valueOf(activeCount));
            
            // Tasa de ocupación
            long occupiedRooms = allRooms.stream()
                .filter(room -> !room.isAvailable())
                .count();
            double occupancyRate = allRooms.size() > 0 ? 
                (double) occupiedRooms / allRooms.size() * 100 : 0;
            occupancyLabel.setText(String.format("%.1f%%", occupancyRate));
            
            // Ingresos del mes (simplificado)
            double monthlyRevenue = allReservations.stream()
                .filter(r -> "CHECKED_OUT".equals(r.getStatus()))
                .mapToDouble(Reservation::getTotalCost)
                .sum();
            monthlyRevenueLabel.setText(String.format("$%.2f", monthlyRevenue));
            
            statusLabel.setText("Datos actualizados: " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            
        } catch (Exception e) {
            showError("Error al cargar datos del dashboard: " + e.getMessage());
        }
    }
    
    private void openRoomManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/room-management.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Gestión de Habitaciones - Hotel Nova");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Actualizar datos después de cerrar
            loadDashboardData();
            
        } catch (Exception e) {
            showError("Error al abrir gestión de habitaciones: " + e.getMessage());
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
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message + " - Función no implementada en esta versión");
        alert.showAndWait();
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
