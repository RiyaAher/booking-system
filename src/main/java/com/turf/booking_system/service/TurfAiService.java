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
                               "Always use tools when users ask for price quotes or calculations.")
                .build();
    }

    public String askAi(String userMessage) {
        return this.chatClient.prompt()
                .user(userMessage)
                .functions("calculatePriceTool") // Registers the Spring Bean Function
                .call()
                .content();
    }
}
