package usyd.library_reservation_system.library_reservation_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import usyd.library_reservation_system.library_reservation_system.service.ChatService;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*",
        allowedHeaders = {"*", "X-USER-ID"},
        exposedHeaders = {"X-USER-ID"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS}
)
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, Object> request) {
        String message = (String) request.get("message");
        String response = chatService.getChatResponse(message);
        
        return ResponseEntity.ok(Map.of("message", response));
    }
}

