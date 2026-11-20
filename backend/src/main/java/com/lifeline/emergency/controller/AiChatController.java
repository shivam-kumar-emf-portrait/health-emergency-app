package com.lifeline.emergency.controller;

import com.lifeline.emergency.dto.ChatRequest;
import com.lifeline.emergency.dto.ChatResponse;
import com.lifeline.emergency.entity.ChatMessage;
import com.lifeline.emergency.service.AiChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    private Long extractUserId(Authentication auth) {
        if (auth != null && auth.getCredentials() instanceof Long) {
            return (Long) auth.getCredentials();
        }
        return null;
    }

    private boolean isHospitalRole(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_HOSPITAL_ADMIN".equals(a.getAuthority()));
    }

    /**
     * AI Health & Fitness Chat:
     * Requires authentication. Guests cannot access the chat without logging in.
     * Hospital accounts are barred (they must use Hospital SaaS ERP).
     * All chats are stored privately per user and use past conversation memory.
     */
    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest request, Authentication auth) {
        Long userId = extractUserId(auth);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "LOGIN_REQUIRED",
                    "message", "Please log in to chat with the AI Health Assistant. Logging in keeps your health conversations private and saves your history."
            ));
        }

        if (isHospitalRole(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "FORBIDDEN_ROLE",
                    "message", "Hospital accounts cannot access patient health chat. Please use your Hospital SaaS ERP."
            ));
        }

        ChatResponse response = aiChatService.processChat(request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve user's private saved conversation history (requires login)
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication auth) {
        Long userId = extractUserId(auth);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "LOGIN_REQUIRED",
                    "message", "Please log in to view your chat history."
            ));
        }

        if (isHospitalRole(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "FORBIDDEN_ROLE",
                    "message", "Hospital accounts cannot access patient chat history."
            ));
        }

        List<ChatMessage> history = aiChatService.getUserChatHistory(userId);
        return ResponseEntity.ok(Map.of(
                "isSaved", true,
                "history", history
        ));
    }
}
