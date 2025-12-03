package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Set default from address
        ReflectionTestUtils.setField(emailService, "from", "Library Reservation <lzhua002623@gmail.com>");
    }

    // ==================== sendLoginCode Tests ====================

    @Test
    void testSendLoginCode_Success() {
        // Arrange
        String to = "user@example.com";
        String code = "123456";
        int ttlMinutes = 10;
        
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode(to, code, ttlMinutes);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendLoginCode_SetsCorrectFrom() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertEquals("Library Reservation <lzhua002623@gmail.com>", message.getFrom());
    }

    @Test
    void testSendLoginCode_SetsCorrectTo() {
        // Arrange
        String to = "test@example.com";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode(to, "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getTo());
        assertEquals(1, message.getTo().length);
        assertEquals(to, message.getTo()[0]);
    }

    @Test
    void testSendLoginCode_SetsCorrectSubject() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertEquals("Your login verification code", message.getSubject());
    }

    @Test
    void testSendLoginCode_ContainsCodeInText() {
        // Arrange
        String code = "987654";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", code, 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getText());
        assertTrue(message.getText().contains(code));
    }

    @Test
    void testSendLoginCode_ContainsTTLInText() {
        // Arrange
        int ttlMinutes = 15;
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", ttlMinutes);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getText());
        assertTrue(message.getText().contains(String.valueOf(ttlMinutes)));
    }

    @Test
    void testSendLoginCode_TextContainsExpirationWarning() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getText());
        assertTrue(message.getText().contains("expire"));
    }

    @Test
    void testSendLoginCode_TextContainsSecurityNote() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getText());
        assertTrue(message.getText().toLowerCase().contains("ignore"));
    }

    @Test
    void testSendLoginCode_WithDifferentCodes() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert - Different codes should be reflected in the message
        String[] codes = {"111111", "222222", "333333"};
        
        for (String code : codes) {
            emailService.sendLoginCode("user@example.com", code, 10);
        }

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(3)).send(messageCaptor.capture());
        
        for (int i = 0; i < codes.length; i++) {
            assertTrue(messageCaptor.getAllValues().get(i).getText().contains(codes[i]));
        }
    }

    @Test
    void testSendLoginCode_WithDifferentTTL() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", 5);
        emailService.sendLoginCode("user@example.com", "123456", 20);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());
        
        assertTrue(messageCaptor.getAllValues().get(0).getText().contains("5"));
        assertTrue(messageCaptor.getAllValues().get(1).getText().contains("20"));
    }

    @Test
    void testSendLoginCode_WithMultipleRecipients() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        String[] recipients = {
            "user1@example.com",
            "user2@example.com",
            "user3@example.com"
        };

        // Act
        for (String recipient : recipients) {
            emailService.sendLoginCode(recipient, "123456", 10);
        }

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(3)).send(messageCaptor.capture());
        
        for (int i = 0; i < recipients.length; i++) {
            assertEquals(recipients[i], messageCaptor.getAllValues().get(i).getTo()[0]);
        }
    }

    @Test
    void testSendLoginCode_WithCustomFromAddress() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "from", "Custom Sender <custom@example.com>");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        assertEquals("Custom Sender <custom@example.com>", messageCaptor.getValue().getFrom());
    }

    // ==================== sendRegisterCode Tests ====================

    @Test
    void testSendRegisterCode_Success() {
        // Arrange
        String to = "newuser@example.com";
        String code = "654321";
        int ttlMinutes = 10;
        
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode(to, code, ttlMinutes);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendRegisterCode_SetsCorrectFrom() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertEquals("Library Reservation <lzhua002623@gmail.com>", message.getFrom());
    }

    @Test
    void testSendRegisterCode_SetsCorrectTo() {
        // Arrange
        String to = "register@example.com";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode(to, "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getTo());
        assertEquals(1, message.getTo().length);
        assertEquals(to, message.getTo()[0]);
    }

    @Test
    void testSendRegisterCode_SetsCorrectSubject() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertEquals("Your registration code", message.getSubject());
    }

    @Test
    void testSendRegisterCode_ContainsCodeInText() {
        // Arrange
        String code = "789012";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode("user@example.com", code, 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getText());
        assertTrue(message.getText().contains(code));
    }

    @Test
    void testSendRegisterCode_ContainsTTLInText() {
        // Arrange
        int ttlMinutes = 12;
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode("user@example.com", "123456", ttlMinutes);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getText());
        assertTrue(message.getText().contains(String.valueOf(ttlMinutes)));
    }

    @Test
    void testSendRegisterCode_TextContainsValidityInfo() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getText());
        assertTrue(message.getText().toLowerCase().contains("valid"));
    }

    @Test
    void testSendRegisterCode_WithDifferentCodes() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        String[] codes = {"111111", "222222", "333333"};
        for (String code : codes) {
            emailService.sendRegisterCode("user@example.com", code, 10);
        }

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(3)).send(messageCaptor.capture());
        
        for (int i = 0; i < codes.length; i++) {
            assertTrue(messageCaptor.getAllValues().get(i).getText().contains(codes[i]));
        }
    }

    @Test
    void testSendRegisterCode_WithDifferentTTL() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode("user@example.com", "123456", 8);
        emailService.sendRegisterCode("user@example.com", "123456", 15);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());
        
        assertTrue(messageCaptor.getAllValues().get(0).getText().contains("8"));
        assertTrue(messageCaptor.getAllValues().get(1).getText().contains("15"));
    }

    @Test
    void testSendRegisterCode_WithMultipleRecipients() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        
        String[] recipients = {
            "newuser1@example.com",
            "newuser2@example.com",
            "newuser3@example.com"
        };

        // Act
        for (String recipient : recipients) {
            emailService.sendRegisterCode(recipient, "123456", 10);
        }

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(3)).send(messageCaptor.capture());
        
        for (int i = 0; i < recipients.length; i++) {
            assertEquals(recipients[i], messageCaptor.getAllValues().get(i).getTo()[0]);
        }
    }

    @Test
    void testSendRegisterCode_WithCustomFromAddress() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "from", "Registration <register@example.com>");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        assertEquals("Registration <register@example.com>", messageCaptor.getValue().getFrom());
    }

    // ==================== Edge Cases ====================

    @Test
    void testSendLoginCode_WithLongCode() {
        // Arrange
        String longCode = "1234567890123456";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", longCode, 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        assertTrue(messageCaptor.getValue().getText().contains(longCode));
    }

    @Test
    void testSendRegisterCode_WithLongCode() {
        // Arrange
        String longCode = "ABCDEFGHIJKLMNOP";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode("user@example.com", longCode, 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        assertTrue(messageCaptor.getValue().getText().contains(longCode));
    }

    @Test
    void testSendLoginCode_WithZeroTTL() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", 0);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        assertTrue(messageCaptor.getValue().getText().contains("0"));
    }

    @Test
    void testSendRegisterCode_WithZeroTTL() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode("user@example.com", "123456", 0);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        assertTrue(messageCaptor.getValue().getText().contains("0"));
    }

    @Test
    void testSendLoginCode_WithLargeTTL() {
        // Arrange
        int largeTTL = 1440; // 24 hours
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", largeTTL);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        assertTrue(messageCaptor.getValue().getText().contains(String.valueOf(largeTTL)));
    }

    @Test
    void testSendRegisterCode_WithLargeTTL() {
        // Arrange
        int largeTTL = 2880; // 48 hours
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode("user@example.com", "123456", largeTTL);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        assertTrue(messageCaptor.getValue().getText().contains(String.valueOf(largeTTL)));
    }

    @Test
    void testSendLoginCode_WithSpecialCharactersInEmail() {
        // Arrange
        String email = "user+test@example.co.uk";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode(email, "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        assertEquals(email, messageCaptor.getValue().getTo()[0]);
    }

    @Test
    void testSendRegisterCode_WithSpecialCharactersInEmail() {
        // Arrange
        String email = "test.user+register@example.com";
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode(email, "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        assertEquals(email, messageCaptor.getValue().getTo()[0]);
    }

    // ==================== Message Structure Tests ====================

    @Test
    void testSendLoginCode_MessageHasAllRequiredFields() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getFrom());
        assertNotNull(message.getTo());
        assertNotNull(message.getSubject());
        assertNotNull(message.getText());
    }

    @Test
    void testSendRegisterCode_MessageHasAllRequiredFields() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getFrom());
        assertNotNull(message.getTo());
        assertNotNull(message.getSubject());
        assertNotNull(message.getText());
    }

    @Test
    void testSendLoginCode_TextIsNotEmpty() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getText());
        assertFalse(message.getText().isEmpty());
        assertTrue(message.getText().length() > 0);
    }

    @Test
    void testSendRegisterCode_TextIsNotEmpty() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        
        SimpleMailMessage message = messageCaptor.getValue();
        assertNotNull(message.getText());
        assertFalse(message.getText().isEmpty());
        assertTrue(message.getText().length() > 0);
    }

    // ==================== JavaMailSender Interaction Tests ====================

    @Test
    void testSendLoginCode_CallsMailSenderOnce() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", 10);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verifyNoMoreInteractions(mailSender);
    }

    @Test
    void testSendRegisterCode_CallsMailSenderOnce() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendRegisterCode("user@example.com", "123456", 10);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verifyNoMoreInteractions(mailSender);
    }

    @Test
    void testMultipleEmailsSent() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user1@example.com", "111111", 10);
        emailService.sendRegisterCode("user2@example.com", "222222", 10);
        emailService.sendLoginCode("user3@example.com", "333333", 10);

        // Assert
        verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
    }

    // ==================== Comparison Tests ====================

    @Test
    void testLoginAndRegisterCodes_HaveDifferentSubjects() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", 10);
        emailService.sendRegisterCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());
        
        String loginSubject = messageCaptor.getAllValues().get(0).getSubject();
        String registerSubject = messageCaptor.getAllValues().get(1).getSubject();
        
        assertNotEquals(loginSubject, registerSubject);
    }

    @Test
    void testLoginAndRegisterCodes_HaveDifferentTextContent() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", 10);
        emailService.sendRegisterCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());
        
        String loginText = messageCaptor.getAllValues().get(0).getText();
        String registerText = messageCaptor.getAllValues().get(1).getText();
        
        assertNotEquals(loginText, registerText);
    }

    @Test
    void testLoginAndRegisterCodes_UseSameFromAddress() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailService.sendLoginCode("user@example.com", "123456", 10);
        emailService.sendRegisterCode("user@example.com", "123456", 10);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());
        
        String loginFrom = messageCaptor.getAllValues().get(0).getFrom();
        String registerFrom = messageCaptor.getAllValues().get(1).getFrom();
        
        assertEquals(loginFrom, registerFrom);
    }
}

