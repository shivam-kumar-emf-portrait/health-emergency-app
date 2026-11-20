package com.lifeline.emergency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeline.emergency.dto.ChatRequest;
import com.lifeline.emergency.dto.ChatResponse;
import com.lifeline.emergency.entity.ChatMessage;
import com.lifeline.emergency.entity.ChatSession;
import com.lifeline.emergency.entity.User;
import com.lifeline.emergency.repository.ChatSessionRepository;
import com.lifeline.emergency.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openrouter.apiKey:}")
    private String openRouterApiKey;

    @Value("${openrouter.apiUrl:https://openrouter.ai/api/v1/chat/completions}")
    private String openRouterApiUrl;

    @Value("${openrouter.model:meta-llama/llama-3.1-8b-instruct}")
    private String modelName;

    private static final String HEALTH_AND_FITNESS_PROMPT = """
            You are a friendly, caring, and easy-to-understand Health and Fitness Assistant.

            STRICT TOPIC RULE:
            - You ONLY talk about health, fitness, exercises, medical symptoms, first aid, diet, nutrition, weight management, and healthy living.
            - You MUST NOT answer questions on any other topic (such as programming, coding, math, general science, politics, movies, entertainment, sports trivia, gaming, business, finance, or gossip).
            - If the user asks about ANYTHING that is NOT health or fitness, politely refuse with this exact response:
              "I am your Health and Fitness Assistant. I can only help you with questions about your health, body symptoms, fitness, exercises, and diet. How can I help with your health today?"

            EASY ENGLISH GUIDELINES:
            - Use very simple, everyday English. Do not use difficult medical or scientific words.
            - A person with basic English should easily understand your answer.
            - Explain what symptoms could mean in simple words.
            - Keep each response brief (2 to 4 simple sentences).
            - Ask ONLY ONE simple question at the end to guide the user.

            PERSONAL MEMORY:
            - You remember what the user told you in earlier messages. Use this memory to give personalized, continuous health advice.

            SAFETY AND FIRST AID:
            - You do not prescribe medicines.
            - If the user has a serious emergency (severe chest pain, breathing trouble, heavy bleeding, sudden weakness, stroke symptoms), tell them in simple words to go to the nearest emergency hospital immediately or call for an ambulance.
            """;

    private static final String NON_HEALTH_REFUSAL = 
            "I am your Health and Fitness Assistant. I can only help you with questions about your health, body symptoms, fitness, exercises, and diet. How can I help with your health today?";

    public AiChatService(ChatSessionRepository chatSessionRepository, UserRepository userRepository) {
        this.chatSessionRepository = chatSessionRepository;
        this.userRepository = userRepository;
    }

    private boolean isNonHealthTopic(String msg) {
        String lower = msg.toLowerCase();
        String[] nonHealthTriggers = {
            "write code", "python", "javascript", "java ", "c++", "html", "css", "sql",
            "solve equation", "2+2", "calculate math", "who won", "president",
            "movie", "song", "actor", "actress", "bitcoin", "crypto", "stock market",
            "capital of", "history of", "tell me a joke", "gaming", "playstation", "xbox"
        };
        for (String trigger : nonHealthTriggers) {
            if (lower.contains(trigger)) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public ChatResponse processChat(ChatRequest request, Long userId) {
        String userMessage = request.getMessage() != null ? request.getMessage().trim() : "";
        if (userMessage.isEmpty()) {
            return new ChatResponse("Please describe what health or fitness question you have.", true, "MILD", "General Health");
        }

        // Enforce health & fitness topic boundary
        if (isNonHealthTopic(userMessage)) {
            return new ChatResponse(NON_HEALTH_REFUSAL, true, "INFO", "Health & Fitness Only");
        }

        // Triage severity detection for emergency red flags
        String lower = userMessage.toLowerCase();
        String detectedSeverity = "MILD";
        String suggestedSpecialty = "General Health";

        if (lower.contains("chest pain") || lower.contains("heart") || lower.contains("palpitation") || lower.contains("left arm")) {
            detectedSeverity = "EMERGENCY";
            suggestedSpecialty = "Heart Specialist (Cardiologist)";
        } else if (lower.contains("stroke") || lower.contains("numb") || lower.contains("paraly") || lower.contains("face droop") || lower.contains("slur")) {
            detectedSeverity = "EMERGENCY";
            suggestedSpecialty = "Brain Specialist (Neurologist)";
        } else if (lower.contains("breath") || lower.contains("chok") || lower.contains("wheez") || lower.contains("asthma")) {
            detectedSeverity = "EMERGENCY";
            suggestedSpecialty = "Lung Specialist (Pulmonologist)";
        } else if (lower.contains("fracture") || lower.contains("accident") || lower.contains("crash") || lower.contains("bleeding heavily")) {
            detectedSeverity = "EMERGENCY";
            suggestedSpecialty = "Trauma & Bone Specialist";
        } else if (lower.contains("bone") || lower.contains("sprain") || lower.contains("joint") || lower.contains("knee") || lower.contains("back pain")) {
            detectedSeverity = "MODERATE";
            suggestedSpecialty = "Bone Specialist (Orthopedic)";
        } else if (lower.contains("pregnant") || lower.contains("labor") || lower.contains("cramp")) {
            detectedSeverity = "MODERATE";
            suggestedSpecialty = "Women's Health / Delivery";
        }

        // Load user's private persistent chat session for continuous memory
        ChatSession session = null;
        List<Map<String, String>> memoryHistory = new ArrayList<>();
        if (userId != null) {
            session = chatSessionRepository.findTopByUserIdOrderByUpdatedAtDesc(userId)
                    .orElseGet(() -> {
                        Optional<User> u = userRepository.findById(userId);
                        return u.map(ChatSession::new).orElse(null);
                    });

            if (session != null && session.getMessages() != null) {
                // Include last 10 messages from past conversations as memory
                List<ChatMessage> existing = session.getMessages();
                int start = Math.max(0, existing.size() - 10);
                for (int i = start; i < existing.size(); i++) {
                    ChatMessage m = existing.get(i);
                    memoryHistory.add(Map.of("role", m.getRole(), "content", m.getContent()));
                }
            }
        }

        // Call OpenRouter with continuous memory
        String aiReply = callOpenRouterWithMemory(userMessage, memoryHistory);

        // Save privately into user's chat session
        if (session != null) {
            session.getMessages().add(new ChatMessage("user", userMessage));
            session.getMessages().add(new ChatMessage("assistant", aiReply));
            chatSessionRepository.save(session);
        }

        return new ChatResponse(aiReply, true, detectedSeverity, suggestedSpecialty);
    }

    private String callOpenRouterWithMemory(String currentMsg, List<Map<String, String>> memory) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openRouterApiKey);
            headers.set("HTTP-Referer", "http://localhost:8080");
            headers.set("X-Title", "Lifeline Health and Fitness Assistant");

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", HEALTH_AND_FITNESS_PROMPT));

            // Add previous memory messages
            if (memory != null && !memory.isEmpty()) {
                messages.addAll(memory);
            }

            // Append current user message
            messages.add(Map.of("role", "user", "content", currentMsg));

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", modelName);
            payload.put("messages", messages);
            payload.put("temperature", 0.6);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(openRouterApiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
                if (!contentNode.isMissingNode() && !contentNode.asText().isEmpty()) {
                    return contentNode.asText();
                }
            }
        } catch (Exception e) {
            System.err.println("OpenRouter call failed or timed out: " + e.getMessage() + ". Executing friendly health fallback.");
        }

        return generateEasyEnglishFallback(currentMsg);
    }

    private String generateEasyEnglishFallback(String msg) {
        if (isNonHealthTopic(msg)) {
            return NON_HEALTH_REFUSAL;
        }

        String lower = msg.toLowerCase();
        if (lower.contains("chest") || lower.contains("heart")) {
            return "Chest pain is important to check carefully. Does it feel heavy or sharp, and does it spread to your left arm or neck? If it is severe, please go to the nearest emergency hospital right away.";
        } else if (lower.contains("headache") || lower.contains("head")) {
            return "Headaches can happen from stress, lack of sleep, or dehydration. Did your headache start suddenly or slowly? Please make sure to drink some water and rest in a quiet room.";
        } else if (lower.contains("weight") || lower.contains("diet") || lower.contains("fat") || lower.contains("calories")) {
            return "A good diet and daily movement are the best ways to stay healthy. Are you looking to lose weight, build strength, or eat more balanced meals every day?";
        } else if (lower.contains("exercise") || lower.contains("workout") || lower.contains("gym") || lower.contains("fitness")) {
            return "Staying active helps your heart, muscles, and energy levels. Do you prefer light walking, home exercises, or gym workouts?";
        } else if (lower.contains("stomach") || lower.contains("belly") || lower.contains("digest")) {
            return "Stomach discomfort can happen after eating heavy food or from dehydration. Is the pain in the upper or lower stomach, and do you feel any nausea?";
        } else if (lower.contains("fever") || lower.contains("cold") || lower.contains("cough")) {
            return "I am sorry you are feeling unwell. How many days have you had the fever or cough? Be sure to drink plenty of warm water and rest.";
        } else {
            return "Thank you for asking. How long have you been feeling this, and does it feel mild, medium, or severe right now?";
        }
    }

    public List<ChatMessage> getUserChatHistory(Long userId) {
        if (userId == null) return Collections.emptyList();
        return chatSessionRepository.findTopByUserIdOrderByUpdatedAtDesc(userId)
                .map(ChatSession::getMessages)
                .orElse(Collections.emptyList());
    }
}
