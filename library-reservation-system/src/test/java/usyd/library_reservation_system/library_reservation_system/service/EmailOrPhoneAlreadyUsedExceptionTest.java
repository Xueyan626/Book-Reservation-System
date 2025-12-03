package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EmailOrPhoneAlreadyUsedExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Email already exists";
        EmailOrPhoneAlreadyUsedException exception = new EmailOrPhoneAlreadyUsedException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        EmailOrPhoneAlreadyUsedException exception = new EmailOrPhoneAlreadyUsedException("Test message");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testExceptionCanBeThrown() {
        String message = "Phone number already in use";
        
        Exception exception = assertThrows(EmailOrPhoneAlreadyUsedException.class, () -> {
            throw new EmailOrPhoneAlreadyUsedException(message);
        });

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testExceptionWithEmailMessage() {
        String message = "Email 'test@example.com' is already registered";
        EmailOrPhoneAlreadyUsedException exception = new EmailOrPhoneAlreadyUsedException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testExceptionWithPhoneMessage() {
        String message = "Phone number '1234567890' is already in use";
        EmailOrPhoneAlreadyUsedException exception = new EmailOrPhoneAlreadyUsedException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testExceptionWithEmptyMessage() {
        String emptyMessage = "";
        EmailOrPhoneAlreadyUsedException exception = new EmailOrPhoneAlreadyUsedException(emptyMessage);

        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void testExceptionWithNullMessage() {
        EmailOrPhoneAlreadyUsedException exception = new EmailOrPhoneAlreadyUsedException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionWithLongMessage() {
        String longMessage = "The email address you provided is already associated with an existing account in our system. Please use a different email address or try to recover your existing account.";
        EmailOrPhoneAlreadyUsedException exception = new EmailOrPhoneAlreadyUsedException(longMessage);

        assertEquals(longMessage, exception.getMessage());
    }

    @Test
    void testMultipleInstances() {
        EmailOrPhoneAlreadyUsedException exception1 = new EmailOrPhoneAlreadyUsedException("Email exists");
        EmailOrPhoneAlreadyUsedException exception2 = new EmailOrPhoneAlreadyUsedException("Phone exists");

        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertNotSame(exception1, exception2);
    }

    @Test
    void testExceptionInheritance() {
        EmailOrPhoneAlreadyUsedException exception = new EmailOrPhoneAlreadyUsedException("Test");
        
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
    }
}

