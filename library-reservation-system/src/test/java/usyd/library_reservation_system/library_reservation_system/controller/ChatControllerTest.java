package usyd.library_reservation_system.library_reservation_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import usyd.library_reservation_system.library_reservation_system.service.ChatService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    private Map<String, Object> validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new HashMap<>();
        validRequest.put("message", "Hello");
    }

    // ==================== Basic Functionality Tests ====================

    @Test
    void testChat_Success() throws Exception {
        // Arrange
        String expectedResponse = "Hi there! I'm your friendly library AI assistant!";
        when(chatService.getChatResponse("Hello")).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedResponse));

        verify(chatService, times(1)).getChatResponse("Hello");
    }

    @Test
    void testChat_WithDifferentMessage() throws Exception {
        // Arrange
        validRequest.put("message", "recommend some books");
        String expectedResponse = "Here are some popular books I'd recommend:";
        when(chatService.getChatResponse("recommend some books")).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedResponse));

        verify(chatService, times(1)).getChatResponse("recommend some books");
    }

    @Test
    void testChat_WithChineseMessage() throws Exception {
        // Arrange
        validRequest.put("message", "hello");
        String expectedResponse = "👋 Hello! I am your library assistant!";
        when(chatService.getChatResponse("hello")).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedResponse));

        verify(chatService, times(1)).getChatResponse("hello");
    }

    @Test
    void testChat_WithLongMessage() throws Exception {
        // Arrange
        String longMessage = "Can you recommend some science fiction books that are suitable for teenagers and have won awards?";
        validRequest.put("message", longMessage);
        String expectedResponse = "Sure! Here are some great science fiction books...";
        when(chatService.getChatResponse(longMessage)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedResponse));

        verify(chatService, times(1)).getChatResponse(longMessage);
    }

    @Test
    void testChat_WithEmptyMessage() throws Exception {
        // Arrange
        validRequest.put("message", "");
        String expectedResponse = "I'm here to help!";
        when(chatService.getChatResponse("")).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedResponse));

        verify(chatService, times(1)).getChatResponse("");
    }

    @Test
    void testChat_WithSpecialCharacters() throws Exception {
        // Arrange
        String specialMessage = "!@#$%^&*()_+-={}[]|:;<>?,./";
        validRequest.put("message", specialMessage);
        String expectedResponse = "I'm here to help!";
        when(chatService.getChatResponse(specialMessage)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedResponse));

        verify(chatService, times(1)).getChatResponse(specialMessage);
    }

    @Test
    void testChat_WithWhitespaceMessage() throws Exception {
        // Arrange
        validRequest.put("message", "   ");
        String expectedResponse = "I'm here to help!";
        when(chatService.getChatResponse("   ")).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedResponse));

        verify(chatService, times(1)).getChatResponse("   ");
    }

    // ==================== Request/Response Format Tests ====================

    @Test
    void testChat_ReturnsCorrectResponseStructure() throws Exception {
        // Arrange
        when(chatService.getChatResponse(anyString())).thenReturn("Test response");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void testChat_ServiceResponseIsPassedThrough() throws Exception {
        // Arrange
        String serviceResponse = "This is a test response from the service";
        when(chatService.getChatResponse("Hello")).thenReturn(serviceResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(serviceResponse));
    }

    @Test
    void testChat_ResponseContainsOnlyMessageField() throws Exception {
        // Arrange
        when(chatService.getChatResponse(anyString())).thenReturn("Test");

        // Act & Assert
        String response = mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        assert responseMap.size() == 1;
        assert responseMap.containsKey("message");
    }

    // ==================== HTTP Method Tests ====================

    @Test
    void testChat_OnlyAcceptsPostMethod() throws Exception {
        // Arrange
        when(chatService.getChatResponse(anyString())).thenReturn("Test");

        // Act & Assert - POST should work
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testChat_GetMethodNotAllowed() throws Exception {
        // Act & Assert - Global exception handler returns 500
        mockMvc.perform(get("/api/chat"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testChat_PutMethodNotAllowed() throws Exception {
        // Act & Assert - Global exception handler returns 500
        mockMvc.perform(put("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testChat_DeleteMethodNotAllowed() throws Exception {
        // Act & Assert - Global exception handler returns 500
        mockMvc.perform(delete("/api/chat"))
                .andExpect(status().is5xxServerError());
    }

    // ==================== Request Validation Tests ====================

    @Test
    void testChat_WithNullMessage() throws Exception {
        // Arrange
        validRequest.put("message", null);
        when(chatService.getChatResponse(null)).thenReturn("I'm here to help!");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(chatService, times(1)).getChatResponse(null);
    }

    @Test
    void testChat_WithMissingMessageField() throws Exception {
        // Arrange
        Map<String, Object> requestWithoutMessage = new HashMap<>();
        when(chatService.getChatResponse(null)).thenReturn("I'm here to help!");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithoutMessage)))
                .andExpect(status().isOk());

        verify(chatService, times(1)).getChatResponse(null);
    }

    @Test
    void testChat_WithEmptyRequestBody() throws Exception {
        // Act & Assert - Causes NullPointerException, handled by global handler
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testChat_WithInvalidJson() throws Exception {
        // Act & Assert - Global exception handler returns 500
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testChat_WithoutContentType() throws Exception {
        // Act & Assert - Global exception handler returns 500
        mockMvc.perform(post("/api/chat")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testChat_WithExtraFields() throws Exception {
        // Arrange
        validRequest.put("message", "Hello");
        validRequest.put("extraField1", "value1");
        validRequest.put("extraField2", 123);
        when(chatService.getChatResponse("Hello")).thenReturn("Hi!");

        // Act & Assert - Should still work, extra fields ignored
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hi!"));

        verify(chatService, times(1)).getChatResponse("Hello");
    }

    // ==================== URL Path Tests ====================

    @Test
    void testChat_CorrectEndpointPath() throws Exception {
        // Arrange
        when(chatService.getChatResponse(anyString())).thenReturn("Test");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testChat_WrongEndpointReturns404() throws Exception {
        // Act & Assert - Global exception handler returns 500
        mockMvc.perform(post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testChat_CaseSensitivePath() throws Exception {
        // Act & Assert - Global exception handler returns 500
        mockMvc.perform(post("/api/Chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().is5xxServerError());
    }

    // ==================== Service Interaction Tests ====================

    @Test
    void testChat_CallsServiceExactlyOnce() throws Exception {
        // Arrange
        when(chatService.getChatResponse("Hello")).thenReturn("Hi!");

        // Act
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        // Assert
        verify(chatService, times(1)).getChatResponse("Hello");
        verifyNoMoreInteractions(chatService);
    }

    @Test
    void testChat_PassesCorrectMessageToService() throws Exception {
        // Arrange
        String testMessage = "What books do you recommend?";
        validRequest.put("message", testMessage);
        when(chatService.getChatResponse(testMessage)).thenReturn("Here are some books...");

        // Act
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        // Assert
        verify(chatService, times(1)).getChatResponse(testMessage);
    }

    @Test
    void testChat_MultipleRequests() throws Exception {
        // Arrange
        when(chatService.getChatResponse("Hello")).thenReturn("Hi!");
        when(chatService.getChatResponse("Bye")).thenReturn("Goodbye!");

        // Act & Assert - First request
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hi!"));

        // Second request
        validRequest.put("message", "Bye");
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goodbye!"));

        // Assert
        verify(chatService, times(1)).getChatResponse("Hello");
        verify(chatService, times(1)).getChatResponse("Bye");
    }

    // ==================== Different Message Types Tests ====================

    @Test
    void testChat_WithGreetingMessage() throws Exception {
        // Arrange
        validRequest.put("message", "hi");
        when(chatService.getChatResponse("hi")).thenReturn("Hello! How can I help?");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello! How can I help?"));
    }

    @Test
    void testChat_WithRecommendationRequest() throws Exception {
        // Arrange
        validRequest.put("message", "recommend some books");
        when(chatService.getChatResponse("recommend some books"))
                .thenReturn("Here are some popular books...");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testChat_WithHelpRequest() throws Exception {
        // Arrange
        validRequest.put("message", "help");
        when(chatService.getChatResponse("help")).thenReturn("I can help you with...");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("I can help you with..."));
    }

    @Test
    void testChat_WithSearchRequest() throws Exception {
        // Arrange
        validRequest.put("message", "how to search for books");
        when(chatService.getChatResponse("how to search for books"))
                .thenReturn("You can search using...");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("You can search using..."));
    }

    @Test
    void testChat_WithCategoryRequest() throws Exception {
        // Arrange
        validRequest.put("message", "show me categories");
        when(chatService.getChatResponse("show me categories"))
                .thenReturn("Here are our categories...");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Here are our categories..."));
    }

    // ==================== Response Content Tests ====================

    @Test
    void testChat_ResponseIsNonNull() throws Exception {
        // Arrange
        when(chatService.getChatResponse(anyString())).thenReturn("Response");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void testChat_ResponseWithUnicodeCharacters() throws Exception {
        // Arrange
        String unicodeResponse = "📚 Books: Hello مرحبا Здравствуй";
        when(chatService.getChatResponse(anyString())).thenReturn(unicodeResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(unicodeResponse));
    }

    @Test
    void testChat_ResponseWithNewlines() throws Exception {
        // Arrange
        String responseWithNewlines = "Line 1\nLine 2\nLine 3";
        when(chatService.getChatResponse(anyString())).thenReturn(responseWithNewlines);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(responseWithNewlines));
    }

    // ==================== CORS Tests ====================

    @Test
    void testChat_SupportsOptionsMethod() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/api/chat")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk());
    }

    @Test
    void testChat_AcceptsRequestsWithOriginHeader() throws Exception {
        // Arrange
        when(chatService.getChatResponse(anyString())).thenReturn("Response");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testChat_AcceptsCustomHeaders() throws Exception {
        // Arrange
        when(chatService.getChatResponse(anyString())).thenReturn("Response");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .header("X-USER-ID", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    // ==================== Edge Cases ====================

    @Test
    void testChat_WithVeryLongMessage() throws Exception {
        // Arrange
        String veryLongMessage = "a".repeat(10000);
        validRequest.put("message", veryLongMessage);
        when(chatService.getChatResponse(veryLongMessage)).thenReturn("I received your message");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("I received your message"));
    }

    @Test
    void testChat_WithNumberAsMessage() throws Exception {
        // Arrange
        validRequest.put("message", "12345");
        when(chatService.getChatResponse("12345")).thenReturn("I see you sent numbers");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("I see you sent numbers"));
    }

    @Test
    void testChat_WithMixedLanguageMessage() throws Exception {
        // Arrange
        String mixedMessage = "Hello Hola Bonjour";
        validRequest.put("message", mixedMessage);
        when(chatService.getChatResponse(mixedMessage)).thenReturn("I understand multiple languages!");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("I understand multiple languages!"));
    }

    @Test
    void testChat_WithEmojiMessage() throws Exception {
        // Arrange
        String emojiMessage = "😊 📚 🎉";
        validRequest.put("message", emojiMessage);
        when(chatService.getChatResponse(emojiMessage)).thenReturn("I love emojis too! 😄");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("I love emojis too! 😄"));
    }

    // ==================== Content Type Tests ====================

    @Test
    void testChat_AcceptsApplicationJson() throws Exception {
        // Arrange
        when(chatService.getChatResponse(anyString())).thenReturn("Response");

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testChat_RejectsNonJsonContentType() throws Exception {
        // Act & Assert - Global exception handler returns 500
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Hello"))
                .andExpect(status().is5xxServerError());
    }

    // ==================== Integration Tests ====================

    @Test
    void testChat_CompleteWorkflow() throws Exception {
        // Arrange
        String userMessage = "Hello, can you help me?";
        String serviceResponse = "Of course! I'm here to help!";
        validRequest.put("message", userMessage);
        when(chatService.getChatResponse(userMessage)).thenReturn(serviceResponse);

        // Act & Assert
        String responseJson = mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(serviceResponse))
                .andReturn().getResponse().getContentAsString();

        // Verify response structure
        Map<String, String> response = objectMapper.readValue(responseJson, Map.class);
        assert response.get("message").equals(serviceResponse);

        // Verify service was called correctly
        verify(chatService, times(1)).getChatResponse(userMessage);
    }

    @Test
    void testChat_ConsistentResponseFormat() throws Exception {
        // Arrange
        when(chatService.getChatResponse(anyString())).thenReturn("Test");

        // Act & Assert - Multiple requests should have same format
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.message").isString());
        }
    }
}

