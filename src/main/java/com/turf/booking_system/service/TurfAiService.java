package com.turf.booking_system.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TurfAiService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public TurfAiService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public String askAi(String userMessage) {
        try {
            // 1. Fetch relevant policy snippets from VectorStore
            List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.query(userMessage).withTopK(2));

            String policyContext = similarDocuments.stream()
                    .map(doc -> doc.getContent())
                    .collect(Collectors.joining("\n---\n"));

            String currentContext = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("EEEE, yyyy-MM-dd HH:mm")
            );

            // 2. Pass context + function tools to Ollama
            return this.chatClient.prompt()
                .system("""
                    You are the AI Concierge for Turf Arena.
                    Current reference date and time is: %s.
                    
                    RELEVANT TURF POLICIES & INFORMATION:
                    %s
                    
                    INSTRUCTIONS:
                    1. Use the policy context above to answer rules, amenities, footwear, or cancellation questions.
                    2. Use availability and price tools when users ask to check booking slots or price calculations.
                    """.formatted(currentContext, policyContext))
                .user(userMessage)
                .functions("checkAvailabilityTool", "calculatePriceTool")
                .call()
                .content();    
        } catch (Exception e) {
            return "Sorry, I ran into an issue getting that information ☹️ Please try again!";
        }
    }
}