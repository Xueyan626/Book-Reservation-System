package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import usyd.library_reservation_system.library_reservation_system.dto.RegisterRequest;
import usyd.library_reservation_system.library_reservation_system.dto.RegisterResponse;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "John Doe",
                "1234567890",
                "john@example.com",
                "password123"
        );

        String encodedPassword = "$2a$10$encodedPasswordHash";
        UserEntity savedEntity = UserEntity.builder()
                .userId(1)
                .nickname("John Doe")
                .telephone("1234567890")
                .email("john@example.com")
                .passwordHash(encodedPassword)
                .isActive(true)
                .build();

        when(userRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
        when(userRepository.existsByTelephone("1234567890")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.userId());
        assertEquals("John Doe", response.nickname());
        assertEquals("1234567890", response.telephone());
        assertEquals("john@example.com", response.email());

        verify(userRepository, times(1)).existsByEmailIgnoreCase("john@example.com");
        verify(userRepository, times(1)).existsByTelephone("1234567890");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testRegister_EmailTrimmedAndLowercased() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Jane Smith",
                "0987654321",
                "  Jane@EXAMPLE.COM  ",
                "password123"
        );

        String encodedPassword = "$2a$10$encodedPasswordHash";
        UserEntity savedEntity = UserEntity.builder()
                .userId(2)
                .nickname("Jane Smith")
                .telephone("0987654321")
                .email("jane@example.com")
                .passwordHash(encodedPassword)
                .isActive(true)
                .build();

        when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
        when(userRepository.existsByTelephone("0987654321")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertEquals("jane@example.com", response.email());
        verify(userRepository, times(1)).existsByEmailIgnoreCase("jane@example.com");
    }

    @Test
    void testRegister_TelephoneTrimmed() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Bob Wilson",
                "  5551234567  ",
                "bob@example.com",
                "password123"
        );

        String encodedPassword = "$2a$10$encodedPasswordHash";
        UserEntity savedEntity = UserEntity.builder()
                .userId(3)
                .nickname("Bob Wilson")
                .telephone("5551234567")
                .email("bob@example.com")
                .passwordHash(encodedPassword)
                .isActive(true)
                .build();

        when(userRepository.existsByEmailIgnoreCase("bob@example.com")).thenReturn(false);
        when(userRepository.existsByTelephone("5551234567")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertEquals("5551234567", response.telephone());
        verify(userRepository, times(1)).existsByTelephone("5551234567");
    }

    @Test
    void testRegister_EmailAlreadyExists_ThrowsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Test User",
                "1111111111",
                "existing@example.com",
                "password123"
        );

        when(userRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

        // Act & Assert
        EmailOrPhoneAlreadyUsedException exception = assertThrows(
                EmailOrPhoneAlreadyUsedException.class,
                () -> authService.register(request)
        );

        assertEquals("Email already used: existing@example.com", exception.getMessage());
        verify(userRepository, times(1)).existsByEmailIgnoreCase("existing@example.com");
        verify(userRepository, never()).existsByTelephone(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testRegister_TelephoneAlreadyExists_ThrowsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Test User",
                "2222222222",
                "new@example.com",
                "password123"
        );

        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(userRepository.existsByTelephone("2222222222")).thenReturn(true);

        // Act & Assert
        EmailOrPhoneAlreadyUsedException exception = assertThrows(
                EmailOrPhoneAlreadyUsedException.class,
                () -> authService.register(request)
        );

        assertEquals("Telephone already used: 2222222222", exception.getMessage());
        verify(userRepository, times(1)).existsByEmailIgnoreCase("new@example.com");
        verify(userRepository, times(1)).existsByTelephone("2222222222");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testRegister_ConcurrentConflict_ThrowsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "Test User",
                "3333333333",
                "concurrent@example.com",
                "password123"
        );

        String encodedPassword = "$2a$10$encodedPasswordHash";

        when(userRepository.existsByEmailIgnoreCase("concurrent@example.com")).thenReturn(false);
        when(userRepository.existsByTelephone("3333333333")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
        when(userRepository.save(any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // Act & Assert
        EmailOrPhoneAlreadyUsedException exception = assertThrows(
                EmailOrPhoneAlreadyUsedException.class,
                () -> authService.register(request)
        );

        assertEquals("Email or telephone already used", exception.getMessage());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testRegister_NicknameTrimmed() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "  Alice Brown  ",
                "4444444444",
                "alice@example.com",
                "password123"
        );

        String encodedPassword = "$2a$10$encodedPasswordHash";
        UserEntity savedEntity = UserEntity.builder()
                .userId(4)
                .nickname("Alice Brown")
                .telephone("4444444444")
                .email("alice@example.com")
                .passwordHash(encodedPassword)
                .isActive(true)
                .build();

        when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        when(userRepository.existsByTelephone("4444444444")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertEquals("Alice Brown", response.nickname());
    }
}
