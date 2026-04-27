package com.hotelNova.views;

import com.hotelNova.controllers.RoomController;
import com.hotelNova.models.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

public class RoomManagementController {
    
    @FXML
    private ComboBox<String> typeFilterComboBox;
    
    @FXML
    private ComboBox<String> statusFilterComboBox;
    
    @FXML
    private Button filterButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private TableView<Room> roomTableView;
    
    @FXML
    private TableColumn<Room, Integer> roomNumberColumn;
    
    @FXML
    private TableColumn<Room, String> typeColumn;
    
    @FXML
    private TableColumn<Room, Double> priceColumn;
    
    @FXML
    private TableColumn<Room, String> statusColumn;
    
    @FXML
    private TableColumn<Room, Void> actionsColumn;
    
    @FXML
    private TextField roomNumberField;
    
    @FXML
    private ComboBox<String> roomTypeComboBox;
    
    @FXML
    private TextField priceField;
    
    @FXML
    private CheckBox availableCheckBox;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private Button deleteButton;
    
    @FXML
    private Label statusLabel;
    
    private final RoomController roomController = new RoomController();
    private final ObservableList<Room> roomList = FXCollections.observableArrayList();
    private Room selectedRoom;
    
    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTableView();
        setupEventHandlers();
        loadRooms();
    }
    
    private void setupComboBoxes() {
        // Filtros
        typeFilterComboBox.getItems().addAll("Todos", "Individual", "Doble", "Suite", "Presidencial");
        typeFilterComboBox.setValue("Todos");
        
        statusFilterComboBox.getItems().addAll("Todos", "Disponible", "Ocupado");
        statusFilterComboBox.setValue("Todos");
        
        // Formulario
        roomTypeComboBox.getItems().addAll("Individual", "Doble", "Suite", "Presidencial");
    }
    
    private void setupTableView() {
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isAvailable() ? "Disponible" : "Ocupado"));
        
        // Columna de acciones
        actionsColumn.setCellFactory(param -> new TableCell<Room, Void>() {
            private final Button editButton = new Button("Editar");
            private final Button deleteButton = new Button("Eliminar");
            private final HBox hbox = new HBox(5, editButton, deleteButton);
            
            {
                editButton.setOnAction(e -> editRoom(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> deleteRoom(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
        
        roomTableView.setItems(roomList);
        roomTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> selectedRoom = newSelection);
    }
    
    private void setupEventHandlers() {
        filterButton.setOnAction(e -> filterRooms());
        refreshButton.setOnAction(e -> loadRooms());
        saveButton.setOnAction(e -> saveRoom());
        clearButton.setOnAction(e -> clearForm());
        deleteButton.setOnAction(e -> deleteSelectedRoom());
    }
    
    private void loadRooms() {
        try {
            List<Room> rooms = roomController.getAvailableRooms();
            roomList.clear();
            roomList.addAll(rooms);
            statusLabel.setText("Habitaciones cargadas: " + rooms.size());
        } catch (Exception e) {
            showError("Error al cargar habitaciones: " + e.getMessage());
        }
    }
    
    private void filterRooms() {
        String typeFilter = typeFilterComboBox.getValue();
        String statusFilter = statusFilterComboBox.getValue();
        
        List<Room> allRooms = roomController.getAvailableRooms();
        
        ObservableList<Room> filteredRooms = FXCollections.observableArrayList();
        
        for (Room room : allRooms) {
            boolean typeMatch = "Todos".equals(typeFilter) || room.getType().equals(typeFilter);
            boolean statusMatch = "Todos".equals(statusFilter) || 
                ("Disponible".equals(statusFilter) && room.isAvailable()) ||
                ("Ocupado".equals(statusFilter) && !room.isAvailable());
            
            if (typeMatch && statusMatch) {
                filteredRooms.add(room);
            }
        }
        
        roomList.clear();
        roomList.addAll(filteredRooms);
        statusLabel.setText("Habitaciones filtradas: " + filteredRooms.size());
    }
    
    private void saveRoom() {
        try {
            int roomNumber = Integer.parseInt(roomNumberField.getText().trim());
            String type = roomTypeComboBox.getValue();
            double price = Double.parseDouble(priceField.getText().trim());
            boolean available = availableCheckBox.isSelected();
            
            if (type == null) {
                showError("Debe seleccionar un tipo de habitación");
                return;
            }
            
            Room room;
            if (selectedRoom != null && selectedRoom.getRoomNumber() == roomNumber) {
                room = selectedRoom;
                room.setType(type);
                room.setPricePerNight(price);
                room.setAvailable(available);
                if (!roomController.updateRoom(room)) {
                    showError("Error al actualizar habitación");
                    return;
                }
            } else {
                room = new Room(roomNumber, type, price, available);
                if (!roomController.saveRoom(room)) {
                    showError("Error al guardar habitación");
                    return;
                }
            }
            
            showSuccess("Habitación guardada exitosamente");
            clearForm();
            loadRooms();
            
        } catch (NumberFormatException e) {
            showError("El número de habitación y precio deben ser valores numéricos");
        } catch (Exception e) {
            showError("Error al guardar habitación: " + e.getMessage());
        }
    }
    
    private void editRoom(Room room) {
        selectedRoom = room;
        roomNumberField.setText(String.valueOf(room.getRoomNumber()));
        roomTypeComboBox.setValue(room.getType());
        priceField.setText(String.valueOf(room.getPricePerNight()));
        availableCheckBox.setSelected(room.isAvailable());
    }
    
    private void deleteRoom(Room room) {
        if (confirmAction("¿Está seguro de eliminar esta habitación?")) {
            try {
                showError("Función de eliminación no implementada en el controlador");
            } catch (Exception e) {
                showError("Error al eliminar habitación: " + e.getMessage());
            }
        }
    }
    
    private void deleteSelectedRoom() {
        if (selectedRoom == null) {
            showError("Debe seleccionar una habitación para eliminar");
            return;
        }
        deleteRoom(selectedRoom);
    }
    
    private void clearForm() {
        selectedRoom = null;
        roomNumberField.clear();
        roomTypeComboBox.setValue(null);
        priceField.clear();
        availableCheckBox.setSelected(false);
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
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
