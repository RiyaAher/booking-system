package com.turf.booking_system.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class TurfAiService {

    private static final Logger log = LoggerFactory.getLogger(TurfAiService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public TurfAiService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public String askAi(String userMessage) {
        try {
            log.info("--> [USER QUERY]: {}", userMessage);

            // 1. Fetch relevant policy snippets from VectorStore
            List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.query(userMessage).withTopK(3)
            );

            String policyContext = similarDocuments.stream()
                    .map(doc -> doc.getContent())
                    .collect(Collectors.joining("\n---\n"));

            log.info("--> [RAG CONTEXT RETRIEVED]:\n{}", 
                policyContext.isEmpty() ? "NO MATCHING CONTEXT FOUND" : policyContext);

            String currentContext = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("EEEE, yyyy-MM-dd HH:mm")
            );

            // 2. Pass context + function tools to Ollama
            String response = this.chatClient.prompt()
                .system("""
                    You are the official AI Concierge for Turf Arena.
                    Current reference date/time: %s.
                    
                    RELEVANT TURF POLICIES & RULES:
                    %s

                    CRITICAL TOOL & DATE INSTRUCTIONS:
                    1. When invoking tools, ALWAYS convert relative days (like "tomorrow", "this Sunday", "next Friday") into exact ISO-8601 string format: 'YYYY-MM-DDTHH:mm:ss' (e.g., '2026-08-02T10:00:00').
                    2. If the user specifies a duration (e.g., "2 hours on Sunday") but NO specific start time, default the start time to 10:00 AM (10:00:00) on that day.
                    3. For footwear, cancellation, or rules questions, rely directly on the RELEVANT TURF POLICIES text above.
                    4. For pricing or availability calculations, ALWAYS invoke the appropriate function tool.
                    5. Default the turfName parameter to 'Main Turf' if not specified by the user.
                    """.formatted(currentContext, policyContext))
                .user(userMessage)
                .functions("checkAvailabilityTool", "calculatePriceTool")
                .call()
                .content();

            log.info("--> [AI RESPONSE]: {}", response);
            return response;

        } catch (Exception e) {
            log.error("--> [AI ERROR]: ", e);
            return "Sorry, I ran into an issue getting that information ☹️ Please try again!";
        }
    }
}