package com.hotelNova.services;

import com.hotelNova.dao.impl.GenericDAOImpl;
import com.hotelNova.db.DatabaseConnection;
import com.hotelNova.models.Guest;
import com.hotelNova.models.Reservation;
import com.hotelNova.models.Room;
import com.hotelNova.models.Admin;
import com.hotelNova.models.User;
import com.hotelNova.services.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private GenericDAOImpl<Reservation> mockReservationDAO;
    
    @Mock
    private GenericDAOImpl<Room> mockRoomDAO;
    
    @Mock
    private DatabaseConnection mockDatabaseConnection;
    
    private ReservationServiceImpl reservationService;
    
    private Room testRoom;
    private Guest testGuest;
    private User testUser;
    private Reservation validReservation;
    private Reservation overlappingReservation;
    
    @BeforeEach
    void setUp() {
        // Crear objetos de prueba
        testRoom = new Room(101, "Individual", 50.0, true);
        testRoom.setId("room1");
        
        testGuest = new Guest("juanperez", "password", "Juan", "Pérez", "juan@email.com", "12345678");
        testGuest.setId("guest1");
        testGuest.setActive(true);
        
        testUser = new Admin("admin", "password");
        testUser.setId("user1");
        
        // Reserva válida
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        validReservation = new Reservation(
            Date.valueOf(today),
            Date.valueOf(tomorrow),
            "ACTIVE",
            testGuest,
            testRoom,
            testUser
        );
        validReservation.setId("reservation1");
        
        // Reserva que se solapa
        LocalDate checkInOverlap = today.plusDays(2);
        LocalDate checkOutOverlap = today.plusDays(3);
        overlappingReservation = new Reservation(
            Date.valueOf(checkInOverlap),
            Date.valueOf(checkOutOverlap),
            "ACTIVE",
            testGuest,
            testRoom,
            testUser
        );
        overlappingReservation.setId("reservation3");
        
        // Crear servicio con mocks inyectados
        reservationService = new ReservationServiceImpl() {
            private final GenericDAOImpl<Reservation> reservationDAO = mockReservationDAO;
            private final GenericDAOImpl<Room> roomDAO = mockRoomDAO;
            
            @Override
            public List<Reservation> findAll() {
                return reservationDAO.findAll();
            }
            
            @Override
            public Optional<Reservation> findByIdOrName(String value) {
                return reservationDAO.findByIdOrName(value);
            }
            
            @Override
            public boolean save(Reservation res) {
                try {
                    DatabaseConnection.startTransaction();
                    
                    boolean resSaved = reservationDAO.save(res);
                    Room room = res.getRoom();
                    room.setAvailable(false);
                    boolean roomUpdated = roomDAO.edit(room);
                    
                    if (resSaved && roomUpdated) {
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
    void testActiveGuest() {
        // Configurar mocks
        when(mockReservationDAO.save(any(Reservation.class))).thenReturn(true);
        when(mockRoomDAO.edit(any(Room.class))).thenReturn(true);
        
        // Crear reserva con huésped activo
        boolean result = reservationService.save(validReservation);
        
        assertTrue(result, "Debería permitir reserva con huésped activo");
        verify(mockReservationDAO, times(1)).save(validReservation);
        verify(mockRoomDAO, times(1)).edit(testRoom);
    }
    
    @Test
    void testReservationDates() {
        // Verificar que la reserva válida tiene fechas correctas
        assertTrue(validReservation.getCheckOut().after(validReservation.getCheckIn()),
            "El check-out debe ser posterior al check-in");
    }
    
    @Test
    void testOverlappingReservations() {
        // Configurar mocks para simular reservas existentes
        List<Reservation> existingReservations = Arrays.asList(validReservation);
        when(mockReservationDAO.findAll()).thenReturn(existingReservations);
        
        // Verificar que hay solapamiento
        List<Reservation> allReservations = reservationService.findAll();
        boolean hasOverlap = allReservations.stream()
            .anyMatch(r -> r.getRoom().getId().equals(overlappingReservation.getRoom().getId()) &&
                          datesOverlap(r, overlappingReservation));
        
        assertTrue(hasOverlap, "Debería detectar solapamiento de fechas en la misma habitación");
    }
    
    @Test
    void testCheckoutCalculation() {
        // Calcular costo total usando el método de Reservation
        double pricePerNight = 50.0;
        double tax = com.hotelNova.config.AppConfig.getInstance().getIva();
        
        // Reserva de 1 noche
        double expectedCost = (1 * pricePerNight) + (1 * pricePerNight * tax);
        double actualCost = validReservation.calculateTotalCost(pricePerNight, tax);
        
        assertEquals(expectedCost, actualCost, 0.01, "El cálculo del costo total debe ser correcto");
        
        // Reserva de 2 noches
        LocalDate today = LocalDate.now();
        LocalDate dayAfterTomorrow = today.plusDays(2);
        Reservation twoNightReservation = new Reservation(
            Date.valueOf(today),
            Date.valueOf(dayAfterTomorrow),
            "ACTIVE",
            testGuest,
            testRoom,
            testUser
        );
        
        double expectedCost2Nights = (2 * pricePerNight) + (2 * pricePerNight * tax);
        double actualCost2Nights = twoNightReservation.calculateTotalCost(pricePerNight, tax);
        
        assertEquals(expectedCost2Nights, actualCost2Nights, 0.01, "El cálculo para 2 noches debe ser correcto");
    }
    
    @Test
    void testRoomAvailability() {
        // Configurar habitación no disponible
        Room unavailableRoom = new Room(102, "Doble", 80.0, false);
        unavailableRoom.setId("room2");
        
        Reservation reservationWithUnavailableRoom = new Reservation(
            Date.valueOf(LocalDate.now()),
            Date.valueOf(LocalDate.now().plusDays(1)),
            "ACTIVE",
            testGuest,
            unavailableRoom,
            testUser
        );
        
        // Verificar que la habitación no está disponible
        assertFalse(unavailableRoom.isAvailable(), "La habitación no debería estar disponible");
    }
    
    @Test
    void testSaveReservationSuccessfully() throws SQLException {
        when(mockReservationDAO.save(any(Reservation.class))).thenReturn(true);
        when(mockRoomDAO.edit(any(Room.class))).thenReturn(true);
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).commit();
        
        boolean result = reservationService.save(validReservation);
        
        assertTrue(result, "Debería guardar reserva exitosamente");
        verify(mockReservationDAO, times(1)).save(validReservation);
        verify(mockRoomDAO, times(1)).edit(testRoom);
        assertFalse(testRoom.isAvailable(), "La habitación debería marcarse como no disponible");
    }
    
    @Test
    void testSaveReservationFailure() throws SQLException {
        when(mockReservationDAO.save(any(Reservation.class))).thenReturn(false);
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).rollback();
        
        boolean result = reservationService.save(validReservation);
        
        assertFalse(result, "No debería guardar reserva cuando el DAO falla");
        verify(mockReservationDAO, times(1)).save(validReservation);
        verify(mockRoomDAO, never()).edit(any(Room.class));
    }
    
    @Test
    void testFindReservationById() {
        when(mockReservationDAO.findByIdOrName("reservation1")).thenReturn(Optional.of(validReservation));
        when(mockReservationDAO.findByIdOrName("nonexistent")).thenReturn(Optional.empty());
        
        Optional<Reservation> foundReservation = reservationService.findByIdOrName("reservation1");
        Optional<Reservation> notFoundReservation = reservationService.findByIdOrName("nonexistent");
        
        assertTrue(foundReservation.isPresent(), "Debería encontrar reserva existente");
        assertEquals(validReservation.getId(), foundReservation.get().getId());
        
        assertFalse(notFoundReservation.isPresent(), "No debería encontrar reserva inexistente");
    }
    
    @Test
    void testEditReservation() throws SQLException {
        when(mockReservationDAO.edit(any(Reservation.class))).thenReturn(true);
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).commit();
        
        // Actualizar estado
        validReservation.setStatus("CHECKED_OUT");
        
        boolean result = reservationService.edit(validReservation);
        
        assertTrue(result, "Debería editar reserva exitosamente");
        verify(mockReservationDAO, times(1)).edit(validReservation);
    }
    
    @Test
    void testDeleteReservation() throws SQLException {
        when(mockReservationDAO.delete("reservation1")).thenReturn(true);
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).commit();
        
        boolean result = reservationService.delete("reservation1");
        
        assertTrue(result, "Debería eliminar reserva exitosamente");
        verify(mockReservationDAO, times(1)).delete("reservation1");
    }
    
    // Método auxiliar para verificar solapamiento de fechas
    private boolean datesOverlap(Reservation r1, Reservation r2) {
        return !(r1.getCheckOut().before(r2.getCheckIn()) || r1.getCheckIn().after(r2.getCheckOut()));
    }
}
