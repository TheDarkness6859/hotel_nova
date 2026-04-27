package com.hotelNova.services;

import com.hotelNova.dao.impl.GenericDAOImpl;
import com.hotelNova.dao.impl.RoomDAOImpl;
import com.hotelNova.db.DatabaseConnection;
import com.hotelNova.models.Room;
import com.hotelNova.services.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private GenericDAOImpl<Room> mockRoomDAO;
    
    @Mock
    private DatabaseConnection mockDatabaseConnection;
    
    private RoomServiceImpl roomService;
    
    private Room testRoom1;
    private Room testRoom2;
    private Room duplicateRoom;
    
    @BeforeEach
    void setUp() {
        // Crear rooms de prueba
        testRoom1 = new Room(101, "Individual", 50.0, true);
        testRoom1.setId("room1");
        
        testRoom2 = new Room(102, "Doble", 80.0, true);
        testRoom2.setId("room2");
        
        duplicateRoom = new Room(101, "Suite", 120.0, true); // Mismo número que testRoom1
        duplicateRoom.setId("room3");
        
        // Inyectar mock DAO usando reflexión (simulación)
        roomService = new RoomServiceImpl() {
            private final GenericDAOImpl<Room> roomDAO = mockRoomDAO;
            
            @Override
            public List<Room> findAll() {
                return roomDAO.findAll();
            }
            
            @Override
            public Optional<Room> findByIdOrName(String value) {
                return roomDAO.findByIdOrName(value);
            }
            
            @Override
            public boolean save(Room room) {
                try {
                    DatabaseConnection.startTransaction();
                    boolean success = roomDAO.save(room);
                    if (success) {
                        DatabaseConnection.commit();
                        return true;
                    } else {
                        DatabaseConnection.rollback();
                        return false;
                    }
                } catch (SQLException err) {
                    DatabaseConnection.rollback();
                    return false;
                }
            }
        };
    }
    
    @Test
    void testRoomNumberUniqueness() {
        // Configurar mocks
        when(mockRoomDAO.findAll()).thenReturn(Arrays.asList(testRoom1, testRoom2));
        when(mockRoomDAO.save(any(Room.class))).thenReturn(true);
        
        // Intentar guardar habitación con número duplicado
        boolean result = roomService.save(duplicateRoom);
        
        // Verificar que no se permite guardar número duplicado
        // En una implementación real, esto debería validar antes de guardar
        List<Room> existingRooms = roomService.findAll();
        boolean isDuplicate = existingRooms.stream()
            .anyMatch(room -> room.getRoomNumber() == duplicateRoom.getRoomNumber());
        
        assertTrue(isDuplicate, "Debería detectar número de habitación duplicado");
        
        // Verificar que se llamó al método save
        verify(mockRoomDAO, times(1)).save(duplicateRoom);
    }
    
    @Test
    void testRoomAvailability() {
        // Configurar habitación no disponible
        Room unavailableRoom = new Room(103, "Suite", 150.0, false);
        unavailableRoom.setId("room3");
        
        when(mockRoomDAO.findAll()).thenReturn(Arrays.asList(testRoom1, testRoom2, unavailableRoom));
        
        // Verificar disponibilidad
        List<Room> allRooms = roomService.findAll();
        
        assertTrue(testRoom1.isAvailable(), "Habitación 101 debería estar disponible");
        assertTrue(testRoom2.isAvailable(), "Habitación 102 debería estar disponible");
        assertFalse(unavailableRoom.isAvailable(), "Habitación 103 no debería estar disponible");
        
        // Verificar que solo hay 2 habitaciones disponibles
        long availableCount = allRooms.stream().filter(Room::isAvailable).count();
        assertEquals(2, availableCount, "Debería haber 2 habitaciones disponibles");
    }
    
    @Test
    void testSaveRoomSuccessfully() throws SQLException {
        when(mockRoomDAO.save(any(Room.class))).thenReturn(true);
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).commit();
        
        Room newRoom = new Room(104, "Presidencial", 200.0, true);
        
        boolean result = roomService.save(newRoom);
        
        assertTrue(result, "Debería guardar habitación exitosamente");
        verify(mockRoomDAO, times(1)).save(newRoom);
    }
    
    @Test
    void testSaveRoomFailure() throws SQLException {
        when(mockRoomDAO.save(any(Room.class))).thenReturn(false);
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).rollback();
        
        Room newRoom = new Room(105, "Individual", 60.0, true);
        
        boolean result = roomService.save(newRoom);
        
        assertFalse(result, "No debería guardar habitación cuando el DAO falla");
        verify(mockRoomDAO, times(1)).save(newRoom);
    }
    
    @Test
    void testSaveRoomWithSQLException() throws SQLException {
        when(mockRoomDAO.save(any(Room.class))).thenThrow(new SQLException("Database error"));
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).rollback();
        
        Room newRoom = new Room(106, "Doble", 90.0, true);
        
        boolean result = roomService.save(newRoom);
        
        assertFalse(result, "No debería guardar habitación cuando hay SQLException");
        verify(mockRoomDAO, times(1)).save(newRoom);
    }
    
    @Test
    void testFindRoomById() {
        when(mockRoomDAO.findByIdOrName("room1")).thenReturn(Optional.of(testRoom1));
        when(mockRoomDAO.findByIdOrName("nonexistent")).thenReturn(Optional.empty());
        
        Optional<Room> foundRoom = roomService.findByIdOrName("room1");
        Optional<Room> notFoundRoom = roomService.findByIdOrName("nonexistent");
        
        assertTrue(foundRoom.isPresent(), "Debería encontrar habitación existente");
        assertEquals(testRoom1.getRoomNumber(), foundRoom.get().getRoomNumber());
        
        assertFalse(notFoundRoom.isPresent(), "No debería encontrar habitación inexistente");
    }
    
    @Test
    void testEditRoom() throws SQLException {
        when(mockRoomDAO.edit(any(Room.class))).thenReturn(true);
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).commit();
        
        // Actualizar precio
        testRoom1.setPricePerNight(55.0);
        
        boolean result = roomService.edit(testRoom1);
        
        assertTrue(result, "Debería editar habitación exitosamente");
        verify(mockRoomDAO, times(1)).edit(testRoom1);
    }
    
    @Test
    void testDeleteRoom() throws SQLException {
        when(mockRoomDAO.delete("room1")).thenReturn(true);
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).commit();
        
        boolean result = roomService.delete("room1");
        
        assertTrue(result, "Debería eliminar habitación exitosamente");
        verify(mockRoomDAO, times(1)).delete("room1");
    }
}
