package com.turf.booking_system.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TurfAiService {

    private static final Logger log = LoggerFactory.getLogger(TurfAiService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    // Explicitly inject openAiChatModel (Groq) so Render finds the correct bean
    public TurfAiService(
            ChatClient.Builder chatClientBuilder, 
            @Qualifier("openAiChatModel") ChatModel chatModel,
            @Autowired(required = false) VectorStore vectorStore) {
        
        this.chatClient = ChatClient.builder(chatModel).build();
        this.vectorStore = vectorStore;
    }

    public String askAi(String userMessage) {
        try {
            log.info("--> [USER QUERY]: {}", userMessage);

            // 1. Safely fetch policy snippets from VectorStore (if available)
            String policyContext = "";
            if (vectorStore != null) {
                try {
                    List<Document> similarDocuments = vectorStore.similaritySearch(
                        SearchRequest.query(userMessage).withTopK(3)
                    );

                    policyContext = similarDocuments.stream()
                            .map(doc -> doc.getContent())
                            .collect(Collectors.joining("\n---\n"));
                } catch (Exception ve) {
                    log.warn("VectorStore search failed or bypassed: {}", ve.getMessage());
                }
            }

            log.info("--> [RAG CONTEXT RETRIEVED]:\n{}", 
                policyContext.isEmpty() ? "NO MATCHING CONTEXT FOUND" : policyContext);

            String currentContext = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("EEEE, yyyy-MM-dd HH:mm")
            );

            // 2. Pass context + tools to Groq AI
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