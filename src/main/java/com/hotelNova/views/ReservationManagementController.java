package com.hotelNova.views;

import com.hotelNova.config.AppConfig;
import com.hotelNova.controllers.ReservationController;
import com.hotelNova.controllers.RoomController;
import com.hotelNova.models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ListCell;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class ReservationManagementController {
    
    // Check-in fields
    @FXML
    private TextField guestIdField;
    
    @FXML
    private ComboBox<Room> roomComboBox;
    
    @FXML
    private DatePicker checkInDatePicker;
    
    @FXML
    private DatePicker checkOutDatePicker;
    
    @FXML
    private Button createReservationButton;
    
    @FXML
    private Label reservationStatus;
    
    // Check-out fields
    @FXML
    private TableView<Reservation> activeReservationsTableView;
    
    @FXML
    private TableColumn<Reservation, String> reservationIdColumn;
    
    @FXML
    private TableColumn<Reservation, String> guestNameColumn;
    
    @FXML
    private TableColumn<Reservation, Integer> roomNumberColumn;
    
    @FXML
    private TableColumn<Reservation, Date> checkInDateColumn;
    
    @FXML
    private TableColumn<Reservation, Date> checkOutDateColumn;
    
    @FXML
    private TableColumn<Reservation, Double> totalCostColumn;
    
    @FXML
    private TableColumn<Reservation, Void> checkoutActionsColumn;
    
    @FXML
    private Label checkoutStatus;
    
    // All reservations fields
    @FXML
    private TableView<Reservation> allReservationsTableView;
    
    @FXML
    private TableColumn<Reservation, String> allReservationIdColumn;
    
    @FXML
    private TableColumn<Reservation, String> allGuestColumn;
    
    @FXML
    private TableColumn<Reservation, Integer> allRoomColumn;
    
    @FXML
    private TableColumn<Reservation, Date> allCheckInColumn;
    
    @FXML
    private TableColumn<Reservation, Date> allCheckOutColumn;
    
    @FXML
    private TableColumn<Reservation, String> allStatusColumn;
    
    @FXML
    private TableColumn<Reservation, Double> allCostColumn;
    
    @FXML
    private Button refreshReservationsButton;
    
    private final ReservationController reservationController = new ReservationController();
    private final RoomController roomController = new RoomController();
    private final ObservableList<Reservation> activeReservations = FXCollections.observableArrayList();
    private final ObservableList<Reservation> allReservations = FXCollections.observableArrayList();
    private final ObservableList<Room> availableRooms = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTables();
        setupEventHandlers();
        loadInitialData();
        
        // Set default dates
        checkInDatePicker.setValue(LocalDate.now());
        checkOutDatePicker.setValue(LocalDate.now().plusDays(1));
    }
    
    private void setupComboBoxes() {
        roomComboBox.setItems(availableRooms);
        roomComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                setText(empty || room == null ? "" : 
                    String.format("Habitación %d - %s - $%.2f/noche", 
                        room.getRoomNumber(), room.getType(), room.getPricePerNight()));
            }
        });
    }
    
    private void setupTables() {
        setupActiveReservationsTable();
        setupAllReservationsTable();
    }
    
    private void setupActiveReservationsTable() {
        reservationIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        guestNameColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getGuest() != null ? 
                cellData.getValue().getGuest().getFirstName() + " " + cellData.getValue().getGuest().getLastName() : "N/A"));
        roomNumberColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getRoom() != null ? 
                cellData.getValue().getRoom().getRoomNumber() : 0));
        checkInDateColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkOutDateColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        
        checkoutActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button checkoutButton = new Button("Check-out");
            
            {
                checkoutButton.setOnAction(e -> processCheckout(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : checkoutButton);
            }
        });
        
        activeReservationsTableView.setItems(activeReservations);
    }
    
    private void setupAllReservationsTable() {
        allReservationIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        allGuestColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getGuest() != null ? 
                cellData.getValue().getGuest().getFirstName() + " " + cellData.getValue().getGuest().getLastName() : "N/A"));
        allRoomColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getRoom() != null ? 
                cellData.getValue().getRoom().getRoomNumber() : 0));
        allCheckInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        allCheckOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        allStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        allCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        
        allReservationsTableView.setItems(allReservations);
    }
    
    private void setupEventHandlers() {
        createReservationButton.setOnAction(e -> createReservation());
        refreshReservationsButton.setOnAction(e -> loadAllReservations());
    }
    
    private void loadInitialData() {
        loadAvailableRooms();
        loadAllReservations();
        loadActiveReservations();
    }
    
    private void loadAvailableRooms() {
        try {
            List<Room> rooms = roomController.getAvailableRooms();
            availableRooms.clear();
            availableRooms.addAll(rooms);
        } catch (Exception e) {
            showError("Error al cargar habitaciones disponibles: " + e.getMessage());
        }
    }
    
    private void loadAllReservations() {
        try {
            List<Reservation> reservations = reservationController.getAllReservations();
            allReservations.clear();
            allReservations.addAll(reservations);
        } catch (Exception e) {
            showError("Error al cargar todas las reservas: " + e.getMessage());
        }
    }
    
    private void loadActiveReservations() {
        try {
            List<Reservation> allReservations = reservationController.getAllReservations();
            activeReservations.clear();
            
            for (Reservation reservation : allReservations) {
                if ("ACTIVE".equals(reservation.getStatus()) || "CHECKED_IN".equals(reservation.getStatus())) {
                    activeReservations.add(reservation);
                }
            }
        } catch (Exception e) {
            showError("Error al cargar reservas activas: " + e.getMessage());
        }
    }
    
    private void createReservation() {
        try {
            String guestId = guestIdField.getText().trim();
            Room selectedRoom = roomComboBox.getValue();
            LocalDate checkIn = checkInDatePicker.getValue();
            LocalDate checkOut = checkOutDatePicker.getValue();
            
            if (guestId.isEmpty() || selectedRoom == null || checkIn == null || checkOut == null) {
                showError("Todos los campos son obligatorios");
                return;
            }
            
            if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                showError("La fecha de check-out debe ser posterior a la de check-in");
                return;
            }
            
            if (checkIn.isBefore(LocalDate.now())) {
                showError("La fecha de check-in no puede ser anterior a hoy");
                return;
            }
            
            if (!selectedRoom.isAvailable()) {
                showError("La habitación seleccionada no está disponible");
                return;
            }
            
            if (hasOverlappingReservation(selectedRoom, checkIn, checkOut)) {
                showError("La habitación ya está reservada en esas fechas");
                return;
            }
            
            Guest guest = new Guest(guestId, "guest" + guestId, "password", "Huésped", guestId, "guest" + guestId + "@email.com", "123456789");
            guest.setActive(true);
            
            if (!guest.isActive()) {
                showError("El huésped no está activo");
                return;
            }
            
            User currentUser = new Admin("admin", "admin");
            
            Reservation reservation = new Reservation(
                Date.valueOf(checkIn),
                Date.valueOf(checkOut),
                "ACTIVE",
                guest,
                selectedRoom,
                currentUser
            );
            
            double totalCost = reservation.calculateTotalCost(selectedRoom.getPricePerNight(), AppConfig.getInstance().getIva());
            reservation.setTotalCost(totalCost);
            
            if (reservationController.createReservation(reservation)) {
                showSuccess("Reserva creada exitosamente. Costo total: $" + String.format("%.2f", totalCost));
                clearReservationForm();
                loadAllReservations();
                loadActiveReservations();
                loadAvailableRooms();
            } else {
                showError("Error al crear la reserva");
            }
            
        } catch (Exception e) {
            showError("Error al crear reserva: " + e.getMessage());
        }
    }
    
    private void processCheckout(Reservation reservation) {
        try {
            if (confirmAction("¿Procesar check-out para la reserva " + reservation.getId() + "?")) {
                // Actualizar estado de la reserva
                reservation.setStatus("CHECKED_OUT");
                
                // Liberar habitación
                if (reservation.getRoom() != null) {
                    reservation.getRoom().setAvailable(true);
                    roomController.updateRoom(reservation.getRoom());
                }
                
                checkoutStatus.setText("Check-out procesado exitosamente");
                checkoutStatus.setTextFill(javafx.scene.paint.Color.GREEN);
                
                loadActiveReservations();
                loadAllReservations();
                loadAvailableRooms();
            }
        } catch (Exception e) {
            showError("Error al procesar check-out: " + e.getMessage());
        }
    }
    
    private void clearReservationForm() {
        guestIdField.clear();
        roomComboBox.setValue(null);
        checkInDatePicker.setValue(LocalDate.now());
        checkOutDatePicker.setValue(LocalDate.now().plusDays(1));
    }
    
    private void showSuccess(String message) {
        reservationStatus.setText(message);
        reservationStatus.setTextFill(javafx.scene.paint.Color.GREEN);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private boolean hasOverlappingReservation(Room room, LocalDate checkIn, LocalDate checkOut) {
        try {
            List<Reservation> existingReservations = reservationController.getAllReservations();
            
            for (Reservation reservation : existingReservations) {
                if (reservation.getRoom() != null && 
                    reservation.getRoom().getId().equals(room.getId()) &&
                    ("ACTIVE".equals(reservation.getStatus()) || "CHECKED_IN".equals(reservation.getStatus()))) {
                    
                    LocalDate existingCheckIn = reservation.getCheckIn().toLocalDate();
                    LocalDate existingCheckOut = reservation.getCheckOut().toLocalDate();
                    
                    if (!(checkOut.isBefore(existingCheckIn) || checkIn.isAfter(existingCheckOut))) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean confirmAction(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().get() == ButtonType.OK;
    }
}
