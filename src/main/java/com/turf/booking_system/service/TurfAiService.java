package com.turf.booking_system.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TurfAiService {

    private final ChatClient chatClient;

    public TurfAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String askAi(String userMessage) {
        try {
            String currentContext = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("EEEE, yyyy-MM-dd HH:mm")
            );

            return this.chatClient.prompt()
                .system("""
                    You are the AI Concierge for Turf Arena.
                    Current reference date and time is: %s.
                    
                    When users mention dates/times naturally (like "next Saturday", "tomorrow at 4pm", "coming Sunday 10am"):
                    1. Calculate the exact ISO-8601 date-time string relative to the current reference date (%s).
                    2. If duration/end time is not specified, default to 1 hour after the start time.
                    3. Always pass the calculated ISO dates (yyyy-MM-ddTHH:mm:ss) to the tools.
                    """.formatted(currentContext, currentContext))
                .user(userMessage)
                .functions("checkAvailabilityTool", "calculatePriceTool")
                .call()
                .content();    
        } catch (Exception e) {
            return "Sorry, I ran into an issue getting that information ☹️ Please try again!";
        }
    }
}