package com.turf.booking_system.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TurfAiService {

    private final ChatClient chatClient;

    public TurfAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("You are the AI Concierge for Turf Arena. " +
                               "You help users check pitch availability and calculate pricing. " +
                               "Always use tools when users ask for booking availability, price quotes or calculations.")
                .build();
    }

    public String askAi(String userMessage) {
        try {
            return this.chatClient.prompt()
                .user(userMessage)
                .functions("checkAvailabilityTool", "calculatePriceTool") // Registers the Spring Bean Function
                .call()
                .content();    
        } catch (Exception e) {
            return "Sorry, I ran into an issue getting that information ☹️ Please try again!";
        }
    }
}
