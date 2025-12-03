package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InvalidCredentialsExceptionTest {

    @Test
    void testDefaultConstructor() {
        InvalidCredentialsException exception = new InvalidCredentialsException();

        assertNotNull(exception);
        assertEquals("Invalid email/telephone or password", exception.getMessage());
        assertEquals("global", exception.getField());
    }

    @Test
    void testConstructorWithMessage() {
        String customMessage = "Custom error message";
        InvalidCredentialsException exception = new InvalidCredentialsException(customMessage);

        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
        assertEquals("global", exception.getField());
    }

    @Test
    void testConstructorWithFieldAndMessage() {
        String field = "email";
        String message = "Email is invalid";
        InvalidCredentialsException exception = new InvalidCredentialsException(field, message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(field, exception.getField());
    }

    @Test
    void testGetField() {
        InvalidCredentialsException exception1 = new InvalidCredentialsException();
        assertEquals("global", exception1.getField());

        InvalidCredentialsException exception2 = new InvalidCredentialsException("password", "Password is incorrect");
        assertEquals("password", exception2.getField());
    }

    @Test
    void testExceptionIsRuntimeException() {
        InvalidCredentialsException exception = new InvalidCredentialsException();
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThrows(InvalidCredentialsException.class, () -> {
            throw new InvalidCredentialsException();
        });
    }

    @Test
    void testExceptionWithEmptyMessage() {
        String emptyMessage = "";
        InvalidCredentialsException exception = new InvalidCredentialsException(emptyMessage);

        assertEquals(emptyMessage, exception.getMessage());
        assertEquals("global", exception.getField());
    }

    @Test
    void testExceptionWithNullMessage() {
        InvalidCredentialsException exception = new InvalidCredentialsException((String) null);

        assertNull(exception.getMessage());
        assertEquals("global", exception.getField());
    }

    @Test
    void testExceptionWithEmptyField() {
        String emptyField = "";
        String message = "Test message";
        InvalidCredentialsException exception = new InvalidCredentialsException(emptyField, message);

        assertEquals(message, exception.getMessage());
        assertEquals(emptyField, exception.getField());
    }

    @Test
    void testExceptionWithNullField() {
        String message = "Test message";
        InvalidCredentialsException exception = new InvalidCredentialsException(null, message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getField());
    }

    @Test
    void testMultipleInstances() {
        InvalidCredentialsException exception1 = new InvalidCredentialsException("email", "Email error");
        InvalidCredentialsException exception2 = new InvalidCredentialsException("password", "Password error");

        assertNotEquals(exception1.getField(), exception2.getField());
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
    }
}

