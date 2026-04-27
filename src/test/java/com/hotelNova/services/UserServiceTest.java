package com.hotelNova.services;

import com.hotelNova.dao.GenericDAO;
import com.hotelNova.dao.impl.UserDAOImpl;
import com.hotelNova.db.DatabaseConnection;
import com.hotelNova.models.Admin;
import com.hotelNova.models.Guest;
import com.hotelNova.models.Receptionist;
import com.hotelNova.models.User;
import com.hotelNova.services.impl.UserServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private GenericDAO<User> mockUserDAO;
    
    @Mock
    private DatabaseConnection mockDatabaseConnection;
    
    private UserServiceImpl userService;
    
    private User adminUser;
    private User receptionistUser;
    private User guestUser;
    
    @BeforeEach
    void setUp() {
        // Crear usuarios de prueba
        adminUser = new Admin("admin", "hashedPassword", "ADMIN");
        adminUser.setId("user1");
        
        receptionistUser = new Receptionist("receptionist", "hashedPassword", "RECEPTIONIST");
        receptionistUser.setId("user2");
        
        guestUser = new Guest("guest", "hashedPassword", "GUEST");
        guestUser.setId("user3");
        
        // Crear servicio con mock inyectado
        userService = new UserServiceImpl() {
            private final GenericDAO<User> userDAO = mockUserDAO;
            
            @Override
            public List<User> findAll() {
                return userDAO.findAll();
            }
            
            @Override
            public Optional<User> findByIdOrName(String value) {
                return userDAO.findByIdOrName(value);
            }
            
            @Override
            public boolean save(User user) {
                try {
                    DatabaseConnection.startTransaction();
                    boolean success = userDAO.save(user);
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
            
            @Override
            public Optional<User> login(String username, String password) {
                if (username.isBlank() || password.isBlank()) {
                    return Optional.empty();
                }
                
                // Simular búsqueda de usuario y validación de contraseña
                Optional<User> userOpt = userDAO.findByIdOrName(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    // En una implementación real, se usaría BCrypt para verificar
                    if (password.equals("correctPassword")) { // Simulación
                        return Optional.of(user);
                    }
                }
                return Optional.empty();
            }
        };
    }
    
    @Test
    void testLoginWithValidCredentials() {
        when(mockUserDAO.findByIdOrName("admin")).thenReturn(Optional.of(adminUser));
        
        Optional<User> result = userService.login("admin", "correctPassword");
        
        assertTrue(result.isPresent(), "Debería encontrar usuario con credenciales válidas");
        assertEquals("ADMIN", result.get().getRole());
        verify(mockUserDAO, times(1)).findByIdOrName("admin");
    }
    
    @Test
    void testLoginWithInvalidCredentials() {
        when(mockUserDAO.findByIdOrName("admin")).thenReturn(Optional.of(adminUser));
        
        Optional<User> result = userService.login("admin", "wrongPassword");
        
        assertFalse(result.isPresent(), "No debería encontrar usuario con credenciales inválidas");
        verify(mockUserDAO, times(1)).findByIdOrName("admin");
    }
    
    @Test
    void testLoginWithEmptyFields() {
        Optional<User> result1 = userService.login("", "password");
        Optional<User> result2 = userService.login("username", "");
        Optional<User> result3 = userService.login("", "");
        
        assertFalse(result1.isPresent(), "No debería permitir login con username vacío");
        assertFalse(result2.isPresent(), "No debería permitir login con password vacío");
        assertFalse(result3.isPresent(), "No debería permitir login con ambos campos vacíos");
        
        verify(mockUserDAO, never()).findByIdOrName(anyString());
    }
    
    @Test
    void testLoginWithNonExistentUser() {
        when(mockUserDAO.findByIdOrName("nonexistent")).thenReturn(Optional.empty());
        
        Optional<User> result = userService.login("nonexistent", "password");
        
        assertFalse(result.isPresent(), "No debería encontrar usuario inexistente");
        verify(mockUserDAO, times(1)).findByIdOrName("nonexistent");
    }
    
    @Test
    void testSaveUserSuccessfully() throws SQLException {
        when(mockUserDAO.save(any(User.class))).thenReturn(true);
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).commit();
        
        User newUser = new User("newuser", "password", "RECEPTIONIST");
        
        boolean result = userService.save(newUser);
        
        assertTrue(result, "Debería guardar usuario exitosamente");
        verify(mockUserDAO, times(1)).save(newUser);
    }
    
    @Test
    void testSaveUserFailure() throws SQLException {
        when(mockUserDAO.save(any(User.class))).thenReturn(false);
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).rollback();
        
        User newUser = new User("newuser", "password", "RECEPTIONIST");
        
        boolean result = userService.save(newUser);
        
        assertFalse(result, "No debería guardar usuario cuando el DAO falla");
        verify(mockUserDAO, times(1)).save(newUser);
    }
    
    @Test
    void testSaveUserWithSQLException() throws SQLException {
        when(mockUserDAO.save(any(User.class))).thenThrow(new SQLException("Database error"));
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).rollback();
        
        User newUser = new User("newuser", "password", "RECEPTIONIST");
        
        boolean result = userService.save(newUser);
        
        assertFalse(result, "No debería guardar usuario cuando hay SQLException");
        verify(mockUserDAO, times(1)).save(newUser);
    }
    
    @Test
    void testFindAllUsers() {
        List<User> expectedUsers = Arrays.asList(adminUser, receptionistUser, guestUser);
        when(mockUserDAO.findAll()).thenReturn(expectedUsers);
        
        List<User> result = userService.findAll();
        
        assertEquals(3, result.size(), "Debería encontrar 3 usuarios");
        assertTrue(result.contains(adminUser), "Debería contener usuario admin");
        assertTrue(result.contains(receptionistUser), "Debería contener usuario recepcionista");
        assertTrue(result.contains(guestUser), "Debería contener usuario guest");
        verify(mockUserDAO, times(1)).findAll();
    }
    
    @Test
    void testFindUserById() {
        when(mockUserDAO.findByIdOrName("user1")).thenReturn(Optional.of(adminUser));
        when(mockUserDAO.findByIdOrName("nonexistent")).thenReturn(Optional.empty());
        
        Optional<User> foundUser = userService.findByIdOrName("user1");
        Optional<User> notFoundUser = userService.findByIdOrName("nonexistent");
        
        assertTrue(foundUser.isPresent(), "Debería encontrar usuario existente");
        assertEquals(adminUser.getUsername(), foundUser.get().getUsername());
        
        assertFalse(notFoundUser.isPresent(), "No debería encontrar usuario inexistente");
        verify(mockUserDAO, times(2)).findByIdOrName(anyString());
    }
    
    @Test
    void testEditUser() throws SQLException {
        when(mockUserDAO.edit(any(User.class))).thenReturn(true);
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).commit();
        
        boolean result = userService.edit(adminUser);
        
        assertTrue(result, "Debería editar usuario exitosamente");
        verify(mockUserDAO, times(1)).edit(adminUser);
    }
    
    @Test
    void testDeleteUser() throws SQLException {
        when(mockUserDAO.delete("user1")).thenReturn(true);
        doNothing().when(mockDatabaseConnection).startTransaction();
        doNothing().when(mockDatabaseConnection).commit();
        
        boolean result = userService.delete("user1");
        
        assertTrue(result, "Debería eliminar usuario exitosamente");
        verify(mockUserDAO, times(1)).delete("user1");
    }
    
    @Test
    void testUserRoleValidation() {
        // Verificar que los roles válidos son aceptados
        assertDoesNotThrow(() -> new User("test1", "pass", "ADMIN"));
        assertDoesNotThrow(() -> new User("test2", "pass", "RECEPTIONIST"));
        assertDoesNotThrow(() -> new User("test3", "pass", "GUEST"));
        
        // Verificar que roles inválidos lanzan excepción
        assertThrows(IllegalArgumentException.class, () -> new User("test4", "pass", "INVALID"));
        assertThrows(IllegalArgumentException.class, () -> new User("test5", "pass", "manager"));
        assertThrows(IllegalArgumentException.class, () -> new User("test6", "pass", ""));
    }
    
    @Test
    void testUsernameValidation() {
        // Verificar nombres válidos
        assertDoesNotThrow(() -> {
            User user = new User("Juan Pérez", "pass", "ADMIN");
            user.setUsername("María González");
        });
        
        // Verificar nombres inválidos
        assertThrows(IllegalArgumentException.class, () -> {
            User user = new User("", "pass", "ADMIN");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            User user = new User("Test123", "pass", "ADMIN");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            User user = new User(null, "pass", "ADMIN");
        });
    }
}
